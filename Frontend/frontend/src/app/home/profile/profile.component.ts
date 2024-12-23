import { Component } from '@angular/core';
import { AuthService, UserService } from 'src/app/service';
import { Address } from 'src/app/model/address.model';
import { Post } from 'src/app/model/post.model';
import { PostService } from 'src/app/service/post.service';
import { ClientService } from 'src/app/service/client.service';
import { Client } from 'src/app/model/client.model';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent {
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
  newPassword: string = ''; 
  confirmPassword: string = '';
  oldPassword: string = ''; 
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
  isPasswordFormVisible: boolean = false; 
  isAddressFormVisible: boolean = false; 
  isEditProfileModalVisible = false;
  changePasswordSucces = true;
  isClient = false;
  isPasswordEntered = false;
  errormessage : string = '';
  notMyProfile = false;

  constructor(private userService: UserService, private postService: PostService, private clientService: ClientService, private authService : AuthService,  private route: ActivatedRoute) { }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      const userId = params['userId'] ? +params['userId'] : null; 
      console.log('userididididid', userId);
      if (userId) {
        this.notMyProfile = true;
        console.log('usao i ovdje');
        this.getUserProfile(userId); 
      } else {
        this.notMyProfile = false;
        this.getUserProfile(this.userService.currentUser.id); 
      }
    });
  
  }

  getUserProfile(id: number): void {
    console.log('usao i ovdje', id);
    if(this.notMyProfile){
      console.log('usao jer nisam ulogovan');
      this.clientService.getClientById(id).subscribe({
        next: (data) => {
          console.log(data);
          this.client = data; 
          this.user = this.client; 
          this.getUserPosts(); 
          this.checkIfisClient();
        },
        error: (err) => {
          console.error('Greška prilikom dohvatanja klijenta:', err);
        },
      });
      
    }

    if(!this.notMyProfile){
      if (this.userService.currentUser.role === 'ROLE_CLIENT') {
        this.clientService.getClientById(id).subscribe({
          next: (data) => {
            console.log(data);
            this.client = data; 
            this.getUserPosts(); 
            this.checkIfisClient();
          },
          error: (err) => {
            console.error('Greška prilikom dohvatanja klijenta:', err);
          },
        });
      }
  
      this.user = this.userService.currentUser; 
      this.editableUser = this.user;
      this.address = this.user.address || null; 
      this.user.city = this.address.city;
      this.user.country = this.address.country;
      this.user.postalCode = this.address.postalCode;
      this.user.street = this.address.street;
      console.log(this.user);
      console.log(this.address);
      this.editableAddress = this.user.address;
    }
  }

  hasSignedIn() {
    return !!this.userService.currentUser;
  }
  
  checkIfisClient() {
    this.isClient = (this.user.role === 'ROLE_CLIENT');
  }

  checkPassword(): boolean {
    console.log('ssda');
    console.log(this.oldPassword.trim() !== '');
    return (
      this.oldPassword.trim() !== '' &&
      this.newPassword.trim() !== '' &&
      this.confirmPassword.trim() !== '' &&
      this.newPassword === this.confirmPassword
    );
  }

  passwordMatchValidator() {
    console.log(this.newPassword === this.confirmPassword);
    return this.newPassword === this.confirmPassword; 
  }

  getUserPosts(): void {
    //const userId = this.userService.currentUser.id; 
    this.postService.getPostsByUserId(this.client.id).subscribe(
      (data) => {
        this.posts = data;
      },
      (error) => {
        console.error('Error fetching posts:', error);
      }
    );
  }

  updatePassword(): void {
    this.authService.updatePassword(this.user.id, this.oldPassword, this.newPassword).subscribe({
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
      }
    });
  }

  togglePasswordChange(): void {
    this.isPasswordFormVisible = !this.isPasswordFormVisible;
  }

  toggleAddressForm(): void {
    this.isAddressFormVisible = !this.isAddressFormVisible;
  }

  toggleEditProfileModal() {
    this.isEditProfileModalVisible = !this.isEditProfileModalVisible;
  }

  get formattedAddress(): string {
    if (!this.address) {
      return 'Adresa nije dostupna';
    }
    const { street, city, country, postalCode } = this.address;
    return `${street}, ${city}, ${country}, ${postalCode}`;
  }

  updateUserProfile(): void {
    const userId = this.userService.getUserId();
    console.log(this.editableUser);
    if (userId === null) {
      alert('Došlo je do greške: Korisnik nije prijavljen.');
      return;
    }
    this.userService.updateUser(userId, this.editableUser).subscribe({
      next: (updatedUser) => {
        console.log(updatedUser);
        this.user = updatedUser.body;
        this.address = updatedUser.body.address;
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
