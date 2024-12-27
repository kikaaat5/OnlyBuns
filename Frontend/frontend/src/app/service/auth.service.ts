import { Injectable } from '@angular/core';
import { HttpHeaders } from '@angular/common/http';
import { ApiService } from './api.service';
import { UserService } from './user.service';
import { ConfigService } from './config.service';
import { catchError, map } from 'rxjs/operators';
import { Router } from '@angular/router';
import { of } from 'rxjs/internal/observable/of';
import { Observable } from 'rxjs';

@Injectable()
export class AuthService {

  private access_token;

  constructor(
    private apiService: ApiService,
    private userService: UserService,
    private config: ConfigService,
    private router: Router
  ) {
    this.access_token = localStorage.getItem("jwt"); 
  }

  initializeAuthState() {
    const token = this.getToken();
    if (token) {
      // Optionally validate the token with the server
      this.access_token = token;
      this.initializeSession();
    } else {
      this.logout();
    }
  }

  initializeSession() {
    const token = this.getToken();
    if (token) {
      // Load user information
      this.userService.getMyInfo().subscribe(
        (user) => {
          console.log('User successfully initialized:', user);
        },
        (err) => {
          console.error('Error initializing user:', err);
          this.logout(); // Logout if token is invalid
        }
      );
    } else {
      this.logout(); // Logout if no token is found
    }
  }

  login(user:any) {
    const loginHeaders = new HttpHeaders({
      'Accept': 'application/json',
      'Content-Type': 'application/json'
    });
    // const body = `username=${user.username}&password=${user.password}`;
    const body = {
      'email': user.username,
      'password': user.password
    };

    return this.apiService.post(this.config.login_url, JSON.stringify(body), loginHeaders)
      .pipe(map((res) => {
        console.log('Full response:', res);
        console.log('Login success');
        this.access_token = res.body.accessToken;
        localStorage.setItem("jwt", res.body.accessToken)
      }));
  }

  signup(user:any) {
    console.log(user);
    const signupHeaders = new HttpHeaders({
      'Accept': 'application/json',
      'Content-Type': 'application/json'
    });
    return this.apiService.post(this.config.signup_url, JSON.stringify(user), signupHeaders)
      .pipe(map(() => {
        console.log('Sign up success');
      }));
  }

  logout() {
    this.userService.currentUser = null;
    localStorage.removeItem("jwt");
    this.access_token = null;       
    this.router.navigate(['/login']);
  }

  activateAccount(token: string): Observable<any> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${this.getToken()}`,
      'Accept': 'application/json',
      'Content-Type': 'application/json'
    });
  
    return this.apiService.get(this.config.activation_url + `/${token}`, { headers });
  }

  tokenIsPresent() {
    const token = this.getToken();
    const isTokenPresent = !!token;
    console.log('Is token present:', isTokenPresent);
    return isTokenPresent;
  }

  getToken(): string | null {
    const token = localStorage.getItem("jwt");
    console.log('Token returned by getToken:', token);
    return token;
  }

  updatePassword(userId: number, oldPassword: string, newPassword: string) {
    const body = { oldPassword, newPassword };
    return this.apiService.post(`${this.config.user_url}/change-password/${userId}`, body).pipe(map(user => {  
      this.logout();
    }));
  }


}
