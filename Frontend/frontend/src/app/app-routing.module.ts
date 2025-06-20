import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HomeComponent } from './home/home.component';
import { LoginComponent } from './login/login.component';
import { SignUpComponent } from './sign-up/sign-up.component';
import { ClientListComponent } from './client-list/client-list.component'; // Proveri da li se ovo koristi
import { PostListComponent } from './post-list/post-list.component';
import { ActivateAccountComponent } from './activate-account/activate-account.component';
import { FollowingComponent } from './home/following/following.component';
import { TrendsComponent } from './home/trends/trends.component';
import { ChatComponent } from './home/chat/chat.component';
import { ProfileComponent } from './home/profile/profile.component'; // Putanja do tvoje ProfileComponent
import { NearbyComponent } from './home/nearby/nearby.component';
import { PostComponent } from './home/post/post.component';
import { MapComponent } from './map/map.component';
import { PublicClientListComponent } from './home/public-client-list/public-client-list.component';


const routes: Routes = [
  {
    path: '',
    component: SignUpComponent, // Početna stranica za neprijavljene
  },
  {
    path: 'home',
    component: HomeComponent,
    children: [
      { path: 'following', component: FollowingComponent },
      { path: 'trends', component: TrendsComponent },
      { path: 'nearby', component: NearbyComponent },
      { path: 'chat', component: ChatComponent }, // Uklonjen duplikat ako je bio

      // UNIFIKOVANE RUTE ZA PROFIL UNUTAR HOME SEKCIIJE
      // Ruta za moj profil: /home/profile
      { path: 'profile', component: ProfileComponent },
      // Ruta za tuđi profil: /home/profile/:userId
      { path: 'profile/:userId', component: ProfileComponent },

      { path: 'post', component: PostComponent, children:[
        {path:'posts',component:PostListComponent},
        {path:'map',component:MapComponent}
      ]},
      // Ako 'posts' na top-levelu nije za ceo sajt, razmisli da bude child rute ovde
     { path: 'posts', component: PostListComponent}, 
      { path: 'explore-clients', component: PublicClientListComponent },
    ]
  },
  {
    path: 'login',
    component: LoginComponent,
  },
  {
    path: 'signup',
    component: SignUpComponent,
  },
  {
    path: 'client-list', component: ClientListComponent 
  },
  {
    path: 'posts', 
    component: PostListComponent
  },
  {
    path: 'activate/:token', 
    component: ActivateAccountComponent,
  },
  // OBAVEZNO UKLONJENA TOP-LEVEL RUTA ZA PROFIL KOJA SE KONFLIKTOVALA
  // { path: 'profile/:userId', component: ProfileComponent } // OVO JE UKLONJENO
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { useHash: true })], // useHash: true ostaje
  exports: [RouterModule]
})
export class AppRoutingModule { }