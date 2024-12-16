import {Injectable} from '@angular/core';
import {ApiService} from './api.service';
import {ConfigService} from './config.service';
import {map} from 'rxjs/operators';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  currentUser!:any;

  constructor(
    private apiService: ApiService,
    private config: ConfigService,
  ) {
  }

  getMyInfo() {
    return this.apiService.get(this.config.whoami_url)
      .pipe(map(user => {
        this.currentUser = user;
        console.log(this.currentUser);
        return user;
      }));
  }

  getAll() {
    return this.apiService.get(this.config.users_url);
  }

  getUserId(): number | null {
    return this.currentUser ? this.currentUser.id : null;
  }

  updateUser(id: number, user: any): Observable<any> {
    return this.apiService.post(`${this.config.user_url}/${id}`, user);
  }

 
}
