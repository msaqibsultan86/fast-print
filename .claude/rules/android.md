# Android rules

## Signing
Every APK ships signed with the one keystore whose SHA-1 is
`52:6F:D2:C7:57:17:13:81:66:A6:C0:60:95:DB:0D:B0:DC:30:FB:13`.
A different key breaks in-app updates and Google Sign-In at the same time.

`assembleRelease` with no signing config emits an UNSIGNED apk. Android rejects it with
"package appears to be invalid". Always verify with `apksigner verify` before publishing.

Never commit `keystore.properties`, `*.keystore`, `*.jks` or `client_secret*.json`.

## Google Sign-In
Two OAuth clients, two jobs:
- Android client: package name + SHA-1. Registers trust. Never referenced in code.
- Web client: its ID is the `serverClientId` the code passes. This is the one in `FPNative`.

Passing the Android ID fails instantly and looks like a code bug.

## Printing
Printing goes straight to the printer over IPP (631) or raw JetDirect (9100).
Do not reintroduce `PrintManager` or any system print dialog.

## The android/ directory is tracked
It must stay in git. Excluding it makes CI regenerate a bare Capacitor project, silently
dropping the plugin, permissions, icons and signing config.

## Builds
Local Gradle does not work on this machine: Java cannot open a loopback socket. Use CI.
