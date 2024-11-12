import { BrowserModule } from '@angular/platform-browser';
import { NgModule, NO_ERRORS_SCHEMA } from '@angular/core';
import { AppRoutingModule } from './app-routing.module';
import { HttpClientModule } from '@angular/common/http';  
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { AppComponent } from './app.component';
import { SignUpComponent } from './sign-up/sign-up.component';
import { LoginComponent } from './login/login.component';
import { HeaderComponent } from './header/header.component';

import {ApiService} from './service/api.service';
import {FooService} from './service/foo.service';
import {AuthService} from './service/auth.service';
import {UserService} from './service/user.service';
import {ConfigService} from './service/config.service';

import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { TokenInterceptor } from './interceptor/TokenInterceptor';
import { AngularMaterialModule } from './angular-material/angular-material.module';
import { ActivateAccountComponent } from './activate-account/activate-account.component';

import { FollowingComponent } from './home/following/following.component';
import { TrendsComponent } from './home/trends/trends.component';
import { NearbyComponent } from './home/nearby/nearby.component';
import { ChatComponent } from './home/chat/chat.component';
import { ProfileComponent } from './home/profile/profile.component';
import { HomeComponent } from './home/home.component';


@NgModule({
  declarations: [
    AppComponent,
    HomeComponent,
    SignUpComponent,
    LoginComponent,
    HeaderComponent,
    ActivateAccountComponent,
    FollowingComponent,
    TrendsComponent,
    NearbyComponent,
    ChatComponent,
    ProfileComponent
  ],

  imports: [
    BrowserModule,
    AppRoutingModule,
    ReactiveFormsModule,
    NoopAnimationsModule,
    HttpClientModule,
    FormsModule,
    AngularMaterialModule,

  ],
  providers: [{
    provide: HTTP_INTERCEPTORS,
    useClass: TokenInterceptor,
    multi: true
  },
  FooService,
  AuthService,
  ApiService,
  UserService,
  ConfigService,
],
  bootstrap: [AppComponent],
  schemas: [NO_ERRORS_SCHEMA]
})
export class AppModule { }
