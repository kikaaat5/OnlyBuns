import { Component } from '@angular/core';
import { AuthService } from './service/auth.service';
import { UserService } from './service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'OnlyBunsMKJ';

  constructor(
    private authService: AuthService
  ) { }

  ngOnInit(): void {
    this.authService.initializeAuthState();
  }
}
