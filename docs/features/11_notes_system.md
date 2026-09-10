# Feature 11: Minimalist Notes Engine

## Overview
The Notes Module provides an Obsidian/Apple Notes style writing surface in Calender28. Notes feature full-screen infinite text canvas writing with zero border/box clutter, automatic headline derivation, Markdown format switching via menu, native Android Intent sharing, and zero emojis in UI strings.

Detailed technical and UI specifications are documented in [docs/NOTES_FEATURE.md](../NOTES_FEATURE.md).

---

## Architectural Highlights
- **Full-Screen Canvas**: `BasicTextField` with infinite scroll; no artificial cards or outlined boxes.
- **Content-Driven**: No separate title or category inputs. Search and titles are derived directly from the content.
- **Format Toggle**: Default is Plain Text (`.txt`). Three-dot menu provides one-tap toggle to `"Save as MD"` / `"Save as TXT"`.
- **Pure Theme Tokens**: Strict compliance with `MaterialTheme.colorScheme` and `MaterialTheme.typography`.
- **Vector-Only Iconography**: Exclusively vector `Icon()` implementations; no hardcoded emojis in user-facing UI.
