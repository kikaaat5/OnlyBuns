import { Component, OnInit } from '@angular/core';
import { PostService } from '../service/post.service'; 
import { DatePipe } from '@angular/common'; 

@Component({
  selector: 'app-post-list',
  templateUrl: './post-list.component.html',
  styleUrls: ['./post-list.component.css'],
  providers: [DatePipe]
})
export class PostListComponent implements OnInit {
  posts: any[] = [];

  constructor(private postService: PostService) {}


  
  ngOnInit(): void {
    this.loadPosts();
  }
  
  getStaticComments() {
    return [
      { id: 1, userId: 2, content: 'Ovo je fantastična slika!', createdAt: '2024-11-10T12:30:00' },
      { id: 2, userId: 3, content: 'Volim ove zečeve!', createdAt: '2024-11-10T13:00:00' },
      { id: 3, userId: 4, content: 'Slika je prelepa!', createdAt: '2024-11-10T14:00:00' }
    ];
  }

  loadPosts(): void {
    this.postService.getPosts().subscribe(
      (data) => {
        // Dodavanje statičkih komentara
        this.posts = data.map(post => ({
          ...post,
          comments: this.getStaticComments()  // Dodaj komentare svakom postu
        }));
        console.log(this.posts);
      },
      (error) => {
        console.error('Greška pri preuzimanju objava', error);
      }
    );
  } 
 
  deletePost(postId: number): void {
    const userId = 1; 
    this.postService.deletePost(postId, userId).subscribe(
        (response) => {
          console.log('Response:', response);
            console.log(`Post ${postId} deleted successfully`);
            this.loadPosts();  
        },
        (error) => {
          
            console.error(`Error deleting post ${postId}`, error);
        }
    );
}

}
