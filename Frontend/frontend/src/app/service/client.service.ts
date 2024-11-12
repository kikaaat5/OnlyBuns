import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
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
  
  searchClients(params: any): Observable<Client[]> {
    return this.http.get<Client[]>(`${this.baseUrl}/search`, { params });
  }

}
