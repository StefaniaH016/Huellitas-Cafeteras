import { Type } from '@angular/core';
import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';
import { PublicLayout } from './layouts/public-layout/public-layout';
import { PrivateLayout } from './layouts/private-layout/private-layout.component';
import { AvisoData } from './pages/aviso/aviso.component';

const aviso = (): Promise<Type<unknown>> =>
  import('./pages/aviso/aviso.component').then((m) => m.AvisoComponent);

const ACCESO_DENEGADO: AvisoData = {
  icono: '🚫',
  titulo: 'Acceso denegado',
  mensaje: 'No tienes permiso para acceder a esta sección.',
};

const NO_ENCONTRADA: AvisoData = {
  icono: '🐾',
  titulo: '404 – Página no encontrada',
  mensaje: 'La página que buscas no existe o aún no ha sido implementada.',
};

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
      {
        path: 'politica-datos',
        title: 'Política de tratamiento de datos · Huellitas Cafeteras',
        loadComponent: () =>
          import('./pages/politica-datos/politica-datos.component').then(
            (m) => m.PoliticaDatosComponent,
          ),
      },
      {
        path: 'terminos',
        title: 'Términos y condiciones · Huellitas Cafeteras',
        loadComponent: () =>
          import('./pages/terminos/terminos.component').then((m) => m.TerminosComponent),
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
        canActivate: [roleGuard],
        data: { roles: ['CIUDADANO_VOLUNTARIO'] },
        loadComponent: () =>
          import('./pages/mis-reportes/mis-reportes.component').then((m) => m.MisReportesComponent),
      },
      {
        path: 'notificaciones',
        loadComponent: () =>
          import('./pages/notificaciones/notificaciones.component').then(
            (m) => m.NotificacionesComponent,
          ),
      },
      {
        path: 'cuenta',
        loadComponent: () =>
          import('./pages/cuenta/cuenta.component').then((m) => m.CuentaComponent),
      },
      {
        path: 'acceso-denegado',
        loadComponent: aviso,
        data: ACCESO_DENEGADO,
      },
      {
        path: '404',
        loadComponent: aviso,
        data: NO_ENCONTRADA,
      },
      // Feature routes
      {
        path: 'casos',
        loadComponent: () => import('./pages/casos/casos.component').then((m) => m.CasosComponent),
      },
      {
        path: 'reportar',
        canActivate: [roleGuard],
        data: { roles: ['CIUDADANO_VOLUNTARIO'] },
        loadComponent: () =>
          import('./pages/reportar/reportar.component').then((m) => m.ReportarComponent),
      },
      {
        path: 'casos/:id/editar',
        canActivate: [roleGuard],
        data: { roles: ['CIUDADANO_VOLUNTARIO'] },
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
