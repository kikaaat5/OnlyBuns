import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-post',
  templateUrl: './post.component.html',
  styleUrls: ['./post.component.css']
})
export class PostComponent {

  posts = []; // Ovo će biti lista postojećih objava, popunjena iz servisa
  showCreatePostForm = false;
  newPost = {
    description: '',
    image: '', // putanja slike (ili URL)
    location: '',
    creationTime: new Date() // Automatski setuje trenutni datum/vreme
  };

  openCreatePostForm() {
    this.showCreatePostForm = true;
  }

  closeCreatePostForm() {
    this.showCreatePostForm = false;
    this.newPost = { description: '', image: '', location: '', creationTime: new Date() };
  }

  submitNewPost() {
    // Dodavanje nove objave u listu
    //this.posts.push({ ...this.newPost });
    this.closeCreatePostForm();
  }

  onImageSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (e: any) => {
        this.newPost.image = e.target.result;
      };
      reader.readAsDataURL(file);
    }
  }
}