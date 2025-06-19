import { Component, OnInit } from '@angular/core';
import { Client } from 'src/app/model/client.model';
import { ClientService } from 'src/app/service/client.service';
import { FollowService } from 'src/app/service/follow.service';
import { AuthService } from 'src/app/service/auth.service';
import { tap, catchError } from 'rxjs/operators';
import { UserService } from 'src/app/service';
import { of } from 'rxjs';
import { Router } from '@angular/router';

@Component({
  selector: 'app-public-client-list', 
  templateUrl: './public-client-list.component.html',
  styleUrls: ['./public-client-list.component.css']
})
export class PublicClientListComponent implements OnInit {

  clients: Client[] = [];
  followingStatus: { [clientId: number]: boolean } = {};
  loading: boolean = true;
  errorMessage: string | null = null;
  currentUserId: number | null = null; 

  constructor(
    private clientService: ClientService,
    private followService: FollowService,
     private userService: UserService,
    private authService: AuthService,
    private router: Router
  ) { }

  ngOnInit(): void {
    if (this.userService.currentUser) {
      this.currentUserId = this.userService.currentUser.id;
      console.log('Ulogovani korisnik ID (iz postojeceg currentUser):', this.currentUserId);
      this.loadClients(); 
    } else {
      console.log('currentUser nije odmah dostupan, dohvatam sa servera...');
      this.userService.getMyInfo().pipe(
        tap(user => {
          this.currentUserId = user ? user.id : null;
          console.log('Ulogovani korisnik ID (nakon getMyInfo poziva):', this.currentUserId);
          this.loadClients(); 
        }),
        catchError(err => {
          console.error('Greška pri dohvatanju informacija o ulogovanom korisniku:', err);
          this.currentUserId = null; 
          this.errorMessage = 'Nije moguće dohvatiti informacije o ulogovanom korisniku.';
          this.loadClients(); 
          return of(null); 
        })
      ).subscribe(); 
    }
  }

  loadClients(): void {
    this.loading = true;
    this.errorMessage = null;
    this.clientService.getAllClients().subscribe({
      next: (data: Client[]) => {
        this.clients = data;
        if (this.currentUserId !== null) {
          this.clients = this.clients.filter(client => client.id !== this.currentUserId);
        }
        this.checkFollowingStatusForAllClients();
        this.loading = false;
      },
      error: (err) => {
        console.error('Greška pri dohvatanju klijenata:', err);
        this.errorMessage = 'Try again later.';
        this.loading = false;
      }
    });
  }

  checkFollowingStatusForAllClients(): void {
    if (this.currentUserId === null) {
      this.clients.forEach(client => {
        this.followingStatus[client.id] = false; 
      });
      return;
    }
    this.clients.forEach(client => {
      this.followService.isFollowing(client.id).subscribe({
        next: (isFollowing: boolean) => {
          this.followingStatus[client.id] = isFollowing;
        },
        error: (err) => {
          console.error(`Greška pri proveri statusa praćenja za klijenta ${client.id}:`, err);
          this.followingStatus[client.id] = false;
        }
      });
    });
  }

  toggleFollow(followedClientId: number): void {
    if (this.currentUserId === null) {
      alert('You have to be logged in to follow or unfollow!');
      return;
    }

    const isCurrentlyFollowing = this.followingStatus[followedClientId];

    if (isCurrentlyFollowing) {
      this.followService.unfollowClient(followedClientId).subscribe({
        next: (response) => {
          console.log(response);
          this.followingStatus[followedClientId] = false;
          //alert('Uspešno ste otpratili klijenta.');
        },
        error: (err) => {
          console.error('Greška pri otpraćivanju:', err);
          const errorMsg = err.error || err.message || 'Nepoznata greška.';
          //alert('Greška pri otpraćivanju: ' + errorMsg);
        }
      });
    } else {
      this.followService.followClient(followedClientId).subscribe({
        next: (response) => {
          console.log(response);
          this.followingStatus[followedClientId] = true;
          //alert('Uspešno ste zapratili klijenta!');
        },
        error: (err) => {
          console.error('Greška pri praćenju:', err);
          const errorMsg = err.error || err.message || 'Nepoznata greška.';
          //alert('Greška pri praćenju: ' + errorMsg);
        }
      });
    }
  }

  getButtonText(clientId: number): string {
    return this.followingStatus[clientId] ? 'Unfollow' : 'Follow';
  }

  getButtonStyle(clientId: number): any {
    return this.followingStatus[clientId] ? { 'background-color': '#dc3545', 'color': 'white' } : { 'background-color': '#28a745', 'color': 'white' };
  }

  viewProfile(clientId: number): void {
    console.log(`Navigacija na profil klijenta sa ID-em: ${clientId}`);
    this.router.navigate(['/home', 'profile', clientId]); 
  }
}