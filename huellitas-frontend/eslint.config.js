// @ts-check
// Configuración ESLint para Angular 21 con estándares de calidad
// Específico para Angular 17+ (control flow @if, @for, @switch)
// NO incluye reglas de Karma/Jasmine o versiones anteriores

const eslint = require('@eslint/js');
const { defineConfig } = require('eslint/config');
const tseslint = require('typescript-eslint');
const angular = require('angular-eslint');

module.exports = defineConfig([
  {
    files: ['**/*.ts'],
    extends: [
      eslint.configs.recommended,
      tseslint.configs.recommended,
      tseslint.configs.stylistic,
      angular.configs.tsRecommended,
    ],
    processor: angular.processInlineTemplates,
    rules: {
      // ===== REGLAS ANGULAR - SELECTORES =====
      '@angular-eslint/directive-selector': [
        'error',
        {
          type: 'attribute',
          prefix: 'app',
          style: 'camelCase',
        },
      ],
      '@angular-eslint/component-selector': [
        'error',
        {
          type: 'element',
          prefix: 'app',
          style: 'kebab-case',
        },
      ],

      // ===== REGLAS ANGULAR - BEST PRACTICES ANGULAR 21 =====
      '@angular-eslint/prefer-inject': 'error', // Usar inject() en lugar de constructor
      '@angular-eslint/no-empty-lifecycle-method': 'error', // No métodos lifecycle vacíos
      '@angular-eslint/prefer-standalone': 'warn', // Preferir componentes standalone
      '@angular-eslint/no-outputs-metadata-property': 'error', // Usar @Output() no metadata
      '@angular-eslint/no-inputs-metadata-property': 'error', // Usar @Input() no metadata

      // ===== REGLAS TYPESCRIPT - TYPE SAFETY =====
      '@typescript-eslint/no-explicit-any': 'error', // NO usar 'any'
      '@typescript-eslint/no-unused-vars': ['error', {
        argsIgnorePattern: '^_', // Permitir parámetros con prefijo _
        varsIgnorePattern: '^_'  // Permitir variables con prefijo _
      }],
      '@typescript-eslint/no-empty-function': ['error', {
        allow: ['constructors'] // Permitir constructores vacíos
      }],
      '@typescript-eslint/explicit-function-return-type': 'warn', // Avisar si falta tipo retorno
      '@typescript-eslint/array-type': ['error', { default: 'array' }], // Usar string[] en lugar de Array<string>
      '@typescript-eslint/consistent-type-definitions': ['error', 'interface'], // Usar interface, no type

      // ===== REGLAS GENERALES - CALIDAD DE CÓDIGO =====
      'prefer-const': 'error', // Usar const si no reasigna
      'no-var': 'error', // NO usar var (usar const/let)
      'no-console': ['warn', { allow: ['warn', 'error'] }], // console.log solo si es error
      'no-debugger': 'error', // NO dejar debugger en código
      'eqeqeq': ['error', 'always'], // Usar === en lugar de ==
      'curly': ['error', 'all'], // Llaves en todos los bloques

      // ===== REGLAS DE COMPLEJIDAD =====
      'complexity': ['warn', 10], // Función no más de 10 complejidad ciclomática
      'max-lines-per-function': ['warn', 50], // Función no más de 50 líneas
      'max-depth': ['warn', 4], // Máximo 4 niveles de anidación
    },
  },
  {
    files: ['**/*.html'],
    extends: [
      angular.configs.templateRecommended,
      angular.configs.templateAccessibility
    ],
    rules: {
      // ===== CONTROL FLOW - ANGULAR 17+ =====
      '@angular-eslint/template/prefer-control-flow': 'error', // Usar @if, @for, @switch en lugar de *ngIf, *ngFor
      '@angular-eslint/template/no-negated-async': 'error', // No negar pipes async

      // ===== ACCESIBILIDAD =====
      '@angular-eslint/template/label-has-associated-control': 'error', // Labels deben estar asociados
      '@angular-eslint/template/click-events-have-key-events': 'error', // Click debe tener keyup/keydown
      '@angular-eslint/template/interactive-supports-focus': 'error', // Elementos interactivos focusables

      // ===== BEST PRACTICES ANGULAR =====
      '@angular-eslint/template/banana-in-box': 'error', // Syntax correcto [(ngModel)]
      '@angular-eslint/template/no-duplicate-attributes': 'error', // No atributos duplicados
      '@angular-eslint/template/valid-aria': 'error', // ARIA válido
    },
  },
]);
