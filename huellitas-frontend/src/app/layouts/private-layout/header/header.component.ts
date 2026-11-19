import { Component, DestroyRef, OnInit, inject, computed, signal } from '@angular/core';
import { NavigationEnd, Router, RouterLink, RouterLinkActive } from '@angular/router';
import { filter, fromEvent, interval } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AuthService } from '../../../core/services/auth.service';
import { NotificacionService } from '../../../core/services/notificacion.service';
import { TokenService } from '../../../core/services/token.service';
import { normalizeRole } from '../../../core/utils/role.utils';

@Component({
  selector: 'app-header',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css',
})
export class HeaderComponent implements OnInit {
  private authService = inject(AuthService);
  private tokenService = inject(TokenService);
  private notificacionService = inject(NotificacionService);
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);

  // Decode token once and cache the payload
  private payload = signal(this.tokenService.decodePayload());
  private rawRol = signal(this.authService.getRol());
  readonly sinLeer = this.notificacionService.sinLeer;

  // Normalize role for role checks
  private rol = computed(() => normalizeRole(this.rawRol()));

  private fullName = computed(() => {
    const p = this.payload();
    const name = p?.['nombre'] ?? p?.['name'] ?? p?.['sub'];
    return typeof name === 'string' && name.trim() ? name.trim() : 'Usuario';
  });

  userName = computed(() => this.fullName().split(/\s+/)[0]);

  initials = computed(() => {
    const name = this.fullName();
    return name
      .split(/\s+/)
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

  ngOnInit(): void {
    this.notificacionService.actualizarContador();
    interval(60_000)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.notificacionService.actualizarContador());
    this.router.events
      .pipe(
        filter((event): event is NavigationEnd => event instanceof NavigationEnd),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(() => this.notificacionService.actualizarContador());
    fromEvent(window, 'focus')
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.notificacionService.actualizarContador());
  }

  logout(): void {
    this.authService.logout();
  }
}
