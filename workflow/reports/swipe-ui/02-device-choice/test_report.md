# Screen 02 device acceptance

- Revision: `729ebac999a3e5e73beb3c82a71ddb2376ffae0b` (supplied candidate)
- Build: `androidApp/build/outputs/apk/debug/androidApp-debug.apk`
- APK SHA-256: `ab022ed7d1feec67902b50421c8c251eec45610750b616600fa4a48ef08d2666` (matched supplied hash)
- Device: `kn7lsossiznrxgvw`
- Result: **BLOCKED** — no UI actions were performed.

## Acceptance mapping

| Criterion | Result | Evidence |
|---|---|---|
| AC-01 layout, hierarchy, insets, no tab bar | BLOCKED | Could not connect to Android Debug Bridge/device; no screenshot. |
| AC-02 equal-width cards, readable descriptions, full-card taps | BLOCKED | Could not inspect or interact with the screen. |
| AC-03 back returns to «Открытия» | BLOCKED | No UI action performed. |
| AC-04 «На одном устройстве» opens screen 03 | BLOCKED | No UI action performed. |
| AC-05 «На нескольких устройствах» opens screen 10 | BLOCKED | No UI action performed. |
| AC-06 screen 10 inherits «События» mode | BLOCKED | No UI action performed. |

## Reproduction / environment

Ran the supplied ADB executable with `adb devices -l`. The ADB server failed to start: `could not install *smartsocket* listener: Operation not permitted` and `cannot connect to daemon`. The phone and authenticated app session therefore could not be checked. App data was not cleared, app was not uninstalled, and no install or UI action was attempted. APK hash verification succeeded.

No initial or transition screenshots/video could be recorded because the device was inaccessible.
