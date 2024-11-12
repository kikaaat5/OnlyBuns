import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService, UserService } from '../service';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

interface DisplayMessage {
  msgType: string;
  msgBody: string;
}

@Component({
  selector: 'app-sign-up',
  templateUrl: './sign-up.component.html',
  styleUrls: ['./sign-up.component.css']
})
export class SignUpComponent implements OnInit {

  title = 'Sign up';
  form!: FormGroup;

  /**
   * Boolean used in telling the UI
   * that the form has been submitted
   * and is awaiting a response
   */
  submitted = false;

  /**
   * Notification message from received
   * form request or router
   */
  notification!: DisplayMessage;

  returnUrl!: string;
  private ngUnsubscribe: Subject<void> = new Subject<void>();

  constructor(
    private userService: UserService,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute,
    private formBuilder: FormBuilder
  ) {

  }

  ngOnInit() {
    this.route.params
      .pipe(takeUntil(this.ngUnsubscribe))
      .subscribe((params: any) => {
        this.notification = params as DisplayMessage;
      });
    // get return url from route parameters or default to '/'
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';
    // Kreiranje forme sa validacijom
    this.form = this.formBuilder.group({
      username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(64)]],
      password: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(32)]],
      confirmpassword: ['', Validators.required],
      firstname: ['', Validators.required],
      lastname: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      street: ['', Validators.required],
      city: ['', Validators.required],
      postalCode: ['', Validators.required],
      country: ['', Validators.required],
      role: ['ROLE_CLIENT']
    }, { validator: this.passwordMatchValidator }); 
  }

  passwordMatchValidator(formGroup: FormGroup) {
    const password = formGroup.get('password')?.value;
    const confirmPassword = formGroup.get('confirmpassword')?.value;
    if (password !== confirmPassword) {
      formGroup.get('confirmpassword')?.setErrors({ mismatch: true });
    } else {
      formGroup.get('confirmpassword')?.setErrors(null);
    }
  }

  ngOnDestroy() {
    this.ngUnsubscribe.next();
    this.ngUnsubscribe.complete();
  }

  onSubmit() {
    this.notification;
    this.submitted = true;
    this.authService.signup(this.form.value).subscribe(
      () => {
        this.notification = {
          msgType: 'success',
          msgBody: 'Registration successful! Please check your email to activate your account.'
        };
      },
      error => {
        if (error.status === 400) {
          this.notification = {
            msgType: 'error',
            msgBody: 'A user with this email already exists. Please use a different email.'
          };
        } else if(error.status == 409) {
          this.notification = {
            msgType: 'error',
            msgBody: 'A user with this username already exists. Please use a different username.'
          };
        } else {
          this.notification = {
            msgType: 'error',
            msgBody: 'Error occured!'
          };
        }
      }
    );
  }


}
