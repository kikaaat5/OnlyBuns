import { Component, OnInit, Input, OnDestroy } from '@angular/core';
import { PostService } from '../service/post.service';
import { DatePipe } from '@angular/common';
import { UserService } from '../service/user.service';
import { Client } from '../model/client.model';
import { ClientService } from '../service/client.service';
import { ActivatedRoute, Router } from '@angular/router';
import { FollowService } from '../service/follow.service';
import { firstValueFrom, forkJoin, Observable, of, Subscription } from 'rxjs';
import { switchMap, map, catchError } from 'rxjs/operators';
import { Post, PostComment } from '../model/post.model'; 
import { CommentService } from '../service/comment.service';


@Component({
  selector: 'app-post-list',
  templateUrl: './post-list.component.html',
  styleUrls: ['./post-list.component.css'],
  providers: [DatePipe]
})
export class PostListComponent implements OnInit, OnDestroy {
  @Input() showFollowingPostsOnly: boolean = false;

  posts: Post[] = []; // Koristi importovani Post model
  loggedUserId: number | null = null;
  clients: Client[] = [];
  followedClientIds: number[] = [];
  imageBaseUrl: string = 'http://localhost:8080/api/images';
  newComment: PostComment = {
    postId:1,
    userId: 0,
    username:"",
    content: "",
    createdAt:null
  }
  commentInputs: { [postId: number]: string } = {};
  showCommentInput: boolean = false ;
  activeCommentPostId: number | null = null;


  private userSubscription: Subscription | undefined;

  constructor(
    private userService: UserService,
    private postService: PostService,
    private clientService: ClientService,
    private commentService: CommentService,
    private followService: FollowService,
    private route: ActivatedRoute,
    private router: Router,
    private datePipe: DatePipe
  ) {}

  ngOnInit(): void {
    this.userSubscription = this.userService.getMyInfo().subscribe({
      next: (user) => {
        this.loggedUserId = user ? user.id : null;
        console.log('PostListComponent: loggedUserId postavljen iz getMyInfo():', this.loggedUserId);
        this.loadData();
      },
      error: (err) => {
        this.loggedUserId = null;
        console.warn('PostListComponent: Greška pri dohvatanju informacija o korisniku:', err);
        this.loadData();
      }
    });
  }

  ngOnDestroy(): void {
    if (this.userSubscription) {
      this.userSubscription.unsubscribe();
    }
  }

  hasSignedIn(): boolean {
    return !!this.userService.currentUser;
  }

  isClient(): boolean {
    return this.hasSignedIn() && this.userService.currentUser?.role === 'ROLE_CLIENT';
  }

  getAuthorsUsername(post: Post): string { 
    if (post !== undefined && this.clients) {
      const client = this.clients.find(client => client.id === post.userId);
      if (client) {
        return `${client.username}`;
      }
    }
    return 'Nepoznat autor';
  }

  toggleComments(post: Post) {
    post.showComments = !post.showComments; 
  }

  goToUserProfile(userId?: number): void {
    console.log('Navigacija na profil korisnika sa ID:', userId);
    if (userId) {
      this.router.navigate(['/profile', userId]);
    } else {
      console.error('User ID is required to navigate to the profile page.');
    }
  }

  getStaticComments(): any[] {
    return [
      { id: 1, userId: 2, content: 'Ovo je fantastična slika!', createdAt: '2024-11-10T12:30:00' },
      { id: 2, userId: 3, content: 'Volim ove zečeve!', createdAt: '2024-11-10T13:00:00' },
      { id: 3, userId: 4, content: 'Slika je prelepa!', createdAt: '2024-11-10T14:00:00' }
    ];
  }
  async getComments(postId: number): Promise<PostComment[]> {
  try {
    return await firstValueFrom(this.commentService.getCommentsForPost(postId));
  } catch (error) {
    console.error(`Greška pri dohvatanju komentara za post ${postId}:`, error);
    return [];
  }
}


  loadData(): void {
    console.log('loadData() pozvana. showFollowingPostsOnly:', this.showFollowingPostsOnly, 'loggedUserId:', this.loggedUserId);

    if (this.showFollowingPostsOnly && this.loggedUserId === null) {
      this.posts = [];
      console.log('Nije moguće učitati postove praćenih: Korisnik nije ulogovan ili ID nije dostupan.');
      return;
    }

    const clientsObservable = this.clientService.getAllClients();
    let postsToFetchObservable: Observable<any[]>;

    if (this.showFollowingPostsOnly && this.loggedUserId !== null) {
      const followingObservable = this.followService.getFollowing(this.loggedUserId).pipe(
        map(followingClients => followingClients.map(client => client.id)),
        catchError(error => {
          console.error('Greška pri dohvatanju liste praćenih korisnika:', error);
          return of([]);
        })
      );

      postsToFetchObservable = followingObservable.pipe(
        switchMap(followedIds => {
          this.followedClientIds = followedIds;
          return this.postService.getPosts();
        }),
        map(allPosts => allPosts.filter(post => this.followedClientIds.includes(post.userId)))
      );
    } else {
      postsToFetchObservable = this.postService.getPosts();
    }

    forkJoin([
      clientsObservable,
      postsToFetchObservable,
      this.loggedUserId !== null ? this.postService.getLikesByUserId(this.loggedUserId) : of([])
    ]).subscribe({
      next: async ([allClients, fetchedPosts, userLikes]) => {
        this.clients = allClients;

        const likedPostIds = new Set(userLikes.map(like => like.postId));
        const mappedPosts = await Promise.all(
      fetchedPosts.map(async post => ({
        ...post,
        createdAt: new Date(
          Number(post.createdAt[0]),
          Number(post.createdAt[1]) - 1,
          Number(post.createdAt[2]),
          Number(post.createdAt[3]),
          Number(post.createdAt[4])
        ),
        
        comments: await this.getComments(post.id).then(comments => {
          return comments.map(c => {
            let createdAt = c.createdAt;

            // Ako je niz (npr. [2024, 7, 19, 13, 45]), konvertuj ga u Date
            if (Array.isArray(createdAt)) {
              createdAt = new Date(
                createdAt[0],
                createdAt[1] - 1,
                createdAt[2],
                createdAt[3],
                createdAt[4]
              );
            }

            return {
              ...c,
              createdAt
            };
          });
        }),

        
        hasLiked: likedPostIds.has(post.id)
      }))
    );

    this.posts = mappedPosts.sort((a, b) => b.createdAt.getTime() - a.createdAt.getTime());
    
    console.log("Učitani postovi sa komentarima i lajkovima:", this.posts.length);
  },

      error: (error) => {
        console.error('Greška pri učitavanju podataka:', error);
        this.posts = [];
      }
    });
  }

  likePost(postId: number): void {
    if (!this.isClient()) {
      alert('Samo klijenti mogu lajkovati objave. Prijavite se kao klijent.');
      return;
    }

    if (this.loggedUserId === null) {
      console.error('User ID is null. Cannot like post.');
      alert('Vaša korisnička sesija nije aktivna. Prijavite se ponovo.');
      return;
    }

    const postToUpdate = this.posts.find(p => p.id === postId);

    if (!postToUpdate) {
      console.error('Post not found in local array:', postId);
      return;
    }

    if (postToUpdate.hasLiked) {
      // Ako je post već lajkovan, pozovi dislike
      this.postService.dislikePost(postId, this.loggedUserId).subscribe(
        () => {
          postToUpdate.likesCount = Math.max(0, postToUpdate.likesCount - 1);
          postToUpdate.hasLiked = false;
          console.log(`Post ${postId} uspešno dislajkovan.`);
        },
        (error) => {
          console.error('Greška pri dislajkovanju:', error);
          alert('Došlo je do greške prilikom dislajkovanja objave. Pokušajte ponovo.');
        }
      );
    } else {
      // Ako post nije lajkovan, pozovi createLike
      const newLike = {
        id: 0,
        userId: this.loggedUserId,
        postId: postId,
        likedAt: this.datePipe.transform(new Date(), 'yyyy-MM-ddTHH:mm:ss') || ''
      };

      this.postService.createLike(newLike).subscribe(
        () => {
          postToUpdate.likesCount += 1;
          postToUpdate.hasLiked = true;
          console.log(`Post ${postId} uspešno lajkovan.`);
        },
        (error) => {
          console.error('Greška pri lajkovanju:', error);
          if (error.status === 409) {
            alert('Već ste lajkovali ovu objavu!');
            postToUpdate.hasLiked = true; // Ažuriraj frontend stanje ako backend vrati 409
          } else {
            alert('Došlo je do greške prilikom lajkovanja objave. Pokušajte ponovo.');
          }
        }
      );
    }
  }

  commentPost(postId: number, content:string): void {
    if (!this.isClient()) {
      alert('Samo klijenti mogu komentarisati objave. Prijavite se kao klijent.');
      return;
    }

    if (this.loggedUserId === null) {
      console.error('User ID is null. Cannot comment post.');
      alert('Vaša korisnička sesija nije aktivna. Prijavite se ponovo.');
      return;
    }

    const postToUpdate = this.posts.find(p => p.id === postId);

    if (!postToUpdate) {
      console.error('Post not found in local array:', postId);
      return;
    }

     this.newComment  = {      
      postId: postId,
      userId: this.loggedUserId,
      username:"",
      content: content,
      createdAt: new Date()
    };
    this.commentService.addComment(this.newComment).subscribe({
      next:() =>{
        this.loadData();
      },
      error: err =>{
        console.error("greska prilikom slanja komentara");     
      }      
    })
  }
 submitComment(postId: number): void {
      const content = this.commentInputs[postId]?.trim();
      if (!content) return;

      this.commentPost(postId, content);

      this.commentInputs[postId] = '';
      this.activeCommentPostId = null;
      
    }


  toggleCommentInput(postId: number): void {
    if (this.activeCommentPostId === postId) {
      this.activeCommentPostId = null;
    } else {
      this.activeCommentPostId = postId;
    }
}

    
}
  
