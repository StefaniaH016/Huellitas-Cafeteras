import { Component, inject, signal, computed } from '@angular/core';
import {
  ReactiveFormsModule,
  FormBuilder,
  Validators,
  AbstractControl,
  ValidationErrors,
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

type TipoCuenta = 'CIUDADANO' | 'FUNDACION' | 'VETERINARIA' | null;

interface FormValue {
  nombre?: string;
  email?: string;
  telefono?: string;
  municipio?: string;
  password?: string;
  confirmPassword?: string;
  nombreEntidad?: string;
  nit?: string;
  documentoLegal?: string;
  municipioOperacion?: string;
  documentoSoporte?: string;
  aceptaDatos?: boolean;
}

interface RegistroRequest {
  nombre: string;
  correo: string;
  contrasena: string;
  telefono: string;
  barrioId?: number;
  rol: string;
  nombreEntidad?: string;
  nit?: string;
  documentoLegal?: string;
  municipioOperacion?: string;
  documentoSoporte?: string;
}

function confirmPasswordValidator(control: AbstractControl): ValidationErrors | null {
  const parent = control.parent;
  if (!parent) {
    return null;
  }
  const password = parent.get('password')?.value;
  return control.value === password ? null : { passwordMismatch: true };
}

@Component({
  selector: 'app-registro',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './registro.component.html',
  styleUrl: './registro.component.css',
})
export class RegistroComponent {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private httpClient = inject(HttpClient);

  step = signal(1);
  tipoCuenta = signal<TipoCuenta>(null);
  isLoading = signal(false);
  errorMessage = signal<string | null>(null);

  step2Form = this.fb.group({
    nombre: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    telefono: ['', Validators.required],
    municipio: ['', Validators.required],
    password: ['', [Validators.required, Validators.minLength(8)]],
    confirmPassword: ['', [Validators.required, confirmPasswordValidator]],
    nombreEntidad: [''],
    nit: [''],
    documentoLegal: [''],
    municipioOperacion: [''],
    documentoSoporte: [''],
    aceptaDatos: [false, Validators.requiredTrue],
  });

  isInstitutional = computed(
    () => this.tipoCuenta() === 'FUNDACION' || this.tipoCuenta() === 'VETERINARIA',
  );

  selectTipo(tipo: TipoCuenta): void {
    this.tipoCuenta.set(tipo);
  }

  goToStep2(): void {
    if (!this.tipoCuenta()) {
      return;
    }
    if (this.isInstitutional()) {
      // Conditionally enable validators for institutional fields
      this.step2Form.get('nombreEntidad')!.setValidators([Validators.required]);
      this.step2Form.get('nit')!.setValidators([Validators.required]);
      this.step2Form.get('municipioOperacion')!.setValidators([Validators.required]);
    } else {
      // Clear validators for citizen fields
      this.step2Form.get('nombreEntidad')!.clearValidators();
      this.step2Form.get('nit')!.clearValidators();
      this.step2Form.get('municipioOperacion')!.clearValidators();
    }
    // Update validity for the affected controls
    this.step2Form.get('nombreEntidad')!.updateValueAndValidity({ emitEvent: false });
    this.step2Form.get('nit')!.updateValueAndValidity({ emitEvent: false });
    this.step2Form.get('municipioOperacion')!.updateValueAndValidity({ emitEvent: false });
    this.step.set(2);
  }

  goBack(): void {
    this.step.set(1);
  }

  private buildInstitutionalFields(formValue: FormValue): Partial<RegistroRequest> {
    if (!this.isInstitutional()) {
      return {};
    }
    return {
      nombreEntidad: formValue.nombreEntidad || undefined,
      nit: formValue.nit || undefined,
      documentoLegal: formValue.documentoLegal || undefined,
      municipioOperacion: formValue.municipioOperacion || undefined,
      documentoSoporte: formValue.documentoSoporte || undefined,
    };
  }

  private buildRegistroPayload(): RegistroRequest {
    const formValue = this.step2Form.value as unknown as FormValue;
    return {
      nombre: formValue.nombre || '',
      correo: formValue.email || '',
      telefono: formValue.telefono || '',
      contrasena: formValue.password || '',
      rol: this.tipoCuenta() || 'CIUDADANO',
      ...this.buildInstitutionalFields(formValue),
    } as RegistroRequest;
  }

  onSubmit(): void {
    this.step2Form.markAllAsTouched();
    if (this.step2Form.invalid) {
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set(null);

    const payload = this.buildRegistroPayload();

    this.httpClient
      .post<{ message: string }>(`${environment.apiUrl}/v1/auth/registro`, payload)
      .subscribe({
        next: () => {
          this.isLoading.set(false);
          this.router.navigate(['/login']);
        },
        error: (err) => {
          this.isLoading.set(false);
          const errMsg = err.error?.message || 'Error al registrarse. Intenta de nuevo.';
          this.errorMessage.set(errMsg);
        },
      });
  }
}
