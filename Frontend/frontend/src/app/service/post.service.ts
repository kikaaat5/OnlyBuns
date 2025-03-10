import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Post } from '../model/post.model';
import { catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';
import { Like } from '../model/like.model';

@Injectable({
  providedIn: 'root'
})
export class PostService {
  
  private apiUrl = 'http://localhost:8080/api/posts'; 
  private likeApiUrl = 'http://localhost:8080/api/likes';

  constructor(private http: HttpClient) {}

  getPosts(): Observable<Post[]> {
    return this.http.get<Post[]>(`${this.apiUrl}`);
  }

  /*deletePost(postId: number, userId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${postId}?userId=${userId}`);
  }*/

    deletePost(postId: number, userId: number): Observable<void> {
      console.log(`Sending DELETE request to: ${this.apiUrl}/${postId}?userId=${userId}`);
      return this.http.delete<void>(`${this.apiUrl}/${postId}?userId=${userId}`).pipe(
          catchError((error) => {
              console.error('Error occurred while sending DELETE request:', error);
              console.error('Error details:', error.message, error.status, error.error); 
              return throwError(error);
          })
      );
  }

  /*updatePost(id: number, updatedPost: any, userId: number): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${id}?userId=${userId}`, updatedPost);
  }*/

  updatePost(postId: number, updatedPost: any, userId: number): Observable<any> {
    return this.http.put(`${this.apiUrl}/${postId}?userId=${userId}`, updatedPost);
  }

  createPost(post: Post): Observable<Post> {
    console.log('create metoda servis',post)
    return this.http.post<Post>(this.apiUrl, post);
  }

  getPostsByUserId(userId: number): Observable<Post[]> {
    return this.http.get<Post[]>(`${this.apiUrl}/posts/${userId}`).pipe(
      catchError((error) => {
        console.error('Error fetching posts for user:', error);
        return throwError(error);
      })
    );
  }

  likePost(postId: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${postId}/like`, {}).pipe(
      catchError((error) => {
        console.error('Error occurred while liking the post:', error);
        return throwError(error);
      })
    );
  }

  createLike(like: Like): Observable<Like> {
    console.log('Sending POST request to create like:', like);
    console.log(this.likeApiUrl);
    return this.http.post<Like>(this.likeApiUrl, like).pipe(
      catchError((error) => {
        console.error('Error occurred while creating like:', error);
        return throwError(error);
      })
    );
  }

}
