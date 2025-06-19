import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { Client } from 'src/app/model/client.model'; 

@Component({
  selector: 'app-follow-list-modal', 
  templateUrl: './follow-list-modal.component.html',
  styleUrls: ['./follow-list-modal.component.css']
})
export class FollowListModalComponent implements OnInit {

  @Input() visible: boolean = false; 
  @Input() followList: Client[] = []; 
  @Input() listType: 'followers' | 'following' | null = null; 
  @Input() currentSignedInUserId: number | null = null;

  @Output() close = new EventEmitter<void>(); 
  @Output() toggleFollow = new EventEmitter<{ clientId: number, isFollowing: boolean }>(); 

  constructor() { }

  ngOnInit(): void {
  }

  onClose(): void {
    this.close.emit(); 
  }

   toggleFollowFromModal(client: Client): void {
    console.log('>>> toggleFollowFromModal - Kliknuto na dugme za klijenta:', client);
    console.log('>>> toggleFollowFromModal - ID klijenta za emitovanje:', client.id);
    console.log('>>> toggleFollowFromModal - isFollowing status za emitovanje:', client.isFollowing || false);
    this.toggleFollow.emit({ clientId: client.id, isFollowing: client.isFollowing || false });
    console.log('>>> toggleFollowFromModal - Događaj emitovan.'); // NEW LOG
    //this.toggleFollow.emit({ clientId: client.id, isFollowing: client.isFollowing || false }); 
  }

  /*toggleFollowFromModal(client: Client): void {
  console.log('Kliknuto na dugme za klijenta:', client);
  console.log('ID klijenta:', client.id, 'Tip:', typeof client.id);
  console.log('ID ulogovanog korisnika (currentSignedInUserId):', this.currentSignedInUserId, 'Tip:', typeof this.currentSignedInUserId);
  console.log('Da li su ID-evi jednaki (client.id === currentSignedInUserId):', client.id === this.currentSignedInUserId);

  this.toggleFollow.emit({ clientId: client.id, isFollowing: client.isFollowing || false });
}*/

  get modalTitle(): string {
    if (this.listType === 'followers') {
      return 'Followers';
    } else if (this.listType === 'following') {
      return 'Following';
    }
    return 'Lista korisnika'; 
  }
}