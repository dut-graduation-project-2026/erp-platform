# ERP Design System - Enterprise Edition

## 1. Visual Theme & Atmosphere

Our ERP design system embraces **functional clarity over decorative aesthetics**. The visual language prioritizes data readability, quick information scanning, and user confidence in complex workflows. Built on a sophisticated monochromatic foundation with strategic accent colors, the system communicates professionalism while maintaining warmth and approachability.

The color palette is deliberately restrained—near-black text on white surfaces create maximum contrast for extended reading sessions. Depth is achieved through a refined shadow system that clearly delineates interactive layers without visual clutter. This approach ensures that users can process dense information hierarchies quickly while maintaining focus on the data that matters.

**Segoe UI** serves as the primary typeface, the industry standard for Windows-based enterprise applications. It's optimized for screen display, highly legible at all sizes, and ensures cross-platform consistency. The system uses a clear weight hierarchy (300 for lighter elements, 400 for body, 500-600 for emphasis) to establish information priority without resorting to color variation.

**Key Characteristics:**
- Monochromatic grayscale foundation with strategic blue and semantic accents
- Segoe UI typeface family for universal enterprise compatibility
- Refined multi-layered shadow system (8 levels) for depth hierarchy
- Grid-based spacing with 8px base unit for predictable layouts
- Border-radius scale from 2px to 16px—functional, never decorative
- White canvas with charcoal (#242424) primary text
- Data tables and forms as primary content—the interface serves the user's workflow
- Built for accessibility and WCAG AA compliance

## 2. Color Palette & Roles

### Primary & Text Colors
- **Charcoal** (`#242424`): Primary heading and label text — our signature dark neutral, warm but professional
- **Midnight** (`#111111`): Deepest text and navigation elements — used at full opacity for critical information
- **White** (`#ffffff`): Primary background and surface canvas — the dominant visual

### Secondary & Interactive
- **Primary Blue** (`#0066cc`): Primary actions, CTA buttons, selected states — confident, professional, accessible
- **Accent Blue** (`#0099ff`): Links and secondary interactive elements — lighter variant for distinction
- **Dark Blue** (`#004499`): Hover states and active navigation — deeper interaction feedback
- **Light Gray** (`#f8f8f8`): Subtle background differentiation — almost imperceptible section breaks
- **Mid Gray** (`#898989`): Secondary text, descriptions, placeholders — 60% opacity for subtlety

### Semantic Colors
- **Success Green** (`#28a745`): Confirmation, positive status, successful operations — calming and clear
- **Warning Yellow** (`#ffc107`): Caution, pending states, require attention — warm and noticeable
- **Error Red** (`#dc3545`): Errors, failures, critical alerts — urgent but not alarming
- **Info Blue** (`#17a2b8`): Information, help text, neutral notifications — consistent with primary blue family

### Neutrals & Borders
- **Pure Black** (`#000000`): Maximum contrast text, critical UI elements only
- **Border Gray** (`rgba(34, 42, 53, 0.10–0.15)`): Subtle dividers using shadow borders
- **Input Border** (`#d0d0d0`): Form field boundaries — subtle containment without heaviness
- **Disabled Gray** (`#e0e0e0`): Disabled elements and inactive states

### Opacity Hierarchy
- **100% opacity**: Critical text, primary buttons, primary navigation
- **75% opacity** (`rgba(..., 0.75)`): Secondary text, hover states
- **60% opacity** (`rgba(..., 0.60)`): Tertiary text, muted labels
- **40% opacity** (`rgba(..., 0.40)`): Disabled elements, placeholders
- **10% opacity** (`rgba(..., 0.10)`): Subtle borders and dividers

### Design Philosophy
The color system follows a hierarchy principle: **use color conservatively to guide attention**. Primary actions are blue, states are semantic (red/green/yellow), and neutral UI remains grayscale. This approach prevents color fatigue in long working sessions typical of ERP usage.

## 3. Typography Rules

### Font Family
- **Primary**: `Segoe UI` — Microsoft's enterprise typeface, optimized for screen display
- **Fallback**: `Arial, sans-serif` — ensures consistency on systems without Segoe UI
- **Code/Technical**: `Consolas` or `Courier New` — monospace for IDs, codes, calculations
- **System Font Stack**: `"Segoe UI", "Helvetica Neue", Arial, sans-serif`

### Hierarchy

| Role | Font | Size | Weight | Line Height | Letter Spacing | Usage |
|------|------|------|--------|-------------|----------------|-------|
| Page Title | Segoe UI | 32px | 700 | 1.20 | 0px | Main page headings, module titles |
| Section Header | Segoe UI | 24px | 600 | 1.15 | 0px | Section breaks, major groupings |
| Subsection Title | Segoe UI | 18px | 600 | 1.20 | 0px | Form sections, tab headers |
| Form Label | Segoe UI | 14px | 600 | 1.40 | 0px | Field labels, required indicators |
| Field Value | Segoe UI | 14px | 400 | 1.50 | 0px | Data display in forms, read-only fields |
| Body Text | Segoe UI | 14px | 400 | 1.50 | 0px | Main content paragraphs, descriptions |
| Table Header | Segoe UI | 13px | 600 | 1.20 | 0px | Column titles, sortable headers |
| Table Data | Segoe UI | 13px | 400 | 1.40 | 0px | Row content, list items |
| Button Text | Segoe UI | 14px | 600 | 1.00 | 0px | Button labels, CTA text |
| Menu Item | Segoe UI | 14px | 500 | 1.40 | 0px | Navigation links, sidebar items |
| Badge / Status | Segoe UI | 12px | 600 | 1.20 | 0px | Status tags, workflow states |
| Helper Text | Segoe UI | 12px | 400 | 1.40 | +0.1px | Instructional text, tooltips, hints |
| Placeholder | Segoe UI | 13px | 400 | 1.40 | +0.1px | Input hints, empty field guides |
| Code / Technical | Consolas | 12px | 400 | 1.20 | 0px | Invoice IDs, serial numbers, calculations |
| Info Text | Segoe UI | 11px | 400 | 1.50 | 0px | Footer text, metadata, timestamps |
| Error Message | Segoe UI | 12px | 500 | 1.40 | 0px | Error alerts, validation messages |

### Typography Principles
- **Weight 600 for emphasis**: Use for labels, headers, and CTAs — creates instant hierarchy without color
- **Weight 400 for body**: Standard reading text, field values, descriptions — neutral and readable
- **Weight 300 for subtle**: Used sparingly for helper text and metadata — creates visual rest
- **Line-height ranges**: Body text uses 1.40–1.50 for comfortable reading; compact UI uses 1.00–1.20
- **Letter-spacing**: Generally 0px; +0.1px only for helper text below 12px to improve readability
- **Consistency over creativity**: Segoe UI across all contexts ensures platform compatibility and professional appearance

## 4. Component Stylings

### Buttons
- **Primary CTA**: `#0066cc` background, white text, 4px radius. Hover: opacity to 0.85. Active: `#004499` background. Confident and action-oriented
- **Secondary Button**: White background with `#d0d0d0` border, `#242424` text, 4px radius. Hover: `#f8f8f8` background. Lower visual weight
- **Tertiary / Ghost**: Transparent background, `#0066cc` text, 4px radius. Hover: `#f0f4ff` background. Minimal visual load
- **Danger**: `#dc3545` background, white text, 4px radius. Reserved for destructive actions only
- **Disabled**: `#e0e0e0` background, `#999999` text. No hover effect. Clear disabled state
- **Icon Button**: 32x32px minimum, centered icon, 4px radius. Same color rules as text buttons
- **Button Padding**: Vertical 10px, horizontal 16px — comfortable touch target and visual weight
- **Focus Ring**: `#0066cc` outline at 2px width, 2px offset — visible for keyboard navigation

### Cards & Containers
- **Standard Card**: White background, single shadow (0px 1px 3px rgba(0,0,0,0.12)), 4px radius. Light elevation without drama
- **Content Card**: White background, subtle border (`1px solid #e0e0e0`), 4px radius. Clear containment without shadow
- **Data Panel**: White background, `0px 1px 2px rgba(0,0,0,0.05)` shadow, 4px radius. Minimal visual separation
- **Hover**: Slight shadow increase to 0px 2px 8px rgba(0,0,0,0.15) when interactive. Subtle feedback
- **Selected**: `#f0f4ff` background with `2px solid #0066cc` left border. Visual indication of active state
- **Radius**: 4px for standard components, 8px for larger containers, 2px for compact UI

### Tables
- **Header**: `#f8f8f8` background, `#242424` text at weight 600, `1px solid #e0e0e0` bottom border
- **Row**: White background alternating with `#fafafa` for zebra striping — improves scanning
- **Cell Padding**: Vertical 12px, horizontal 16px — adequate spacing for dense data
- **Row Hover**: `#f0f4ff` background — indicates interactivity
- **Selected Row**: `#f0f4ff` background with `3px solid #0066cc` left border — clear selection indicator
- **Sortable Header**: Clickable with pointer cursor, arrow icon on hover, active sort shown in `#0066cc`
- **Column Resize**: 2px hover zone on borders — drag to adjust width
- **Sticky Header**: Remains visible during scroll, uses `0px 2px 4px rgba(0,0,0,0.10)` shadow for elevation

### Inputs & Forms
- **Text Input**: White background, `1px solid #d0d0d0` border, 4px radius, 8px padding. Focus: `#0066cc` border, no outline needed
- **Select Dropdown**: White background, dark text, `1px solid #d0d0d0` border, 4px radius. Dropdown arrow on right
- **Checkbox**: 16x16px, `#0066cc` when checked, `#999999` border when unchecked. Label 12px to the right
- **Radio Button**: 16x16px circular, same color logic as checkbox. Clear selection state
- **Textarea**: White background, border same as text input, 4px radius, min-height 100px, vertical resize allowed
- **Label**: `#242424` at weight 600, positioned above input. Required asterisk in `#dc3545`
- **Error State**: `1px solid #dc3545` border, `#dc3545` error message below at 12px weight 400
- **Disabled**: `#e0e0e0` background, `#999999` text, cursor not-allowed
- **Placeholder**: `#898989` text, 60% opacity — visible but clearly different from entered text
- **Help Text**: `#898989` at 12px weight 400, appears below input. Line-height 1.40

### Navigation
- **Top Navigation Bar**: White background, `1px solid #e0e0e0` bottom border. Height 56px
- **Nav Link**: `#242424` text at 14px weight 500, 4px bottom padding, underline on hover. Active state: `#0066cc` text with blue underline
- **Active State**: `3px solid #0066cc` bottom border, retained after click
- **Breadcrumb**: Gray separators (`>`), left-aligned at 13px, final item in `#0066cc`
- **Sidebar**: `#f8f8f8` background, width 240px on desktop, collapsible on mobile. Section headers in weight 600
- **Sidebar Item**: 12px vertical padding, 16px horizontal, left-border accent on active (3px solid `#0066cc`)
- **Mobile Nav**: Hamburger menu (three horizontal lines), converts to full-screen overlay

### Icons
- 16px for inline text icons, 20px for button icons, 24px for standalone. Use solid icons for actions, outlines for states
- Color: Match surrounding text color or use `#0066cc` for interactive elements
- Hover: Change to `#004499` on interactive icons

## 5. Layout Principles

### Spacing System
- **Base Unit**: 8px
- **Scale**: 2px, 4px, 8px, 12px, 16px, 20px, 24px, 32px, 40px, 48px, 56px, 64px, 80px, 96px
- **Section Padding**: 32px–48px vertical between major sections
- **Card Padding**: 16px–24px internal spacing
- **Form Spacing**: 12px between form groups, 8px between related fields
- **Component Gap**: 4px–8px between buttons and controls
- **List Item Gap**: 8px–12px between list items

### Grid & Container
- **Max Width**: 1200px for primary content, centered with 24px gutters on sides
- **Page Layout**: Sidebar (240px fixed) + main content (remaining width)
- **Column Patterns**: Full-width for data tables, 2-column for form layouts, 3-column for dashboards
- **Responsive Breakpoints**: 
  - Mobile: <768px (single column)
  - Tablet: 768px–1024px (two columns)
  - Desktop: >1024px (full multi-column)

### Whitespace Philosophy
- **Breathable layouts**: Use 32px–48px vertical spacing to separate concepts — prevents cognitive overload
- **Data-centric**: Forms and tables have compact spacing (12px) to fit more information
- **Visual hierarchy through space**: Distance between elements indicates relationship — grouped items are close, separate sections are far
- **Padding consistency**: All containers use 16px–24px internal padding for predictable spacing

### Border Radius Scale
- **2px**: Tight corners on small inline elements, subtle softness
- **4px**: Standard radius for buttons, inputs, cards — the workhorse radius
- **8px**: Larger cards, containers, modal dialogs
- **16px**: Large section containers, emphasis boxes
- **100px**: Avatars and circular elements

## 6. Depth & Elevation

| Level | Shadow | Use |
|-------|--------|-----|
| 0 | None | Page canvas, flat text areas |
| 1 | `0px 1px 2px rgba(0,0,0,0.05)` | Subtle elements, hover states |
| 2 | `0px 1px 3px rgba(0,0,0,0.12)` | Standard cards, default elevation |
| 3 | `0px 2px 8px rgba(0,0,0,0.15)` | Elevated cards, interactive hover |
| 4 | `0px 4px 12px rgba(0,0,0,0.20)` | Prominent cards, modal overlays |
| 5 | `0px 8px 20px rgba(0,0,0,0.25)` | Floating elements, dropdowns |
| 6 | `0px 12px 28px rgba(0,0,0,0.30)` | Modals, notifications |
| 7 | `0px 16px 36px rgba(0,0,0,0.35)` | Full-page overlays, system alerts |

### Shadow Philosophy
- **Minimal, functional shadows**: Depth is used to clarify interaction layers, not to decorate
- **Soft blur (1–4px)**: Creates ambient depth without harshness
- **Dark shadow at 5–35% opacity**: Creates visual separation without overwhelming the interface
- **No inset shadows**: Avoid inner-beveled effects — keep surfaces clean
- **Focus: subtle elevation changes**: Hover states increase shadow slightly to indicate interactivity

### Color Overlays
- **30–50% opacity dark overlay**: Used for modal backgrounds to darken page content
- **Avoid gradients and glows**: Maintain flat, clean surfaces

## 7. Do's and Don'ts

### Do
- **Use Segoe UI exclusively** — ensures platform consistency and legibility across all devices
- **Maintain the grayscale foundation** — boldness and clarity come from contrast and hierarchy, not color
- **Apply semantic colors consistently** — blue for primary actions, red for errors, green for success
- **Use the shadow system for elevation** — distinguishes interactive layers without visual clutter
- **Keep backgrounds white** — the monochromatic philosophy requires a clean canvas for data clarity
- **Apply weight 600 for emphasis** — labels, headers, and CTAs create hierarchy through boldness
- **Use positive letter-spacing (+0.1px) for small text** — improves readability below 12px
- **Maintain consistent padding** — predictable spacing creates professional, organized appearance
- **Test with real data** — ERP interfaces must handle variable-length text and large datasets gracefully
- **Prioritize accessibility** — WCAG AA compliance ensures usability for all users

### Don't
- **Don't use decorative elements** — illustrations, gradients, and unnecessary graphics distract from data
- **Don't mix typefaces** — Segoe UI is the standard; substitutes break the system
- **Don't reduce whitespace aggressively** — cramped layouts increase cognitive load and errors
- **Don't use rounded corners larger than 8px** — excessively soft curves feel unprofessional in enterprise context
- **Don't add brand colors outside the semantic system** — custom colors confuse users about meaning
- **Don't rely on color alone for meaning** — always use text labels and icons alongside color
- **Don't create hover effects without feedback** — subtle shadow increase or background change indicates interactivity
- **Don't forget focus states** — keyboard users need visible focus indicators for all interactive elements
- **Don't use opacity to disable elements** — use distinct color (`#e0e0e0`) and `cursor: not-allowed`
- **Don't mix shadow styles** — use the defined levels consistently across all components

## 8. Component Patterns

### Form Pattern
```
Label (weight 600, 14px)
Input Field (14px, 4px radius, border)
Helper Text (weight 400, 12px, gray)
Error Message (weight 500, 12px, red) — only on error
Spacing between fields: 12px
```

### Data Table Pattern
```
Header Row (weight 600, #f8f8f8 bg, 1px border-bottom)
Data Rows (alternating white/#fafafa, 12px padding)
Hover State (#f0f4ff background)
Selected State (left border 3px #0066cc, #f0f4ff background)
```

### Card Pattern
```
White background
Box Shadow Level 2 (0px 1px 3px rgba(0,0,0,0.12))
4px border-radius
16–24px padding
Hover: Shadow Level 3 (if interactive)
```

### Modal Pattern
```
Dark overlay (rgba(0,0,0,0.50))
White card (Shadow Level 6)
Close button (top-right, 32x32px)
Title (weight 600, 24px)
Content (14px body text)
Footer (button group, right-aligned)
```

### Button Group Pattern
```
Primary button first (blue, CTA text)
Secondary buttons (white, 8px gap between buttons)
Danger action last (red, if destructive)
All buttons: 40px minimum height for touch targets
```

## 9. Responsive Behavior

### Breakpoints
| Device | Width | Layout Changes |
|--------|-------|-----------------|
| Mobile | <768px | Single column, collapsed sidebar, stacked forms |
| Tablet | 768px–1024px | Sidebar converts to collapsible drawer, 2-column forms |
| Desktop | >1024px | Full layout, fixed sidebar, 3-column dashboards |

### Mobile Adaptations
- **Navigation**: Hamburger menu, slide-out drawer
- **Forms**: Single column, full-width inputs
- **Tables**: Horizontal scroll or card view (one row per card)
- **Buttons**: Full-width CTAs for easy thumb access
- **Font Sizes**: Maintain 14px+ for readability on small screens
- **Touch Targets**: Minimum 40x40px for all interactive elements

### Tablet Adjustments
- **Sidebar**: Collapsible drawer that overlays content
- **Grid**: 2-column layouts for feature comparison
- **Tables**: Condensed but maintain horizontal scroll if needed
- **Spacing**: Reduce from 48px to 32px for section gaps

### Desktop Optimization
- **Sidebar**: Fixed 240px left sidebar
- **Main Content**: Max-width 1200px, centered
- **Multi-column**: 3-column layouts for data dashboards
- **Spacing**: Full 48px section spacing
- **Density**: Information-dense tables without compromising readability

## 10. Accessibility Guidelines

### Color Contrast
- **Text on background**: Minimum 4.5:1 ratio (WCAG AA standard)
- **Text on button**: Minimum 3:1 ratio
- **Icon on background**: Minimum 3:1 ratio
- **Charcoal (#242424) on white**: 17.3:1 — excellent contrast
- **Mid-gray (#898989) on white**: 5.3:1 — acceptable for secondary text

### Keyboard Navigation
- **Tab order**: Left-to-right, top-to-bottom, logical flow
- **Focus indicator**: 2px `#0066cc` outline with 2px offset — visible at all times
- **Focusable elements**: All buttons, links, inputs, and interactive components
- **Skip links**: Allow jumping over navigation to main content
- **Keyboard-only actions**: All mouse interactions available via keyboard

### Motion & Animation
- **Respect prefers-reduced-motion**: Disable animations for users who request reduced motion
- **Transition timing**: Keep animations under 200ms — feels responsive without distraction
- **Avoid flashing**: No content that flashes more than 3 times per second

### Semantic HTML
- **Use proper headings**: `<h1>` for page title, `<h2>` for sections, `<h3>` for subsections
- **Form labels**: Every input has associated `<label>` element
- **Alt text**: All images include descriptive alt text
- **ARIA**: Use sparingly for dynamic content only — prefer semantic HTML

## 11. Design Tokens & Quick Reference

### Color Quick Reference
```
Primary Text: #242424
Deep Text: #111111
Secondary Text: #898989
Background: #ffffff
Light Background: #f8f8f8
Primary Action: #0066cc
Dark Hover: #004499
Success: #28a745
Error: #dc3545
Warning: #ffc107
Border: #e0e0e0
```

### Type Quick Reference
```
Page Title: Segoe UI, 32px, 700, 1.20
Section Header: Segoe UI, 24px, 600, 1.15
Body Text: Segoe UI, 14px, 400, 1.50
Label: Segoe UI, 14px, 600, 1.40
Helper Text: Segoe UI, 12px, 400, 1.40
```

### Spacing Quick Reference
```
Large Section Gap: 48px
Standard Section Gap: 32px
Component Gap: 8px
Form Field Gap: 12px
Card Padding: 16–24px
Button Padding: 10px vertical, 16px horizontal
```

### Shadow Quick Reference
```
Subtle: 0px 1px 2px rgba(0,0,0,0.05)
Standard: 0px 1px 3px rgba(0,0,0,0.12)
Elevated: 0px 2px 8px rgba(0,0,0,0.15)
Modal: 0px 12px 28px rgba(0,0,0,0.30)
```

## 12. Implementation Checklist

When building new screens:
- [ ] Verify all text uses Segoe UI typeface
- [ ] Check color palette uses only defined colors — no arbitrary hex codes
- [ ] Confirm buttons follow button patterns (primary, secondary, danger)
- [ ] Verify spacing follows 8px grid system
- [ ] Test focus states on all interactive elements
- [ ] Ensure contrast meets WCAG AA standards
- [ ] Check responsiveness at mobile (mobile), tablet (768px), and desktop (>1024px)
- [ ] Test with real data — especially long text and empty states
- [ ] Verify keyboard navigation works without mouse
- [ ] Check loading and error states for all components
