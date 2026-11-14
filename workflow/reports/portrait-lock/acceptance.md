# Acceptance — portrait orientation lock

Scope: platform orientation configuration for the mobile app on Android and iOS. Request: keep the app in portrait orientation. Avoid added UI logic.

- AC-01: Android app declares portrait-only orientation through platform configuration; rotating the device to landscape does not rotate the app UI.
- AC-02: iOS app declares portrait-only supported orientation through platform configuration; rotating the device to landscape does not rotate the app UI.
- AC-03: The change is limited to platform orientation settings; app UI and navigation behavior are unchanged.
- AC-04: Applicable Android and iOS builds succeed, and the Developer records the platform configuration paths and verification evidence.

Exercise portrait and landscape rotation on each available platform/device. If an iOS build/device is unavailable, report that platform check as BLOCKED rather than PASS.
