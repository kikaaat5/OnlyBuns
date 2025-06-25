import { Component } from '@angular/core';
import { Client } from 'src/app/model/client.model';
import { Post } from 'src/app/model/post.model';
import { ClientService } from 'src/app/service/client.service';
import { PostService } from 'src/app/service/post.service';

@Component({
  selector: 'app-trends',
  templateUrl: './trends.component.html',
  styleUrls: ['./trends.component.css']
})
export class TrendsComponent {

  posts: Post[] = []; 
  tenMostPopularPosts: Post[] = [];
  fiveLastWeeksMostPopularPosts: Post[] = [];
  topTenLastWeeksMostActiveClients: Client[] = [];
  postsTotalCount: number = 0;
  lastMonthCount: number = 0;
  imageBaseUrl = 'http://localhost:8080';

  constructor(private postService: PostService, private clientService: ClientService){}

  ngOnInit(): void {
    this.loadPosts();
    this.loadTopTenPosts();
    this.loadLastWeeksTopFivePosts();
    this.loadTopTenClients();
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

  loadTopTenPosts() {
    this.postService.getTenMostLikedPosts().subscribe({
      next: (data) => {
        this.tenMostPopularPosts = data;
      },
      error: (err) => {
        console.error('Greška prilikom dohvatanja postova:', err);
      },
    });  
  }

  loadLastWeeksTopFivePosts() {
    this.postService.getFiveLastWeeksMostLikedPosts().subscribe({
      next: (data) => {
        this.fiveLastWeeksMostPopularPosts = data;
      },
      error: (err) => {
        console.error('Greška prilikom dohvatanja postova:', err);
      },
    });  
  }

  loadTopTenClients() {
    this.clientService.getTopTenActiveClients().subscribe({
      next: (data) => {
        this.topTenLastWeeksMostActiveClients = data;
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
      return postDate >= oneMonthAgo;
    }).length;
  }
}
