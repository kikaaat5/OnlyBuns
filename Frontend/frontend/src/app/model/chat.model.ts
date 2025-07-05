export interface ClientDTO {
  id: number;
  username: string;
}

// Model za chat sobu
export interface ChatRoomDTO {
  id: number;
  name: string | null; // null za privatne četove
  type: 'PRIVATE' | 'GROUP';
  admin?: ClientDTO; // Admin postoji samo za GROUP četove
  // members?: ClientDTO[]; // Možeš dodati ako ti zatreba za prikaz članova
}

// Model za poruku u četu
export interface ChatMessageDTO {
  id: number;
  sender: ClientDTO;
  chatRoomId: number;
  content: string;
  timestamp: string; // Vreme će biti string u ISO 8601 formatu sa backenda
}

// DTO za zahtev slanja poruke (šalje se sa frontenda na backend)
export interface MessageSendRequestDTO {
  chatRoomId: number;
  content: string;
}

// DTO za zahtev kreiranja chat sobe (šalje se sa frontenda na backend)
export interface ChatRoomCreateRequestDTO {
  name: string;
  memberIds: number[];
}