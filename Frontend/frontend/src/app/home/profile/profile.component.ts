import { Component } from '@angular/core';
import { AuthService, UserService } from 'src/app/service';
import { Address } from 'src/app/model/address.model';
import { Post } from 'src/app/model/post.model';
import { PostService } from 'src/app/service/post.service';
import { ClientService } from 'src/app/service/client.service';
import { Client } from 'src/app/model/client.model';

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
  isClient = false;

  constructor(private userService: UserService, private postService: PostService, private clientService: ClientService, private authService : AuthService) { }

  ngOnInit(): void {
    this.getUserProfile();
    this.getUserPosts(); 
    this.checkIfisClient();
  }

  getUserProfile(): void {
    const id = this.userService.currentUser.id;
    if (this.userService.currentUser.role === 'ROLE_CLIENT') {
      this.clientService.getClientById(id).subscribe({
        next: (data) => {
          console.log(data);
          this.client = data; 
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
    this.editableAddress = this.user.address;
  }

  checkIfisClient() {
    this.isClient = (this.user.role === 'ROLE_CLIENT');
  }

  getUserPosts(): void {
    const userId = this.userService.currentUser.id; 
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
    if (this.newPassword === this.confirmPassword) {
      // Poziv servisa za promenu lozinke (trenutno zakomentarisano)
      // this.userService.updatePassword(this.user.id, this.newPassword).subscribe(() => {
      //   alert('Lozinka uspešno promenjena.');
      //   this.newPassword = '';
      //   this.confirmPassword = '';
      //   this.isPasswordFormVisible = false;
      // });
    } else {
      alert('Lozinke se ne poklapaju!');
    }
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

  saveAddress(): void {
    if (this.address) {
      // Poziv servisa za ažuriranje adrese korisnika (trenutno zakomentarisano)
      // this.userService.updateAddress(this.user.id, this.address).subscribe(() => {
      //   alert('Adresa uspešno ažurirana.');
      //   this.isAddressFormVisible = false;
      // });
    }
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
        alert('Profil je uspešno ažuriran.');
        this.getUserProfile();
        this.isEditProfileModalVisible = false;

      },
      error: (err) => {
        console.error('Greška prilikom ažuriranja profila:', err);
        alert('Došlo je do greške prilikom ažuriranja profila.');
      },
    });
}
  
}
