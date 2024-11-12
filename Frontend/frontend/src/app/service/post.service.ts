import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Post } from '../model/post.model';

@Injectable({
  providedIn: 'root'
})
export class PostService {
  
  private apiUrl = 'http://localhost:8080/api/posts'; 

  constructor(private http: HttpClient) {}

  getPosts(): Observable<Post[]> {
    return this.http.get<Post[]>(`${this.apiUrl}`);
  }

  deletePost(postId: number, userId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${postId}?userId=${userId}`);
  }

  updatePost(id: number, updatedPost: any, userId: number): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${id}?userId=${userId}`, updatedPost);
  }
}
