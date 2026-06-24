# UI Conventions

## Design Tokens

### Colors

| Token | Value | Usage |
|-------|-------|-------|
| `--bg-primary` | `#f8f9fa` | Page background |
| `--bg-white` | `#ffffff` | Cards, modals, sidebar |
| `--bg-sidebar` | `#1a1d23` | Sidebar background |
| `--bg-sidebar-hover` | `#2d3139` | Sidebar item hover |
| `--bg-sidebar-active` | `#3b4252` | Sidebar active item |
| `--bg-column` | `#f1f3f5` | List/column backgrounds |
| `--text-primary` | `#212529` | Headings, body text |
| `--text-secondary` | `#6c757d` | Muted text, labels |
| `--text-sidebar` | `#adb5bd` | Sidebar text |
| `--text-sidebar-active` | `#ffffff` | Sidebar active item text |
| `--text-placeholder` | `#adb5bd` | Placeholder text |
| `--border` | `#dee2e6` | Borders, dividers |
| `--border-focus` | `#4dabf7` | Focused input border |
| `--accent` | `#228be6` | Primary buttons, links |
| `--accent-hover` | `#1c7ed6` | Button hover state |
| `--accent-light` | `#e7f5ff` | Accent backgrounds |
| `--danger` | `#fa5252` | Error text, error borders |
| `--danger-bg` | `#fff5f5` | Error message backgrounds |
| `--danger-hover` | `#e03131` | Danger button hover |
| `--success` | `#40c057` | Success indicators |
| `--success-bg` | `#ebfbee` | Success backgrounds |
| `--warning` | `#f59f00` | Warning indicators |
| `--warning-bg` | `#fff9db` | Warning backgrounds |

### Typography

| Element | Font | Size | Weight | Color |
|---------|------|------|--------|-------|
| Page title | Inter | 24px | 700 | `--text-primary` |
| Section heading | Inter | 18px | 600 | `--text-primary` |
| Card title | Inter | 16px | 600 | `--text-primary` |
| Body text | Inter | 14px | 400 | `--text-primary` |
| Small / muted text | Inter | 13px | 400 | `--text-secondary` |
| Form label | Inter | 14px | 500 | `--text-primary` |
| Form input | Inter | 14px | 400 | `--text-primary` |
| Button text | Inter | 14px | 500 | `#ffffff` (primary) / `--text-primary` (secondary) |
| Error message | Inter | 13px | 400 | `--danger` |
| Empty state text | Inter | 14px | 400 | `--text-secondary` |
| Sidebar nav item | Inter | 14px | 500 | `--text-sidebar` |
| Badge / counter | Inter | 12px | 600 | `--text-secondary` |
| Table header | Inter | 12px | 600 | `--text-secondary` |

### Spacing

| Token | Value |
|-------|-------|
| `--space-xs` | 4px |
| `--space-sm` | 8px |
| `--space-md` | 12px |
| `--space-lg` | 16px |
| `--space-xl` | 24px |
| `--space-2xl` | 32px |
| `--space-3xl` | 48px |

### Border Radius

| Element | Radius |
|---------|--------|
| Cards | 8px |
| Buttons | 6px |
| Inputs | 6px |
| Modals | 12px |
| Sidebar | 0 (flush to viewport edge) |
| Badges | 9999px (pill) |
| Avatars | 50% (circle) |

### Shadows

| Element | Shadow |
|---------|--------|
| Card | `0 1px 3px rgba(0,0,0,0.08)` |
| Card hover | `0 2px 8px rgba(0,0,0,0.12)` |
| Modal overlay | `rgba(0,0,0,0.5)` backdrop |
| Modal | `0 8px 32px rgba(0,0,0,0.16)` |
| Dropdown | `0 4px 16px rgba(0,0,0,0.12)` |
| Sidebar (mobile overlay) | `4px 0 16px rgba(0,0,0,0.2)` |

## Components

### Sidebar (Desktop)

- Width: 240px, fixed left
- Background: `--bg-sidebar`, text: `--text-sidebar`
- Logo / app name at top: 20px/700, white, 24px padding
- Nav items: 14px/500, 12px vertical padding, 24px horizontal padding
- Active item: `--bg-sidebar-active` background, `--text-sidebar-active` text, left accent border (3px, `--accent`)
- Hover: `--bg-sidebar-hover`
- Icons: Lucide, 20px, 12px gap before label
- Bottom section: user avatar + name, divider above

#### Nav groups

- Related nav items group under a **group label**: 11px/600, uppercase, `--text-sidebar` at 60% opacity, 0.04em letter-spacing, padding `16px 24px 8px`
- Sub-items under a group get extra left padding (32px) to read as nested
- Example: an "Администрирование" group with a "Пользователи" sub-item

#### Collapsible sidebar (icon rail)

- The sidebar collapses to a **64px icon-only rail** via a toggle button in the top bar (`panel-left` icon when expanded → `panel-left-open` when collapsed)
- Collapsed rail: icons centered (no labels, no group labels), each item 44px tall; use a 1px `--bg-sidebar-hover` divider where a group label would have been; the full label shows as a native `title` tooltip
- Collapsed/expanded state is **persisted in `localStorage`** so it survives reloads
- The active item keeps its 3px `--accent` left border in both states

### Sidebar (Mobile)

- Hidden by default
- Opens as overlay from left, `--bg-sidebar` background
- Close button (`x` icon) top-right
- Same item styles as desktop
- Overlay backdrop: `rgba(0,0,0,0.5)`

### Bottom Navigation (Mobile)

- Fixed bottom, full width, white background, top border
- Height: 56px + safe-area bottom padding
- 4-5 icon items evenly spaced
- Active: `--accent` color icon + label
- Inactive: `--text-secondary` color icon, no label
- Touch targets: min 44x44px

### Top Bar

- White background, bottom border (`--border`)
- Height: 56px (desktop), 56px (mobile)
- Desktop: breadcrumb left, user menu right
- Mobile: hamburger left, page title center, action icon right
- Horizontal padding: 24px (desktop), 16px (mobile)

#### App Shell variant (logo in top bar)

The authenticated app shell places the **logo at the left of the top bar** (not the sidebar) and the **user menu at the right**; the top bar spans the full width above both the sidebar and the content. Used by the dashboard and every authenticated page going forward.

- Logo: 20px/700, `--accent`, left-aligned
- User menu (right): avatar (32px) + user name (14px/500) + `chevron-down` icon, 4px/8px padding, `--bg-primary` on hover
- Clicking the user menu opens a dropdown anchored top-right: header row (name 14px/600 + email 13px/`--text-secondary`), divider, then items (e.g. `log-out` "Выйти")
- Mobile: logo left, avatar right (no name); dropdown anchored under the avatar
- Reusable scaffold: `templates/dashboard-layout.html`

### Card

- White background, 8px radius, card shadow
- Padding: 16px
- Header row: title left, action icons right
- Title: 16px/600, single line (truncate with ellipsis)
- Body: 14px/400, `--text-primary`
- Footer: 13px/400, `--text-secondary`, metadata
- Hover: elevated shadow

### Form / Modal

- Centered modal with overlay backdrop
- 12px border radius, white background
- Padding: 24px
- Max width: 480px (desktop), full width minus 32px (mobile)
- Form fields stack vertically with 16px gap
- Labels above inputs, 14px/500
- Input height: 40px (text input), 120px (textarea)
- Input border: 1px solid `--border`, focus: `--border-focus` with 3px ring
- Character counter below constrained fields: right-aligned, `--text-secondary`, 12px

### Form (Full Page)

- Same field styles as modal form
- Max width: 640px, centered in content area
- Page title at top with back navigation
- Actions at bottom: primary right-aligned, cancel left-aligned

### Error States

- Inline field errors: red text below the field, 13px
- Error input border: `--danger`
- Banner errors: `--danger-bg` background, `--danger` text, `x-circle` icon
- Inline toast: top-right corner, auto-dismiss, shadow

### Buttons

- Primary: `--accent` bg, white text, 6px radius, 40px height, 16px horizontal padding
- Primary hover: `--accent-hover`
- Secondary/Cancel: transparent bg, `--text-primary` text, 1px `--border` border
- Secondary hover: `--bg-column` background
- Danger: `--danger` bg, white text, 6px radius, 40px height
- Danger hover: `--danger-hover`
- Disabled: 50% opacity, no pointer events
- Mobile button height: 44px (touch target)

### Icon Buttons

- Default: transparent background, `--text-secondary` icon color
- Hover: `--bg-column` background, `--text-primary` icon color, 4px radius
- Size: 32px container, 20px icon (desktop); 44px container, 24px icon (mobile touch target)
- Destructive hover: `--danger-bg` background, `--danger` icon color

### Confirmation Dialog

- Centered modal, 400px max width, 12px radius, modal shadow
- Warning icon: `alert-triangle` in circular `--danger-bg` container (48px)
- Title: 18px/600, centered
- Body text: 14px/400, `--text-secondary`, centered
- Actions: desktop side-by-side (Cancel + Danger), mobile stacked (Danger first, Cancel second)
- Overlay: `rgba(0,0,0,0.5)` backdrop

### Empty State

- Centered in content area (vertical + horizontal)
- Icon: 48px, `--text-placeholder` color
- Title: 18px/600, `--text-primary`
- Description: 14px/400, `--text-secondary`
- Optional CTA button below

### Table

- Full width, no outer border
- Header: 12px/600, `--text-secondary`, uppercase, bottom border
- Rows: 14px/400, bottom border (`--border`), 12px vertical padding
- Row hover: `--bg-primary`
- Actions column: icon buttons, right-aligned
- Mobile: card layout (no table), each row becomes a card
- **Sortable header**: header label + a 14px sort glyph (`chevrons-up-down` when unsorted, `arrow-up`/`arrow-down` when sorted); the sorted column's glyph is `--accent` at full opacity, others at 0.6
- **Per-column filter row** (client-side grids): an optional second header row of inputs (30px, `--bg-column` background) — one filter input per column; status columns use an "Все" placeholder. Used when the grid filters/sorts client-side over the full list
- **Mobile sort/filter**: a single full-width search field above the cards + a 44px sort icon button (`arrow-up-down`) — per-column filter inputs collapse into the search on mobile

### Badge / Status Pill

- Pill shape (9999px radius), 12px/600 font
- Padding: 2px 8px
- Variants: accent (`--accent-light` bg, `--accent` text), success, warning, danger
- Use semantic names: `status-active` (success), `status-pending` (warning), `status-locked` (danger), `status-inactive` (`--bg-column` bg, `--text-secondary` text)

### Dropdown Menu

- White background, dropdown shadow, 8px radius
- Items: 14px/400, 32px height, 12px horizontal padding
- Hover: `--bg-primary`
- Divider between groups: 1px `--border`
- Min width: 180px

### Loading Spinner

- `loader-2` Lucide icon with CSS rotation animation
- Size: 20px inline, 32px centered overlay
- Color: `--accent`

### Avatar

- Circle (50% radius)
- Sizes: sm 32px, md 40px, lg 48px
- Default: initials on `--accent-light` background, `--accent` text
- With image: `background-size: cover`

## Layout

### Desktop (1400px)

- Sidebar: 240px fixed left
- Content area: fills remaining width, `--bg-primary` background
- Content max width: none (fluid), horizontal padding: 32px
- Vertical rhythm: 24px gap between sections
- Cards in grid: 2-3 columns with 16px gap
- Modals: centered overlay

### Mobile (375px)

- No sidebar; bottom navigation instead
- Content: full width, 16px horizontal padding
- Cards: single column, stacked
- Forms: full-screen slide-up
- Bottom padding: 72px (nav bar) + safe area
- Modals: bottom sheet or full-screen

### Auth Pages

- Centered card, max width 400px
- Logo at top
- `--bg-primary` background
- No sidebar, no top bar
- Mobile: same layout, 16px padding

### Welcome / Landing (unauthenticated home)

- Centered hero, no card, no sidebar, no top bar, `--bg-primary` background
- Logo: large wordmark (48px desktop / 40px mobile, 700, `--accent`)
- Tagline below logo (18px/600, `--text-primary`), then a short description (14px/`--text-secondary`)
- Single primary CTA button below ("Войти" with `log-in` icon), 44px height
- Max width 480px (desktop); full width minus 32px (mobile)

### App Shell (Dashboard Layout)

The authenticated layout for all signed-in pages.

- **Top bar** (full width, 56px): logo left, user menu right — see "Top Bar → App Shell variant"
- **Sidebar** (240px, `--bg-sidebar`, below the top bar): navigation items per the Sidebar component. The shell renders an empty sidebar until a story adds nav entries; an empty sidebar must read as intentional (subtle muted placeholder), never broken
- **Content** (`--bg-primary`, fluid, 32px padding): page title (24px/700) + page content; use the Empty State component when a section has no data yet
- Loading: while the current-user fetch resolves, show a centered `loader-2` spinner (32px, `--accent`) on `--bg-primary`
- Reusable scaffold: `templates/dashboard-layout.html`
