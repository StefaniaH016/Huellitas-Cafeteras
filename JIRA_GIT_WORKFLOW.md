# 🔗 Workflow: Jira + Git - Guía Rápida

Referencia rápida para vincular commits Jira con Git de manera consistente.

---

## 📋 FLUJO ESTÁNDAR (Paso a Paso)

### 1. Obtén el Código Jira

En tu Jira board, abre la tarea:
- Busca el código en la URL o en la esquina superior izquierda
- Ejemplo: `HC-123`, `HC-45`, `HC-67`

### 2A. OPCIÓN 1: Trabajar Directo en `main` (Recomendado)

```bash
# Actualiza main con cambios remotos
git checkout main
git pull origin main
```

Continúa con el paso 3.

### 2B. OPCIÓN 2: Crear Rama de Feature (Opcional)

Si prefieres trabajar en una rama separada y luego mergear:

```bash
git checkout -b feature/HC-123-descripcion-corta

# Ejemplos:
git checkout -b feature/HC-123-add-login-page
git checkout -b feature/HC-45-fix-navigation-typo
git checkout -b feature/HC-89-optimize-queries
```

Continúa con el paso 3. Al final, mergea a main (paso 5B).

---

### 3. Haz Cambios y Commits Referenciando Jira

```bash
# Haces cambios en los archivos...

git commit -m "HC-123: Agregar página de login

- Crear componente LoginComponent
- Validar credenciales
- Tests unitarios agregados"
```

**Patrón de commit:**
```
HC-{NUMERO}: {Descripción breve}

{Descripción detallada opcional}
- Punto 1
- Punto 2
```

**Obligatorio:** Comienza con `HC-123:` (o tu código Jira)

Puedes hacer múltiples commits para una tarea:

```bash
git commit -m "HC-89: Crear modelo User"
git commit -m "HC-89: Implementar repositorio"
git commit -m "HC-89: Agregar endpoints CRUD"
git commit -m "HC-89: Escribir tests"
```

**Todos los commits de la misma tarea llevan el mismo código HC-XXX.**

---

### 4A. OPCIÓN 1: Push Directo a `main`

```bash
git push origin main
```

No hay rama intermedia, va directo a main.

### 4B. OPCIÓN 2: Push a la Rama y Mergear

```bash
# Push a tu rama de feature
git push -u origin feature/HC-123-descripcion-corta

# Mergear en main localmente
git checkout main
git pull origin main
git merge feature/HC-123-descripcion-corta

# Push a main
git push origin main

# (Opcional) Eliminar rama local después del merge
git branch -d feature/HC-123-descripcion-corta

# (Opcional) Eliminar rama remota
git push origin --delete feature/HC-123-descripcion-corta
```

---

### 5. Jira Se Actualiza Automáticamente

Jira detecta automáticamente los commits que referencian `HC-123`:
- Aparece el commit en la tarea
- Jira ve que está en progreso
- Cuando todos los commits estén en main, se marca como completado

### ✅ CORRECTO

```bash
# Simples y claros
git commit -m "HC-45: Agregar validación de email"

# Detallados
git commit -m "HC-67: Corregir error de login

- Email con puntos no se validaban
- Actualizar regex a RFC 5322
- Tests agregados para casos edge"

# Múltiples commits en una tarea (TODOS con código)
git commit -m "HC-89: Crear modelo User"
git commit -m "HC-89: Implementar repositorio"
git commit -m "HC-89: Agregar endpoints CRUD"
git commit -m "HC-89: Escribir tests"
```

### ❌ INCORRECTO

```bash
# Falta código Jira
git commit -m "Agregar validación"

# Código Jira al final
git commit -m "Agregar validación HC-45"

# Múltiples tareas en un commit
git commit -m "HC-45 y HC-67: Varios cambios"

# Ambiguo
git commit -m "HC-45: Hacer cambios"
```

---

## 🔧 CONFIGURACIÓN RECOMENDADA

### Configurar Alias Git (Opcional)

```bash
# Crear alias para commits rápidos
git config --global alias.jira '!f() { git commit -m "$1"; }; f'

# Uso:
git jira "HC-123: Mi descripción"
```

### Template de Commit

Crear archivo `~/.gitmessage`:

```
# HC-{NUMERO}: {Descripción breve}
#
# Describe QUÉ y POR QUÉ, no CÓMO
#
# - Cambio 1
# - Cambio 2
```

Configurar git:
```bash
git config --global commit.template ~/.gitmessage
```

---

## 📊 CÓMO JIRA VE TUS COMMITS

Cuando haces commit con formato `HC-123:` y lo pusheaste a `main`:

1. Jira detecta automáticamente el código HC-123 en el commit
2. Busca la tarea HC-123 en tu Jira board
3. En la tarea aparece un enlace al commit
4. El historial de la tarea muestra que fue actualizada en GitHub
5. Si todas las subtareas están completadas, se puede marcar como "Done"

**No necesitas hacer nada más.** Jira ve el commit automáticamente en main.

---

## 📱 INTEGRACIÓN IDE

### VS Code con Jira

1. Instalar extensión **Atlassian for VS Code**
2. Autorizar con Jira
3. Ver issues desde el IDE
4. Ver el estado de las tareas mientras trabajas

**Ventaja:** No necesitas ir a Jira, todo desde VS Code

### IntelliJ IDEA con Jira

1. Instalar plugin **Jira Integration**
2. Configurar URL de Jira
3. Ver issues desde la IDE

**Ventaja:** Excelente integración nativa

---

## 🚀 FLUJO COMPLETO EJEMPLO

### Opción 1: Directo a Main (Recomendado para Equipos Ágiles)

```bash
# 1. En Jira, tengo tarea HC-234: "Implementar reseteo de password"

# 2. Asegurar estar en main
git checkout main
git pull origin main

# 3. Hacer cambios en los archivos...
# → Crear componentes
# → Agregar lógica
# → Escribir tests

# 4. Commit 1
git commit -m "HC-234: Crear componente ResetPasswordForm

- Formulario con email y código de verificación
- Validaciones básicas"

# 5. Commit 2
git commit -m "HC-234: Implementar lógica de reseteo

- Generar código verificación
- Enviar email
- Validar código ingresado"

# 6. Commit 3
git commit -m "HC-234: Agregar tests

- Tests unitarios del servicio
- Tests E2E del formulario"

# 7. Push directo a main
git push origin main

# 8. Jira: Automáticamente detecta los 3 commits
# → En HC-234 aparecen los commits
# → El equipo ve que está completado

# ✅ COMPLETO
```

### Opción 2: Con Rama de Feature (Recomendado para Equipos Grandes)

```bash
# 1. En Jira, tengo tarea HC-234: "Implementar reseteo de password"

# 2. Crear rama de feature
git checkout -b feature/HC-234-password-reset
git pull origin main  # Asegurar estar al día

# 3. Hacer cambios en los archivos...
# → Crear componentes
# → Agregar lógica
# → Escribir tests

# 4. Commit 1
git commit -m "HC-234: Crear componente ResetPasswordForm

- Formulario con email y código de verificación
- Validaciones básicas"

# 5. Commit 2
git commit -m "HC-234: Implementar lógica de reseteo

- Generar código verificación
- Enviar email
- Validar código ingresado"

# 6. Commit 3
git commit -m "HC-234: Agregar tests

- Tests unitarios del servicio
- Tests E2E del formulario"

# 7. Push a la rama de feature
git push -u origin feature/HC-234-password-reset

# 8. Mergear a main
git checkout main
git pull origin main
git merge feature/HC-234-password-reset

# 9. Push a main
git push origin main

# 10. (Opcional) Limpiar rama local y remota
git branch -d feature/HC-234-password-reset
git push origin --delete feature/HC-234-password-reset

# 11. Jira: Automáticamente detecta los 3 commits
# → En HC-234 aparecen los commits
# → El equipo ve que está completado

# ✅ COMPLETO
```

---

## 🔍 VERIFICACIONES ANTES DE PUSH

```bash
# Ver commits en main que aún no han sido pusheados
git log --oneline origin/main..main

# Deben mostrar:
# abc1234 HC-123: primer cambio
# def5678 HC-123: segundo cambio

# Ver que todos tienen código HC- ✅

# Asegurar que estás actualizando main con el remoto
git pull origin main
```

---

## ⚠️ PROBLEMAS COMUNES Y SOLUCIONES

### Problema: Hice commit sin código Jira

```bash
# Opción 1: Hacer otro commit correctamente formateado
git commit -m "HC-123: descripción correcta"

# Opción 2: Enmendar último commit (si aún no pusheaste)
git commit --amend -m "HC-123: descripción correcta"

# Opción 3: Reescribir histórico (si aún no pusheaste)
# Cambiar a commits más antiguos
git rebase -i HEAD~3  # Elige "reword" en los commits
```

### Problema: Alguien más pusheó a main mientras tú trabajabas

```bash
# Actualiza main con los cambios remotos
git pull origin main

# Si hay conflictos, resuélvelos en tu editor
# Luego:
git add .
git commit -m "HC-123: Resolver conflictos"
git push origin main
```

### Problema: Jira no ve el commit

Verifica:
1. ¿El commit está en formato `HC-123:` (con dos puntos)?
2. ¿Ya hiciste push a main? (`git push origin main`)
3. ¿GitHub y Jira están conectados? (Preguntar a admin)

---

## 📋 CHECKLIST ANTES DE CADA PUSH

### Si trabajas Directo en Main:
```
[ ] Estoy en rama main: git branch (debe mostrar: * main)
[ ] Tengo los cambios remotos: git pull origin main
[ ] Todos mis commits empiezan con HC-XXX:
[ ] Hago push directo a main: git push origin main
```

### Si trabajas con Rama de Feature:
```
[ ] Tengo rama de feature: git branch (debe mostrar la rama)
[ ] Rama tiene formato: feature/HC-XXX-descripcion
[ ] Todos mis commits empiezan con HC-XXX:
[ ] Todos los commits están en la rama
[ ] Main está actualizado: git pull origin main
[ ] Mergeo a main: git merge feature/HC-XXX-descripcion
[ ] Push a main: git push origin main
```

**En ambos casos:** Jira verá los commits automáticamente

---

## 🎓 COMANDOS ÚTILES

```bash
# Ver rama actual
git branch

# Ver commits en main
git log --oneline

# Ver commits no pusheados
git log --oneline origin/main..main

# Ver último commit
git show HEAD

# Buscar commits con código Jira
git log --grep="HC-" --oneline

# Ver estado del repositorio
git status
```

---

## 📞 SOPORTE

- **Problema con Jira:** Contacta a tu Scrum Master
- **Problema con Git:** Ver sección "Problemas Comunes"
- **Problema con integración:** Preguntar a tu Tech Lead

---

## ✨ BENEFICIOS DE HACER ESTO BIEN

✅ **Trazabilidad:** Cada commit vinculado a una tarea
✅ **Historial limpio:** Fácil de entender qué se hizo y por qué
✅ **Visibilidad:** El equipo ve el progreso en Jira automáticamente
✅ **Reportes:** Jira genera reportes de velocidad
✅ **Auditoría:** Auditoría completa: quién hizo qué, cuándo y por qué

¡Sigue este workflow y tu equipo estará sincronizado! 🎯
