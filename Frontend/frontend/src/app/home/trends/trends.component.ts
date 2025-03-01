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
        this.calculateCounts();
      },
      error: (err) => {
        console.error('Greška prilikom dohvatanja postova:', err);
      },
    });  
  }

  calculateCounts() {
    const oneMonthAgo = new Date();
    oneMonthAgo.setMonth(oneMonthAgo.getMonth() - 1);
  
    this.postsTotalCount = this.posts.length;
    this.lastMonthCount = this.posts.filter(post => {
      const postDate = new Date(
        Number(post.createdAt[0]),    
        Number(post.createdAt[1]) - 1, 
        Number(post.createdAt[2]),    
        Number(post.createdAt[3]),    
        Number(post.createdAt[4])     
      ); 
      console.log(postDate);
      return postDate >= oneMonthAgo;
    }).length;
  }
}
