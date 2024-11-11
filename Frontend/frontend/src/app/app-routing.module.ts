import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HomeComponent } from './home/home.component';
import { LoginComponent } from './login/login.component';
import { SignUpComponent } from './sign-up/sign-up.component';
import { FollowingComponent } from './following/following.component';
import { TrendsComponent } from './trends/trends.component';
import { ChatComponent } from './chat/chat.component';
import { ProfileComponent } from './profile/profile.component';
import { NearbyComponent } from './nearby/nearby.component';

const routes: Routes = [
  {
    path: '',
    component: SignUpComponent, 
  },
  {
    path: 'home',
    component: HomeComponent,
  },
  {
    path: 'login',
    component: LoginComponent,
  },
  {
    path: 'signup',
    component: SignUpComponent,
  },
  { path: 'following', 
    component: FollowingComponent
  },
  { path: 'trends', 
    component: TrendsComponent 
  },
  { path: 'nearby', 
    component: NearbyComponent 
  },
  { path: 'chat',
    component: ChatComponent
  },
  { path: 'profile',
    component: ProfileComponent 
  },
  
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
