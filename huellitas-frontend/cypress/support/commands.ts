/// <reference types="cypress" />

// Import e2e support
import './e2e';

declare global {
  // eslint-disable-next-line @typescript-eslint/no-namespace
  namespace Cypress {
    interface Chainable {
      loginComoCiudadano(): Chainable<void>
      login(email: string, password: string): Chainable<void>
      drag(subject: string, options?: Partial<TypeOptions>): Chainable<Element>
      dismiss(subject: string, options?: Partial<TypeOptions>): Chainable<Element>
    }
  }
}

// Implementación del comando de login por rol (Subtarea 3.2)
// Hace login via API y copia el estado que guarda la app (auth_token, hc_rol, hc_usuario_id)
Cypress.Commands.add('loginComoCiudadano', () => {
  // @ts-ignore - cy.env() returns any, but we know it's a string
  cy.session(['ciudadano'] as string[], () => {
    // @ts-ignore - cy.env() returns any
    cy.request('POST', `${cy.env('apiUrl')}/auth/login`, {
      // @ts-ignore
      correo: cy.env('CIUDADANO_EMAIL'),
      // @ts-ignore
      contrasena: cy.env('CIUDADANO_PASS'),
    }).then(({ body }: any) => {
      localStorage.setItem('auth_token', body.token);
      localStorage.setItem('hc_rol', body.usuario.rol);
      localStorage.setItem('hc_usuario_id', String(body.usuario.id));
    });
  });
});

export {};