import { Routes } from '@angular/router';
import { EventListComponent } from './Components/EventList/EventList.component';
import { EventDetailComponent } from './Components/EventDetail/EventDetail.component';
import { CreateEventComponent } from './Components/CreateEvent/CreateEvent.component';
import { UpdateEventComponent } from './Components/UpdateEvent/UpdateEvent.component';
import { LoginComponent } from './Components/Login/Login.component';
import { SignupComponent } from './Components/Signup/Signup.component';
import { OrganizerDashboardComponent } from './Components/OrganizerDashboard/OrganizerDashboard.component';
import {NotificationListComponent} from './Components/NotificationList/NotificationList.component';
import { AuthGuard , AuthGuardOrganizateur} from './Others/guard/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/events', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'signup', component: SignupComponent },
  { path: 'events', component: EventListComponent },
  { path: 'new', component: CreateEventComponent, canActivate: [AuthGuard , AuthGuardOrganizateur] },
  { path: 'update/:id', component: UpdateEventComponent, canActivate: [AuthGuard , AuthGuardOrganizateur] },
  { path: 'events/:id', component: EventDetailComponent },
  { path: 'dashboard', component: OrganizerDashboardComponent, canActivate: [AuthGuard , AuthGuardOrganizateur] },
  { path: 'notifications', component: NotificationListComponent , canActivate: [AuthGuard] }
  
];
