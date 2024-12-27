import { Component } from '@angular/core';
import { Post } from 'src/app/model/post.model';
import { PostService } from 'src/app/service/post.service';

@Component({
  selector: 'app-trends',
  templateUrl: './trends.component.html',
  styleUrls: ['./trends.component.css']
})


export class TrendsComponent {

  posts: Post[] = []; 
  postsTotalCount: number = 0;
  lastMonthCount: number = 0;

  constructor(private postService: PostService){}

  ngOnInit(): void {
    this.loadPosts();
  }

  loadPosts() {
    this.postService.getPosts().subscribe({
      next: (data) => {
        this.posts = data;
      },
      error: (err) => {
        console.error('Greška prilikom dohvatanja postova:', err);
      },
    });  
  }

  getPostsTotalCount() {
    this.postsTotalCount = this.posts.length;
  }
  
}
