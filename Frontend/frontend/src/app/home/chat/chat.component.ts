import { Component, OnInit, OnDestroy } from '@angular/core'; 
import { ChatService } from 'src/app/service/chat.service'; 
import { ChatRoomDTO, ChatMessageDTO, MessageSendRequestDTO } from 'src/app/model/chat.model';
import { UserService } from 'src/app/service';
import { AuthService } from 'src/app/service';
import { Subscription } from 'rxjs'; 

@Component({
  selector: 'app-chat',
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.css']
})

export class ChatComponent implements OnInit, OnDestroy { 

  // --- Podaci za prikaz ---
  // Lista svih chat soba trenutnog korisnika
  myChatRooms: ChatRoomDTO[] = [];
  // ID chat sobe koja je trenutno selektovana i čije se poruke prikazuju
  selectedChatRoomId: number | null = null;
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


  constructor(
    private chatService: ChatService, 
    private authService: AuthService,
    private userService: UserService 
  ) { }

  ngOnInit(): void {

    this.currentUserId = this.userService.currentUser?.id || null;
   
    // 2. Poveži se na WebSocket
    // ChatService sada automatski dohvaća token
     try {
        this.chatService.connect();
        console.log('ChatService connect called successfully.'); // Dodaj log
    } catch (error) {
        console.error('Error calling chatService.connect():', error); // Dodaj log
    }

    // 3. Pretplati se na status WebSocket konekcije
    this.connectionStatusSubscription = this.chatService.connectionStatus$.subscribe(isConnected => {
      console.log('WebSocket connection status in ChatComponent:', isConnected);
      if (isConnected) {
        // Ako je povezano, učitaj chat sobe
        this.loadMyChatRooms();
      } else {
        // Ako je prekinuta veza, možeš prikazati poruku korisniku
        console.warn('WebSocket konekcija je prekinuta.');
        this.myChatRooms = []; // Očisti liste ako veza pukne
        this.chatMessages = [];
        this.selectedChatRoomId = null;
      }
    });

    // 4. Pretplati se na dolazne poruke (opšti stream poruka iz servisa)
    // Specifične poruke za odabranu sobu će se filtrirati u selectChatRoom
    this.messageSubscription = this.chatService.messages$.subscribe(message => {
      // Proveravamo da li poruka pripada trenutno aktivnoj chat sobi
      if (message.chatRoomId === this.selectedChatRoomId) {
        this.chatMessages.push(message);
        console.log('Nova poruka primljena za trenutnu sobu:', message);
      }
    });
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

    this.chatService.disconnect();
  }

  // --- Metode za interakciju sa servisom (biće dodate u sledećim koracima) ---

  loadMyChatRooms(): void {
    // Logika za učitavanje soba
  }

  selectChatRoom(roomId: number): void {
    // Logika za selekciju sobe
  }

  sendMessage(): void {
    // Logika za slanje poruke
  }

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