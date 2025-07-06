// src/app/home/chat/chat.component.ts

import { Component, OnInit, OnDestroy, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { ChatService } from 'src/app/service/chat.service';
import { ChatRoomDTO, ChatMessageDTO, MessageSendRequestDTO } from 'src/app/model/chat.model';
import { UserService } from 'src/app/service'; // Pretpostavljam da je ovo putanja do UserService
import { AuthService } from 'src/app/service'; // Pretpostavljam da je ovo putanja do AuthService
import { Subscription } from 'rxjs';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-chat',
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.css']
})
export class ChatComponent implements OnInit, OnDestroy, AfterViewChecked {

  // --- Podaci za prikaz ---
  // Lista svih chat soba trenutnog korisnika
  myChatRooms: ChatRoomDTO[] = [];
  // ID chat sobe koja je trenutno selektovana
  selectedChatRoomId: number | null = null;
  // Objekat chat sobe koja je trenutno selektovana
  selectedChatRoom: ChatRoomDTO | null = null;
  // Lista poruka za trenutno selektovanu chat sobu
  chatMessages: ChatMessageDTO[] = [];
  // Sadržaj poruke koju korisnik kuca
  messageContent: string = '';

  // --- Pomoćne varijable ---
  // Trenutni ID korisnika (preuzeti od AuthService-a ili nekog drugog mesta)
  currentUserId: number | null = null;
  // Opciono: ID drugog korisnika za lakše testiranje privatnog četa
  otherUserIdForTest: number = 2; // Ovo je samo za testiranje

  // --- RxJS pretplate (za čišćenje resursa) ---
  private chatRoomSubscription: Subscription | undefined;
  private messageSubscription: Subscription | undefined;
  private connectionStatusSubscription: Subscription | undefined;
  private stompSubscription: any | undefined; // Promenjeno u 'any' za STOMP pretplatu

  // Referenca na DOM element za prikaz poruka (za skrolovanje)
  @ViewChild('messagesDisplay') private messagesDisplayRef!: ElementRef;

  // Flag za kontrolu skrolovanja: postavlja se na true kada treba skrolovati, resetuje se nakon skrolovanja
  private shouldScroll: boolean = false;

  constructor(
    public chatService: ChatService,
    private authService: AuthService,
    private userService: UserService,
    private route: ActivatedRoute
  ) { }

  ngOnInit(): void {
    this.currentUserId = this.userService.currentUser?.id || null;

    if (!this.currentUserId) {
      console.error('Korisnik nije ulogovan ili ID nije dostupan. Ne mogu inicijalizovati chat.');
      // Opciono: Preusmeri korisnika na stranicu za prijavu
      // this.router.navigate(['/login']);
      return;
    }

    // 1. Poveži se na WebSocket servis
    try {
      this.chatService.connect();
      console.log('ChatService connect called successfully.');
    } catch (error) {
      console.error('Greška pri pozivanju chatService.connect():', error);
    }

    // 2. Pretplati se na status WebSocket konekcije
    this.connectionStatusSubscription = this.chatService.connectionStatus$.subscribe(isConnected => {
      console.log('WebSocket connection status in ChatComponent:', isConnected);
      if (isConnected) {
        // Ako je povezan, učitaj chat sobe
        this.loadMyChatRooms();
      } else {
        // Ako je konekcija prekinuta, resetuj stanje chata
        console.warn('WebSocket konekcija je prekinuta.');
        this.myChatRooms = [];
        this.chatMessages = [];
        this.selectedChatRoomId = null;
        this.selectedChatRoom = null;
      }
    });

    // 3. Pretplati se na dolazne poruke iz servisa (koje servis emituje)
    this.messageSubscription = this.chatService.messages$.subscribe(message => {
      console.log('ChatComponent: Primljena poruka iz servisa:', message);
      // Proveri da li poruka pripada trenutno selektovanoj chat sobi
      if (message.chatRoomId === this.selectedChatRoomId) {
        this.chatMessages.push(message);
        console.log('ChatComponent: Poruka dodata u chatMessages. Trenutni broj poruka:', this.chatMessages.length);
        this.shouldScroll = true; // Postavi flag da se skroluje nakon renderovanja nove poruke
      } else {
        console.log('ChatComponent: Primljena poruka za drugu sobu. Ignorisano. ID sobe:', message.chatRoomId, 'ID selektovane sobe:', this.selectedChatRoomId);
      }
    });

    // Opciono: Ako koristiš rute za direktno otvaranje chat sobe (npr. /chat/:id)
    // this.chatRoomSubscription = this.route.paramMap.subscribe(params => {
    //   const roomId = params.get('id');
    //   if (roomId) {
    //     const parsedRoomId = +roomId;
    //     if (this.chatService.stompClient && this.chatService.stompClient.connected) {
    //       this.selectChatRoom(parsedRoomId);
    //     }
    //   }
    // });
  }

  ngOnDestroy(): void {
    // Važno: Odjavi se od svih pretplata da sprečiš memory leak
    if (this.chatRoomSubscription) {
      this.chatRoomSubscription.unsubscribe();
    }
    if (this.messageSubscription) {
      this.messageSubscription.unsubscribe();
    }
    if (this.connectionStatusSubscription) {
      this.connectionStatusSubscription.unsubscribe();
    }
    if (this.stompSubscription) { // Odjavi se od STOMP pretplate
      this.stompSubscription.unsubscribe();
      console.log('ChatComponent: Odjavljena STOMP pretplata u ngOnDestroy.');
    }

    // Diskonektuj se sa WebSocket-a kada se komponenta uništi
    this.chatService.disconnect();
  }

  // ngAfterViewChecked se poziva nakon svake provere prikaza komponente
  // Koristimo ga za skrolovanje jer se DOM ažurira pre nego što se ova metoda pozove
  ngAfterViewChecked(): void {
    // Skroluj samo ako je flag postavljen
    if (this.shouldScroll) {
      this.scrollToBottom();
      this.shouldScroll = false; // Resetuj flag nakon skrolovanja
    }
  }

  // Pomoćna metoda za skrolovanje do dna kontejnera poruka
  scrollToBottom(): void {
    try {
      // Proveri da li je messagesDisplayRef definisan i da li ima nativeElement
      if (this.messagesDisplayRef && this.messagesDisplayRef.nativeElement) {
        const element = this.messagesDisplayRef.nativeElement;
        // Postavi scrollTop na scrollHeight da bi se skrolovalo do samog dna
        element.scrollTop = element.scrollHeight;
        console.log('Skrolujem do dna. ScrollHeight:', element.scrollHeight);
      } else {
        console.warn('messagesDisplayRef nije dostupan za skrolovanje.');
      }
    } catch (err) {
      console.error('Nije moguće skrolovati do dna:', err);
    }
  }

  // Učitava chat sobe za trenutnog korisnika
  loadMyChatRooms(): void {
    console.log('Učitavam chat sobe...');
    this.chatService.getMyChatRooms().subscribe(
      (rooms: ChatRoomDTO[]) => {
        this.myChatRooms = rooms;
        console.log('Učitane chat sobe:', this.myChatRooms);
        // Opciono: Automatski selektuj prvu sobu ako postoji i nijedna nije selektovana
        // if (this.myChatRooms.length > 0 && !this.selectedChatRoomId) {
        //   this.selectChatRoom(this.myChatRooms[0].id);
        // }
      },
      (error) => {
        console.error('Greška pri učitavanju chat soba:', error);
        // Obrada greške (npr. prikazivanje poruke korisniku)
      }
    );
  }

  // Generiše ime za privatni chat (ime drugog člana)
  getPrivateChatName(room: ChatRoomDTO): string {
    if (room.type === 'PRIVATE' && this.currentUserId !== null && room.members) {
      // Pronađi člana koji NIJE trenutni korisnik
      const otherMember = room.members.find(member => member.id !== this.currentUserId);
      if (otherMember && otherMember.username) {
        return otherMember.username;
      }
    }
    return 'Nepoznati korisnik';
  }

  // Selektuje chat sobu i učitava njenu istoriju poruka
  selectChatRoom(roomId: number): void {
    // Prvo, odjavi se od prethodne STOMP pretplate ako postoji
    if (this.stompSubscription) {
      this.stompSubscription.unsubscribe();
      console.log('ChatComponent: Odjavljena prethodna STOMP pretplata.');
    }

    this.selectedChatRoomId = roomId;
    this.selectedChatRoom = this.myChatRooms.find(room => room.id === roomId) || null;
    this.chatMessages = []; // Obriši poruke iz prethodnog chata

    if (this.selectedChatRoom && this.currentUserId !== null) {
      console.log('Selektovana chat soba:', this.selectedChatRoom.name || this.getPrivateChatName(this.selectedChatRoom), this.selectedChatRoom.id);

      const selectedRoomId = this.selectedChatRoom.id;

      // Dohvati istoriju poruka
      this.chatService.getChatHistory(selectedRoomId).subscribe(
        (messages: ChatMessageDTO[]) => {
          this.chatMessages = messages;
          console.log('Učitana istorija chata za sobu', selectedRoomId, ':', this.chatMessages);
          this.shouldScroll = true; // KLJUČNO: Postavi flag da se skroluje nakon učitavanja istorije
        },
        (error) => {
          console.error('Greška pri učitavanju istorije chata za sobu', selectedRoomId, ':', error);
          this.chatMessages = []; // Obriši poruke u slučaju greške
        }
      );

      // Pretplaćivanje na WebSocket temu za ovu specifičnu sobu
      if (this.chatService.stompClient && this.chatService.stompClient.connected) {
          this.stompSubscription = this.chatService.stompClient.subscribe(`/topic/chat/room/${selectedRoomId}`, (message: any) => {
            console.log('ChatComponent: Primljena poruka direktno sa STOMP pretplate:', JSON.parse(message.body));
            // Emituj poruku kroz messagesSubject u servisu da bi je obradio ngOnInit pretplatnik
            this.chatService['messageSubject'].next(JSON.parse(message.body));
          });
          console.log('ChatComponent: Nova STOMP pretplata aktivna za sobu:', selectedRoomId);
      } else {
          console.warn('ChatComponent: StompClient nije povezan, ne mogu se pretplatiti na sobu.');
      }

    } else {
      console.warn('Pokušano selektovanje nepostojeće chat sobe ili ID trenutnog korisnika je null:', roomId);
    }
  }

  // Metoda za slanje poruke
  sendMessage(event?: Event): void {
    // Spreči podrazumevano ponašanje (npr. prelamanje reda na Enter)
    if (event instanceof KeyboardEvent) {
      event.preventDefault();
    }

    // Validacija sadržaja poruke
    if (!this.messageContent.trim()) {
      console.warn('Ne može se poslati prazna poruka.');
      return;
    }

    // Provera da li je chat soba selektovana
    if (!this.selectedChatRoomId) {
      console.error('Nijedna chat soba nije selektovana za slanje poruke.');
      return;
    }

    // Provera da li je ID trenutnog korisnika dostupan
    if (!this.currentUserId) {
      console.error('ID trenutnog korisnika nije dostupan. Ne mogu poslati poruku.');
      return;
    }

    // Kreiraj objekat zahteva za slanje poruke
    const messageRequest: MessageSendRequestDTO = {
      chatRoomId: this.selectedChatRoomId,
      content: this.messageContent.trim()
      // Sender ID se šalje automatski preko Principal-a na backendu
    };

    console.log('Šaljem zahtev za poruku:', messageRequest);
    this.chatService.sendMessage(messageRequest); // Pozovi servis za slanje poruke

    this.messageContent = ''; // Očisti input polje nakon slanja
    // Skrolovanje će se desiti kada se poruka primi nazad preko WebSocket-a (u messageSubscription)
  }

  // --- Opcione metode za upravljanje chat sobama (nisu direktno vezane za skrolovanje) ---
  getOrCreatePrivateChat(otherUserId: number): void {
    // Logika za privatni chat
  }

  createGroupChat(): void {
    // Logika za kreiranje grupe
  }

  addMember(): void {
    // Logika za dodavanje člana
  }

  removeMember(): void {
    // Logika za uklanjanje člana
  }
}
