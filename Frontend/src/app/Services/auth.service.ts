import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { MatSnackBar } from '@angular/material/snack-bar';
import { BehaviorSubject, tap } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';
import { Router } from '@angular/router';

interface AuthResponse {
  token: string;
  user: {
    id: number;
    username: string;
    role: 'ORGANIZER' | 'PARTICIPANT';
  };
} 

export type UserRole = 'ORGANIZER' | 'PARTICIPANT';

export interface CurrentUser {
  id: number;
  email: string;
  role: UserRole;
}

@Injectable({ providedIn: 'root' }) //providedIn: 'root' means the service is a singleton (Service is global + singleton)
//Now you can inject UserService anywhere: for example : constructor(private authservice: AuthService) {}

export class AuthService {
 private currentUserSubject = new BehaviorSubject<CurrentUser | null>(null);
  currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient , private snackBar: MatSnackBar , private router: Router) {
    this.loadUserFromToken(); 
  }

  // Call this on every app startup and after login
  private loadUserFromToken(): void {
    const token = localStorage.getItem('token');
    if (!token) {
      this.currentUserSubject.next(null);
      return;
    }

    try {
      const payload = this.decodeJwt(token);

      const user: CurrentUser = {
        id: Number(payload.sub),           
        email: payload.email,
        role: payload.role as UserRole    
      };

      this.currentUserSubject.next(user);
    } catch (error) {
      console.error('Invalid or expired token', error);
      this.logout();
    }
  }

  private decodeJwt(token: string): any {
    try {
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(
        atob(base64)
          .split('')
          .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join('')
      );
      return JSON.parse(jsonPayload);
    } catch (e) {
      return {};
    }
  }

  login(credentials: { username: string; password: string }) {
    return this.http.post<AuthResponse>('http://localhost:8085/authentifications/signin', credentials)
      .pipe(
        tap(res => {
          localStorage.setItem('token', res.token);
          
          this.loadUserFromToken();
        })
      );
  }

 
signup(data: any) {
  return this.http.post('http://localhost:8085/authentifications/signup', data, {
    responseType: 'text' 
  }).pipe(
    tap((response) => {
      console.log('Signup successful:', response);
    }),
    catchError((error) => {
      console.error('Signup error:', error);
      return throwError(() => error); 
    })
  );
}


  logout(): void {
    localStorage.removeItem('token');
    this.currentUserSubject.next(null);
    this.snackBar.open('Déconnexion réussie !', 'OK', { duration: 3000 , panelClass :['success-snackbar']});
  }

  isLoggedIn(): boolean {
    return !!localStorage.getItem('token');
  }

  getCurrentUser(): CurrentUser | null {
    return this.currentUserSubject.value;
  }

  isOrganizer(): boolean {
    return this.getCurrentUser()?.role === 'ORGANIZER';
  }

  isParticipant(): boolean {
    return this.getCurrentUser()?.role === 'PARTICIPANT';
  }



  showSessionExpiredMessage() {
   
    // Alternatives:
    this.snackBar.open('Session expired. Please login again.', 'Close', { duration: 9000 , panelClass:['warning-snackbar']});
    // or Toastr, PrimeNG Messages, etc.
  }



  isTokenExpired(token?: string): boolean {
  const t = token || localStorage.getItem('token');
  if (!t) return true;

  try {
    const payload = JSON.parse(atob(t.split('.')[1]));
    if (!payload.exp) return false;
    // Consider expired 10 seconds before actual expiry (safety)
    return Date.now() >= (payload.exp * 1000) - 10_000;
  } catch {
    return true;
  }
}


  // In your AuthService or a separate TokenService
private getTokenExpirationDate(token: string): Date | null {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    if (payload.exp) {
      return new Date(payload.exp * 1000);
    }
  } catch (e) { }
  return null;
}

// Call this on app startup and after login
startTokenExpirationCheck() {
  const token = localStorage.getItem('token');
  if (!token) return;

  const expiresAt = this.getTokenExpirationDate(token);
  if (!expiresAt) return;

  const now = new Date();
  const timeout = expiresAt.getTime() - now.getTime() - 60 * 1000; // 1 minute before

  if (timeout > 0) {
    setTimeout(() => {
      this.logout();
      this.showSessionExpiredMessage();
      this.router.navigate(['/login']);
    }, timeout);
  } else {
    // Already expired
    this.logout();
    this.router.navigate(['/login']);
  }
}
  
}