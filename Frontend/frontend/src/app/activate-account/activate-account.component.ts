import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../service/auth.service';

@Component({
  selector: 'app-activate-account',
  templateUrl: './activate-account.component.html',
  styleUrls: ['./activate-account.component.css']
})
export class ActivateAccountComponent implements OnInit {

  message: string = '';

  constructor(
    private route: ActivatedRoute,
    private authService: AuthService,
    private router: Router
  ) { }

  ngOnInit(): void {
    const token = this.route.snapshot.paramMap.get('token');
    console.log('usao ovdjeeeeeeeeeee');
    // Dohvati token iz URL-a

    if (token) {
      console.log(token)
      this.authService.activateAccount(token).subscribe(
        response => {
          this.message = 'Your account has been successfully activated!';
          setTimeout(() => {
            this.router.navigate(['/login']);
          }, 3000);
        },
        error => {
          this.message = 'Activation failed: ' + error.error.message;
        }
      );
    }
  }
}
