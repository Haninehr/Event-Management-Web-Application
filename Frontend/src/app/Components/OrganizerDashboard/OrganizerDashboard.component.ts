import { Component, OnInit } from '@angular/core';
import { EventService } from '../../Services/event.service';
import { RegistrationService } from '../../Services/registration.service'; 
import { AuthService } from '../../Services/auth.service';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { Router } from '@angular/router';
import { combineLatest, timer, of } from 'rxjs';
import { finalize, map, switchMap, catchError } from 'rxjs/operators';
import { RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-organizer-dashboard',
  standalone: true,
  imports: [CommonModule, MatCardModule, RouterModule, MatIconModule],
  templateUrl: './OrganizerDashboard.component.html',
  styleUrl: './OrganizerDashboard.component.scss',
})
export class OrganizerDashboardComponent implements OnInit {
  myEvents: any[] = [];
  eventsWithParticipantCount: Array<{
    event: any;
    participantCount: number;
  }> = [];

  totalEvents = 0;
  activeEvents = 0;
  canceledEvents = 0;
  averageParticipation = 0;

  isLoading = true;

  constructor(
    private eventService: EventService,
    private registrationService: RegistrationService, // ← Inject it
    public authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    this.loadMyEvents();
  }

  loadMyEvents() {
    this.isLoading = true;

    const minDuration$ = timer(3000);
    const events$ = this.eventService.getEvents();

    combineLatest([minDuration$, events$]).pipe(
      map(([_, events]) => events),
      finalize(() => this.isLoading = false)
    ).subscribe(events => {
      if (!this.authService.isOrganizer()) {
        this.router.navigate(['/events']);
        return;
      }

      const organizerId = this.authService.getCurrentUser()?.id;
      this.myEvents = events.filter(e => e.organizerId === organizerId);

      // Calculate basic stats
      this.totalEvents = this.myEvents.length;
      this.activeEvents = this.myEvents.filter(e => e.status === 'ACTIVE').length;
      this.canceledEvents = this.myEvents.filter(e => e.status === 'CANCELED').length;

      // Fetch participant count for each event
      if (this.myEvents.length === 0) {
        this.eventsWithParticipantCount = [];
        this.averageParticipation = 0;
        return;
      }

      const participantCountObservables = this.myEvents.map(event =>
        this.registrationService.getParticipantCount(event.id).pipe(
          catchError(() => of(0))
        ).pipe(
          map(count => ({ event, participantCount: count as number }))
        )
      );

      combineLatest(participantCountObservables).subscribe(results => {
        this.eventsWithParticipantCount = results;

        // Update average participation
        const totalParticipants = results.reduce((sum, item) => sum + item.participantCount, 0);
        this.averageParticipation = this.totalEvents > 0
          ? Math.round((totalParticipants / this.totalEvents) * 10) / 10
          : 0;
      });
    });
  }
}