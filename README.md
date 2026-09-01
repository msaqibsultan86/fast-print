# Fast Print

Print anything. Any size. Beautifully.

An Android app for printing to Wi-Fi printers with a live preview of the sheet — pick a format,
set the paper size, margins, scale, colour and copies, drop in an image and position it, then
print. Finished jobs are kept so they can be reprinted later.

Built from the *Fast Print* design handoff, in Kotlin + Jetpack Compose.

Setup, Google Sign-In configuration and build instructions: **[SETUP.md](SETUP.md)**.

## Screens

`Launch → Sign in → Welcome → Home → Customize → Printers → Printing → Done | Failed`, plus
Settings, Print history, and the image editor overlay.

| Screen | What it does |
|---|---|
| Launch | Brand splash, 2 s, tap to skip |
| Sign in | Google (Credential Manager) or guest |
| Welcome | Real Google name, email and profile photo |
| Home | Six format presets — Documents, Photos, Labels, Cards, Posters, Custom |
| Customize | Live sheet preview + every print setting; upload / adjust an image |
| Image editor | Drag, zoom (1–3×), rotate 90°, reset, apply/cancel with revert |
| Printers | Live mDNS discovery of network printers |
| Printing / Done / Failed | Real job outcome from the print framework |
| Settings | Profile, default printer, notifications, Terracotta/Sage theme, logout |
| History | Persisted jobs — reprint restores every setting and the image |

## Architecture

```
state/      PrintSettings, ImageTransform, AppViewModel (single source of truth)
print/      PrinterDiscovery (NsdManager), PaperSize, PageRenderer,
            FastPrintDocumentAdapter, PrintController
data/       Room (PrintJobEntity/Dao/AppDatabase), ImageStore, HistoryRepository, Prefs
auth/       GoogleAuth — Credential Manager + Sign in with Google
ui/theme    Organic design tokens, Caprasimo/Figtree type, Terracotta↔Sage accent
ui/screens  One file per screen; SheetPreview is shared by Customize and the editor
```

Two details worth knowing before changing things:

- **`ImageTransform` offsets are normalised** (a fraction of the sheet), not pixels. The preview
  and the printed page are different sizes, and the same transform has to reproduce on both — a
  4×6 photo and a 24×36 poster included.
- **`PageRenderer` and `SheetPreview` deliberately share their rules** (margins, scale, grayscale).
  If you change how one lays out a page, change the other or the preview starts lying.

## Design system

"Organic" — cream `#f5ead8` ground, sand `#ebddc5` surfaces, terracotta `#c67139` primary, sage
`#7a8a5e` secondary, with full 9-step ramps. Caprasimo headings over Figtree body. Everything is
a pill or a generously rounded card. The accent is switchable at runtime between Terracotta and
Sage from Settings.

All colour values live in `ui/theme/Tokens.kt`. Don't introduce colours outside it.

## Status

Every screen and interaction in the handoff is implemented, including real printing, real
discovery, real Google identity and persisted history. Two deliberate departures from the
prototype, both documented in [SETUP.md](SETUP.md#4-printing-and-printer-discovery--how-it-actually-behaves):

- The sign-in background is a branded gradient, not the handoff's third-party CDN video.
- Printer *selection* ultimately happens in the system print dialog, because Android exposes no
  public API to preselect a printer or read back the one chosen.

This project has not been compiled — it was authored on a machine without the Android SDK. Run
the CI workflow or open it in Android Studio to get a build.
