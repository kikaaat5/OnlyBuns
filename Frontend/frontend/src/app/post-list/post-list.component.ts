import { Component, OnInit, Input } from '@angular/core'; // Dodaj Input
import { PostService } from '../service/post.service';
import { DatePipe } from '@angular/common';
import { UserService } from '../service/user.service';
import { Client } from '../model/client.model';
import { ClientService } from '../service/client.service';
import { ActivatedRoute, Router } from '@angular/router';
import { FollowService } from '../service/follow.service';
import { forkJoin, of } from 'rxjs'; // Dodaj forkJoin i of
import { switchMap, map, catchError } from 'rxjs/operators'; // Dodaj switchMap, map i catchError

@Component({
  selector: 'app-post-list',
  templateUrl: './post-list.component.html',
  styleUrls: ['./post-list.component.css'],
  providers: [DatePipe]
})
export class PostListComponent implements OnInit {
  // Novi Input property koji određuje da li treba prikazati samo postove praćenih korisnika
  @Input() showFollowingPostsOnly: boolean = false;

  posts: any[] = [];
  isEditing: boolean = false;
  editedPost: any = null;
  selectedImageBase64: string | null = null;
  loggedUserId: number | null = null;
  clients: Client[] = [];
  followedClientIds: number[] = []; // Lista ID-eva korisnika koje trenutni korisnik prati
  imageBaseUrl: string = 'http://localhost:8080';

  constructor(
    private userService: UserService,
    private postService: PostService,
    private clientService: ClientService,
    private followService: FollowService, // Injektuj FollowService
    private route: ActivatedRoute,
    private router: Router,
    private datePipe: DatePipe
  ) {}

  ngOnInit(): void {
    this.loggedUserId = this.userService.getUserId();
    // Pozovi loadData() koja će se pobrinuti za učitavanje klijenata i postova,
    // uzimajući u obzir showFollowingPostsOnly
    this.loadData();
  }

  hasSignedIn(): boolean {
    return !!this.userService.currentUser;
  }

  // Ova metoda sada služi samo za dohvat username-a, podaci o klijentima se učitavaju u loadData()
  getAuthorsUsername(post: any): string {
    if (post !== undefined && this.clients) {
      const client = this.clients.find(client => client.id === post.userId);
      if (client) {
        return `${client.username}`;
      }
    }
    return 'Nepoznat autor';
  }

  goToUserProfile(userId?: number): void {
    console.log('Navigacija na profil korisnika sa ID:', userId);
    if (userId) {
      this.router.navigate(['/profile', userId]);
    } else {
      console.error('User ID is required to navigate to the profile page.');
    }
  }

  // Pomoćna metoda za statičke komentare (ostaje nepromenjena)
  getStaticComments(): any[] {
    return [
      { id: 1, userId: 2, content: 'Ovo je fantastična slika!', createdAt: '2024-11-10T12:30:00' },
      { id: 2, userId: 3, content: 'Volim ove zečeve!', createdAt: '2024-11-10T13:00:00' },
      { id: 3, userId: 4, content: 'Slika je prelepa!', createdAt: '2024-11-10T14:00:00' }
    ];
  }

  // Glavna metoda za učitavanje podataka (postova i klijenata)
  loadData(): void {
    console.log('loadData() pozvana. showFollowingPostsOnly:', this.showFollowingPostsOnly, 'loggedUserId:', this.loggedUserId);

    // Uvek prvo dohvatamo sve klijente jer nam trebaju za getAuthorsUsername
    const clientsObservable = this.clientService.getAllClients();

    if (this.showFollowingPostsOnly && this.loggedUserId !== null) {
      // Scenario: Prikaz postova samo od korisnika koje pratim
      const followingObservable = this.followService.getFollowing(this.loggedUserId).pipe(
        map(followingClients => followingClients.map(client => client.id)),
        catchError(error => {
          console.error('Greška pri dohvatanju liste praćenih korisnika:', error);
          // Vrati prazan niz ID-eva ako dođe do greške, da se ne blokira prikaz postova
          return of([]);
        })
      );

      forkJoin([clientsObservable, followingObservable]).pipe(
        switchMap(([allClients, followedIds]) => {
          this.clients = allClients; // Popuni listu svih klijenata
          this.followedClientIds = followedIds; // Popuni listu praćenih ID-eva
          console.log('Dohvaćeni svi klijenti i praćeni ID-evi. Praćeni:', this.followedClientIds);
          // Sada dohvati SVE postove, pa ćemo ih filtrirati na frontendu
          return this.postService.getPosts();
        })
      ).subscribe({
        next: (allPosts) => {
          // Filtriraj postove na frontendu da prikažeš samo one od praćenih korisnika
          this.posts = allPosts.filter(post =>
            this.followedClientIds.includes(post.userId)
          ).map(post => ({
            ...post,
            createdAt: new Date(
              Number(post.createdAt[0]),
              Number(post.createdAt[1]) - 1,
              Number(post.createdAt[2]),
              Number(post.createdAt[3]),
              Number(post.createdAt[4])
            ),
            comments: this.getStaticComments(),
          })).sort((a, b) => b.createdAt.getTime() - a.createdAt.getTime());
          console.log("Učitani i filtrirani postovi (Following):", this.posts.length, "postova.");
        },
        error: (error) => {
          console.error('Greška pri učitavanju ili filtriranju postova za praćene korisnike', error);
          this.posts = []; // Resetuj postove u slučaju greške
        }
      });
    } else {
      // Scenario: Prikaz svih postova (za admina ili generalni feed)
      clientsObservable.pipe(
        switchMap(allClients => {
          this.clients = allClients; // Popuni listu svih klijenata
          console.log('Dohvaćeni svi klijenti.');
          return this.postService.getPosts(); // Dohvati SVE postove
        })
      ).subscribe({
        next: (allPosts) => {
          this.posts = allPosts.map(post => ({
            ...post,
            createdAt: new Date(
              Number(post.createdAt[0]),
              Number(post.createdAt[1]) - 1,
              Number(post.createdAt[2]),
              Number(post.createdAt[3]),
              Number(post.createdAt[4])
            ),
            comments: this.getStaticComments(),
          })).sort((a, b) => b.createdAt.getTime() - a.createdAt.getTime());
          console.log("Učitani svi postovi (All Posts):", this.posts.length, "postova.");
        },
        error: (error) => {
          console.error('Greška pri učitavanju svih postova', error);
          this.posts = []; // Resetuj postove u slučaju greške
        }
      });
    }
  }

  deletePost(postId: number): void {
    if (this.loggedUserId) {
      this.postService.deletePost(postId, this.loggedUserId).subscribe(
        (response) => {
          console.log(`Post ${postId} deleted successfully`);
          this.loadData(); // Osvježi podatke nakon brisanja
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
          this.loadData(); // Osvježi podatke nakon ažuriranja
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

  isMyPost(postUserId: number): boolean {
    console.log('--- Provera isMyPost ---');
    console.log('hasSignedIn():', this.hasSignedIn());
    console.log('loggedUserId (trenutni korisnik):', this.loggedUserId);
    console.log('postUserId (autor posta):', postUserId);
    console.log('Poređenje (loggedUserId === postUserId):', this.loggedUserId === postUserId);

    const result = this.hasSignedIn() && this.loggedUserId === postUserId;
    console.log('Konačan rezultat isMyPost:', result);
    console.log('------------------------');
    return result;
  }

  isClient(): boolean {
    return this.hasSignedIn() && this.userService.currentUser?.role === 'ROLE_CLIENT';
  }


  onFileSelected(event: any): void {
    const file = event.target.files[0];

    if (file) {
      const reader = new FileReader();

      reader.onload = () => {
        this.editedPost.imagePath = reader.result as string;
      };

      reader.readAsDataURL(file);
    }
  }

  likePost(postId: number): void {
    if (!this.hasSignedIn()) {
      alert('Please log in to like posts.');
      return;
    }

    if (this.loggedUserId === null) {
      console.error('User ID is null');
      return;
    }

    const newLike = {
      id: 0,
      userId: this.loggedUserId,
      postId: postId,
      likedAt: this.datePipe.transform(new Date(), 'yyyy-MM-ddTHH:mm:ss') || ''
    };

    this.postService.createLike(newLike).subscribe(
      () => {
        this.postService.likePost(postId).subscribe(() => {
          const post = this.posts.find(p => p.id === postId);
          if (post) {
            post.likesCount += 1;
          }
        });
      },
      (error) => {
        console.error('Error creating like:', error);
        alert('An error occurred while liking the post.');
      }
    );
  }
}
