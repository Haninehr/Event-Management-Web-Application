import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private apiUrl = 'http://localhost:8085/notifications';

  constructor(private http: HttpClient) {}

  getNotifications(): Observable<any[]> {
    //const userId = localStorage.getItem('userId'); // ou depuis AuthService
    const userId = this.getUserIdFromToken();
    if (!userId) {
      console.error('User not logged in');
      return new Observable(); 
    }
    const headers = new HttpHeaders({
      Authorization: `Bearer ${localStorage.getItem('token')}`   
    });

    console.log('Fetching notifications for userId:', userId);

    return this.http.get<any[]>(`${this.apiUrl}?userId=${userId}`, { headers });
  }

  markAsRead(id: number): Observable<any> {
    return this.http.patch(`${this.apiUrl}/${id}`, {});
  }


  private getUserIdFromToken(): number | null {
  const token = localStorage.getItem('token');
  if (!token) return null;

  try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const id = Number(payload.sub);
      return isNaN(id) ? null : id;
    } catch (error) {
      console.error('Erreur décodage JWT:', error);
      return null;
    }
}
}