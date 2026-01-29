import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Participant } from '../Others/models/Participant.model';

import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class RegistrationService {
  private api = 'http://localhost:8085/registrations';

  constructor(private http: HttpClient) {}

  private httpOptions() {
  const token = localStorage.getItem('token');
  return token ? { headers: new HttpHeaders({ Authorization: `Bearer ${token}` }) } : {};
}



  register(eventId: number): Observable<any> {
    console.log("heades",this.httpOptions());
    return this.http.post<any>(`${this.api}/${eventId}`, {} , {...this.httpOptions() , responseType: 'text' as 'json'});
  }

 
  unregister(eventId: number): Observable<any> {
    return this.http.delete<any>(`${this.api}/${eventId}`,  {...this.httpOptions() ,  responseType: 'text' as 'json' });
  }


  isRegistered(eventId: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.api}/${eventId}/registered`,
      {...this.httpOptions()  }
    );
  }

  isRefused(eventId: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.api}/${eventId}/refused`,
      {...this.httpOptions()  }
    );
  }
  
  isEnattend(eventId: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.api}/${eventId}/enattend`,
      {...this.httpOptions()  }
    );
  }


  //correct :!!!
  getParticipantCount(eventId: number): Observable<number> {
    return this.http.get<number>(`${this.api}/${eventId}/participantsCount`);
  }

  //correct :  new 
  acceptParticipant(eventId: number , pariticpantId: number) : Observable<any> {
    return this.http.post<any>(`${this.api}/${eventId}/accept/${pariticpantId}`, {} , {...this.httpOptions() , responseType: 'text' as 'json'});
  }

  //correct : new 
  refuseParticipant(eventId: number , pariticpantId: number) : Observable<any> {
    return this.http.post<any>(`${this.api}/${eventId}/refuse/${pariticpantId}`, {} , {...this.httpOptions() , responseType: 'text' as 'json'});
  }

  //correct : new
  getRegistrationStatusinEvent(eventId : number) : Observable<any> {
    return this.http.get<any>(`${this.api}/${eventId}/status`,
      {...this.httpOptions() , responseType: 'text' as 'json' }
    );
  }



  //new
  getRegistredParticipantList(eventId : number) : Observable<Participant[]> {

    return this.http.get<Participant[]>(`${this.api}/${eventId}/accepted`,
      {...this.httpOptions()  }
    );
  }


  //new
  getPendingParticipantList(eventId : number) : Observable<Participant[]> {

    return this.http.get<Participant[]>(`${this.api}/${eventId}/pending`,
      {...this.httpOptions()  }
    );
  }


  //new
  //this does not exist in the controller (registration) ; but exist in the regsitartion service !!!
    getRefusedParticipantList(eventId : number) : Observable<Participant[]> {

    return this.http.get<Participant[]>(`${this.api}/${eventId}/refused`,
      {...this.httpOptions()  }
    );
  }

}