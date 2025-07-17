import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class CommentService {

  private apiUrl = '/api/comments'; // backend endpoint

  constructor(private http: HttpClient) {}

  // ➕ Dodavanje komentara
  addComment(comment: Comment): Observable<any> {
    return this.http.post(`${this.apiUrl}`, comment);
  }

  // 📥 Učitavanje komentara za post
  getCommentsForPost(postId: number): Observable<Comment[]> {
    return this.http.get<Comment[]>(`${this.apiUrl}/post/${postId}`);
  }

  // (opciono) ❌ Brisanje komentara
  deleteComment(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }

  // (opciono) 📥 Učitavanje svih komentara (za admina npr.)
  getAllComments(): Observable<Comment[]> {
    return this.http.get<Comment[]>(this.apiUrl);
  }
}
