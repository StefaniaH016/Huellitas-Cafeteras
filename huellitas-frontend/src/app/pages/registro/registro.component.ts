import { Component, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

type TipoCuenta = 'CIUDADANO' | 'FUNDACION' | 'VETERINARIA' | null;

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
  styleUrl: './registro.component.css'
})
export class RegistroComponent {
  private fb = inject(FormBuilder);
  private router = inject(Router);

  step = signal(1);
  tipoCuenta = signal<TipoCuenta>(null);
  isLoading = signal(false);

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
    aceptaDatos: [false, Validators.requiredTrue]
  });

  selectTipo(tipo: TipoCuenta): void {
    this.tipoCuenta.set(tipo);
  }

  goToStep2(): void {
    if (!this.tipoCuenta()) {
      return;
    }
    if (this.isInstitutional) {
      this.step2Form.get('nombreEntidad')!.addValidators(Validators.required);
      this.step2Form.get('nit')!.addValidators(Validators.required);
      this.step2Form.get('municipioOperacion')!.addValidators(Validators.required);
      this.step2Form.get('nombreEntidad')!.updateValueAndValidity();
      this.step2Form.get('nit')!.updateValueAndValidity();
      this.step2Form.get('municipioOperacion')!.updateValueAndValidity();
    }
    this.step.set(2);
  }

  goBack(): void {
    this.step.set(1);
  }

  onSubmit(): void {
    this.step2Form.markAllAsTouched();
    if (this.step2Form.invalid) {
      return;
    }
    this.isLoading.set(true);
    // TODO: llamar al servicio de registro
    setTimeout(() => {
      this.isLoading.set(false);
      this.router.navigate(['/login']);
    }, 1000);
  }

  get isInstitutional(): boolean {
    return this.tipoCuenta() === 'FUNDACION' || this.tipoCuenta() === 'VETERINARIA';
  }
}
