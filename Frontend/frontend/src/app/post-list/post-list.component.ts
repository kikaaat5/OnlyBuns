import { Component, OnInit } from '@angular/core';
import { PostService } from '../service/post.service'; 
import { DatePipe } from '@angular/common'; 
import { UserService } from '../service/user.service';

@Component({
  selector: 'app-post-list',
  templateUrl: './post-list.component.html',
  styleUrls: ['./post-list.component.css'],
  providers: [DatePipe]
})
export class PostListComponent implements OnInit {
  posts: any[] = [];
  isEditing: boolean = false;    
  editedPost: any = null;
  selectedImageBase64: string | null = null;
  loggedUserId: number | null = null;

  constructor(private postService: PostService, private userService: UserService) {}


  
  ngOnInit(): void {
    this.loggedUserId = this.userService.getUserId();
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
    if (this.loggedUserId) {
      this.postService.deletePost(postId, this.loggedUserId).subscribe(
        (response) => {
          console.log(`Post ${postId} deleted successfully`);
          this.loadPosts();  
        },
        (error) => {
          console.error(`Error deleting post ${postId}`, error);
          alert('This is not your post to delete!');
        }
      );
    }
  }
editPost(post: any): void {
  this.isEditing = true;
  this.editedPost = { ...post };  
}

updatePost(): void {
  if (this.loggedUserId) {
    this.postService.updatePost(this.editedPost.id, this.editedPost, this.loggedUserId).subscribe(
      (response) => {
        console.log(`Post ${this.editedPost.id} updated successfully`);
        this.isEditing = false;
        this.editedPost = null;
        this.loadPosts();  
      },
      (error) => {
        console.error(`Error updating post ${this.editedPost.id}`, error);
        alert('This is not your post to update!');
      }
    );
  }
}

cancelEdit(): void {
  this.isEditing = false;
  this.editedPost = null;
}


/*onFileSelected(event: any): void {
  const file = event.target.files[0];  // Uzmi prvi fajl (ako ih je više)
  
  if (file) {
    const filePath = file.name;  // Možeš čuvati samo ime fajla (putanja može biti relativna ili apstraktna)
    this.editedPost.imagePath = filePath;  // Sačuvaj putanju u objekat post
  }
}*/

onFileSelected(event: any): void {
  const file = event.target.files[0];  // Uzmi prvi fajl (ako ih je više)
  
  if (file) {
    const reader = new FileReader();  // Kreiraj FileReader
    
    reader.onload = () => {
      this.editedPost.imagePath = reader.result as string;  // Sačuvaj Data URL slike u editedPost
    };

    reader.readAsDataURL(file);  // Čitaj fajl kao Data URL
  }
}


}


