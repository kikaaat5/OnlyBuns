import { Component, OnInit } from '@angular/core';
import { AuthService, UserService } from 'src/app/service';
import { Address } from 'src/app/model/address.model';
import { Post } from 'src/app/model/post.model';
import { PostService } from 'src/app/service/post.service';
import { ClientService } from 'src/app/service/client.service';
import { Client } from 'src/app/model/client.model';
import { ActivatedRoute } from '@angular/router';
import { FollowService } from 'src/app/service/follow.service';
import { Subscription } from 'rxjs'; 

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {
  user: any = {}; 
  client: Client = { 
    id: 0,
    email: '',
    username: '',
    password: '', 
    firstname: '',
    lastname: '',
    enabled: false,
    numberOfPosts: 0,
    following: 0,
    followers: 0,
    active: false,
    address: 0 
  };
  posts: Post[] = []; 
  newPassword = ''; 
  confirmPassword = '';
  oldPassword = ''; 
  address: Address = {
    city : '',
    country : '',
    postalCode : 0,
    street : ''
  };
  editableAddress: Address = { 
    city : '',
    country : '',
    postalCode : 0,
    street : ''
  };
  editableUser: any = {}; 
  isPasswordFormVisible = false; 
  isAddressFormVisible = false; 
  isEditProfileModalVisible = false;
  changePasswordSucces = true;
  isClient = false; 
  isPasswordEntered = false; 
  errorMessage : string | null = null; 
  notMyProfile = false; 
  currentSignedInUserId: number | null = null; 
  isFollowingUser = false; 
  isFollowListModalVisible: boolean = false; 
  followList: Client[] = []; 
  currentFollowListType: 'followers' | 'following' | null = null;
  imageBaseUrl = 'http://localhost:8080';

  private routeSubscription: Subscription | undefined; 

  constructor(
    private userService: UserService, 
    private postService: PostService, 
    private clientService: ClientService, 
    private authService : AuthService, 
    private route: ActivatedRoute,
    private followService: FollowService 
  ) { }

  ngOnInit(): void {
    this.currentSignedInUserId = this.userService.currentUser?.id || null;
     console.log('Postavljeni currentSignedInUserId:', this.currentSignedInUserId);
    this.routeSubscription = this.route.params.subscribe(params => {
      const profileIdFromRoute = params['userId'] ? +params['userId'] : null;

      if (profileIdFromRoute) {
        if (this.currentSignedInUserId && profileIdFromRoute === this.currentSignedInUserId) {
          this.notMyProfile = false; 
          this.getUserProfile(this.currentSignedInUserId);
        } else {
          this.notMyProfile = true; 
          this.getUserProfile(profileIdFromRoute);
        }
      } else {
        
        this.notMyProfile = false;
        if (this.currentSignedInUserId) {
          this.getUserProfile(this.currentSignedInUserId);
        } else {
          this.errorMessage = 'Nije moguće prikazati profil. Korisnik nije ulogovan ili ID nije dostupan.';
          console.error(this.errorMessage);
        }
      }
    });
  }

  ngOnDestroy(): void {
    if (this.routeSubscription) {
      this.routeSubscription.unsubscribe();
    }
  }

  getUserProfile(id: number): void {
    this.errorMessage = null; 
    this.clientService.getClientById(id).subscribe({
      next: (data) => {
        this.client = data; 
        this.user = this.client; 
        this.getUserPosts(this.client.id);
        this.checkIfIsClient(); 
        
        if (!this.notMyProfile) {
          this.editableUser = { ...this.user }; 
          this.address = this.user.address || { city: '', country: '', postalCode: 0, street: '' };
          this.editableAddress = { ...this.address }; 
          this.user.city = this.address.city;
          this.user.country = this.address.country;
          this.user.postalCode = this.address.postalCode;
          this.user.street = this.address.street;
        } else {
          if (this.currentSignedInUserId !== null) {
            this.checkFollowingStatus(this.client.id);
          }
        }
      },
      error: (err) => {
        console.error('Greška prilikom dohvatanja klijenta:', err);
        this.errorMessage = 'Nije moguće dohvatiti profil korisnika. Pokušajte ponovo kasnije.';
      },
    });
  }

  checkFollowingStatus(followedClientId: number): void {
    if (this.currentSignedInUserId !== null) { 
      this.followService.isFollowing(followedClientId).subscribe({
        next: (isFollowing: boolean) => {
          this.isFollowingUser = isFollowing;
        },
        error: (err) => {
          console.error(`Greška pri proveri statusa praćenja za klijenta ${followedClientId}:`, err);
          this.isFollowingUser = false; 
        }
      });
    }
  }

  toggleFollow(): void {

    if (this.currentSignedInUserId === null) {
      return;
    }

    const followedClientId = this.client.id; 

    if (this.isFollowingUser) {
      this.followService.unfollowClient(followedClientId).subscribe({
        next: (response) => {
          console.log('Uspešno otpraćeno:', response);
          this.isFollowingUser = false;
          
          if (this.client.followers > 0) {
              this.client.followers--;
          }
          this.updateSignedInUserFollowingCount();
          this.updateFollowStatusInModalList(followedClientId, false);
        },
        error: (err) => {
          console.error('Greška pri otpraćivanju:', err);
          const errorMsg = err.error?.message || err.message || 'Nepoznata greška.';
        }
      });
    } else {
      this.followService.followClient(followedClientId).subscribe({
        next: (response) => {
          console.log('Uspešno zapraćeno:', response);
          this.isFollowingUser = true;
          this.client.followers++;
          this.updateSignedInUserFollowingCount();
          this.updateFollowStatusInModalList(followedClientId, true);
        },
        error: (err) => {
          console.error('Greška pri praćenju:', err);
          const errorMsg = err.error?.message || err.message || 'Nepoznata greška.';
          alert('Greška pri praćenju: ' + errorMsg);
        }
      });
    }
  }
  /*openFollowListModal(type: 'followers' | 'following'): void {
    if (this.client.id === 0) { 
      console.warn('ID klijenta nije dostupan za dohvat liste praćenja.');
      return;
    }

    this.currentFollowListType = type; 
    this.isFollowListModalVisible = true; 
    this.followList = []; 

    const clientIdToFetch = this.client.id;

    const checkIsFollowingStatus = (clients: Client[]) => {
        const promises = clients.map(client => {
            // NE PROVERAVAMO ZA ULOGOVANOG KORISNIKA SEBE SAMOG
            if (this.currentSignedInUserId !== null && client.id !== this.currentSignedInUserId) {
                return this.followService.isFollowing(client.id).toPromise() // Pozivamo backend da proveri
                    .then(isFollowing => {
                        client.isFollowing = isFollowing; // Dodajemo 'isFollowing' properti
                        return client;
                    })
                    .catch(error => {
                        console.error(`Greška pri proveri praćenja za ${client.username}:`, error);
                        client.isFollowing = false; // Podrazumevano na false u slučaju greške
                        return client;
                    });
            } else {
                // Ako je to ulogovani korisnik, pretpostavljamo da "prati" samog sebe, ili se dugme neće prikazati
                // ako je client.id === currentSignedInUserId, što smo već obezbedili u HTML-u.
                // Ali je korisno postaviti isFollowing na true za njega.
                client.isFollowing = (client.id === this.currentSignedInUserId);
                return Promise.resolve(client);
            }
        });
        return Promise.all(promises); // Sačekaj da se sve provere završe
    };

    if (type === 'followers') {
      this.followService.getFollowers(clientIdToFetch).subscribe({
        next: (data: Client[]) => {
          this.followList = data; 
        },
        error: (err) => {
          console.error('Greška pri dohvatanju pratilaca:', err);
          //alert('Greška pri dohvatanju liste pratilaca.');
         // this.closeFollowListModal(); 
        }
      });
    } else {
      this.followService.getFollowing(clientIdToFetch).subscribe({
        next: (data: Client[]) => {
          this.followList = data; 
        },
        error: (err) => {
          console.error('Greška pri dohvatanju praćenih:', err);
          //alert('Greška pri dohvatanju liste praćenih korisnika.');
          //this.closeFollowListModal(); 
        }
      });
    }
  }*/

    openFollowListModal(type: 'followers' | 'following'): void {
    if (this.client.id === 0) {
        console.warn('ID klijenta nije dostupan za dohvat liste praćenja.');
        return;
    }

    this.currentFollowListType = type;
    this.isFollowListModalVisible = true;
    this.followList = []; 

    const clientIdToFetch = this.client.id;

    const checkIsFollowingStatus = (clients: Client[], currentListType: 'followers' | 'following') => {
        const promises = clients.map(client => {
            if (this.currentSignedInUserId !== null && client.id === this.currentSignedInUserId) {
                client.isFollowing = false; 
                return Promise.resolve(client);
            }

            if (currentListType === 'following') {
                client.isFollowing = true; 
                return Promise.resolve(client);
            }
            if (currentListType === 'followers' && this.currentSignedInUserId !== null) {
                return this.followService.isFollowing(client.id).toPromise()
                    .then(isFollowing => {
                        client.isFollowing = isFollowing;
                        return client;
                    })
                    .catch(error => {
                        console.error(`Greška pri proveri praćenja za ${client.username}:`, error);
                        client.isFollowing = false; 
                        return client;
                    });
            } else {
                client.isFollowing = false;
                return Promise.resolve(client);
            }
        });
        return Promise.all(promises); 
    };


    if (type === 'followers') {
        this.followService.getFollowers(clientIdToFetch).subscribe({
            next: (data: Client[]) => {
                checkIsFollowingStatus(data, type).then(updatedClients => { 
                    this.followList = updatedClients;
                });
            },
            error: (err) => {
                console.error('Greška pri dohvatanju pratilaca:', err);
                //this.closeFollowListModal();
            }
        });
    } else { // type === 'following'
        this.followService.getFollowing(clientIdToFetch).subscribe({
            next: (data: Client[]) => {
                checkIsFollowingStatus(data, type).then(updatedClients => { // Prosledi 'type'
                    this.followList = updatedClients;
                });
            },
            error: (err) => {
                console.error('Greška pri dohvatanju praćenih:', err);
                //alert('Greška pri dohvatanju liste praćenih korisnika.');
                //this.closeFollowListModal();
            }
        });
    }
}


handleToggleFollowFromModal(event: { clientId: number, isFollowing: boolean }): void {
   
    if (this.currentSignedInUserId === null) {
        //alert('Morate biti ulogovani da biste pratili/otpratili korisnike.');
        return;
    }

    const { clientId, isFollowing } = event; 

    if (isFollowing) { 
        this.followService.unfollowClient(clientId).subscribe({
            next: (response) => {
                console.log('Uspešno otpraćeno iz modala:', response);
                this.updateFollowStatusInModalList(clientId, false);

                this.getUserProfile(this.client.id); 
                this.updateSignedInUserFollowingCount(); 
            },
            error: (err) => {
                console.error('Greška pri otpraćivanju iz modala:', err);
                const errorMsg = err.error?.message || err.message || 'Nepoznata greška.';
            }
        });
    } else { 
        this.followService.followClient(clientId).subscribe({
            next: (response) => {
                console.log('Uspešno zapraćeno iz modala:', response);
                this.updateFollowStatusInModalList(clientId, true);

                this.getUserProfile(this.client.id); 
                this.updateSignedInUserFollowingCount(); 
            },
            error: (err) => {
                console.error('Greška pri praćenju iz modala:', err);
                const errorMsg = err.error?.message || err.message || 'Nepoznata greška.';
            }
        });
    }
}

private updateFollowStatusInModalList(clientId: number, newStatus: boolean): void {
    const clientInList = this.followList.find(c => c.id === clientId);
    if (clientInList) {
        clientInList.isFollowing = newStatus;
    }
}

  closeFollowListModal(): void {
    this.isFollowListModalVisible = false; 
    this.followList = []; 
    this.currentFollowListType = null;
  }

  private updateSignedInUserFollowingCount(): void {
    if (this.currentSignedInUserId !== null) {
      this.followService.getFollowingCount(this.currentSignedInUserId).subscribe({
        next: (count: number) => {
          if (this.userService.currentUser) {
            this.userService.currentUser.following = count;
          }
        },
        error: (err) => {
          console.error('Greška pri dohvatanju broja praćenja za ulogovanog korisnika:', err);
        }
      });
    }
  }

  getButtonText(): string {
    return this.isFollowingUser ? 'Unfollow' : 'Follow';
  }

  getButtonStyle(): any {
    return this.isFollowingUser ? { 'background-color': '#dc3545', 'color': 'white' } : { 'background-color': '#28a745', 'color': 'white' };
  }

  hasSignedIn(): boolean {
    return !!this.userService.currentUser;
  }
  
  checkIfIsClient(): void { 
    this.isClient = (this.user.role === 'ROLE_CLIENT');
  }

  checkPassword(): boolean {
    return (
      this.oldPassword.trim() !== '' &&
      this.newPassword.trim() !== '' &&
      this.confirmPassword.trim() !== '' &&
      this.newPassword === this.confirmPassword
    );
  }

  passwordMatchValidator(): boolean { 
    return this.newPassword === this.confirmPassword; 
  }

  getUserPosts(userId: number): void { 
    this.postService.getPostsByUserId(userId).subscribe(
      (data) => {
        this.posts = data;
      },
      (error) => {
        console.error('Error fetching posts:', error);
      }
    );
  }

  updatePassword(): void {
    if (!this.currentSignedInUserId) {
        alert('Došlo je do greške: Korisnik nije prijavljen.');
        return;
    }
    this.authService.updatePassword(this.currentSignedInUserId, this.oldPassword, this.newPassword).subscribe({
      next: () => {
        alert('Lozinka je uspešno promenjena.');
        this.oldPassword = '';
        this.newPassword = '';
        this.confirmPassword = '';
        this.isPasswordFormVisible = false;
        this.changePasswordSucces = true;
      },
      error: (err) => {
        this.changePasswordSucces = false;
        console.error('Greška prilikom promene lozinke:', err);
        alert('Greška prilikom promene lozinke: ' + (err.error?.message || err.message || 'Nepoznata greška.'));
      }
    });
  }

  togglePasswordChange(): void {
    this.isPasswordFormVisible = !this.isPasswordFormVisible;
  }

  toggleAddressForm(): void {
    this.isAddressFormVisible = !this.isAddressFormVisible;
  }

  toggleEditProfileModal(): void {
    this.isEditProfileModalVisible = !this.isEditProfileModalVisible;
  }

  get formattedAddress(): string {
    if (!this.address || (!this.address.street && !this.address.city && !this.address.country && !this.address.postalCode)) {
      return 'Adresa nije dostupna';
    }
    const { street, city, country, postalCode } = this.address;
    return `${street || ''}, ${city || ''}, ${country || ''}, ${postalCode || ''}`.replace(/, +/g, ', ').replace(/^, | ,$/g, '');
  }

  updateUserProfile(): void {
    const userId = this.userService.getUserId(); 
    if (userId === null) {
      alert('Došlo je do greške: Korisnik nije prijavljen.');
      return;
    }
    this.userService.updateUser(userId, this.editableUser).subscribe({
      next: (updatedUserResponse) => { 
        const updatedUser = updatedUserResponse.body || updatedUserResponse; 
        console.log(updatedUser);
        this.user = updatedUser;
        this.address = updatedUser.address || { city: '', country: '', postalCode: 0, street: '' };
        this.isEditProfileModalVisible = false;
        alert('Profil je uspešno ažuriran.');
      },
      error: (err) => {
        console.error('Greška prilikom ažuriranja profila:', err);
        alert('Došlo je do greške prilikom ažuriranja profila.');
      },
    });
  }

}
