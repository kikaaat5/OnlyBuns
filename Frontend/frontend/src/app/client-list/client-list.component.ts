import { Component, OnInit } from '@angular/core';
import { ClientService } from '../service/client.service'; 
import { Client } from '../model/client.model';

@Component({
  selector: 'app-client-list',
  templateUrl: './client-list.component.html',
  styleUrls: ['./client-list.component.css']
})
export class ClientListComponent implements OnInit {

  clients: Client[] = []; // Svi klijenti
  filteredClients: Client[] = []; // Filtrirani klijenti
  displayedClients: Client[] = []; // Klijenti koji se prikazuju na trenutnoj stranici
  searchCriteria = {
    name: '',
    surname: '',
    email: '',
    minPosts: 0,
    maxPosts: 0
  };
  pageIndex = 0;
  pageSize = 5;
  totalItems = 0;
  sortCriteria: string = ''; // Trenutni kriterijum sortiranja

  constructor(private clientService: ClientService) {}

  ngOnInit(): void {
    this.loadClients();  
  }

  // Učitavanje svih klijenata
  loadClients(): void {
    this.clientService.getAllClients().subscribe(
      (data: Client[]) => {
        this.clients = data; // Svi klijenti
        this.filteredClients = [...this.clients]; // Kopija za filtriranje i sortiranje
        this.totalItems = this.filteredClients.length; // Ukupan broj klijenata
        this.pageIndex = 0; // Reset na prvu stranicu
        this.applyPagination(); // Prikaz prve stranice
      },
      (error) => {
        console.error('Došlo je do greške prilikom učitavanja klijenata', error);
      }
    );
  }

  // Filtriranje klijenata
  filterClients(): void {
    this.filteredClients = this.clients.filter(client => {
      const matchesName = client.firstname?.toLowerCase().includes(this.searchCriteria.name.toLowerCase()) || false;
      const matchesSurname = client.lastname?.toLowerCase().includes(this.searchCriteria.surname.toLowerCase()) || false;
      const matchesEmail = client.email?.toLowerCase().includes(this.searchCriteria.email.toLowerCase()) || false;
      const matchesMinPosts = this.searchCriteria.minPosts ? client.numberOfPosts >= this.searchCriteria.minPosts : true;
      const matchesMaxPosts = this.searchCriteria.maxPosts ? client.numberOfPosts <= this.searchCriteria.maxPosts : true;

      return matchesName && matchesSurname && matchesEmail && matchesMinPosts && matchesMaxPosts;
    });

    this.totalItems = this.filteredClients.length; // Ukupan broj stavki nakon filtriranja
    this.pageIndex = 0; // Reset na prvu stranicu nakon filtriranja
    this.sortClients(this.sortCriteria); // Primjena trenutnog sortiranja
    this.applyPagination(); // Prikaz filtriranih podataka
  }

  // Sortiranje klijenata prema kriterijumu
  sortClients(criteria: string): void {
    this.sortCriteria = criteria; // Sačuvaj kriterijum za buduće pretrage
    if (criteria === 'followingCount') {
      this.filteredClients.sort((a, b) => b.following - a.following);
    } else if (criteria === 'email') {
      this.filteredClients.sort((a, b) => a.email.localeCompare(b.email));
    }
    this.applyPagination(); // Ažuriraj prikaz na trenutnoj stranici
  }

  // Ažuriranje prikazanih klijenata za trenutnu stranicu
  applyPagination(): void {
    const startIndex = this.pageIndex * this.pageSize;
    const endIndex = startIndex + this.pageSize;
    this.displayedClients = this.filteredClients.slice(startIndex, endIndex);
  }

  // Promjena stranice
  onPageChange(event: any): void {
    this.pageIndex = event.pageIndex;
    this.applyPagination();
  }
}
