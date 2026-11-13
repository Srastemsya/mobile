# FE-F04 targeted Android recheck

**Outcome: AC-07 PASS; AC-08 PASS.** This is a focused recheck of the visual and documentation fixes. It does not replace or modify the original [FE-F04 test report](../test_report.md).

## Build and scope

- Base revision per updated packet: `4a290c7bd764e03113b940965f0cb8875fa5d2a9`, with uncommitted recheck remediation.
- APK: `androidApp/build/outputs/apk/debug/androidApp-debug.apk`.
- SHA-256 verified before install and after checks: `7180aa5aa8a5c7e2e1e64443a3e169037ed3fb260c459ad7a2ae8751f6cc8b3a`.
- Installed successfully on `emulator-5554` and `emulator-5556`; visual checks used the specified app on Android only.
- Run window: 2026-11-13 17:42–17:46 UTC (2026-11-14 local emulator time). Manual commands and evidence list: [run record](evidence/20261113T1742Z-manual-recheck.md).

## AC-07 — PASS

Compared the updated app with Figma nodes [280-10707](https://www.figma.com/design/khewsPf7b4JVyBwJaCmO43/startap_design?node-id=280-10707), [280-10728](https://www.figma.com/design/khewsPf7b4JVyBwJaCmO43/startap_design?node-id=280-10728), and [280-10751](https://www.figma.com/design/khewsPf7b4JVyBwJaCmO43/startap_design?node-id=280-10751).

- Creation screen now uses the reference CTA label “Создать комнату”; title, helper text, name field, and button placement follow the frame. See [Android creation screen](evidence/android-03-create.png).
- The organizer lobby now contains the room name, helper text, avatar group, connected-member count, and an inline invite card. The raw invite URL is not shown. See [Android invite lobby](evidence/android-04-invite-lobby.png).
- Copying changes the inline card to a checkmark and “Ссылка скопирована” confirmation, without a copy button, matching the compact copied state. See [Android copied state](evidence/android-05-copied-state.png).
- The current feature documentation identifies `SrRoomInvite`, `SrAvatarGroup`, and UI Kit tokens. Screenshot review confirms the visible style and layout; component-instance reuse itself is not externally observable in a black-box run.

## AC-08 — PASS

- The “Экраны и состояния” multi-device row in `docs/design/feed.md` now links FE-F04 to nodes `280-10707`, `280-10728`, and `280-10751`.
- The FE-F04 design section documents the inline invite card, avatar group, hidden raw URL, compact copied state, member waiting state, and room filters without a party-size field.
- `shared/src/commonMain/kotlin/fefu/prime/srastem/feed/FEATURE.md` documents Android FE-F04 room creation, invite, `SrRoomInvite`, `SrAvatarGroup`, live membership, hidden party-size, and start behavior.

## Preserved limits

The prior report remains authoritative for the rest of the package. AC-04 and AC-05 remain BLOCKED for live post-TTL behavior because the user excluded waiting for the 30-minute TTL; the prior synthetic invalid-token result is not expiry evidence. AC-06 remains BLOCKED for retry idempotency-key reuse and long-poll cancellation, which are not observable through the tested UI surface. This recheck did not attempt room swipes or voting.
