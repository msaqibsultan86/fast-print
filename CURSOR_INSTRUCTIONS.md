# Cursor AI — build the Fast Print Android APK from this project

You are picking up **Fast Print**, a mobile printing app. The complete, working UI is already built
as a single self-contained web file at `www/index.html` (all screens, image editor, print flow,
history, settings, update flow — plus the sign-in video and every asset embedded inline, no network
needed). This repo wraps it with **Capacitor** to produce a real installable Android APK.

## Your job
1. Produce a working **debug APK** the user can install on their phone.
2. Then wire the two **native-only** features that are currently UX stubs in the web layer:
   - **Real Wi-Fi printer discovery + printing** — Android `PrintManager`, plus Mopria (IPP) +
     mDNS/`NsdManager` (or a Capacitor plugin) to find printers on the local network and print with
     the chosen size / orientation / copies / color / margins / scale. The in-app scan + print
     animation is the UX around it — replace the simulated result with the real job result.
   - **Google Sign-In** — Credential Manager / Google Identity. Populate the profile with the real
     name, email and photo (currently placeholder "Alex Rivera").

## App identity (already set — keep consistent)
- appName: **Fast Print**
- appId / applicationId / package: **com.notzyvex.fastprint** (in `capacitor.config.json`)
- OAuth Android client signing **SHA-1**: `52:6F:D2:C7:57:17:13:81:66:A6:C0:60:95:DB:0D:B0:DC:30:FB:13`
  (add the release-cert SHA-1 as a second Android OAuth client before shipping)

## Build steps
```bash
npm install
npx cap add android
npx cap sync android
cd android
./gradlew assembleDebug
# APK -> android/app/build/outputs/apk/debug/app-debug.apk
```
Requirements: Node 18+, JDK 21, Android SDK. Open `android/` in Android Studio for a signed release
build (Build → Generate Signed Bundle / APK).

## A GitHub Actions workflow is already included
`.github/workflows/build-apk.yml` builds the debug APK on every push to `main` and on manual run
(Actions tab → Run workflow). Download it from the run's **Artifacts** → `fast-print-debug-apk`.
So the user can also just push and let CI build it — no local Android setup required.

## Updating the UI later
Replace `www/index.html` with a newer export and run `npx cap sync android` (or push — CI re-syncs).

## Don't
- Don't rebuild the UI from scratch — `www/index.html` IS the design; wrap it, don't replace it.
- Don't change the package name or you'll break the Google OAuth client mapping.
