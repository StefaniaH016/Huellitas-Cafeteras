import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { TokenService } from '../../../core/services/token.service';
import { signal } from '@angular/core';

@Component({
  selector: 'app-header',
  imports: [RouterLink],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent {
  private authService = inject(AuthService);
  private tokenService = inject(TokenService);

  userName = signal(this.computeUserName());
  initials = signal(this.computeInitials());

  private computeUserName(): string {
    const payload = this.tokenService.decodePayload();
    return (payload?.['nombre'] ?? payload?.['name'] ?? payload?.['sub'] ?? 'Usuario') as string;
  }

  private computeInitials(): string {
    const name = this.computeUserName();
    return name.split(' ').map(n => n[0]).slice(0, 2).join('').toUpperCase();
  }

  logout(): void {
    this.authService.logout();
  }
}
