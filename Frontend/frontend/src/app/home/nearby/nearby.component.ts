import { Component, OnInit } from '@angular/core';
import { Address } from 'src/app/model/address.model';
import { Client } from 'src/app/model/client.model';
import { UserService } from 'src/app/service';
import { ClientService } from 'src/app/service/client.service';
import { HttpClient } from '@angular/common/http';
import * as L from 'leaflet';
import { PostService } from 'src/app/service/post.service';
import { Post } from 'src/app/model/post.model';
import { LocationService } from 'src/app/service/rabbitmqlocation.service';

@Component({
  selector: 'app-nearby',
  templateUrl: './nearby.component.html',
  styleUrls: ['./nearby.component.css']
})
export class NearbyComponent implements OnInit {
  user: any = {};
  posts: Post[] = [];
  locations: any[] = []; 
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
    private postService: PostService,
    private locationService: LocationService
  ) {}

  ngOnInit(): void {
    this.getUserProfile(this.userService.currentUser.id);
    this.loadPosts();
    this.loadLocations(); // Učitavanje lokacija za zečeve
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
    this.postService.getPosts().subscribe({
      next: (data) => {
        this.posts = data;
        console.log(this.posts);
      },
      error: (error) => {
        console.error('Greška pri preuzimanju objava', error);
      }
    });
  } 

  loadLocations(): void {
    this.locationService.getLocations().subscribe({
      next: (data: any[]) => {
        this.locations = data;
        console.log(this.locations);
        this.addLocationMarkers();
      },
      error: (err: any) => {
        console.error('Greška pri učitavanju lokacija:', err);
      }
    });
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

          this.addPostMarkers();
          this.addLocationMarkers();

          const userIcon = L.icon({
            iconUrl: 'assets/icons/marker.png',
            iconSize: [25, 41],
            iconAnchor: [12, 41],
            popupAnchor: [1, -34]
          });

          L.marker(coordinates, { icon: userIcon }).addTo(this.map)
            .bindPopup(`<b>${fullAddress}</b>`)
            .openPopup();
        } else {
          console.error('Adresa nije pronađena!');
        }
      },
      error: (err) => {
        console.error('Greška prilikom geokodiranja adrese:', err);
      }
    });
  }

  addPostMarkers(): void {
    if (!this.map) return;

    this.posts.forEach(post => {
      const postCoordinates: L.LatLngTuple = [post.latitude, post.longitude];

      const postIcon = L.icon({
        iconUrl: 'assets/icons/rabbit (1).png',
        iconSize: [25, 41],
        iconAnchor: [12, 41],
        popupAnchor: [1, -34]
      });

      const reverseGeocodeUrl = `https://nominatim.openstreetmap.org/reverse?lat=${post.latitude}&lon=${post.longitude}&format=json`;

      this.http.get<any>(reverseGeocodeUrl).subscribe({
        next: (geoResponse) => {
          const address = geoResponse.display_name || 'Adresa nije dostupna';

          const popupContent = `
            <div style="text-align: center;">
              <img src="${post.imagePath}" alt="Post image" style="width: 100px; height: auto; border-radius: 5px; margin-bottom: 5px;" />
              <p style="margin: 0; font-size: 14px;">${address}</p>
            </div>
          `;

          L.marker(postCoordinates, { icon: postIcon }).addTo(this.map)
            .bindPopup(popupContent);
        },
        error: () => {
          console.error(`Reverse geocoding failed for post at coordinates (${post.latitude}, ${post.longitude})`);
        }
      });
    });
  }

  addLocationMarkers(): void {
    if (!this.map) return;
  
    const locationIcon = L.icon({
      iconUrl: 'assets/icons/hospital.png',
      iconSize: [25, 41],
      iconAnchor: [12, 41],
      popupAnchor: [1, -34]
    });
  
    let delay = 0; // Start with no delay
    this.locations.forEach(location => {
      const fullAddress = location.location;
      const geocodeUrl = `https://nominatim.openstreetmap.org/search?q=${encodeURIComponent(fullAddress)}&format=json&addressdetails=1`;
  
      setTimeout(() => {
        this.http.get<any[]>(geocodeUrl).subscribe({
          next: (response) => {
            if (response.length > 0) {
              const latitude = parseFloat(response[0].lat);
              const longitude = parseFloat(response[0].lon);
              const locationCoordinates: L.LatLngTuple = [latitude, longitude];
  
              L.marker(locationCoordinates, { icon: locationIcon })
                .addTo(this.map)
                .bindPopup(`<b>${location.name}</b><br>${location.location}`);
            } else {
              console.error('Lokacija nije pronađena za adresu:', fullAddress);
            }
          },
          error: (err) => {
            console.error('Greška pri geokodiranju adrese:', err);
          }
        });
      }, delay);
  
      delay += 1000; // Delay next request by 1 second
    });
  }
  
  
}
