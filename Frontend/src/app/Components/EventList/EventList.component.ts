import { Component, OnInit } from '@angular/core';
import { EventService } from '../../Services/event.service';
import { Event } from '../../Others/models/event.model';
import { AuthService } from '../../Services/auth.service';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { RegistrationService } from '../../Services/registration.service';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import {  switchMap, map, catchError } from 'rxjs/operators';
import { of, forkJoin } from 'rxjs';
import { delay } from 'rxjs/operators';

@Component({
  selector: 'app-event-list',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatButtonModule,
    MatInputModule, MatDatepickerModule, MatFormFieldModule, 
    RouterModule, FormsModule , MatProgressSpinnerModule, MatIconModule],
  templateUrl: './EventList.component.html',
  styleUrl: './EventList.component.scss'
})
export class EventListComponent implements OnInit {


  today: Date = new Date();  
  tomorrow: Date;

  isLoading = true;  
  events: Event[] = [];
  searchKeyword = '';
  searchLocation = '';
  searchDate: Date | null = null;


  hasEverSearchedWithFilters = false;

  constructor( private eventService: EventService ,  private registrationService: RegistrationService ,  public authService: AuthService) { 
    this.tomorrow = new Date(this.today);
    this.tomorrow.setDate(this.tomorrow.getDate() + 1);
  }

  ngOnInit() {
    this.loadAllEvents();
  }

  get filteredEvents(): Event[] {
    if (!this.events || this.events.length === 0) return [];

    return this.events.filter(event => {
      // If event is CANCELED → only show if user is logged in AND registered
      if (event.status === 'CANCELED') {
        return this.authService.isLoggedIn() && event.isRegistered || this.authService.isOrganizer();
      }
      if (event.status == 'ENDED'){
        return false;
      }
      // Otherwise, show all non-canceled events
      return true;
    });
  }

  private loadAllEvents() {
    this.isLoading = true;
    const params = {}; // empty params = all events

    this.eventService.getEvents(params).pipe(
      delay(3500),
      switchMap(events => this.enrichEvents(events)) // we'll extract this below
    ).subscribe({
      next: (events) => {
        this.events = events;
        this.isLoading = false;
      },
      error: (err) => {
        console.error(err);
        this.events = [];
        this.isLoading = false;
      }
    });
  }

  search() {
    this.isLoading = true;

    const allFiltersEmpty =
      this.searchKeyword.trim() === '' &&
      this.searchLocation.trim() === '' &&
      this.searchDate === null;

    if (allFiltersEmpty) {
      // User clicked Search with empty fields

      if (this.hasEverSearchedWithFilters) {
        // User previously filtered → now wants to reset → load all
        this.hasEverSearchedWithFilters = false; // optional: reset or keep true
        this.loadAllEvents();
      } else {
        // First time or no previous filter → do nothing (keep current list)
        this.isLoading = false;
        return;
      }
    } else {
      // Real filters applied → mark that user has filtered before
      this.hasEverSearchedWithFilters = true;

      const params: any = {};
      if (this.searchKeyword.trim()) params.keyword = this.searchKeyword.trim();
      if (this.searchLocation.trim()) params.location = this.searchLocation.trim();
      if (this.searchDate) params.date = this.PipeDate(this.searchDate);

      this.eventService.getEvents(params).pipe(
        delay(3500),
        switchMap(events => this.enrichEvents(events))
      ).subscribe({
        next: (events) => {
          this.events = events;
          this.isLoading = false;
        },
        error: (err) => {
          console.error(err);
          this.events = [];
          this.isLoading = false;
        }
      });
    }
  }

  // Extracted helper to avoid code duplication
  private enrichEvents(events: Event[]) {
    if (!events || events.length === 0) {
      return of([]);
    }

    const userId = this.authService.getCurrentUser()?.id;
    if (!userId || !this.authService.isLoggedIn()) {
      console.log("notlogedin !!!");
      const requests = events.map(event =>
        this.registrationService.getParticipantCount(event.id).pipe(
          map(count => ({ ...event, participantCount: count, isRegistered: false, Organizerofevent: false }))
        )
      );
      return forkJoin(requests);
    }

    const enrichedEvents$ = events.map(event => forkJoin({
      count: this.registrationService.getParticipantCount(event.id).pipe(catchError(() => of(0))),
      registered: this.registrationService.isRegistered(event.id).pipe(catchError(() => of(false))),
    }).pipe(
      map(({ count, registered }) => ({
        ...event,
        participantCount: count,
        isRegistered: registered,
        Organizerofevent: event.organizerId === userId
      }))
    ));

    return forkJoin(enrichedEvents$);
  }

  

  PipeDate(searchDate: Date | null) {
    if (!searchDate) return '';


    return (
      searchDate.getFullYear() +
      '-' +
      String(searchDate.getMonth() + 1).padStart(2, '0') +
      '-' +
      String(searchDate.getDate()).padStart(2, '0')
    );
  }

  parseDateString(date: string | null | undefined): Date | null {
  if (!date) return null;

  const parsed = new Date(date);
  if (isNaN(parsed.getTime())) {
    return null;
  }

  parsed.setHours(0, 0, 0, 0); // optional – compare only dates
  //console.log(parsed);
  //console.log(this.today);
  return parsed;
}

  
}