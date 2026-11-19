# 📱 Guía de Diseño Responsivo Mobile - Huellitas Cafeteras

> Principios HCI, accesibilidad táctil y buenas prácticas para optimizar pantallas móviles (≤ 760px)

---

## 🎯 Principios Fundamentales

### 1. **Accesibilidad Táctil (WCAG AA)**
- **Mínimo de 44x44px** para elementos interactivos (botones, links, checkboxes)
- **Separación mínima**: 1rem (16px) entre elementos táctiles
- **Altura de línea**: 1.5 - 1.6 para mejor legibilidad

### 2. **Espaciado Consistente (Spacing Scale)**
```
0.25rem (4px)   - Micro
0.5rem  (8px)   - Extra pequeño
0.75rem (12px)  - Pequeño
1rem    (16px)  - Base
1.25rem (20px)  - Medio
1.5rem  (24px)  - Grande
2rem    (32px)  - Extra grande
```

### 3. **Tipografía Legible**
```
h1: 1.5rem  (24px) con line-height 1.2
h2: 1.3rem  (21px)
h3: 1.1rem  (18px)
p:  0.95rem (15px) con line-height 1.5
```

### 4. **Responsive Grid**
- Mobile: 1 columna (100%)
- Tablet: 2 columnas máximo
- Desktop: 2-4 columnas

---

## 📐 Patrones de Layout Mobile

### Patrón 1: Layout Simple (1 Columna)
```html
<!-- HTML -->
<div class="main-area">
  <h1>Título</h1>
  <p class="lead">Descripción</p>
  
  <div class="panel">
    <!-- Contenido -->
  </div>
</div>
```

```css
/* CSS */
.main-area {
  max-width: 100%;
  padding: 0.75rem;
}

@media (max-width: 760px) {
  .main-area {
    padding: 0.75rem;
  }
  
  .main-area h1 {
    font-size: 1.5rem;
    margin: 0.75rem 0 0.5rem;
  }
}
```

### Patrón 2: Grid Adaptativo
```html
<!-- HTML -->
<div class="grid-2">
  <div class="form-field"><!-- Campo 1 --></div>
  <div class="form-field"><!-- Campo 2 --></div>
</div>
```

```css
/* CSS - Desktop: 2 columnas | Mobile: 1 columna */
.grid-2 {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1.25rem;
}

@media (max-width: 760px) {
  .grid-2 {
    grid-template-columns: 1fr;
    gap: 1rem;
  }
}
```

### Patrón 3: Flex Row → Column
```html
<!-- HTML -->
<div class="flex-row">
  <div>Elemento 1</div>
  <div>Elemento 2</div>
</div>
```

```css
/* CSS - Desktop: fila | Mobile: columna */
.flex-row {
  display: flex;
  flex-direction: row;
  gap: 1rem;
}

@media (max-width: 760px) {
  .flex-row {
    flex-direction: column;
    gap: 0.75rem;
  }
}
```

### Patrón 4: Drawer/Modal Mobile
```html
<!-- HTML -->
<button class="btn-toggle" (click)="toggleDrawer()">
  ☰ Filtros
</button>

<aside class="sidebar" [class.is-open]="mostrarFiltros()">
  <!-- Contenido filtros -->
</aside>
```

```css
/* CSS */
.btn-toggle {
  display: none; /* Oculto en desktop */
  min-height: 44px;
  padding: 0.75rem 1.25rem;
  border-radius: 999px;
  cursor: pointer;
}

.sidebar {
  display: none;
}

@media (max-width: 760px) {
  .btn-toggle {
    display: inline-flex;
    align-items: center;
    gap: 0.5rem;
  }

  .sidebar {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 99;
    background: rgba(0, 0, 0, 0.5);
    display: none;
  }

  .sidebar.is-open {
    display: flex;
    align-items: flex-end;
  }

  .sidebar::after {
    content: '';
    background: var(--card-bg);
    width: 100%;
    max-width: 360px;
    max-height: 90vh;
    border-radius: 20px 20px 0 0;
    padding: 1.5rem;
    overflow-y: auto;
    -webkit-overflow-scrolling: touch;
    animation: slideUp 0.3s ease;
  }

  @keyframes slideUp {
    from { transform: translateY(100%); }
    to { transform: translateY(0); }
  }
}
```

---

## 🎛️ Componentes Mobile-First

### Botones (44x44px mínimo)
```css
.btn {
  min-height: 44px;
  min-width: 44px;
  padding: 0.65rem 1rem;
  font-size: 0.95rem;
  border-radius: 10px;
  transition: all 0.2s ease;
}

.btn:active {
  transform: scale(0.98);
  opacity: 0.85;
}
```

### Inputs (Evitar zoom en iOS)
```css
input,
textarea,
select {
  font-size: 1rem; /* ← Evita zoom automático */
  padding: 0.75rem 1rem;
  min-height: 44px;
  border-radius: 10px;
  border: 1px solid var(--line);
}

@media (max-width: 760px) {
  input,
  textarea,
  select {
    font-size: 1rem; /* Mantener 1rem en mobile */
    min-height: 44px;
  }
}
```

### Checkboxes & Radios
```css
input[type='checkbox'],
input[type='radio'] {
  min-width: 20px;
  min-height: 20px;
  cursor: pointer;
  accent-color: var(--pine-700);
}

label {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  min-height: 44px; /* Área táctil */
  padding: 0.5rem 0;
}
```

### Listas (Lista de casos, notificaciones)
```css
.list-row {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 1rem;
  padding: 1rem;
  border-bottom: 1px solid var(--line);
  min-height: 60px;
}

@media (max-width: 760px) {
  .list-row {
    grid-template-columns: auto 1fr;
    gap: 0.75rem;
    padding: 0.9rem;
  }

  /* Botón de acción pasa a fila completa */
  .list-actions {
    grid-column: 1 / -1;
    width: 100%;
    margin-top: 0.5rem;
  }
}
```

### Tablas (Card Layout en Mobile)
```css
table {
  width: 100%;
}

@media (max-width: 760px) {
  table thead {
    display: none;
  }

  table tbody {
    display: block;
  }

  table tr {
    display: block;
    margin-bottom: 1rem;
    border: 1px solid var(--line);
    border-radius: 10px;
    padding: 1rem;
  }

  table td {
    display: block;
    padding: 0.5rem 0;
    text-align: left;
  }

  table td::before {
    content: attr(data-label);
    font-weight: 600;
    width: 120px;
    display: inline-block;
  }
}
```

---

## 🎨 Ejemplos Completos

### Ejemplo 1: Componente de Filtros (Casos)
```html
<!-- HTML -->
<div class="dash-layout">
  <button class="filters-toggle" (click)="toggleFiltros()">
    ☰ Filtros
  </button>

  <aside class="sidebar" [class.is-open]="mostrarFiltros()">
    <h2>Filtros</h2>
    <!-- Checkboxes de filtros -->
    <button class="btn btn-primary btn-block">Aplicar</button>
  </aside>

  <div class="main-area">
    <h1>Casos activos</h1>
    <!-- Lista de casos -->
  </div>
</div>
```

```css
/* CSS - Optimizado Mobile */
.dash-layout {
  display: grid;
  grid-template-columns: minmax(250px, 300px) 1fr;
  gap: 1.5rem;
}

.filters-toggle {
  display: none;
}

@media (max-width: 760px) {
  .dash-layout {
    grid-template-columns: 1fr;
    gap: 1rem;
    padding: 0.75rem;
  }

  .filters-toggle {
    display: inline-flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.75rem 1.25rem;
    min-height: 44px;
    border-radius: 999px;
    border: 1px solid var(--line);
    background: var(--card-bg);
    cursor: pointer;
    font-weight: 600;
  }

  .sidebar {
    display: none;
    position: fixed;
    inset: 0;
    z-index: 99;
    background: rgba(0, 0, 0, 0.5);
  }

  .sidebar.is-open {
    display: flex;
    align-items: flex-end;
    padding: 1rem;
  }

  .sidebar.is-open::after {
    content: '';
    background: var(--card-bg);
    width: 100%;
    max-width: 360px;
    max-height: 90vh;
    border-radius: 20px 20px 0 0;
    padding: 1.5rem;
    overflow-y: auto;
  }

  .main-area {
    grid-column: 1 / -1;
  }
}
```

### Ejemplo 2: Formulario Responsivo
```html
<!-- HTML -->
<form class="panel">
  <div class="panel-head"><h2>Mis datos</h2></div>
  
  <div class="grid-2">
    <div class="form-field">
      <label for="nombre">Nombre</label>
      <input id="nombre" type="text" />
    </div>
    <div class="form-field">
      <label for="correo">Correo</label>
      <input id="correo" type="email" />
    </div>
  </div>

  <button class="btn btn-primary btn-block">Guardar</button>
</form>
```

```css
/* CSS */
.grid-2 {
  grid-template-columns: repeat(2, 1fr);
  gap: 1.25rem;
}

@media (max-width: 760px) {
  .grid-2 {
    grid-template-columns: 1fr;
    gap: 1rem;
  }

  .btn-block {
    width: 100%;
    min-height: 44px;
  }
}
```

---

## 🚫 Errores Comunes a Evitar

| ❌ NO HACER | ✅ SÍ HACER |
|------------|----------|
| Botones pequeños (< 44px) | Mínimo 44x44px |
| Elementos superpuestos | Separación clara de 1rem |
| Font-size < 0.9rem | Font-size ≥ 0.9rem (preferible 1rem en inputs) |
| Sin touch padding | Padding suficiente alrededor |
| Grid de 4 columnas en mobile | Grid de 1-2 columnas máximo |
| Scroll horizontal | Full-width responsive |
| Gaps < 0.5rem | Gaps ≥ 0.75rem mínimo |
| Sin safe areas | Usar `env(safe-area-inset-*)` |
| Colores solo por color | Colores + iconos + patrones |
| Sin breakpoints | Media queries en 760px, 1024px |

---

## 🔍 Checklist de Revisión Mobile

Antes de hacer commit, verifica:

- [ ] Todos los botones ≥ 44x44px
- [ ] Inputs con font-size 1rem (evita zoom iOS)
- [ ] Espaciado consistente (0.75rem, 1rem, 1.25rem, 1.5rem)
- [ ] Sin elementos superpuestos en viewport móvil
- [ ] Breakpoints en 760px (mobile/tablet) y 1024px (tablet/desktop)
- [ ] Media queries con `max-width` para mobile-first
- [ ] Grid adaptativo: 1 col mobile → 2+ desktop
- [ ] Flex direction: column mobile → row desktop
- [ ] Drawer/Modal con full-screen en mobile
- [ ] Safe areas configuradas (notch, home indicator)
- [ ] Legibilidad: h1 ≥ 1.5rem, p ≥ 0.95rem
- [ ] Line-height ≥ 1.5 para párrafos
- [ ] No hay horiztonal scroll
- [ ] Imágenes responsive (max-width: 100%)
- [ ] Focus states visibles (outline 3px)

---

## 📱 Testing en Móvil

### Chrome DevTools
```
1. F12 → Ctrl+Shift+M (o Cmd+Shift+M en Mac)
2. Seleccionar dispositivo (iPhone, Samsung, etc.)
3. Verificar media queries en la parte inferior
```

### Dispositivos Reales
```bash
# Emular en Android
adb forward tcp:4200 tcp:4200
# Acceder a: http://localhost:4200

# Emular en iOS
Safari Dev Tools → Remote Debugging
```

### Herramientas
- **Lighthouse**: Auditoría de rendimiento y accesibilidad
- **axe DevTools**: Validar WCAG AA
- **Mobile Friendly Test**: Verificar responsividad

---

## 📚 Referencias

- [WCAG 2.1 AA Standards](https://www.w3.org/WAI/WCAG21/quickref/)
- [Material Design Mobile](https://material.io/design/platform-guidance/android-bars.html)
- [Apple Human Interface Guidelines](https://developer.apple.com/design/human-interface-guidelines/)
- [MDN - Responsive Design](https://developer.mozilla.org/en-US/docs/Learn/CSS/CSS_layout/Responsive_Design)

---

## 🎯 Implementación en Componentes

Cada componente debe seguir estos patrones:

### 1. Revisar estructura HTML
- ¿Elementos superpuestos?
- ¿Botones < 44px?
- ¿Espaciado consistente?

### 2. Agregar media query mobile
```css
@media (max-width: 760px) {
  /* Ajustar grid, padding, font-size */
}
```

### 3. Validar en dispositivos reales
- iPhone SE (375px)
- iPhone 12 (390px)
- Samsung Galaxy A12 (360px)

---

**Última actualización**: 5 de noviembre de 2026
