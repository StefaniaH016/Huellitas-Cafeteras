import { Component, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  loginForm = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required],
    mantenerSesion: [false]
  });

  recoveryForm = this.fb.group({
    recoveryEmail: ['', [Validators.required, Validators.email]]
  });

  showError = signal(false);
  showRecovery = signal(false);
  isLoading = signal(false);

  onSubmit(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }
    this.isLoading.set(true);
    this.showError.set(false);
    const { email, password } = this.loginForm.value;
    this.authService.login(email!, password!).subscribe({
      next: (rol) => {
        this.isLoading.set(false);
        this.authService.navigateAfterLogin(rol);
      },
      error: () => {
        this.isLoading.set(false);
        this.showError.set(true);
      }
    });
  }

  toggleRecovery(): void {
    this.showRecovery.update(v => !v);
  }

  onRecovery(): void {
    if (this.recoveryForm.invalid) {
      this.recoveryForm.markAllAsTouched();
      return;
    }
    // TODO: implementar endpoint de recuperación
    alert('Si el correo existe, recibirás un enlace de recuperación.');
  }
}
