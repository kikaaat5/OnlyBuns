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
  filteredClients: Client[] = [];
  searchCriteria = {
    name: '',
    surname: '',
    email: '',
    minPosts: 0,
    maxPosts: 0
  };

  constructor(private clientService: ClientService) { }

  ngOnInit(): void {
    this.loadClients();  
  }
  /*searchClients(): void {
    const { name, surname, email, minPosts, maxPosts } = this.searchCriteria;

    this.clientService.searchClients(name, surname, email, minPosts, maxPosts)
      .subscribe(clients => {
        this.clients = clients;
      });
  }*/
  filterClients(): void {
        this.filteredClients = this.clients.filter(client => {
          const matchesName = client.firstName && client.firstName.toLowerCase().includes(this.searchCriteria.name.toLowerCase());
          const matchesSurname = client.lastName && client.lastName.toLowerCase().includes(this.searchCriteria.surname.toLowerCase());
          const matchesEmail = client.email && client.email.toLowerCase().includes(this.searchCriteria.email.toLowerCase());
          const matchesMinPosts = this.searchCriteria.minPosts ? client.numberOfPosts >= this.searchCriteria.minPosts : true;
          const matchesMaxPosts = this.searchCriteria.maxPosts ? client.numberOfPosts <= this.searchCriteria.maxPosts : true;
      
          return matchesName && matchesSurname && matchesEmail && matchesMinPosts && matchesMaxPosts;
        });
  }

  /*sortClients(criteria: string): void {
    if (criteria === 'followingCount') {
      this.clientService.sortClientsByFollowingCount().subscribe(clients => {
        this.clients = clients;
      });
    } else if (criteria === 'email') {
      this.clientService.sortClientsByEmail().subscribe(clients => {
        this.clients = clients;
      });
    }
  }*/
  sortClients(criteria: string): void {
      if (criteria === 'followingCount') {
        this.filteredClients.sort((a, b) => b.following - a.following);
      } else if (criteria === 'email') {
        this.filteredClients.sort((a, b) => a.email.localeCompare(b.email));
      }
  }
  loadClients(): void {
    this.clientService.getAllClients().subscribe(
      (data: Client[]) => {
        this.clients = data;  
        console.log(data);
        this.filteredClients = data;
      },
      (error) => {
        console.error('Došlo je do greške prilikom učitavanja klijenata', error);
      }
    );
  }

}
