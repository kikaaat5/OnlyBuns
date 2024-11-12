import { Component, OnInit } from '@angular/core';
import { ClientService } from '../service/client.service'; 
import { Client } from '../model/client.model';


@Component({
  selector: 'app-client-list',
  templateUrl: './client-list.component.html',
  styleUrls: ['./client-list.component.css']
})
export class ClientListComponent implements OnInit {

  clients: Client[] = []; 

  constructor(private clientService: ClientService) { }

  ngOnInit(): void {
    this.loadClients();  
  }

  loadClients(): void {
    this.clientService.getAllClients().subscribe(
      (data: Client[]) => {
        this.clients = data;  
      },
      (error) => {
        console.error('Došlo je do greške prilikom učitavanja klijenata', error);
      }
    );
  }

}
