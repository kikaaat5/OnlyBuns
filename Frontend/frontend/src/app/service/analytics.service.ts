import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PostCommentStats, ClientActivityStats } from '../model/analytics.model'; 

@Injectable({
  providedIn: 'root'
})
export class AnalyticsService {

  private baseUrl = 'http://localhost:8080/api/analytics';

  constructor(private http: HttpClient) { }

  getPostCommentStats(): Observable<PostCommentStats> {
    return this.http.get<PostCommentStats>(`${this.baseUrl}/posts-comments-stats`);
  }

  getClientActivityStats(): Observable<ClientActivityStats> {
    return this.http.get<ClientActivityStats>(`${this.baseUrl}/client-activity-stats`);
  }
  
}