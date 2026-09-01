# Fast Print — setup

Kotlin + Jetpack Compose Android app. `applicationId` is `com.notzyvex.fastprint`, minSdk 26,
compile/target SDK 35, JDK 17.

---

## 1. Open and build

1. Open the project folder in **Android Studio** (Ladybug or newer — it needs AGP 8.7).
2. Let it sync. Android Studio writes `sdk.dir` into `local.properties` for you.
3. **Run** on a device or emulator, or **Build → Build Bundle(s)/APK(s) → Build APK(s)**.

Command line, once the SDK is installed:

```bash
./gradlew assembleDebug
```

The APK lands in `app/build/outputs/apk/debug/`.

### Building without a local SDK

`.github/workflows/android.yml` builds a debug APK on every push and uploads it as a workflow
artifact called `fast-print-debug-apk`. Push the branch, open the run in the Actions tab, and
download it from there. This is the path to use if you don't have the Android SDK installed
locally.

---

## 2. Change the package name

If `com.notzyvex.fastprint` isn't what you want, it appears in three places and they must match
the OAuth client you register:

- `app/build.gradle.kts` → `namespace` and `defaultConfig.applicationId`
- the `package` line at the top of every file under `app/src/main/java/…`
- the directory path itself

Android Studio's **Refactor → Rename** on the package handles all three.

---

## 3. Google Sign-In

Sign-in uses **Credential Manager** with the Sign in with Google option. Two things are needed:
an OAuth *Android* client (which binds your package + signing certificate) and an OAuth *Web*
client (whose ID the app actually passes as `serverClientId`).

### 3a. Get your signing SHA-1

Debug keystore (every dev machine has one after the first build):

```bash
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

On Windows the keystore is at `%USERPROFILE%\.android\debug.keystore`.

For release, use the SHA-1 of your upload/release keystore. If you enable **Play App Signing**,
also register the SHA-1 Google Play shows you under *Release → Setup → App signing* — that's the
certificate that actually signs what users install.

### 3b. Create the OAuth clients

In the [Google Cloud console](https://console.cloud.google.com/apis/credentials):

1. Configure the **OAuth consent screen** if you haven't (External, add your email as a test user).
2. **Create credentials → OAuth client ID → Android**
   - Package name: `com.notzyvex.fastprint` (must equal `applicationId`)
   - SHA-1: the debug fingerprint from 3a. Add a second Android client for the release/Play
     fingerprint when you ship.
3. **Create credentials → OAuth client ID → Web application**
   - No redirect URIs needed. Copy its **Client ID**.

> The Android client makes Google trust your app. The **Web** client ID is the one the code
> uses. Passing the Android client ID instead is the usual cause of sign-in failing immediately.

### 3c. Put the web client ID in the build

Create `local.properties` in the project root (it is gitignored — see `local.properties.example`):

```properties
GOOGLE_WEB_CLIENT_ID=1234567890-abcdefg.apps.googleusercontent.com
```

It's injected as a string resource at build time; no client ID or secret is committed.

For CI, add the same value as a repository secret named `GOOGLE_WEB_CLIENT_ID`.

**Without it the app still builds and runs** — Google sign-in reports that it isn't configured,
and *Continue as guest* works normally.

---

## 4. Printing and printer discovery — how it actually behaves

This is the part where Android's platform constrains the design, so it's worth being precise.

**What the app does itself**

- Discovers printers on the local network over mDNS/DNS-SD (`NsdManager`), browsing
  `_ipps._tcp`, `_ipp._tcp` and `_pdl-datastream._tcp`. That covers AirPrint, Mopria and most
  legacy network printers. The Printers screen lists what it finds, live.
- Renders the job itself in `PageRenderer` / `FastPrintDocumentAdapter`: exact custom media size
  in inches or centimetres, orientation, margins (none / 8 mm / 18 mm), scale (fit / fill /
  actual-72%), grayscale for Black & white, the uploaded image with its zoom / rotation /
  position, and one page per copy.
- Reports **real** success or failure by polling the `PrintJob` to a terminal state — there is no
  simulated outcome anywhere (the prototype's "Brother Laser always fails" rule is gone).

**What the platform owns**

Android has no public API to preselect a printer, and `PrintJobInfo.getPrinterId()` is a hidden
SystemApi, so an app cannot read back which printer the user picked. Tapping **Print now** hands
off to the system print dialog, and that dialog is the authority on where the job lands. The
Printers screen is therefore the user's own saved preference — it's what gets recorded in
history — while the final choice happens in the system UI.

**Discovery permissions**

- API 33+: `NEARBY_WIFI_DEVICES` (declared `neverForLocation`), requested when the user opens the
  Printers screen.
- API 32 and below: `ACCESS_FINE_LOCATION`, declared with `maxSdkVersion="32"`.

If discovery is denied or finds nothing, printing still works — the system dialog does its own
enumeration.

**Testing it**

Use a Mopria-certified or AirPrint/IPP Wi-Fi printer on the same network and subnet as the
phone. Notes from the field:

- **Emulators can't do this.** Test discovery on a real device on real Wi-Fi.
- Many corporate/guest networks enable **AP isolation** or block multicast, which silently kills
  mDNS. A home network or a phone hotspot is the reliable test.
- Some OEM builds (notably a few Xiaomi/Huawei power-saving modes) throttle multicast when the
  screen is off.
- Printers reachable only over USB or Bluetooth won't appear in discovery, but do appear in the
  system print dialog if a print service supports them.
- If nothing appears, install the vendor's print service (Mopria Print Service, HP Print Service
  Plugin, etc.) from Play — **Add a printer** on the Printers screen opens the system print
  settings for exactly that.

---

## 5. Release build

1. **Build → Generate Signed App Bundle / APK**.
2. Create or select a keystore. Keep it and its passwords safe — losing it means you cannot
   update the app.
3. Register that keystore's SHA-1 as a second Android OAuth client (3b) or Google Sign-In will
   fail in release while working in debug.
4. `isMinifyEnabled` is on for release; `app/proguard-rules.pro` already keeps Room and
   Credential Manager. Smoke-test sign-in and printing on the release build.

---

## 6. Third-party assets and licensing

- **Fonts** — Caprasimo and Figtree, both SIL Open Font License 1.1, bundled in
  `app/src/main/res/font/`.
- **Icons** — Lucide geometry (ISC), redrawn as Compose vectors in `ui/icons/LucideIcons.kt`.
- **Ko-fi badge** — supplied in the handoff, converted from AVIF to WebP because AVIF only
  decodes on API 31+ and this app supports API 26. Links to <https://ko-fi.com/notzyvex>.
- **Sign-in background** — the handoff pointed at a stock video on a third-party CDN. That is not
  something to ship a dependency on, and no licensed clip came with the bundle, so the sign-in
  screen uses the branded gradient hero the spec allows as the alternative. To use a video
  instead, add the Media3 dependency, drop the clip in `app/src/main/res/raw/`, and swap the
  hero `Box` in `SignInScreen.kt` for a `PlayerView`.
- **Google "G"** — the official four-colour mark, drawn from the paths in the handoff, used on a
  white pill per Google's branding requirements.
