import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';
import { PublicLayout } from './layouts/public-layout/public-layout';
import { PrivateLayout } from './layouts/private-layout/private-layout.component';

export const routes: Routes = [
  {
    path: '',
    component: PublicLayout,
    children: [
      {
        path: 'login',
        loadComponent: () =>
          import('./pages/login/login.component').then((m) => m.LoginComponent),
      },
      {
        path: 'registro',
        loadComponent: () =>
          import('./pages/registro/registro.component').then(
            (m) => m.RegistroComponent
          ),
      },
    ],
  },
  {
    path: '',
    component: PrivateLayout,
    children: [
      {
        path: '',
        canActivate: [authGuard],
        loadComponent: () =>
          import('./pages/home/home.component').then((m) => m.HomeComponent),
      },
      {
        path: 'fundacion',
        canActivate: [authGuard, roleGuard],
        data: { roles: ['FUNDACION'] },
        loadComponent: () =>
          import('./pages/panel-fundacion/panel-fundacion.component').then(
            (m) => m.PanelFundacionComponent
          ),
      },
      {
        path: 'veterinaria',
        canActivate: [authGuard, roleGuard],
        data: { roles: ['VETERINARIA'] },
        loadComponent: () =>
          import(
            './pages/panel-veterinaria/panel-veterinaria.component'
          ).then((m) => m.PanelVeterinariaComponent),
      },
      {
        path: 'admin',
        canActivate: [authGuard, roleGuard],
        data: { roles: ['ADMIN'] },
        loadComponent: () =>
          import('./pages/panel-admin/panel-admin.component').then(
            (m) => m.PanelAdminComponent
          ),
      },
      {
        path: 'mis-reportes',
        canActivate: [authGuard],
        loadComponent: () =>
          import('./pages/placeholder/placeholder.component').then(
            (m) => m.PlaceholderComponent
          ),
        data: { title: 'Mis reportes' },
      },
      {
        path: 'mensajes',
        canActivate: [authGuard],
        loadComponent: () =>
          import('./pages/placeholder/placeholder.component').then(
            (m) => m.PlaceholderComponent
          ),
        data: { title: 'Mensajes' },
      },
      {
        path: 'notificaciones',
        canActivate: [authGuard],
        loadComponent: () =>
          import('./pages/placeholder/placeholder.component').then(
            (m) => m.PlaceholderComponent
          ),
        data: { title: 'Notificaciones' },
      },
    ],
  },
  {
    path: 'acceso-denegado',
    loadComponent: () =>
      import('./pages/acceso-denegado/acceso-denegado.component').then(
        (m) => m.AccesoDenegadoComponent
      ),
  },
  {
    path: '404',
    loadComponent: () =>
      import('./pages/not-found/not-found.component').then(
        (m) => m.NotFoundComponent
      ),
  },
  { path: '**', redirectTo: '/404' },
];
