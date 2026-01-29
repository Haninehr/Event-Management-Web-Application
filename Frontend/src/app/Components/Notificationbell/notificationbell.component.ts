
import { Component, OnInit } from '@angular/core';
import { NotificationService } from '../../Services/notification.service';
import { MatBadgeModule } from '@angular/material/badge';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router ,RouterModule} from '@angular/router';

@Component({
  selector: 'app-notificationbell',
  standalone: true,
  imports: [CommonModule, MatBadgeModule, MatIconModule, MatMenuModule , RouterModule],
  templateUrl: './notificationbell.component.html', 
  styleUrl: './notificationbell.component.scss'
})
export class NotificationBellComponent implements OnInit { 
  notifications: any[] = [];
  unreadCount = 0;

  constructor(private notificationService: NotificationService , private router:Router) {}

  ngOnInit() {
    this.loadNotifications();
    setInterval(() => this.loadNotifications(), 10000); 
  }

  loadNotifications() {
    this.notificationService.getNotifications().subscribe(notifs => {
      
      this.notifications = notifs.filter((n: any) => !n.isread);
      this.unreadCount = notifs.filter((n: any) => !n.isread).length;

      
    });
  }

  markAsRead(id: number , eventid: number) {
    
  if (!eventid) return;

  // Optimistically mark as read in UI
  

  this.notificationService.markAsRead(id).subscribe({
    next: () => {
      this.loadNotifications(); // refresh count
    },
    error: (err) => {
      
      console.error('Failed to mark as read', err);
    }
  });

  // Navigate immediately for better UX
  this.router.navigate(['/events', eventid] , { onSameUrlNavigation: 'reload' });
  }

  
}