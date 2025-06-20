import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Client } from '../model/client.model'; // Pretpostavljam da je Client model u folderu '../model'

@Injectable({
  providedIn: 'root'
})
export class FollowService {

  // Ovde sam zamenio putanju sa environment.apiUrl za bolju praksu,
  // kao što smo prethodno diskutovali. Ako želiš hardkodovanu putanju,
  // možeš vratiti na 'http://localhost:8080/api/follows';
  private baseUrl = 'http://localhost:8080/api/follows'; 

  constructor(private http: HttpClient) { }

  followClient(followedClientId: number): Observable<string> {
    // ISPRAVLJENO: Koristimo backtickove (`) i ${...} za template literale
    return this.http.post(`${this.baseUrl}/${followedClientId}/follow`, {}, { responseType: 'text' });
  }

  unfollowClient(followedClientId: number): Observable<string> {
    // ISPRAVLJENO
    return this.http.delete(`${this.baseUrl}/${followedClientId}/unfollow`, { responseType: 'text' });
  }

  isFollowing(otherClientId: number): Observable<boolean> {
    // ISPRAVLJENO
    return this.http.get<boolean>(`${this.baseUrl}/${otherClientId}/isFollowing`);
  }

  getFollowing(clientId: number): Observable<Client[]> {
    // ISPRAVLJENO
    return this.http.get<Client[]>(`${this.baseUrl}/${clientId}/following`);
  }

  getFollowers(clientId: number): Observable<Client[]> {
    // ISPRAVLJENO
    return this.http.get<Client[]>(`${this.baseUrl}/${clientId}/followers`);
  }

  getFollowingCount(clientId: number): Observable<number> {
    // ISPRAVLJENO
    return this.http.get<number>(`${this.baseUrl}/${clientId}/following/count`);
  }

  getFollowersCount(clientId: number): Observable<number> {
    // ISPRAVLJENO
    return this.http.get<number>(`${this.baseUrl}/${clientId}/followers/count`);
  }
}