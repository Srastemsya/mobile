# FE-F04 manual visual recheck record

Window: 2026-11-13 17:42–17:46 UTC (2026-11-14 local emulator time). Manual Android UI observations; no `run.sh` suite was invoked and no automated test counts are claimed.

| Action | Result |
|---|---|
| `shasum -a 256 androidApp/build/outputs/apk/debug/androidApp-debug.apk` | exit 0; SHA-256 `7180aa5aa8a5c7e2e1e64443a3e169037ed3fb260c459ad7a2ae8751f6cc8b3a` |
| `adb devices` using `/private/tmp/srastemsya-sdk.7DCrTs/platform-tools/adb` | exit 0; `emulator-5554` and `emulator-5556` online |
| `adb -s emulator-5554 install -r androidApp/build/outputs/apk/debug/androidApp-debug.apk` | exit 0; success |
| Same install command for `emulator-5556` | exit 0; success |
| `adb -s emulator-5554 shell am start -n fefu.prime.srastem/.MainActivity` | activity started; opened Feed mode and room creation |
| Create a room and capture the lobby | succeeded; current screen shows inline invite card, avatar group, member count, and no raw link |
| Tap “Скопировать ссылку”; capture after transient Android clipboard toast disappeared | card changed to compact checkmark/copied confirmation; saved screenshot contains no token or clipboard toast |

Evidence: [starting app](android-01-home.png), [mode selector](android-02-mode-selection.png), [create](android-03-create.png), [invite lobby](android-04-invite-lobby.png), [copied state](android-05-copied-state.png). No credentials or invite tokens are retained.
