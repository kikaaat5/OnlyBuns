import { afterNextRender, Component, Input } from '@angular/core';
import { Post } from 'src/app/model/post.model';
import { MapService } from 'src/app/service/map.service';
import { PostService } from 'src/app/service/post.service';
import { UserService } from 'src/app/service/user.service';

@Component({
  selector: 'app-post',
  templateUrl: './post.component.html',
  styleUrls: ['./post.component.css']
})
export class PostComponent {

  constructor(private postService: PostService, private mapService: MapService,private userService: UserService) {}

  posts = []; // Ovo će biti lista postojećih objava, popunjena iz servisa
  showCreatePostForm = false;
  showMap = false;
  location:string ='';
  loggedUserId: number | null = 0;
  successMessage: string | null = null;
  errorMessage: string | null = null;
  selectedImage: File | null = null;
  imageBaseUrl = 'http://localhost:8080'; // backend URL
  previewImageUrl: string | null = null;



  newPost: Post = {
    id: 0, 
    userId: 0, // Example user ID; replace or set dynamically as needed
    description: '',
    createdAt: new Date().toISOString(),
    imagePath: '',
    compressedImagePath: '',
    longitude: 0,
    latitude: 0,
    likesCount: 0,
    comments: []
  };

  ngOnInit(): void {
    this.loggedUserId = this.userService.getUserId();
  }

  openMap(){
    this.showMap = true;
  }

  openCreatePostForm() {
    this.showCreatePostForm = true;
  }

  closeCreatePostForm() {
    this.showCreatePostForm = false;
   
      this.newPost = {
        id: 0,
        userId:0,
        description: '',
        createdAt: new Date().toISOString(),
        imagePath: '',
        compressedImagePath: '',
        longitude: 0,
        latitude: 0,
        likesCount: 0,
        comments: []
    };
    this.location = '';
    this.showMap = false;
    this.previewImageUrl = null;
    this.selectedImage = null;


  
  }

  onLocationChange(): void {
   
    if (this.location) {
      this.mapService.search(this.location).subscribe((result) => {
        if (result && result.length > 0) {
          this.newPost.latitude = result[0].lat;
          this.newPost.longitude = result[0].lon;
        }
      });
    }
  }

  onLocationSelected(event: { lat: number; lng: number,address: string }): void {
    this.newPost.latitude = event.lat;
    this.newPost.longitude = event.lng;
    this.location = event.address; 
    console.log('Selected coordinates: ', event);
    this.mapService.reverseSearch(event.lat, event.lng ).subscribe((result) => {
      if (result && result.length > 0) {
        this.location = result.display_name;
        console.log('res',result.display_name)
      }
    });
    console.log('lokacija',this.location)
  }

  submitNewPost() {
    if (this.loggedUserId !== null) {
      this.newPost.userId = this.loggedUserId;
    }
  
    const formData = new FormData();
    formData.append('description', this.newPost.description);
    formData.append('userId', this.newPost.userId.toString());
    formData.append('latitude', this.newPost.latitude.toString());
    formData.append('longitude', this.newPost.longitude.toString());
  
    if (this.selectedImage) {
      formData.append('image', this.selectedImage);
    }
    this.postService.createPost(formData).subscribe(
      response => {
        console.log('Post created successfully:', response);
        this.successMessage = "Objava je uspešno dodata!";
        this.errorMessage = null;
        setTimeout(() => this.successMessage = null, 3000);
        this.closeCreatePostForm();
      },
      error => {
        console.error('Error creating post:', error);
        this.errorMessage = "Greška pri dodavanju objave.";
        this.successMessage = null;
        setTimeout(() => this.errorMessage = null, 3000);
      }
    );
  }


  /*submitNewPost() {
    if (this.loggedUserId !== null){
      this.newPost.userId = this.loggedUserId ;
    }
    console.log(this.loggedUserId);
    this.postService.createPost(this.newPost).subscribe(
      response => {
        console.log('Post created successfully:', response);
        this.successMessage = "Objava je uspešno dodata!";
        this.errorMessage = null;
        setTimeout(() => {
          this.successMessage = null;
        }, 3000);
        
        this.closeCreatePostForm();
      },
      error => {
        console.error('Error creating post:', error);
        this.errorMessage = "Greška pri dodavanju objave.";
        this.successMessage = null;
        setTimeout(() => {
          this.errorMessage = null;
        }, 3000);
        
      }
    );
  }*/


  /*onImageSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (e: any) => {
        const imagePath = e.target.result;  // Ovo je duža putanja slike (base64)
       
      this.newPost.imagePath = imagePath;
      };
      reader.readAsDataURL(file);
    }
  }*/

    onImageSelected(event: any) {
      const file = event.target.files[0];
      if (file) {
        this.selectedImage = file;
    
        const reader = new FileReader();
        reader.onload = (e: any) => {
          this.previewImageUrl = e.target.result; // base64 privremeni prikaz
        };
        reader.readAsDataURL(file);
      }
    }


  getImageUrl(post: Post): string {
    return post.imagePath ? `${this.imageBaseUrl}${post.imagePath}` : '';
  }
  

  
}