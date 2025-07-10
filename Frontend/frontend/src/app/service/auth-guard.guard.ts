// src/app/service/auth.guard.ts

import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { Observable, of } from 'rxjs';
import { switchMap, catchError, map, tap } from 'rxjs/operators'; // Dodaj 'tap'

import { UserService } from './user.service';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {

  constructor(
    private userService: UserService,
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(): Observable<boolean> {
    console.log('AuthGuard: canActivate() - Početak provere rute.');

    // Ako je korisnik već učitan, dozvoli pristup
    if (this.userService.currentUser) {
      console.log('AuthGuard: Korisnik već učitan (this.userService.currentUser je popunjen). Dozvoljavam pristup.');
      return of(true);
    }

    console.log('AuthGuard: Korisnik nije učitan. Proveravam token...');

    // Ako korisnik nije učitan, prvo proveri da li postoji token
    const token = this.authService.getToken();
    if (!token) {
      console.log('AuthGuard: Token NIJE pronađen. Preusmeravam na /login.');
      this.router.navigate(['/login']);
      return of(false);
    }

    console.log('AuthGuard: Token je pronađen. Pokušavam da dohvatim informacije o korisniku (getMyInfo())...');

    // Ako postoji token, ali ne i objekat korisnika, učitaj ga
    return this.userService.getMyInfo().pipe(
      tap(user => { // Koristimo tap da logujemo bez menjanja toka
        if (user) {
          console.log('AuthGuard: userService.getMyInfo() je uspešno vratio korisnika. Korisničko ime:', user.username);
        } else {
          console.log('AuthGuard: userService.getMyInfo() je vratio NULL/UNDEFINED. Korisnik nije dohvaćen.');
        }
      }),
      map(user => {
        // Ako je korisnik uspešno učitan, dozvoli pristup
        if (user) {
          console.log('AuthGuard: Finalna provera - korisnik postoji. Dozvoljavam pristup ruti.');
          return true;
        }
        // Ako iz nekog razloga user nije stigao, preusmeri na login
        console.log('AuthGuard: Finalna provera - korisnik NE postoji. Preusmeravam na /login.');
        this.router.navigate(['/login']);
        return false;
      }),
      catchError(error => { // hvata greške iz getMyInfo()
        console.error('AuthGuard: Greška pri dohvatanju korisničkih informacija (getMyInfo()):', error);
        // U slučaju greške pri dohvatanju korisnika, preusmeri na login
        this.router.navigate(['/login']);
        return of(false);
      })
    );
  }
}