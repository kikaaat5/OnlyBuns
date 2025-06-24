import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Client } from '../model/client.model'; // Pretpostavljam da je Client model u folderu '../model'

@Injectable({
  providedIn: 'root'
})
export class FollowService {

  private baseUrl = 'http://localhost:8080/api/follows'; 

  constructor(private http: HttpClient) { }

  followClient(followedClientId: number): Observable<string> {
    return this.http.post(`${this.baseUrl}/${followedClientId}/follow`, {}, { responseType: 'text' });
  }

  unfollowClient(followedClientId: number): Observable<string> {
    return this.http.delete(`${this.baseUrl}/${followedClientId}/unfollow`, { responseType: 'text' });
  }

  isFollowing(otherClientId: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.baseUrl}/${otherClientId}/isFollowing`);
  }

  getFollowing(clientId: number): Observable<Client[]> {
    return this.http.get<Client[]>(`${this.baseUrl}/${clientId}/following`);
  }

  getFollowers(clientId: number): Observable<Client[]> {
    return this.http.get<Client[]>(`${this.baseUrl}/${clientId}/followers`);
  }

  getFollowingCount(clientId: number): Observable<number> {
    return this.http.get<number>(`${this.baseUrl}/${clientId}/following/count`);
  }

  getFollowersCount(clientId: number): Observable<number> {
    return this.http.get<number>(`${this.baseUrl}/${clientId}/followers/count`);
  }
}