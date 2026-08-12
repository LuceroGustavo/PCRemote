---
version: alpha
name: PCRemota Theme System
description: Tema claro/oscuro para administración remota de equipos en red, derivado de la referencia Nexus Terminal.
colors:
  backgroundDark: "#0B1326"
  backgroundLight: "#EDF4F1"
  surfaceDark: "#171F33"
  surfaceLight: "#F7FBF8"
  cardDark: "#1E293B"
  cardLight: "#F8FBF7"
  textDark: "#DAE2FD"
  textLight: "#243244"
  mutedDark: "#94A3B8"
  mutedLight: "#64748A"
  borderDark: "#334155"
  borderLight: "#C7D9D6"
  accent: "#0EA5A6"
  success: "#10B981"
  danger: "#F43F5E"
typography:
  body:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: 400
    lineHeight: 1.6
  headline:
    fontFamily: Inter
    fontSize: 20px
    fontWeight: 600
    lineHeight: 1.4
  technicalLabel:
    fontFamily: JetBrains Mono
    fontSize: 12px
    fontWeight: 500
    lineHeight: 1
rounded:
  sm: 0.125rem
  md: 0.25rem
  lg: 0.5rem
  full: 9999px
spacing:
  gutter: 24px
  marginMobile: 16px
  marginDesktop: 32px
  cardPadding: 20px
components:
  themeToggle:
    backgroundColor: "{colors.cardDark}"
    textColor: "{colors.mutedDark}"
    rounded: "{rounded.full}"
  primaryAction:
    backgroundColor: "{colors.accent}"
    textColor: "#06121C"
    rounded: "{rounded.md}"
---

## Overview

PCRemota conserva la estética técnica Enterprise Dark de la referencia `Documentacion/referencia/DESIGN.md`, pero ahora cuenta con un modo claro pastel, menos blanco y más cómodo para uso diurno prolongado.

## Colors

Los colores se consumen en CSS mediante variables `--pc-*` dentro de `src/main/resources/static/css/tema.css`. El atributo `html[data-theme="dark|light"]` decide el set activo.

## Typography

Inter resuelve lectura general y JetBrains Mono identifica etiquetas técnicas, IPs, estados y acciones administrativas.

## Layout

La app mantiene sidebar fijo en desktop, header superior y contenido scrollable. Las superficies deben usar tarjetas con borde visible y contraste suficiente en ambos temas.

## Elevation & Depth

En modo oscuro se usan sombras sutiles y acentos luminosos. En modo claro se evita el blanco puro: fondos salvia/perla, superficies crema verdosas, bordes suaves y sombras amplias de baja opacidad.

## Shapes

Usar radios compactos: `0.125rem` a `0.5rem`, alineado al carácter de herramienta técnica.

## Components

- Theme toggle: persistente por `localStorage` con labels accesibles.
- Primary actions: cian técnico, texto oscuro, hover más profundo.
- Inputs: fondo semántico, borde por tema y focus ring cian.

- Equipment card connector: usar `.equipo-card-shell` y subclases `.equipo-title`, `.equipo-meta`, `.equipo-label`, `.equipo-value`, `.equipo-action-secondary` para que datos y acciones hereden tokens claro/oscuro. En light, las tarjetas deben ser pastel cálidas (`#FBFAF3` hacia `#F8FBF7`), no blanco puro.
- Material Symbols: cargar `/fonts/mso.css`; si la fuente no resuelve, `app.js` activa fallback de glifos para evitar texto crudo como `TOP_` o nombres de icono visibles.

## Do's and Don'ts
- Equipment cards in light mode must remain white, with a subtle slate border, a cyan left rail, and dark slate text; do not inherit dark utility backgrounds.


- Do: usar tokens `--pc-*` para nuevas pantallas.
- Do: probar cada vista en claro y oscuro.
- Don't: agregar nuevos hex hardcodeados si existe un token equivalente.
