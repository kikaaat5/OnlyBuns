import { Component, OnInit } from '@angular/core';
import { Address } from 'src/app/model/address.model';
import { Client } from 'src/app/model/client.model';
import { UserService } from 'src/app/service';
import { ClientService } from 'src/app/service/client.service';
import { HttpClient } from '@angular/common/http';
import * as L from 'leaflet';
import { PostService } from 'src/app/service/post.service';
import { Post } from 'src/app/model/post.model';

@Component({
  selector: 'app-nearby',
  templateUrl: './nearby.component.html',
  styleUrls: ['./nearby.component.css']
})
export class NearbyComponent implements OnInit {
  user: any = {};
  posts: Post[] = [];
  address: Address = {
    city: '',
    country: '',
    postalCode: 0,
    street: ''
  };

  private map!: L.Map;

  constructor(
    private userService: UserService,
    private clientService: ClientService,
    private http: HttpClient,
    private postService: PostService
  ) {}

  ngOnInit(): void {
    this.getUserProfile(this.userService.currentUser.id);
    this.loadPosts();
  }

  getUserProfile(id: number): void {
    this.clientService.getClientById(id).subscribe({
      next: (data: Client) => {
        this.user = data;
        this.address = this.user.address;
        console.log('Korisnik:', this.user);
        console.log('Adresa:', this.address);
        this.initializeMap(); 
      },
      error: (err) => {
        console.error('Greška prilikom dohvatanja profila:', err);
      }
    });
  }

  loadPosts(): void {
    this.postService.getPosts().subscribe(
      (data) => {
        this.posts = data;
        console.log(this.posts);
      },
      (error) => {
        console.error('Greška pri preuzimanju objava', error);
      }
    );
  } 

  initializeMap(): void {
    const fullAddress = `${this.address.street}, ${this.address.city}, ${this.address.country}`;
    const geocodeUrl = `https://nominatim.openstreetmap.org/search?q=${encodeURIComponent(fullAddress)}&format=json&addressdetails=1`;
    console.log(fullAddress);

    this.http.get<any[]>(geocodeUrl).subscribe({
      next: (response) => {
        if (response.length > 0) {
          const coordinates: L.LatLngTuple = [parseFloat(response[0].lat), parseFloat(response[0].lon)];
      
          if (!this.map) {
            this.map = L.map('map').setView(coordinates, 7); 
          } else {
            this.map.setView(coordinates, 7);  
          }

          
      
          L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '© OpenStreetMap contributors'
          }).addTo(this.map);
      
          this.posts.forEach(post => {
            const postCoordinates: L.LatLngTuple = [post.latitude, post.longitude];
  
            const postIcon = L.icon({
              iconUrl: 'assets/icons/rabbit (1).png',  
              iconSize: [25, 41], 
              iconAnchor: [12, 41], 
              popupAnchor: [1, -34]
            });
  
            L.marker(postCoordinates, { icon: postIcon }).addTo(this.map)
              .bindPopup(`<i>${post.description}</i>`)
              .openPopup();

         
          });

          const customIcon = L.icon({
            iconUrl: 'assets/icons/marker.png', 
            iconSize: [25, 41], 
            iconAnchor: [12, 41], 
            popupAnchor: [1, -34] 
          });

          L.marker(coordinates, { icon: customIcon }).addTo(this.map)
            .bindPopup(`<b>${fullAddress}</b>`)
            .openPopup();
        }
         else {
          console.error('Adresa nije pronađena!');
        }
      },
    });
  }
}
