import { Component, OnInit, OnDestroy, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { ChatService } from 'src/app/service/chat.service';
import { ChatRoomDTO, ChatMessageDTO, MessageSendRequestDTO, ClientDTO, ChatRoomCreateRequestDTO } from 'src/app/model/chat.model';
import { UserService } from 'src/app/service/user.service';
import { AuthService } from 'src/app/service';
import { ClientService } from 'src/app/service/client.service';
import { Subscription, interval, of } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';
import { filter, take, tap, switchMap, catchError } from 'rxjs/operators';

@Component({
  selector: 'app-chat',
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.css']
})
export class ChatComponent implements OnInit, OnDestroy, AfterViewChecked {

  // --- Podaci za prikaz ---
  myChatRooms: ChatRoomDTO[] = [];
  selectedChatRoomId: number | null = null;
  selectedChatRoom: ChatRoomDTO | null = null;
  chatMessages: ChatMessageDTO[] = [];
  messageContent: string = '';

  // --- Pomoćne varijable ---
  currentUserId: number | null = null;
  isAdminOfSelectedRoom: boolean = false;

  // Varijable za "New Chat" funkcionalnost (privatni i grupni)
  showNewChatPanel: boolean = false;
  chatCreationType: 'private' | 'group' = 'private';

  // Za privatni chat
  searchQuery: string = '';
  foundUsers: ClientDTO[] = [];
  selectedUserToChat: ClientDTO | null = null;

  // Za grupni chat kreiranje
  newGroupName: string = '';
  groupMemberSearchQuery: string = '';
  foundUsersForGroup: ClientDTO[] = [];
  selectedGroupMembers: ClientDTO[] = []; // Ovo je za kreiranje NOVE grupe

  // Varijable za "Manage Members" funkcionalnost
  showManageMembersPanel: boolean = false;
  membersToManage: ClientDTO[] = [];
  manageMembersSearchQuery: string = '';
  foundUsersForManageMembers: ClientDTO[] = [];
  selectedUsersToAdd: ClientDTO[] = []; // Ovo je za dodavanje članova u POSTOJEĆU grupu

  // --- RxJS pretplate (za čišćenje resursa) ---
  private chatRoomSubscription: Subscription | undefined;
  private messageSubscription: Subscription | undefined;
  private connectionStatusSubscription: Subscription | undefined;
  private userPollingSubscription: Subscription | undefined;
  private searchSubscription: Subscription | undefined;
  // NOVO: STOMP pretplata, sada ponovo upravljana u komponenti
  private stompSubscription: any | undefined;


  @ViewChild('messagesDisplay') private messagesDisplayRef!: ElementRef;
  private shouldScroll: boolean = false;

  constructor(
    public chatService: ChatService,
    private authService: AuthService,
    private userService: UserService,
    private clientService: ClientService,
    private route: ActivatedRoute,
    private router: Router
  ) { }

  ngOnInit(): void {
    console.log('ChatComponent: ngOnInit pokrenut.');

    this.userPollingSubscription = interval(50).pipe(
      filter(() => {
        const userId = this.userService.currentUser?.id || null;
        if (userId !== null) {
          this.currentUserId = userId;
          console.log('ChatComponent: Korisnik ID je konačno dostupan:', this.currentUserId);
          return true;
        }
        return false;
      }),
      take(1),
      switchMap(() => {
        console.log('ChatComponent: Korisnik je dostupan. Pokrećem inicijalizaciju chata...');
        try {
          this.chatService.connect();
          console.log('ChatService connect called successfully.');
        } catch (error) {
          console.error('Greška pri pozivanju chatService.connect():', error);
          this.router.navigate(['/login']);
          return of(false);
        }
        return this.chatService.connectionStatus$.pipe(
          filter(isConnected => isConnected),
          take(1)
        );
      })
    ).subscribe({
      next: (isConnected) => {
        if (isConnected) {
          console.log('ChatComponent: WebSocket povezan. Učitavam chat sobe i pretplaćujem se na poruke.');
          this.loadMyChatRooms();

          // Ova pretplata hvata poruke koje ChatService emituje (npr. one koje stižu direktno sa STOMP teme u selectChatRoom)
          this.messageSubscription = this.chatService.messages$.subscribe(message => {
            console.log('ChatComponent: Primljena poruka iz servisa:', message);
            if (message && message.chatRoomId === this.selectedChatRoomId) {
              this.chatMessages.push(message);
              console.log('ChatComponent: Poruka dodata u chatMessages. Trenutni broj poruka:', this.chatMessages.length);
              this.shouldScroll = true;
            } else {
              console.log('ChatComponent: Primljena poruka za drugu sobu ili je null. Ignorisano. ID sobe:', message?.chatRoomId, 'ID selektovane sobe:', this.selectedChatRoomId);
            }
          });

          this.chatRoomSubscription = this.route.paramMap.subscribe(params => {
            const roomId = params.get('id');
            if (roomId) {
              const parsedRoomId = +roomId;
              if (this.myChatRooms.length > 0) {
                  this.selectChatRoom(parsedRoomId);
              } else {
                  // Ako sobe nisu učitane, čekaj da se učitaju pa onda selektuj sobu
                  this.chatService.getMyChatRooms().pipe(
                      tap(rooms => this.myChatRooms = rooms),
                      filter(rooms => rooms.some(r => r.id === parsedRoomId)),
                      take(1)
                  ).subscribe(() => this.selectChatRoom(parsedRoomId));
              }
            }
          });
        } else {
          console.error('ChatComponent: Inicijalizacija prekinuta zbog neuspešne konekcije.');
        }
      },
      error: (err) => {
        console.error('ChatComponent: Fatalna greška tokom inicijalizacije:', err);
      },
      complete: () => console.log('ChatComponent: Glavni tok inicijalizacije kompletan.')
    });

    this.connectionStatusSubscription = this.chatService.connectionStatus$.subscribe(isConnected => {
      console.log('WebSocket connection status in ChatComponent:', isConnected);
      if (!isConnected && this.currentUserId !== null) {
        console.warn('WebSocket konekcija je prekinuta. Resetujem stanje chata.');
        // Opcionalno, odjavi STOMP pretplatu i resetuj UI
        if (this.stompSubscription) {
          this.stompSubscription.unsubscribe();
          this.stompSubscription = undefined;
          console.log('ChatComponent: STOMP pretplata odjavljena zbog prekida konekcije.');
        }
        this.chatMessages = [];
        this.selectedChatRoomId = null;
        this.selectedChatRoom = null;
        this.isAdminOfSelectedRoom = false;
      }
    });

    this.searchSubscription = this.clientService.getAllClients().pipe(
      tap(() => console.log('Svi klijenti učitani za pretragu.')),
      catchError(err => {
          console.error("Greška pri učitavanju svih klijenata za pretragu:", err);
          return of([]);
      })
    ).subscribe();
  }

  ngOnDestroy(): void {
    if (this.chatRoomSubscription) {
      this.chatRoomSubscription.unsubscribe();
    }
    if (this.messageSubscription) {
      this.messageSubscription.unsubscribe();
    }
    if (this.connectionStatusSubscription) {
      this.connectionStatusSubscription.unsubscribe();
    }
    if (this.userPollingSubscription) {
      this.userPollingSubscription.unsubscribe();
      console.log('ChatComponent: Odjavljena user polling pretplata.');
    }
    if (this.searchSubscription) {
      this.searchSubscription.unsubscribe();
      console.log('ChatComponent: Odjavljena search pretplata.');
    }
    // KLJUČNO: Odjavi se od STOMP pretplate kada se komponenta uništi
    if (this.stompSubscription) {
      this.stompSubscription.unsubscribe();
      console.log('ChatComponent: Odjavljena STOMP pretplata u ngOnDestroy.');
    }
    this.chatService.disconnect();
  }

  ngAfterViewChecked(): void {
    if (this.shouldScroll) {
      this.scrollToBottom();
      this.shouldScroll = false;
    }
  }

  scrollToBottom(): void {
    try {
      if (this.messagesDisplayRef && this.messagesDisplayRef.nativeElement) {
        const element = this.messagesDisplayRef.nativeElement;
        element.scrollTop = element.scrollHeight;
        console.log('Skrolujem do dna. ScrollHeight:', element.scrollHeight);
      } else {
        console.warn('messagesDisplayRef nije dostupan za skrolovanje.');
      }
    } catch (err) {
      console.error('Nije moguće skrolovati do dna:', err);
    }
  }

  // Uklonjena trackByMessageId funkcija


  loadMyChatRooms(): void {
    console.log('Učitavam chat sobe...');
    if (this.currentUserId === null) {
      console.error('loadMyChatRooms: Korisnik ID nije dostupan. Ne mogu učitati sobe.');
      return;
    }
    this.chatService.getMyChatRooms().subscribe(
      (rooms: ChatRoomDTO[]) => {
        this.myChatRooms = rooms.sort((a, b) => {
          if (a.type === 'GROUP' && b.type === 'PRIVATE') return -1;
          if (a.type === 'PRIVATE' && b.type === 'GROUP') return 1;
          return (a.name || '').localeCompare(b.name || '');
        });
        console.log('Učitane chat sobe:', this.myChatRooms);
        if (this.selectedChatRoomId) {
             this.selectChatRoom(this.selectedChatRoomId); // Ponovo selektuj istu sobu ako je bila selektovana
        }
      },
      (error) => {
        console.error('Greška pri učitavanju chat soba:', error);
      }
    );
  }

  getPrivateChatName(room: ChatRoomDTO): string {
    if (room.type === 'PRIVATE' && this.currentUserId !== null && room.members) {
      const otherMember = room.members.find(member => member.id !== this.currentUserId);
      return otherMember ? otherMember.username || `User ${otherMember.id}` : 'Unknown User';
    }
    if (room.type === 'GROUP') {
      return room.name || 'Group Chat';
    }
    return 'Unknown Chat';
  }

  selectChatRoom(roomId: number): void {
    this.showManageMembersPanel = false;
    this.selectedUsersToAdd = [];

    // KLJUČNO: Odjavi se od prethodne STOMP pretplate pre pretplate na novu sobu
    if (this.stompSubscription) {
        this.stompSubscription.unsubscribe();
        console.log('ChatComponent: Odjavljena prethodna STOMP pretplata.');
    }

    this.selectedChatRoomId = roomId;
    this.chatService.getChatRoomDetails(roomId).subscribe({
      next: (roomDetails: ChatRoomDTO) => {
        this.selectedChatRoom = roomDetails;
        this.chatMessages = []; // Obriši poruke iz prethodnog chata

        this.checkAdminStatus();
        this.membersToManage = this.selectedChatRoom?.members ? [...this.selectedChatRoom.members] : [];

        console.log('Selektovana chat soba:', this.selectedChatRoom.name || this.getPrivateChatName(this.selectedChatRoom), this.selectedChatRoom.id);

        const selectedRoomId = this.selectedChatRoom.id;

        this.chatService.getChatHistory(selectedRoomId).subscribe(
          (messages: ChatMessageDTO[]) => {
            this.chatMessages = messages;
            console.log('Učitana istorija chata za sobu', selectedRoomId, ':', this.chatMessages);
            this.shouldScroll = true;
          },
          (error) => {
            console.error('Greška pri učitavanju istorije chata za sobu', selectedRoomId, ':', error);
            this.chatMessages = [];
          }
        );

        // KLJUČNO: Pretplati se na WebSocket temu za ovu specifičnu sobu
        if (this.chatService.stompClient && this.chatService.stompClient.connected) {
          this.stompSubscription = this.chatService.stompClient.subscribe(`/topic/chat/room/${selectedRoomId}`, (message: any) => {
            console.log('ChatComponent: Primljena poruka DIREKTNO sa STOMP pretplate za sobu', selectedRoomId, ':', JSON.parse(message.body));
            // Emituj poruku kroz messagesSubject u servisu da bi je obradio ngOnInit pretplatnik
            this.chatService['messageSubject'].next(JSON.parse(message.body));
          });
          console.log('ChatComponent: Nova STOMP pretplata AKTIVNA za sobu:', selectedRoomId);
        } else {
          console.warn('ChatComponent: StompClient nije povezan, ne mogu se pretplatiti na sobu. Pokušavam da ponovo povežem.');
          // Opcionalno: Pokušaj ponovno povezivanje ili prikaži poruku korisniku
          this.chatService.connect(); // Pokušaj ponovno povezivanje
        }

        this.router.navigate(['/chat', roomId], { replaceUrl: true });
      },
      error: (error) => {
        console.error('Greška pri dohvatanju detalja chat sobe:', error);
        this.selectedChatRoom = null;
        this.isAdminOfSelectedRoom = false;
        alert('Failed to load chat room details: ' + (error.error?.message || error.message));
      }
    });
  }

  sendMessage(event?: Event): void {
    if (event instanceof KeyboardEvent) {
      event.preventDefault();
    }

    if (!this.messageContent.trim()) {
      console.warn('Ne može se poslati prazna poruka.');
      return;
    }

    if (!this.selectedChatRoomId) {
      console.error('Nijedna chat soba nije selektovana za slanje poruke.');
      return;
    }

    if (!this.currentUserId) {
      console.error('ID trenutnog korisnika nije dostupan. Ne mogu poslati poruku.');
      return;
    }

    const messageRequest: MessageSendRequestDTO = {
      chatRoomId: this.selectedChatRoomId,
      content: this.messageContent.trim()
    };

    console.log('Šaljem zahtev za poruku:', messageRequest);
    this.chatService.sendMessage(messageRequest);

    this.messageContent = '';
  }

  // --- METODE ZA ZAPOČINJANJE NOVOG CHATA (PRIVATNI/GRUPNI) ---

  toggleNewChatPanel(): void {
    this.showNewChatPanel = !this.showNewChatPanel;
    if (this.showNewChatPanel) {
      this.chatCreationType = 'private';
      this.resetNewChatForm();
      this.showManageMembersPanel = false;
    }
  }

  selectChatCreationType(type: 'private' | 'group'): void {
    this.chatCreationType = type;
    this.resetNewChatForm();
  }

  cancelNewChatPanel(): void {
    this.showNewChatPanel = false;
    this.resetNewChatForm();
  }

  private resetNewChatForm(): void {
    this.searchQuery = '';
    this.foundUsers = [];
    this.selectedUserToChat = null;
    this.newGroupName = '';
    this.groupMemberSearchQuery = '';
    this.foundUsersForGroup = [];
    this.selectedGroupMembers = [];
  }

  // --- METODE ZA PRIVATNI CHAT ---

  searchUsers(): void {
    if (this.searchQuery.trim().length < 2) {
      this.foundUsers = [];
      return;
    }

    this.clientService.getAllClients().subscribe({
      next: (clients: ClientDTO[]) => {
        this.foundUsers = clients.filter(client =>
          client.id !== this.currentUserId &&
          client.username.toLowerCase().includes(this.searchQuery.trim().toLowerCase())
        );
      },
      error: (error) => {
        console.error('Greška pri pretrazi klijenata za privatni chat:', error);
        this.foundUsers = [];
      }
    });
  }

  selectUser(user: ClientDTO): void {
    this.selectedUserToChat = user;
    this.searchQuery = user.username;
    this.foundUsers = [];
  }

  initiatePrivateChat(): void {
    if (!this.selectedUserToChat || this.selectedUserToChat.id === null) {
      alert('Please select a user to start a private chat.');
      return;
    }
    if (this.currentUserId === null) {
      console.error('Current user ID is not available. Cannot initiate private chat.');
      return;
    }

    const existingPrivateRoom = this.myChatRooms.find(room =>
      room.type === 'PRIVATE' &&
      room.members?.length === 2 &&
      room.members?.some(member => member.id === this.selectedUserToChat!.id) &&
      room.members?.some(member => member.id === this.currentUserId)
    );

    if (existingPrivateRoom) {
      console.log('Existing private room found, selecting it:', existingPrivateRoom.id);
      this.selectChatRoom(existingPrivateRoom.id);
      this.cancelNewChatPanel();
      return;
    }

    console.log(`Initiating private chat with client ID: ${this.selectedUserToChat.id}`);
    this.chatService.getOrCreatePrivateChat(this.selectedUserToChat.id).subscribe({
      next: (roomId: number) => {
        console.log('Private room created/retrieved with ID:', roomId);
        this.loadMyChatRooms();
        this.chatService.getMyChatRooms().pipe(
          filter(rooms => rooms.some(r => r.id === roomId)),
          take(1)
        ).subscribe(() => {
          this.selectChatRoom(roomId);
          this.cancelNewChatPanel();
        });
      },
      error: (error) => {
        console.error('Error initiating private chat:', error);
        alert('Failed to start private chat: ' + (error.error?.message || error.message));
      }
    });
  }

  // --- METODE ZA KREIRANJE GRUPNOG CHATA ---

  searchUsersForGroup(): void {
    if (this.groupMemberSearchQuery.trim().length < 2) {
      this.foundUsersForGroup = [];
      return;
    }

    this.clientService.getAllClients().subscribe({
      next: (clients: ClientDTO[]) => {
        this.foundUsersForGroup = clients.filter(client =>
          client.id !== this.currentUserId &&
          !this.isMemberSelectedForNewGroup(client.id) &&
          client.username.toLowerCase().includes(this.groupMemberSearchQuery.trim().toLowerCase())
        );
      },
      error: (error) => {
        console.error('Greška pri pretrazi klijenata za grupni chat:', error);
        this.foundUsersForGroup = [];
      }
    });
  }

  addMemberToGroup(member: ClientDTO): void {
    if (!this.isMemberSelectedForNewGroup(member.id)) {
      this.selectedGroupMembers.push(member);
      this.groupMemberSearchQuery = '';
      this.foundUsersForGroup = [];
    }
  }

  removeMemberFromSelectedGroupMembers(memberId: number): void { // Nova metoda
    this.selectedGroupMembers = this.selectedGroupMembers.filter(m => m.id !== memberId);
  }

  isMemberSelectedForNewGroup(memberId: number): boolean {
    return this.selectedGroupMembers.some(m => m.id === memberId);
  }

  createGroupChat(): void {
    if (!this.newGroupName.trim()) {
      alert('Group name cannot be empty.');
      return;
    }
    if (this.selectedGroupMembers.length === 0) {
      alert('Please add at least one member to the group.');
      return;
    }
    if (this.currentUserId === null) {
      alert('User not logged in. Cannot create group chat.');
      return;
    }

    const allMemberIds = new Set(this.selectedGroupMembers.map(m => m.id));
    if (this.currentUserId && !allMemberIds.has(this.currentUserId)) {
      allMemberIds.add(this.currentUserId);
    }
    const memberIdsArray = Array.from(allMemberIds);

    const request: ChatRoomCreateRequestDTO = {
      name: this.newGroupName.trim(),
      memberIds: memberIdsArray
    };

    console.log('Sending request to create group chat:', request);

    this.chatService.createGroupChat(request).subscribe({
      next: (newChatRoom: ChatRoomDTO) => {
        console.log('Group room created with ID:', newChatRoom.id, 'and DTO:', newChatRoom);
        this.loadMyChatRooms();
        this.chatService.getMyChatRooms().pipe(
          filter(rooms => rooms.some(r => r.id === newChatRoom.id)),
          take(1)
        ).subscribe(() => {
          this.selectChatRoom(newChatRoom.id);
          this.cancelNewChatPanel();
        });
      },
      error: (error) => {
        console.error('Error creating group chat:', error);
        alert('Failed to create group chat: ' + (error.error?.message || error.message));
      }
    });
  }

  // --- METODE ZA UPRAVLJANJE ČLANOVIMA GRUPE (POSTOJEĆA GRUPA) ---

  checkAdminStatus(): void {
    if (this.selectedChatRoom && this.selectedChatRoom.type === 'GROUP' && this.currentUserId !== null) {
      this.isAdminOfSelectedRoom = (this.selectedChatRoom.admin?.id === this.currentUserId);
      console.log('Admin status za sobu', this.selectedChatRoom.id, ':', this.isAdminOfSelectedRoom);
    } else {
      this.isAdminOfSelectedRoom = false;
    }
  }

  toggleManageMembersPanel(): void {
    this.showManageMembersPanel = !this.showManageMembersPanel;
    if (this.showManageMembersPanel) {
      this.manageMembersSearchQuery = '';
      this.foundUsersForManageMembers = [];
      this.selectedUsersToAdd = [];
      this.loadSelectedChatRoomDetails();
      this.showNewChatPanel = false;
    }
  }

  cancelManageMembersPanel(): void {
    this.showManageMembersPanel = false;
    this.manageMembersSearchQuery = '';
    this.foundUsersForManageMembers = [];
    this.selectedUsersToAdd = [];
  }

  searchUsersForManageMembers(): void {
    if (this.manageMembersSearchQuery.trim().length < 2) {
      this.foundUsersForManageMembers = [];
      return;
    }

    this.clientService.getAllClients().subscribe({
      next: (clients: ClientDTO[]) => {
        this.foundUsersForManageMembers = clients.filter(client =>
          client.id !== this.currentUserId &&
          !this.membersToManage.some(m => m.id === client.id) &&
          !this.selectedUsersToAdd.some(m => m.id === client.id) &&
          client.username.toLowerCase().includes(this.manageMembersSearchQuery.trim().toLowerCase())
        );
      },
      error: (error) => {
        console.error('Greška pri pretrazi klijenata za upravljanje članovima:', error);
        this.foundUsersForManageMembers = [];
      }
    });
  }

  addMemberToExistingGroup(member: ClientDTO): void {
    if (!this.selectedUsersToAdd.some(m => m.id === member.id) && !this.membersToManage.some(m => m.id === member.id)) {
      this.selectedUsersToAdd.push(member);
      this.manageMembersSearchQuery = '';
      this.foundUsersForManageMembers = [];
    }
  }

  // NOVA METODA: Uklanja člana iz privremene liste 'selectedUsersToAdd'
  removeMemberFromSelectedUsersToAdd(memberId: number): void {
    this.selectedUsersToAdd = this.selectedUsersToAdd.filter(m => m.id !== memberId);
  }

  confirmAddSelectedMembers(): void {
    if (!this.selectedChatRoomId || !this.selectedChatRoom || this.selectedUsersToAdd.length === 0) {
      alert('Please select members to add.');
      return;
    }

    if (!this.isAdminOfSelectedRoom) {
      alert('Only the group admin can add members.');
      return;
    }

    const membersToAddPromises = this.selectedUsersToAdd.map(member =>
      this.chatService.addMemberToGroupChat(this.selectedChatRoomId!, member.id).pipe(
        tap(() => {
          console.log(`Successfully added member ${member.id} to room ${this.selectedChatRoomId}.`);
        }),
        catchError(err => {
          console.error(`Error adding member ${member.id} to group chat ${this.selectedChatRoomId}:`, err);
          alert(`Failed to add member ${member.username}: ` + (err.error?.message || err.message));
          return of(null);
        })
      )
    );

    of(...membersToAddPromises).pipe(
      switchMap(obs => obs),
      filter(result => result !== null)
    ).subscribe({
      complete: () => {
        console.log('All selected members processed for addition.');
        this.loadSelectedChatRoomDetails();
        this.loadMyChatRooms();
        this.manageMembersSearchQuery = '';
        this.foundUsersForManageMembers = [];
        this.selectedUsersToAdd = []; // Resetuj listu nakon što su svi obrađeni
      }
    });
  }

  removeMemberFromGroup(memberId: number): void { // Ovo je za uklanjanje iz POSTOJEĆE grupe (poziva backend)
    if (!this.selectedChatRoomId || !this.currentUserId) {
      alert('No chat room selected or user not logged in.');
      return;
    }
    if (!this.isAdminOfSelectedRoom) {
      alert('Only group admin can remove members.');
      return;
    }
    if (memberId === this.currentUserId) {
        alert("You cannot remove yourself from the group this way. If you want to leave, use 'Leave Group' option.");
        return;
    }
    if (this.selectedChatRoom?.admin?.id === memberId) {
        alert('Cannot remove the group admin. Transfer admin role first.');
        return;
    }

    if (!confirm('Are you sure you want to remove this member from the group?')) {
      return;
    }

    this.chatService.removeMemberFromGroupChat(this.selectedChatRoomId, memberId).subscribe({
      next: () => {
        console.log(`Member ${memberId} removed successfully.`);
        this.loadSelectedChatRoomDetails();
        this.loadMyChatRooms();
      },
      error: (error) => {
        console.error('Error removing member:', error);
        alert('Failed to remove member: ' + (error.error?.message || error.message));
      }
    });
  }

  private loadSelectedChatRoomDetails(): void {
    if (this.selectedChatRoomId) {
      this.chatService.getChatRoomDetails(this.selectedChatRoomId).subscribe({
        next: (roomDetails: ChatRoomDTO) => {
          this.selectedChatRoom = roomDetails;
          this.checkAdminStatus();
          this.membersToManage = [...roomDetails.members];
          console.log('Selected chat room details refreshed:', this.selectedChatRoom);
        },
        error: (error) => {
          console.error('Error refreshing selected chat room details:', error);
          this.selectedChatRoom = null;
          this.isAdminOfSelectedRoom = false;
        }
      });
    }
  }
}