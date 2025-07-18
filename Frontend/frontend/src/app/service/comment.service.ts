import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PostComment } from '../model/post.model';

@Injectable({
  providedIn: 'root'
})
export class CommentService {
  private apiUrl = 'http://localhost:8080/api/comments'; // prilagodi ako treba

  constructor(private http: HttpClient) {}

  addComment(commentDto: PostComment): Observable<string> {
    return this.http.post(this.apiUrl, commentDto, { responseType: 'text' });
  }

  // 📥 Učitavanje komentara za post
  getCommentsForPost(postId: number): Observable<PostComment[]> {
    return this.http.get<PostComment[]>(`${this.apiUrl}/post/${postId}`);
  }

  // (opciono) ❌ Brisanje komentara
  deleteComment(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }

  // (opciono) 📥 Učitavanje svih komentara (za admina npr.)
  getAllComments(): Observable<PostComment[]> {
    return this.http.get<PostComment[]>(this.apiUrl);
  }
}
