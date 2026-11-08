import { Component, inject, signal, computed } from '@angular/core';
import {
  ReactiveFormsModule,
  FormBuilder,
  Validators,
  AbstractControl,
  ValidationErrors,
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { AuthBrandHeaderComponent } from '../../shared/components/auth-brand-header/auth-brand-header.component';

type TipoCuenta = 'CIUDADANO_VOLUNTARIO' | 'FUNDACION_REFUGIO' | 'VETERINARIA' | null;

interface FormValue {
  nombre?: string;
  email?: string;
  telefono?: string;
  municipio?: string;
  password?: string;
  confirmPassword?: string;
  nombreEntidad?: string;
  nit?: string;
  tarjetaProf?: string;
  municipioOperacion?: string;
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
  documentoSoporte?: File;
  aceptoTratamientoDatos: boolean;
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
  imports: [ReactiveFormsModule, RouterLink, AuthBrandHeaderComponent],
  templateUrl: './registro.component.html',
  styleUrl: './registro.component.css',
})
export class RegistroComponent {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private authService = inject(AuthService);

  step = signal(1);
  tipoCuenta = signal<TipoCuenta>(null);
  isLoading = signal(false);
  errorMessage = signal<string | null>(null);
  documentoSoporte = signal<File | null>(null);
  errorDocumento = signal('');

  step2Form = this.fb.group({
    nombre: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    telefono: ['', Validators.required],
    municipio: ['', Validators.required],
    password: ['', [Validators.required, Validators.minLength(8)]],
    confirmPassword: ['', [Validators.required, confirmPasswordValidator]],
    nombreEntidad: [''],
    nit: [''],
    tarjetaProf: [''],
    municipioOperacion: [''],
    aceptaDatos: [false, Validators.requiredTrue],
  });

  isInstitutional = computed(
    () => this.tipoCuenta() === 'FUNDACION_REFUGIO' || this.tipoCuenta() === 'VETERINARIA',
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

  seleccionarDocumento(event: Event): void {
    const input = event.target as HTMLInputElement;
    const archivo = input.files?.[0] ?? null;
    this.errorDocumento.set('');
    if (!archivo) {
      this.documentoSoporte.set(null);
      return;
    }
    if (archivo.size > 5 * 1024 * 1024) {
      this.documentoSoporte.set(null);
      this.errorDocumento.set('El documento no puede superar 5 MB.');
      input.value = '';
      return;
    }
    if (!/\.(pdf|jpe?g|png)$/i.test(archivo.name)) {
      this.documentoSoporte.set(null);
      this.errorDocumento.set('Adjunta un archivo PDF, JPG o PNG.');
      input.value = '';
      return;
    }
    this.documentoSoporte.set(archivo);
  }

  private buildInstitutionalFields(formValue: FormValue): Partial<RegistroRequest> {
    if (!this.isInstitutional()) {
      return {};
    }
    const documentoSoporte = this.documentoSoporte();
    return {
      ...(formValue.nombreEntidad && { nombreEntidad: formValue.nombreEntidad }),
      ...(formValue.nit && { nit: formValue.nit }),
      ...(formValue.tarjetaProf && { documentoLegal: formValue.tarjetaProf }),
      ...(formValue.municipioOperacion && { municipioOperacion: formValue.municipioOperacion }),
      ...(documentoSoporte && { documentoSoporte }),
    };
  }

  private buildRegistroPayload(): RegistroRequest {
    const formValue = this.step2Form.value as unknown as FormValue;
    const payload: RegistroRequest = {
      nombre: (formValue.nombre || '').trim(),
      correo: (formValue.email || '').trim(),
      telefono: formValue.telefono || '',
      contrasena: formValue.password || '',
      rol: this.tipoCuenta() || 'CIUDADANO_VOLUNTARIO',
      // Ley 1581: el backend guarda aceptoTratamientoDatos y fechaAceptacionDatos
      aceptoTratamientoDatos: formValue.aceptaDatos === true,
    };

    // Agregar campos institucionales si aplica
    const institutionalFields = this.buildInstitutionalFields(formValue);
    return { ...payload, ...institutionalFields };
  }

  onSubmit(): void {
    this.step2Form.markAllAsTouched();
    if (this.step2Form.invalid || this.errorDocumento()) {
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set(null);

    const payload = this.buildRegistroPayload();

    this.authService.registro(payload).subscribe({
      next: () => {
        this.isLoading.set(false);
        void this.router.navigate(['/login']);
      },
      error: (err: unknown) => {
        this.isLoading.set(false);
        const errorMsg =
          (err as Record<string, Record<string, string> | undefined>)?.['error']?.['message'] ||
          'Error al registrarse. Intenta de nuevo.';
        this.errorMessage.set(errorMsg);
      },
    });
  }
}
