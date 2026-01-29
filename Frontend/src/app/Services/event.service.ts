import { Injectable } from '@angular/core';
import { HttpClient , HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Event } from '../Others/models/event.model';


@Injectable({ providedIn: 'root' })
export class EventService {
  private apiUrl = 'http://localhost:8085/events';

  constructor(private http: HttpClient) {}

  getEvents(params?: any): Observable<Event[]> { 
    return this.http.get<Event[]>(this.apiUrl, { params });
  }

  getEvent(id: number): Observable<Event> {
    return this.http.get<Event>(`${this.apiUrl}/${id}`);
  }

  private httpOptions() {
  const token = localStorage.getItem('token');
  return token ? { headers: new HttpHeaders({ Authorization: `Bearer ${token}` }) } : {};
}

  createEvent(event: any): Observable<Event> {
    return this.http.post<Event>(this.apiUrl, event , {
     ...this.httpOptions() 
    });
  }

  
  uploadMedia(eventId: number, file: File, mediaType: string = 'IMAGE' , title: string): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('type', mediaType);
    formData.append("title",title);

    return this.http.post(`${this.apiUrl}/${eventId}/media`, formData , {
      ...this.httpOptions() , responseType: 'text' as 'json'
    });
  }

  updateEvent(id: number, event: any): Observable<Event> {
    return this.http.put<Event>(`${this.apiUrl}/${id}`, event 
      , {
      ...this.httpOptions() 
    }
    );
  }

  deleteEvent(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`
      , {
      ...this.httpOptions() 
    }
    );
  }


  deleteMedia(eventId: number, mediaUrl: string): Observable<string> {
    let params = new HttpParams().set('mediaUrl', mediaUrl);

    return this.http.delete<string>(`${this.apiUrl}/${eventId}/media`, { params , ...this.httpOptions()});
  }

 
}