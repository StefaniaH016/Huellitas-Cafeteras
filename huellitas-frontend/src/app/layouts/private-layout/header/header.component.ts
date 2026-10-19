import { Component, inject, computed, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { TokenService } from '../../../core/services/token.service';
import { normalizeRole } from '../../../core/utils/role.utils';

@Component({
  selector: 'app-header',
  imports: [RouterLink],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css',
})
export class HeaderComponent {
  private authService = inject(AuthService);
  private tokenService = inject(TokenService);

  // Decode token once and cache the payload
  private payload = signal(this.tokenService.decodePayload());
  private rawRol = signal(this.authService.getRol());

  // Normalize role for role checks
  private rol = computed(() => normalizeRole(this.rawRol()));

  userName = computed(() => {
    const p = this.payload();
    return (p?.['nombre'] ?? p?.['name'] ?? p?.['sub'] ?? 'Usuario') as string;
  });

  initials = computed(() => {
    const name = this.userName();
    return name
      .split(' ')
      .map((n) => n[0])
      .slice(0, 2)
      .join('')
      .toUpperCase();
  });

  isFundacion = computed(() => this.rol() === 'FUNDACION_REFUGIO');
  isVeterinaria = computed(() => this.rol() === 'VETERINARIA');
  isAdmin = computed(() => this.rol() === 'ADMINISTRADOR');
  isCiudadano = computed(() => {
    const r = this.rol();
    return !r || r === 'CIUDADANO_VOLUNTARIO';
  });

  logout(): void {
    this.authService.logout();
  }
}
