# Fast Print — Android APK

This is the Fast Print app wrapped with **Capacitor** so it builds into a real installable
Android APK. The whole app UI lives in `www/index.html` (self-contained — no internet needed,
the sign-in video and all assets are embedded).

- **App name:** Fast Print
- **Package / applicationId:** `com.notzyvex.fastprint`
- **Signing SHA-1 (for Google Sign-In):** `52:6F:D2:C7:57:17:13:81:66:A6:C0:60:95:DB:0D:B0:DC:30:FB:13`

---

## Easiest: let GitHub build the APK for you (no PC setup)

1. Create a new GitHub repo and upload **all** of these files (keep the folder structure, including
   the hidden `.github/` folder).
2. Push to the `main` branch. The included workflow (`.github/workflows/build-apk.yml`) runs
   automatically.
3. Open the repo's **Actions** tab → the latest run → scroll to **Artifacts** →
   download **`fast-print-debug-apk`**. Unzip it to get `app-debug.apk`.
4. Copy the APK to your phone and install it (enable "Install unknown apps" for your file manager).

To trigger a build manually: Actions tab → "Build Android APK" → **Run workflow**.

---

## Build locally instead

Requirements: Node 18+, JDK 21, Android SDK (Android Studio).

```bash
npm install
npx cap add android
npx cap sync android
cd android
./gradlew assembleDebug        # debug APK
# APK -> android/app/build/outputs/apk/debug/app-debug.apk
```

Open in Android Studio to run on a device or build a **signed release** APK
(Build → Generate Signed Bundle / APK).

---

## Updating the app UI later

Replace `www/index.html` with a newer export, then re-run `npx cap sync android` (locally) or just
push to GitHub (the Action re-syncs automatically).

---

## Notes on the "real" native features

The UI, flows, image editor, history, settings and update screens all work as-is inside the app.
Two capabilities are **native** and are stubbed in the web layer — wire them with Capacitor plugins
when you want the hardware behaviour:

- **Real Wi-Fi printer discovery + printing** — add Android `PrintManager` / a Mopria(IPP) + mDSN
  (NsdManager) bridge, or a Capacitor plugin, and call it from the Print screen. The in-app scan and
  print animation are already the UX around it.
- **Google Sign-In** — use the Credential Manager / Google Identity plugin with the package name and
  SHA-1 above; feed the real name/email/photo into the profile (currently placeholder).

The design handoff (`design_handoff_fast_print/`) has the full component + behaviour spec for these.
