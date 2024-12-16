import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Client } from '../model/client.model';

@Injectable({
  providedIn: 'root'
})
export class ClientService {
  private baseUrl = 'http://localhost:8080/api/clients'; 

  constructor(private http: HttpClient) {}

  // Metoda za dobijanje liste svih klijenata
  getAllClients(): Observable<Client[]> {
    return this.http.get<Client[]>(`${this.baseUrl}`);
  }

  searchClients(name: string, surname: string, email: string, minPosts: number, maxPosts: number): Observable<Client[]> {
    let params = new HttpParams();
    if (name) params = params.set('name', name);
    if (surname) params = params.set('surname', surname);
    if (email) params = params.set('email', email);
    if (minPosts != null) params = params.set('minPosts', minPosts.toString());
    if (maxPosts != null) params = params.set('maxPosts', maxPosts.toString());

    return this.http.get<Client[]>(`${this.baseUrl}/search`, { params });
  }

  sortClientsByFollowingCount(): Observable<Client[]> {
    return this.http.get<Client[]>(`${this.baseUrl}/sort/followingCount`);
  }

  sortClientsByEmail(): Observable<Client[]> {
    return this.http.get<Client[]>(`${this.baseUrl}/sort/email`);
  }

  getClientById(id: number): Observable<Client> {
    return this.http.get<Client>(`${this.baseUrl}/${id}`);
  }
}
