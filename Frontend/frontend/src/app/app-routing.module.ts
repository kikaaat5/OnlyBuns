import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HomeComponent } from './home/home.component';
import { LoginComponent } from './login/login.component';
import { SignUpComponent } from './sign-up/sign-up.component';
import { ActivateAccountComponent } from './activate-account/activate-account.component';
import { FollowingComponent } from './home/following/following.component';
import { TrendsComponent } from './home/trends/trends.component';
import { ChatComponent } from './home/chat/chat.component';
import { ProfileComponent } from './home/profile/profile.component';
import { NearbyComponent } from './home/nearby/nearby.component';

const routes: Routes = [
  {
    path: '',
    redirectTo: '/home',
    pathMatch: 'full'
  },
  { path: 'home', component: HomeComponent, children: [
    { path: 'following', component: FollowingComponent },
    { path: 'trends', component: TrendsComponent },
    { path: 'nearby', component: NearbyComponent },
    { path: 'chat', component: ChatComponent }, 
    { path: 'profile',component: ProfileComponent 
    },
  ]},
  {
    path: 'login',
    component: LoginComponent,
  },
  {
    path: 'signup',
    component: SignUpComponent,
  },
  { path: 'activate/:token', 
    component: ActivateAccountComponent,
   }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { useHash: true })],
  exports: [RouterModule]
})
export class AppRoutingModule { }
