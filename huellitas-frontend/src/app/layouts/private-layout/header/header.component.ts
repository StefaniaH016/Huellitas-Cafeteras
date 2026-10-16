import { Component, inject, computed } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { TokenService } from '../../../core/services/token.service';

@Component({
  selector: 'app-header',
  imports: [RouterLink],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent {
  private authService = inject(AuthService);
  private tokenService = inject(TokenService);

  userName = computed(() => {
    const payload = this.tokenService.decodePayload();
    return (payload?.['nombre'] ?? payload?.['name'] ?? payload?.['sub'] ?? 'Usuario') as string;
  });

  initials = computed(() => {
    const name = this.userName();
    return name.split(' ').map(n => n[0]).slice(0, 2).join('').toUpperCase();
  });

  logout(): void {
    this.authService.logout();
  }
}
