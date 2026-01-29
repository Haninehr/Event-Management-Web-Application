import { Component ,OnInit, OnDestroy} from '@angular/core';
import { RouterModule, RouterOutlet } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatBadgeModule } from '@angular/material/badge';
import { MatMenuModule } from '@angular/material/menu';
import { CommonModule } from '@angular/common';
import { AuthService } from './Services/auth.service';
import { Router } from '@angular/router';
import { NotificationBellComponent } from './Components/Notificationbell/notificationbell.component'; 

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatBadgeModule,
    MatMenuModule,
    NotificationBellComponent,
    RouterModule
  ],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']  
})
export class AppComponent implements OnInit, OnDestroy{

  private checkInterval: any;

  constructor(public authService: AuthService , public router: Router) {}

  logout() {
    this.authService.logout();
  }


 get showCreateEventButton(): boolean {
  if (!this.authService.isLoggedIn() || !this.authService.isOrganizer()) {
    return false;
  }

  const url = this.router.url;

  // Hide the button when on the "create new event" or "update event" pages
  return !url.includes('/new') && 
         !url.includes('/update'); // adjust this path if your update route is different
}


  ngOnInit(): void {
    // This runs once when the app starts
    this.startTokenExpirationChecker();
  }

  ngOnDestroy(): void {
    if (this.checkInterval) {
      clearInterval(this.checkInterval);
    }
  }

  private startTokenExpirationChecker(): void {
    // Check every 2 seconds if token is expired
    this.checkInterval = setInterval(() => {
      if (this.authService.isLoggedIn() && this.authService.isTokenExpired()) {
        console.log('Token expired → forcing logout right now!');
        this.forceLogout();
      }
    }, 2000); // 2 seconds is perfect balance
  }

  private forceLogout(): void {
    clearInterval(this.checkInterval); // stop checking

    this.authService.logout();                    // clear localStorage
    this.authService.showSessionExpiredMessage(); // your alert !

    this.router.navigate(['/login'], {
      queryParams: { returnUrl: this.router.url }
    });
  }
 
}


