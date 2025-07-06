import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http'; 
import { Observable, BehaviorSubject, Subject } from 'rxjs';
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

  constructor(private http: HttpClient, private authService: AuthService) { }

  public connect(): void {
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

    // Konfiguracija STOMP logovanja (opciono, za debug)
    // this.stompClient.debug = (str) => {
    //   console.log(str);
    // };

     const headers = {
      'Authorization': `Bearer ${token}`, 
      // 'X-Auth-Token': token 
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
}
