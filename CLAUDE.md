# Fast Print — project primer

Android printing app. Capacitor shell around a single self-contained web UI, plus a native
Kotlin plugin for everything the web layer cannot do.

## Stack

- **Capacitor 6** + Android (Java 17, Kotlin 1.9.24, AGP 8.2.1)
- UI: `www/index.html` — one 3.6 MB self-contained file, all screens and assets inline
- Native: `android/app/src/main/java/com/notzyvex/fastprint/FastPrintPlugin.kt`
- Package / applicationId: `com.notzyvex.fastprint`

## Architecture

The web layer is the design. The native plugin is the capability layer. They talk through
`window.FPNative`, defined in a `<script>` injected into `www/index.html`.

| Feature | Where it lives |
|---|---|
| All UI, screens, state | `www/index.html` |
| Printer discovery (mDNS) | `FastPrintPlugin.startScan` — NsdManager |
| Printing | `FastPrintPlugin.printDirect` — IPP or raw 9100 |
| PDF composition | `FPNative.print` in JS — JPEG embedded via DCTDecode |
| Update check / download / install | `FastPrintPlugin` + `InstallReceiver` |
| Google Sign-In | `FastPrintPlugin.googleSignIn` — Credential Manager |

## Hard-won facts

- **Printing must not open the system dialog.** We send IPP straight to the printer. Do not
  reintroduce `PrintManager`.
- **`android/` must stay tracked in git.** The original `.gitignore` excluded it, which made CI
  regenerate a bare project and silently drop the plugin, permissions, icons and signing.
- **A release build with no signing config produces an *unsigned* APK**, which Android rejects
  with "package appears to be invalid". Debug builds are auto-signed; release builds are not.
  CI verifies with `apksigner` before publishing.
- **Every APK must be signed with the same key** or in-app updates are rejected and Google
  Sign-In fails. Registered SHA-1: `52:6F:D2:C7:57:17:13:81:66:A6:C0:60:95:DB:0D:B0:DC:30:FB:13`.
- **Credential Manager needs the Web client ID**, not the Android one. Passing the Android ID
  fails instantly and looks like a code bug.
- Local Gradle builds do not work on the author's laptop — Java cannot open a loopback socket.
  Build through GitHub Actions.

## Build

CI only. Push to `main` → debug APK artifact. Push a tag `v1.2.3` → signed release published to
GitHub Releases, which is what the in-app updater reads.

## Conventions

See `.claude/rules/`. The short version: no comments in code, APK named
`FastPrint-<version>.apk`, never commit keystores or `keystore.properties`.
