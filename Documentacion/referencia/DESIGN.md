---
name: Nexus Terminal
colors:
  surface: '#0b1326'
  surface-dim: '#0b1326'
  surface-bright: '#31394d'
  surface-container-lowest: '#060e20'
  surface-container-low: '#131b2e'
  surface-container: '#171f33'
  surface-container-high: '#222a3d'
  surface-container-highest: '#2d3449'
  on-surface: '#dae2fd'
  on-surface-variant: '#bac9cc'
  inverse-surface: '#dae2fd'
  inverse-on-surface: '#283044'
  outline: '#849396'
  outline-variant: '#3b494c'
  surface-tint: '#00daf3'
  primary: '#c3f5ff'
  on-primary: '#00363d'
  primary-container: '#00e5ff'
  on-primary-container: '#00626e'
  inverse-primary: '#006875'
  secondary: '#d0bcff'
  on-secondary: '#3c0091'
  secondary-container: '#571bc1'
  on-secondary-container: '#c4abff'
  tertiary: '#e6edff'
  on-tertiary: '#263143'
  tertiary-container: '#c6d1e9'
  on-tertiary-container: '#4f5a6e'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#9cf0ff'
  primary-fixed-dim: '#00daf3'
  on-primary-fixed: '#001f24'
  on-primary-fixed-variant: '#004f58'
  secondary-fixed: '#e9ddff'
  secondary-fixed-dim: '#d0bcff'
  on-secondary-fixed: '#23005c'
  on-secondary-fixed-variant: '#5516be'
  tertiary-fixed: '#d8e3fb'
  tertiary-fixed-dim: '#bcc7de'
  on-tertiary-fixed: '#111c2d'
  on-tertiary-fixed-variant: '#3c475a'
  background: '#0b1326'
  on-background: '#dae2fd'
  surface-variant: '#2d3449'
  surface-card: '#1E293B'
  surface-server: '#0F172A'
  status-online: '#10B981'
  status-offline: '#F43F5E'
  border-muted: '#334155'
  text-dim: '#94A3B8'
typography:
  display-lg:
    fontFamily: Inter
    fontSize: 32px
    fontWeight: '700'
    lineHeight: '1.2'
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Inter
    fontSize: 20px
    fontWeight: '600'
    lineHeight: '1.4'
  body-base:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: '1.6'
  body-sm:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: '1.5'
  label-technical:
    fontFamily: JetBrains Mono
    fontSize: 12px
    fontWeight: '500'
    lineHeight: '1'
    letterSpacing: 0.05em
rounded:
  sm: 0.125rem
  DEFAULT: 0.25rem
  md: 0.375rem
  lg: 0.5rem
  xl: 0.75rem
  full: 9999px
spacing:
  base: 4px
  gutter: 24px
  margin-mobile: 16px
  margin-desktop: 32px
  card-padding: 20px
---

## Brand & Style

The design system embodies an **Enterprise Dark** aesthetic, tailored for high-stakes remote infrastructure management. The personality is precise, technical, and authoritative, designed to instill confidence in system administrators.

The visual style leans into **Modern Corporate Minimalism** with a **Technical Edge**. It utilizes deep obsidian and slate tones to reduce eye strain during long monitoring sessions, contrasted by vibrant, high-energy accents that highlight critical actions and statuses. 

**Visual Principles:**
- **Density over Decoration:** Information is prioritized with tight, logical grouping.
- **Luminous Interaction:** High-contrast "electric" colors are reserved for active states and critical signals.
- **Structural Integrity:** Use of subtle grid-based patterns and sharp, intentional borders to create a sense of architectural stability.

## Colors

This system uses a tiered "Enterprise Dark" palette. The background is built on `#0F172A` (Deep Slate), providing a low-light foundation that makes chromatic elements pop.

- **Primary (Electric Blue):** Used for primary CTAs, active connection states, and focus indicators.
- **Secondary (Technical Purple):** Reserved for Workstation-specific accents to differentiate from servers.
- **Success/Danger:** `Emerald 500` for "Online" and `Rose 500` for "Offline." These must include a glow effect (soft 12px blur) when active to simulate a physical LED status light.
- **Surface Hierarchy:** 
    - Base: `#0F172A`
    - Cards/Modals: `#1E293B`
    - Borders: Subtle `#334155` for high-density separation without visual noise.

## Typography

The typography strategy prioritizes legibility of alphanumeric strings (IP addresses, IDs). 

**Inter** is used for all UI controls and headings to maintain a clean, professional look. **JetBrains Mono** is introduced for metadata and technical labels to provide a "developer-tool" aesthetic and ensure characters like `0` and `O` or `1` and `l` are easily distinguishable.

**Usage Rules:**
- Use **Display-lg** only for main dashboard overviews.
- Use **Label-technical** in uppercase for status badges and technical specs (CPU, IP).
- Maintain a high contrast ratio (minimum 7:1) for all body text against dark backgrounds.

## Layout & Spacing

The layout uses a **Fluid Grid** model based on a 4px baseline rhythm. 

- **Grid:** 12-column system for desktop, 4-column for mobile.
- **Desktop Strategy:** 3-column card layout for workstations/servers to maximize visibility of technical details.
- **Reflow:** On tablets (below 1024px), cards switch to a 2-column grid. On mobile (below 640px), cards stack vertically with reduced internal padding.
- **Information Grouping:** Elements within cards use 8px (base * 2) spacing for related items and 16px (base * 4) for distinct sections.

## Elevation & Depth

In an Enterprise Dark system, depth is achieved through **Tonal Layering** and **Luminous Outlines** rather than heavy shadows.

- **Level 1 (Base):** Deep Slate background.
- **Level 2 (Cards):** Surface color with a 1px solid border (`#334155`).
- **Level 3 (Modals/Popovers):** Elevated with a subtle ambient shadow (Black, 25% opacity, 20px blur) and a slightly brighter border (`#475569`).
- **Glow Effects:** Critical status indicators (Online) use a `drop-shadow` with the color of the status itself (e.g., green-glow) to indicate "active power."

## Shapes

The shape language is **Soft (0.25rem)** to maintain a disciplined, engineering-focused appearance. 

- **Standard Elements:** Buttons, input fields, and cards use 4px (`rounded-sm`).
- **Functional Accents:** Status badges and "Pills" for tags use 12px or full-pill rounding to distinguish them from structural layout elements.
- **Servers vs. Workstations:** Server cards should use a `0px` left-border accent (4px width) to feel more "monolithic" and rigid compared to the standard workstation cards.

## Components

### Cards (The Core Unit)
- **Technical Card:** Grid-based layout for details. Use `label-technical` for keys (e.g., IP:) and `body-sm` for values.
- **Server Variant:** Darker background (`#0F172A`), thick 4px primary-colored left border, and a `bi-database` icon header.
- **Workstation Variant:** Standard surface color (`#1E293B`), secondary-colored (purple) top-right icon indicator (`bi-pc-display`).

### Status Badges
- **Online:** Emerald text, emerald border, and a 5px emerald outer glow.
- **Offline:** Rose text, muted border, no glow.

### Buttons
- **Primary Action:** Solid Electric Blue with white or black text depending on legibility.
- **Secondary/Technical:** Ghost buttons (outline only) with `#334155` borders that brighten to white/primary on hover.
- **Iconography:** All action buttons must include a 16px icon (Bootstrap Icons) aligned to the left of the text.

### Input Fields
- Dark backgrounds (`#0F172A`) with 1px borders. Focus state should trigger a `1px solid primary_color` with no glow, maintaining a sharp, technical feel.