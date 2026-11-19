/* eslint-disable max-lines-per-function */
import '../support/commands';

describe('Registrar un reporte', () => {
  beforeEach(() => {
    cy.loginComoCiudadano();
    cy.visit('/reportar');
  });

  const llenarPaso1 = (): void => {
    cy.contains('label.chip', 'Herido').click();
    cy.get('#especie').select('PERRO');
    cy.get('#urgencia').select(1);
    cy.get('#descripcion').type('Perro con la pata herida cerca del parque');
    cy.contains('button:visible', 'Continuar').click();
  };

  const elegirUbicacion = (): void => {
    cy.get('#direccion').type('Granada, Armenia, cerca del parque principal');
    cy.get('#reporte-depto').select(1);
    cy.get('#reporte-mun').select(1);
    cy.get('#reporte-barrio').select(1);
  };

  // CP-HU01-01: registro exitoso
  it('CP-HU01-01: publica un reporte con los campos obligatorios', () => {
    llenarPaso1();
    elegirUbicacion();
    cy.contains('button:visible', 'Continuar').click();
    cy.contains('button', 'Publicar reporte').click();
    cy.get('#titulo-listo').should('be.visible').and('contain', 'Reporte publicado');
  });

  // CP-HU01-02: campos vacíos
  it('CP-HU01-02: muestra errores si se deja el formulario vacío', () => {
    cy.contains('button:visible', 'Continuar').click();
    cy.get('#error-tipoCaso').should('be.visible')
      .and('contain', 'Selecciona qué le pasó al animal');
    cy.get('#error-descripcion').should('be.visible')
      .and('contain', 'Describe lo que observaste');
    cy.get('#titulo-paso-1').should('be.visible'); // no avanzó de paso
  });

  // CP-HU01-03: foto inválida
  it('CP-HU01-03: rechaza una foto que no es imagen o pesa más de 5 MB', () => {
    llenarPaso1();
    cy.get('#reporte-fotos').selectFile(
      { contents: Cypress.Buffer.from('no soy una imagen'), fileName: 'doc.pdf', mimeType: 'application/pdf' },
      { force: true },
    );
    cy.get('.field-error[role="alert"]').should('be.visible')
      .and('contain', 'Solo se admiten formatos JPG, PNG o WebP');

    cy.get('#reporte-fotos').selectFile(
      { contents: Cypress.Buffer.alloc(5 * 1024 * 1024 + 1), fileName: 'grande.jpg', mimeType: 'image/jpeg' },
      { force: true },
    );
    cy.get('.field-error[role="alert"]').should('contain', 'máximo 5 MB');
  });

  // CP-HU02-01: ubicación en cascada
  it('CP-HU02-01: habilita municipio y barrio en cascada', () => {
    llenarPaso1();
    cy.get('#reporte-mun').should('be.disabled');
    cy.get('#reporte-barrio').should('be.disabled');
    cy.get('#reporte-depto').select(1);
    cy.get('#reporte-mun').should('be.enabled').select(1);
    cy.get('#reporte-barrio').should('be.enabled');
  });
});
