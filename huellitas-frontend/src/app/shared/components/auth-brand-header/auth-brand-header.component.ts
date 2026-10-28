import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-auth-brand-header',
  standalone: true,
  templateUrl: './auth-brand-header.component.html',
  styleUrl: './auth-brand-header.component.css',
})
export class AuthBrandHeaderComponent {
  @Input() hasActions = false;
}
