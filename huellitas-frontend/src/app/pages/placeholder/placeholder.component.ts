import { Component, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-placeholder',
  imports: [],
  templateUrl: './placeholder.component.html',
  styleUrl: './placeholder.component.css',
  standalone: true,
})
export class PlaceholderComponent {
  private activatedRoute = inject(ActivatedRoute);

  title = this.activatedRoute.snapshot.data['title'] ?? 'Página en desarrollo';
}
