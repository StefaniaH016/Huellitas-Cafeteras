describe('Registrar un reporte', () => {
  beforeEach(() => {
    cy.loginComoCiudadano();
    cy.visit('/reportar');
  });

  const llenarPaso1 = () => {
    cy.contains('label.chip', 'Herido').click();
    cy.get('#especie').select('PERRO');
    cy.get('#urgencia').select('CRITICA');
    cy.get('#descripcion').type('Perro con la pata herida cerca del parque');
    cy.contains('button', 'Continuar').click();
  };

  const elegirUbicacion = () => {
    cy.get('#direccion').type('Granada, Armenia, cerca del parque principal');
    
    // Seleccionar departamento y esperar a que carguen municipios
    cy.get('#zona-depto-sel').select(1);
    cy.wait(500); // Esperar a que se carguen municipios
    
    // Seleccionar municipio y esperar a que carguen barrios
    cy.get('#zona-mun-sel').select(1);
    cy.wait(500); // Esperar a que se carguen barrios
    
    // Seleccionar barrio
    cy.get('#zona-barrio-sel').select(1);
  };

  // CP-HU01-01: registro exitoso (sin foto, es opcional)
  it('CP-HU01-01: publica un reporte con los campos obligatorios', () => {
    llenarPaso1();
    elegirUbicacion();
    cy.contains('button', 'Continuar').click({ force: true });
    cy.wait(1000); // Esperar a que cargue el paso 3
    
    // No agregar foto (es opcional según backend)
    cy.contains('button', 'Publicar reporte').click({ force: true });
    cy.get('#titulo-listo').should('contain', 'Reporte publicado');
  });

  // CP-HU01-02: campos vacíos
  it('CP-HU01-02: muestra errores si se deja el formulario vacío', () => {
    cy.contains('button', 'Continuar').click();
    cy.get('#error-tipoCaso')
      .should('be.visible')
      .and('contain', 'Selecciona');
    cy.get('#error-descripcion')
      .should('be.visible');
    cy.get('#titulo-paso-1').should('be.visible'); // no avanzó de paso
  });

  // CP-HU01-03: foto inválida
  it('CP-HU01-03: rechaza una foto que no es imagen o pesa más de 5 MB', () => {
    llenarPaso1();
    cy.get('#fotos').selectFile(
      {
        contents: Cypress.Buffer.from('no soy una imagen'),
        fileName: 'doc.pdf',
        mimeType: 'application/pdf',
      },
      { force: true }
    );
    cy.get('.field-error').should('contain', 'JPG, PNG o WebP');

    cy.get('#fotos').selectFile(
      {
        contents: Cypress.Buffer.alloc(5 * 1024 * 1024 + 1),
        fileName: 'grande.jpg',
        mimeType: 'image/jpeg',
      },
      { force: true }
    );
    cy.get('.field-error').should('contain', 'máximo 5 MB');
  });

  // CP-HU02-01: ubicación en cascada
  it('CP-HU02-01: habilita municipio y barrio en cascada', () => {
    llenarPaso1();
    cy.get('#zona-mun-sel').should('be.disabled');
    cy.get('#zona-barrio-sel').should('be.disabled');
    cy.get('#zona-depto-sel').select(1);
    cy.get('#zona-mun-sel').should('be.enabled').select(1);
    cy.get('#zona-barrio-sel').should('be.enabled');
  });
});
