// ***********************************************************
// This example support/e2e.ts is processed and
// loaded automatically before your test files.
//
// This is a great place to put global configuration and
// behavior that modifies Cypress.
//
// You can change the location of this file or turn off
// automatically serving support files with the
// 'supportFile' configuration option.
//
// You can read more here:
// https://on.cypress.io/configuration
// ***********************************************************

// Import commands.js using ES2015 syntax:
import './commands'

declare global {
  // eslint-disable-next-line @typescript-eslint/no-namespace
  namespace Cypress {
    interface Chainable {
      loginComoCiudadano(): Chainable<void>
    }
  }
}

Cypress.Commands.add('loginComoCiudadano', () => {
  // @ts-ignore
  cy.session(['ciudadano'], () => {
    cy.request('POST', 'http://localhost:8080/v1/auth/login', {
      correo: 'ciudadano1@huellitas.co',
      contrasena: 'Ciudadano12345!',
    }).then(({ body }: any) => {
      localStorage.setItem('auth_token', body.token);
      localStorage.setItem('hc_rol', body.usuario.rol);
      localStorage.setItem('hc_usuario_id', String(body.usuario.id));
    });
  });
});
export {};