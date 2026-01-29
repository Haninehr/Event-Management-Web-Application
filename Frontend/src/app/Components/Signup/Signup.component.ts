import { Component } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { AuthService } from '../../Services/auth.service';
import { Router, RouterModule } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatSelectModule } from '@angular/material/select';

// RxJS imports pour le délai minimum
import { timer, finalize } from 'rxjs';
import { switchMap } from 'rxjs/operators';

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatInputModule,
    MatButtonModule,
    MatCardModule,
    MatSelectModule,
    RouterModule
  ],
  templateUrl: './Signup.component.html',
  styleUrl: './Signup.component.scss'
})
export class SignupComponent {
  signupForm: any;
  isSubmitting = false; // État de chargement pour le bouton

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    this.signupForm = this.fb.group({
      username: ['', Validators.required],
      email: ['', Validators.required],
      password: ['', Validators.required],
      role: ['PARTICIPANT', Validators.required]
    });
  }

  onSubmit() {
    if (this.signupForm.valid) {
      this.isSubmitting = true;

      // Délai minimum de 3 secondes avant de lancer la requête réelle
      timer(3000).pipe(
        switchMap(() => this.authService.signup(this.signupForm.value)),
        finalize(() => this.isSubmitting = false) // Toujours remis à false à la fin
      )
      .subscribe({
        next: () => {
          this.snackBar.open('Inscription réussie ! Vous pouvez vous connecter', 'OK', {
            duration: 5000,
            panelClass: ['success-snackbar']
          });
          this.router.navigate(['/login']);
        },
        error: () => {
          this.snackBar.open('Erreur lors de l\'inscription', 'OK', {
            duration: 5000,
            panelClass: ['error-snackbar']
          });
          // isSubmitting remis à false automatiquement par finalize()
        }
      });
    } else {
      this.snackBar.open('Veuillez remplir tous les champs correctement', 'OK', {
        duration: 4000,
        panelClass: ['warning-snackbar']
      });
    }
  }
}