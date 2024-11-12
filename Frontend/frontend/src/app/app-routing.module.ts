import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HomeComponent } from './home/home.component';
import { LoginComponent } from './login/login.component';
import { SignUpComponent } from './sign-up/sign-up.component';
import { FollowingComponent } from './home/following/following.component';
import { TrendsComponent } from './home/trends/trends.component';
import { ChatComponent } from './home/chat/chat.component';
import { ProfileComponent } from './home/profile/profile.component';
import { NearbyComponent } from './home/nearby/nearby.component';
import { PostComponent } from './home/post/post.component';

const routes: Routes = [
  {
    path: '',
    component: SignUpComponent, 
  },
  { path: 'home', component: HomeComponent, children: [
    { path: 'following', component: FollowingComponent },
    { path: 'trends', component: TrendsComponent },
    { path: 'nearby', component: NearbyComponent },
    { path: 'chat', component: ChatComponent }, 
    { path: 'profile',component: ProfileComponent },
    { path: 'post', component: PostComponent}
  ]},
  {
    path: 'login',
    component: LoginComponent,
  },
  {
    path: 'signup',
    component: SignUpComponent,
  }
  
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
