import { Component, Input } from '@angular/core';
import { Post } from 'src/app/model/post.model';
import { PostService } from 'src/app/service/post.service';

@Component({
  selector: 'app-post',
  templateUrl: './post.component.html',
  styleUrls: ['./post.component.css']
})
export class PostComponent {

  constructor(private postService: PostService) {}

  posts = []; // Ovo će biti lista postojećih objava, popunjena iz servisa
  showCreatePostForm = false;
  newPost: Post = {
    id: 0, 
    userId: 1, // Example user ID; replace or set dynamically as needed
    description: '',
    createdAt: new Date().toISOString(),
    imagePath: '',
    longitude: 0,
    latitude: 0,
    likesCount: 0,
    comments: []
  };

  ngOnInit(): void {
  }

  openCreatePostForm() {
    this.showCreatePostForm = true;
  }

  closeCreatePostForm() {
    this.showCreatePostForm = false;
    this.newPost = {
      id: 0,
      userId: 1,
      description: '',
      createdAt: new Date().toISOString(),
      imagePath: '',
      longitude: 0,
      latitude: 0,
      likesCount: 0,
      comments: []
    };
  }

  submitNewPost() {
  
    this.postService.createPost(this.newPost).subscribe(
      response => {
        console.log('Post created successfully:', response);
        this.closeCreatePostForm();
      },
      error => {
        console.error('Error creating post:', error);
      }
    );
  }

  onImageSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (e: any) => {
        const imagePath = e.target.result;  // Ovo je duža putanja slike (base64)
       
      this.newPost.imagePath = imagePath;
      };
      reader.readAsDataURL(file);
    }
  }
}