import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';
import { PublicLayout } from './layouts/public-layout/public-layout';
import { PrivateLayout } from './layouts/private-layout/private-layout.component';

export const routes: Routes = [
  // Public routes
  {
    path: '',
    component: PublicLayout,
    children: [
      {
        path: '',
        redirectTo: 'login',
        pathMatch: 'full',
      },
      {
        path: 'login',
        loadComponent: () => import('./pages/login/login.component').then((m) => m.LoginComponent),
      },
      {
        path: 'registro',
        loadComponent: () =>
          import('./pages/registro/registro.component').then((m) => m.RegistroComponent),
      },
    ],
  },
  // Private routes
  {
    path: '',
    component: PrivateLayout,
    canActivate: [authGuard],
    children: [
      {
        path: 'home',
        loadComponent: () => import('./pages/home/home.component').then((m) => m.HomeComponent),
      },
      {
        path: 'fundacion',
        canActivate: [roleGuard],
        data: { roles: ['FUNDACION_REFUGIO'] },
        loadComponent: () =>
          import('./pages/panel-fundacion/panel-fundacion.component').then(
            (m) => m.PanelFundacionComponent,
          ),
      },
      {
        path: 'veterinaria',
        canActivate: [roleGuard],
        data: { roles: ['VETERINARIA'] },
        loadComponent: () =>
          import('./pages/panel-veterinaria/panel-veterinaria.component').then(
            (m) => m.PanelVeterinariaComponent,
          ),
      },
      {
        path: 'admin',
        canActivate: [roleGuard],
        data: { roles: ['ADMINISTRADOR'] },
        loadComponent: () =>
          import('./pages/panel-admin/panel-admin.component').then((m) => m.PanelAdminComponent),
      },
      {
        path: 'mis-reportes',
        loadComponent: () =>
          import('./pages/placeholder/placeholder.component').then((m) => m.PlaceholderComponent),
        data: { title: 'Mis reportes' },
      },
      {
        path: 'mensajes',
        loadComponent: () =>
          import('./pages/placeholder/placeholder.component').then((m) => m.PlaceholderComponent),
        data: { title: 'Mensajes' },
      },
      {
        path: 'notificaciones',
        loadComponent: () =>
          import('./pages/placeholder/placeholder.component').then((m) => m.PlaceholderComponent),
        data: { title: 'Notificaciones' },
      },
      {
        path: 'acceso-denegado',
        loadComponent: () =>
          import('./pages/acceso-denegado/acceso-denegado.component').then(
            (m) => m.AccesoDenegadoComponent,
          ),
      },
      {
        path: '404',
        loadComponent: () =>
          import('./pages/not-found/not-found.component').then((m) => m.NotFoundComponent),
      },
      // Feature routes
      {
        path: 'casos',
        loadComponent: () => import('./pages/casos/casos.component').then((m) => m.CasosComponent),
      },
      {
        path: 'reportar',
        loadComponent: () =>
          import('./pages/reportar/reportar.component').then((m) => m.ReportarComponent),
      },
      {
        path: 'caso-detalle/:id',
        loadComponent: () =>
          import('./pages/caso-detalle/caso-detalle.component').then((m) => m.CasoDetalleComponent),
      },
    ],
  },
  { path: '**', redirectTo: '/404' },
];
