import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { EventService } from '../../Services/event.service';
import { RegistrationService } from '../../Services/registration.service';
import { AuthService } from '../../Services/auth.service'; 
import { Event } from '../../Others/models/event.model';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog } from '@angular/material/dialog';
import { ConfirmDialogComponent, ConfirmDialogData } from '../../Others/confirm-dialog/confirm-dialog.component';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatListModule } from '@angular/material/list';
import { MatExpansionModule } from '@angular/material/expansion';

import { RouterModule } from '@angular/router';
import { T } from '@angular/cdk/keycodes';
import { Observable } from 'rxjs';
import { Participant } from '../../Others/models/Participant.model';


@Component({
  selector: 'app-event-detail',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatProgressSpinnerModule 
    ,MatCardModule, MatChipsModule , MatIconModule 
    , RouterModule , MatExpansionModule , MatListModule ],
  templateUrl: './EventDetail.component.html',
  styleUrl: './EventDetail.component.scss'
})
export class EventDetailComponent implements OnInit {
  event!: Event;
  isRegistered = false;
  isRefused=false;
  isEnattend = false;

  acceptedParticipants: Participant[] = [];
pendingParticipants : Participant[] = [];
refusedParticipants : Participant[] = [];
  
  RegistrationStatus = 'NONE';
  constructor(private route: ActivatedRoute, private dialog: MatDialog, private snackBar: MatSnackBar, private eventService: EventService, private registrationService: RegistrationService, public authService: AuthService) {}

  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.eventService.getEvent(id).subscribe(event => {
      this.event = event;
      //console.log(this.isOrganizer());
      if (this.isOrganizer()){
       
      
      this.getpendingParticipant();
      this.getacceptedParticipant();
      this.getrefusedParticipant();
      }
      if(this.authService.isLoggedIn() && !this.isOrganizer()){
        this.GetRegsitartionStatus();
      }
      
      this.getParticipantNumber();
     
    });
  }



  getpendingParticipant(){
this.registrationService.getPendingParticipantList(this.event.id).subscribe((res: Participant[])=>{
  this.pendingParticipants = res;
}
);
    
  }



  getacceptedParticipant(){
 this.registrationService.getRegistredParticipantList(this.event.id).subscribe((res: Participant[])=>{
  this.acceptedParticipants = res;
});

    
  }


  getrefusedParticipant(){
    this.registrationService.getRefusedParticipantList(this.event.id).subscribe((res: Participant[])=>{
  this.refusedParticipants = res;
});

  }



  hasMedia(): boolean {
  return this.event?.medias?.length > 0;
}


  PerformAcceptParticipant(pariticpantId : number){
this.registrationService.acceptParticipant(this.event.id, pariticpantId).subscribe({
    next: () => {
      this.snackBar.open('Participant accèpté !', '', { duration: 3000 , panelClass:['success-snackbar']});

      
      this.getpendingParticipant();
      this.getacceptedParticipant();
      this.getrefusedParticipant();
      this.getParticipantNumber();
    },
    error: (err) => {
      this.snackBar.open(
        err.error?.message || 'Erreur lors de accept',
        '',
        { duration: 5000, panelClass: ['error-snackbar'] }
      );
    }
  });
  }

  PerformRefuseParticipant(participantId: number) {
  this.registrationService.refuseParticipant(this.event.id, participantId).subscribe({
    next: () => {
      this.snackBar.open('Participant refusé', '', { duration: 3000 , panelClass:['success-snackbar']});

      // Refresh lists
      this.getpendingParticipant();
      this.getacceptedParticipant();
      this.getrefusedParticipant();
      this.getParticipantNumber();
    },
    error: (err) => {
      this.snackBar.open(
        err.error?.message || 'Erreur lors du refus',
        '',
        { duration: 5000, panelClass: ['error-snackbar'] }
      );
    }
  });
}



  
  checkRegistration() {
    this.registrationService.isRegistered(this.event.id).subscribe((res: boolean) => {
      this.isRegistered = res;
    });
  }

  

  
  GetRegsitartionStatus() {
    
    this.registrationService.getRegistrationStatusinEvent(this.event.id).subscribe((res: string)=> {
      this.RegistrationStatus=res;
      console.log("registration status : ",res);
    })
  }
  
  get isDisabled(): boolean {
  return this.RegistrationStatus === 'REFUSED' ||
          
         (this.isEventFull() && this.RegistrationStatus !== 'REGISTERED');
}


isLoading = false;

get currentText(): string {
  switch (this.RegistrationStatus) {
    case 'REGISTERED': return 'Se désinscrire';
    case 'ENATTEND':    return 'En attente';
    case 'REFUSED':    return 'Demande refusée';
    default:           return 'S\'inscrire';   
  }
}


get currentIcon(): string {
  switch (this.RegistrationStatus) {
    case 'REGISTERED': return 'cancel';
    case 'ENATTEND':    return 'schedule';
    case 'REFUSED':    return 'block';
    default:           return 'how_to_reg';   
  }
}

  







toggleRegistration() {
  if (this.RegistrationStatus === 'REFUSED' || this.isLoading) return;

  this.isLoading = true;

  let action$: Observable<any>;
  

  if (this.RegistrationStatus === 'REGISTERED') {
    action$ = this.registrationService.unregister(this.event.id);
  }
  else if (this.RegistrationStatus === 'ENATTEND') {
    action$ = this.registrationService.unregister(this.event.id); 
  }
  else {
    action$ = this.registrationService.register(this.event.id);
  }

  action$.subscribe({
    next: (response) => {
      this.isLoading = false;

  
      if (this.RegistrationStatus === 'REGISTERED') {
        this.RegistrationStatus = 'NONE';
        this.snackBar.open('Inscription annulée', '', {
          duration: 4000,
          panelClass: ['warning-snackbar']
        });
      }
      else if (this.RegistrationStatus === 'ENATTEND') {
        this.RegistrationStatus = 'NONE';
        this.snackBar.open('Demande annulée', '', {
          duration: 4000,
          panelClass: ['warning-snackbar']
        });
      }
      else {
        this.RegistrationStatus = 'ENATTEND'; 
        this.snackBar.open('Demande d\'inscription envoyée !', '', {
          duration: 5000,
          panelClass: ['success-snackbar']
        });
      }

      this.getParticipantNumber?.();
    },
    error: (err) => {
      this.isLoading = false;
      console.error('Erreur inscription:', err);

      this.snackBar.open(
        'Une erreur est survenue. Réessayez.',
        'OK',
        {
          duration: 6000,
          panelClass: ['error-snackbar']
        }
      );
    }
  });
}

   

  getParticipantNumber(){
    this.registrationService.getParticipantCount(this.event.id).subscribe((res: number)=>{
      this.event.participantCount = res;
    });
  }

 isEventFull(): boolean {
  return this.event && (this.event.participantCount || 0) >= (this.event.maxcapacity || 0);
}



getParticipantCount(): number {
  return this.event?.participantCount || 0;
}

getMaxCapacity(): number {
  return this.event?.maxcapacity || 0;
}
isFull(): boolean {
  return this.getParticipantCount() >= this.getMaxCapacity();
}


isOrganizerOfEvent(){
  return this.event.organizerId==this.authService.getCurrentUser()?.id;
}
isOrganizer(){
  return this.authService.isOrganizer();
}

private apiUrl = 'http://localhost:8085';

getFullUrl(relativePath: string): string {
  return this.apiUrl + relativePath;
}


isImage(url: string): boolean {
  return /\.(jpg|jpeg|png|gif|webp|svg)$/i.test(url);
}

getFileName(url: string): string {
  return url.split('/').pop() || 'fichier';
}

openLightbox(url: string) {
  window.open(this.getFullUrl(url), '_blank');
}



cancelEvent(): void {
  const dialogRef = this.dialog.open(ConfirmDialogComponent, {
    width: '420px',
    data: {
      title: 'Annuler l’événement',
      message: `Êtes-vous sûr de vouloir annuler définitivement<br><strong>« ${this.event.title} »</strong> ?<br><br>Cette action est irréversible.`,
      confirmText: 'Oui, annuler l’événement',
      cancelText: 'Non, garder l’événement',
      confirmColor: 'warn'
    } as ConfirmDialogData
  });

  dialogRef.afterClosed().subscribe(confirmed => {
    if (confirmed) {
      this.performCancel();
    }
  });
}

private performCancel(): void {
  this.eventService.deleteEvent(this.event.id).subscribe({
    next: () => {
     
      this.event.status="CANCELED";
     

      this.snackBar.open('Événement annulé avec succès', '', {
        duration: 6000,
        panelClass: ['success-snackbar']
      });
    },
    error: (err) => {
      console.error('Erreur lors de l’annulation', err);
      this.snackBar.open('Impossible d’annuler l’événement', 'Réessayer', {
        duration: 8000,
        panelClass: ['error-snackbar']
      });
    }
  });
}






AcceptParticipant(p : Participant): void {
  const dialogRef = this.dialog.open(ConfirmDialogComponent, {
    width: '550px',
    data: {
      title: 'Accepeter ce pariticpant',
      message: `Êtes-vous sûr de vouloir accept le pariticpant <strong> ${p.username} </strong> <br>pour l'évenement<strong>« ${this.event.title} »</strong> ?<br><br>Cette action est irréversible.`,
      confirmText: 'Oui, accpeter',
      cancelText: 'Non, annuler',
      confirmColor: 'warn'
    } as ConfirmDialogData
  });

  dialogRef.afterClosed().subscribe(confirmed => {
    if (confirmed) {
      this.PerformAcceptParticipant(p.userId);
    }
  });
}


RefuseParticipant(p : Participant): void {
  const dialogRef = this.dialog.open(ConfirmDialogComponent, {
    width: '550px',
    data: {
      title: 'Refuser ce pariticpant',
      message: `Êtes-vous sûr de vouloir refuser le pariticpant <strong> ${p.username} </strong> <br>pour l'évenement<strong>« ${this.event.title} »</strong> ?<br><br>Cette action est irréversible.`,
      confirmText: 'Oui, Refuser',
      cancelText: 'Non, annuler',
      confirmColor: 'warn'
    } as ConfirmDialogData
  });

  dialogRef.afterClosed().subscribe(confirmed => {
    if (confirmed) {
      this.PerformRefuseParticipant(p.userId);
    }
  });
}


}