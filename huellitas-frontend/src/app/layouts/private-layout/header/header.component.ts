import { Component, inject, computed, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { TokenService } from '../../../core/services/token.service';

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

  logout(): void {
    this.authService.logout();
  }
}
