
import { HttpInterceptorFn, HttpRequest, HttpHandlerFn, HttpEvent } from '@angular/common/http';
import { inject } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';

import { AuthService } from '../../Services/auth.service';
import { Router } from '@angular/router';

export const jwtInterceptor: HttpInterceptorFn = (req: HttpRequest<any>, next: HttpHandlerFn) => {
  const token = localStorage.getItem('token');
  const authService = inject(AuthService);
  const router = inject(Router);
  if (token) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }
  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        // Token expired or invalid → clear and redirect
        authService.logout(); // clears token + any user data

        // Show immediate notification
        authService.showSessionExpiredMessage();

        // Redirect to login (with returnUrl if you want)
        router.navigate(['/login'], { 
          queryParams: { returnUrl: router.routerState.snapshot.url } 
        });
      }
      return throwError(() => error);
    })
  );


//paste this into console to see token info !
  /*(() => {
  const token = localStorage.getItem('token');
  if (!token) return console.warn('No token found in localStorage');

  try {
    const payload = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));
    const exp = payload.exp * 1000;
    const now = Date.now();
    const timeLeft = exp - now;

    const format = (ms) => {
      if (ms <= 0) return 'EXPIRED';
      const s = Math.floor(ms / 1000);
      const m = Math.floor(s / 60);
      const h = Math.floor(m / 60);
      return `${h}h ${m % 60}m ${s % 60}s`;
    };

    console.log('%c JWT Token Info ', 'background: #222; color: #bada55; font-size: 14px; padding: 4px 8px; border-radius: 4px;');
    console.log('Token:', token);
    console.log('Payload:', payload);
    console.log('Issued at (iat): ', new Date(payload.iat * 1000).toLocaleString());
    console.log('Expires at (exp):', new Date(exp).toLocaleString());
    console.log('%cTime left: ' + format(timeLeft), 
      timeLeft <= 0 ? 'color: red; font-weight: bold;' : 
      timeLeft < 300000 ? 'color: orange; font-weight: bold;' : 'color: lime; font-weight: bold;');
    console.log('Is expired?', timeLeft <= 0);
  } catch (e) {
    console.error('Invalid JWT token', e);
  }
})();*/
};