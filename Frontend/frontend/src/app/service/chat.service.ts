import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http'; 
import { Observable, BehaviorSubject, Subject, timer } from 'rxjs';
import { filter } from 'rxjs/operators';
import { AuthService } from './auth.service';
import * as SockJS from 'sockjs-client';
import * as Stomp from 'stompjs';

import {
  ChatRoomDTO,
  ClientDTO, 
  ChatMessageDTO,
  MessageSendRequestDTO,
  ChatRoomCreateRequestDTO
} from '../model/chat.model'; 

@Injectable({
  providedIn: 'root'
})
export class ChatService {
  private apiUrl = 'http://localhost:8080/api/chat';
  private wsUrl = 'http://localhost:8080/ws';

  public stompClient!: Stomp.Client;
  private messageSubject = new Subject<ChatMessageDTO>();
  private connectionStatusSubject = new BehaviorSubject<boolean>(false);
  public messages$: Observable<ChatMessageDTO> = this.messageSubject.asObservable();
  public connectionStatus$: Observable<boolean> = this.connectionStatusSubject.asObservable();

  private reconnectAttempts: number = 0;
  private maxReconnectAttempts: number = 10; 
  private disconnectRequested: boolean = false; 
  private chatRoomUpdateSubject = new Subject<ChatRoomDTO>();
  chatRoomUpdates$ = this.chatRoomUpdateSubject.asObservable();

  private chatRoomRemovedSubject = new Subject<number>(); 
  chatRoomRemoved$ = this.chatRoomRemovedSubject.asObservable();


  constructor(private http: HttpClient, private authService: AuthService) { }

  /*public connect(): void {
    if (this.stompClient && this.stompClient.connected) {
      console.log('STOMP klijent je već povezan.');
      this.connectionStatusSubject.next(true); 
      return;
    }

    const token = this.authService.getToken(); 
    if (!token) { 
      console.warn('Nema JWT tokena. Ne mogu uspostaviti WebSocket konekciju.');
      this.connectionStatusSubject.next(false);
      return;
    }

    console.log('Pokušavam da se povežem na WebSocket...');
    const ws = new SockJS(this.wsUrl);
    this.stompClient = Stomp.over(ws);

     const headers = {
      'Authorization': `Bearer ${token}`,  
    };

    this.stompClient.connect(headers, () => {
      console.log('WebSocket uspešno povezan!');
      this.connectionStatusSubject.next(true); 
    }, (error: any) => {
      console.error('Greška pri povezivanju na WebSocket:', error);
      this.connectionStatusSubject.next(false); 
      
      console.log('Pokušavam ponovno povezivanje za 5 sekundi...');
      setTimeout(() => this.connect(), 5000);
    });
  }*/

   public connect(): void {
    console.log('ChatService: Pozvana connect() metoda.');

    if (this.stompClient && this.stompClient.connected) {
      console.log('ChatService: STOMP klijent je već povezan. Vraćam se.');
      this.connectionStatusSubject.next(true);
      return;
    }

    if (this.disconnectRequested) {
      console.log('ChatService: Zahtev za diskonekciju je aktivan, ne pokušavam ponovno povezivanje.');
      return;
    }

    const token = this.authService.getToken();
    if (!token) {
      console.warn('ChatService: Nema JWT tokena. Ne mogu uspostaviti WebSocket konekciju. Pokušavam ponovo za 5 sekundi...');
      this.connectionStatusSubject.next(false);
      timer(5000).subscribe(() => this.connect());
      return;
    }

    console.log('ChatService: Pokušavam da se povežem na WebSocket na URL:', `${this.wsUrl}?token=${token}`);

    const ws = new SockJS(`${this.wsUrl}?token=${token}`);
    this.stompClient = Stomp.over(ws);

    this.stompClient.debug = () => {};

    const headers: { login: string; passcode: string; [key: string]: string; } = {
      login: 'guest',
      passcode: 'guest'
    };

    this.stompClient.connect(headers,
      (frame?: Stomp.Frame) => {
        console.log('ChatService: WebSocket uspešno povezan! Frame:', frame);
        this.connectionStatusSubject.next(true);
        this.reconnectAttempts = 0;
        this.disconnectRequested = false;

        this.stompClient.subscribe(`/user/queue/chat-rooms`, (message: any) => {
          console.log('ChatService: Primljeno obaveštenje o chat sobi:', message.body);
          try {
            const updatedChatRoom: ChatRoomDTO = JSON.parse(message.body);
            this.chatRoomUpdateSubject.next(updatedChatRoom); 
          } catch (e) {
            console.error('ChatService: Greška pri parsiranju chat room update poruke:', e, message.body);
          }
        });

        this.stompClient.subscribe(`/user/queue/chat-rooms-removed`, (message: any) => {
          console.log('ChatService: Primljeno obaveštenje o uklanjanju chat sobe:', message.body);
          try {
            const removedRoomId: number = JSON.parse(message.body); 
            this.chatRoomRemovedSubject.next(removedRoomId); 
          } catch (e) {
            console.error('ChatService: Greška pri parsiranju chat room removed poruke:', e, message.body);
          }
        });

      },
      (error?: string | Stomp.Frame) => {
        console.error('ChatService: Greška pri povezivanju na WebSocket:', error);
        this.connectionStatusSubject.next(false);

        if (!this.disconnectRequested && this.reconnectAttempts < this.maxReconnectAttempts) {
          this.reconnectAttempts++;
          const reconnectDelay = Math.min(1000 * Math.pow(2, this.reconnectAttempts), 30000);
          console.log(`ChatService: Pokušavam ponovno povezivanje (${this.reconnectAttempts}/${this.maxReconnectAttempts}) za ${reconnectDelay / 1000} sekundi...`);
          timer(reconnectDelay).subscribe(() => this.connect());
        } else if (!this.disconnectRequested) {
          console.error('ChatService: Maksimalan broj pokušaja ponovnog povezivanja dostignut. Ne pokušavam više.');
        }
      }
    );
  }


   public disconnect(): void {
    if (this.stompClient && this.stompClient.connected) {
      this.stompClient.disconnect(() => {
        console.log('WebSocket uspešno diskonektovan!');
        this.connectionStatusSubject.next(false);
      });
    } else {
      console.warn('STOMP klijent nije povezan ili je već diskonektovan.');
    }
  }

  public subscribeToChatRoom(chatRoomId: number): Observable<ChatMessageDTO> {
   
    if (!this.stompClient || !this.stompClient.connected) {
      console.error('STOMP klijent nije povezan. Ne može se pretplatiti na chat sobu.');
      return new Observable<ChatMessageDTO>(observer => observer.complete());
    }

    const destination = `/topic/chat/room/${chatRoomId}`;
    console.log(`Pretplaćujem se na: ${destination}`);

    this.stompClient.subscribe(destination, (message) => {
      console.log('Primljena poruka:', message.body);
      try {
        const chatMessage: ChatMessageDTO = JSON.parse(message.body);
        this.messageSubject.next(chatMessage); 
      } catch (e) {
        console.error('Greška pri parsiranju primljene poruke:', e, message.body);
      }
    });

    return this.messages$.pipe(
      filter(msg => msg.chatRoomId === chatRoomId) 
    );
  }

  public sendMessage(messageSendRequest: MessageSendRequestDTO): void {
    if (!this.stompClient || !this.stompClient.connected) {
      console.error('STOMP klijent nije povezan. Ne može se poslati poruka.');
      return;
    }
    
    const destination = '/app/chat.sendMessage'; 
    console.log(`Šaljem poruku na ${destination}:`, messageSendRequest);
    
    this.stompClient.send(destination, {}, JSON.stringify(messageSendRequest));
  }

  public getOrCreatePrivateChat(otherUserId: number): Observable<number> {
    return this.http.post<number>(`${this.apiUrl}/rooms/private/${otherUserId}`, {});
  }

  public createGroupChat(request: ChatRoomCreateRequestDTO): Observable<ChatRoomDTO> {
    return this.http.post<ChatRoomDTO>(`${this.apiUrl}/rooms/group`, request);
  }

  public getMyChatRooms(): Observable<ChatRoomDTO[]> {
    return this.http.get<ChatRoomDTO[]>(`${this.apiUrl}/rooms/me`);
  }

  public getChatHistory(roomId: number): Observable<ChatMessageDTO[]> {
    return this.http.get<ChatMessageDTO[]>(`${this.apiUrl}/rooms/${roomId}/messages`);
  }

  public addMemberToGroupChat(roomId: number, newMemberId: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/rooms/${roomId}/members/${newMemberId}`, {});
  }

  public removeMemberFromGroupChat(roomId: number, memberToRemoveId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/rooms/${roomId}/members/${memberToRemoveId}`);
  }

  public getChatRoomDetails(roomId: number): Observable<ChatRoomDTO> {
    return this.http.get<ChatRoomDTO>(`${this.apiUrl}/rooms/${roomId}`);
  }
}
