import { Component } from '@angular/core';
import { FormBuilder, Validators, FormGroup } from '@angular/forms';
import { AuthService } from '../../Services/auth.service';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';

// RxJS imports
import { timer } from 'rxjs';
import { switchMap, finalize } from 'rxjs/operators';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatInputModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    RouterModule
  ],
  templateUrl: './Login.component.html',
  styleUrls: ['./Login.component.scss']
})
export class LoginComponent {
  loginForm: FormGroup;
  isSubmitting = false; // Renommé en camelCase (recommandé)

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    this.loginForm = this.fb.group({
      email: ['', Validators.required],
      password: ['', Validators.required]
    });
  }

  onSubmit() {
    if (this.loginForm.valid) {
      this.isSubmitting = true;

      const credentials = this.loginForm.value;

      
      timer(1500).pipe(                     
        switchMap(() => this.authService.login(credentials)), 
        finalize(() => this.isSubmitting = false)            // Toujours remis à false à la fin
      )
      .subscribe({
        next: () => {
          this.snackBar.open('Connexion réussie !', '', {
            duration: 3500,
            panelClass: ['success-snackbar']
          });

          if (this.authService.isOrganizer()) {
            this.router.navigate(['/dashboard']);
          } else {
            this.router.navigate(['/events']);
          }
        },
        error: () => {
          this.snackBar.open('Identifiants incorrects', '', {
            duration: 3500,
            panelClass: ['error-snackbar']
          });
          
        }
      });
    } else {
      this.snackBar.open('Veuillez remplir tous les champs', 'OK', {
        duration: 3000,
        panelClass: ['warning-snackbar']
      });
    }
  }
}