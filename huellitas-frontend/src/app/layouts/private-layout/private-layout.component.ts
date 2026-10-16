import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HeaderComponent } from './header/header.component';
import { FooterComponent } from './footer/footer.component';

@Component({
  selector: 'app-private-layout',
  imports: [RouterOutlet, HeaderComponent, FooterComponent],
  templateUrl: './private-layout.component.html',
  styleUrl: './private-layout.component.css'
})
export class PrivateLayout {}
