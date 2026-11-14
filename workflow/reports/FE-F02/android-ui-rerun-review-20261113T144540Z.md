# FE-F02 Android UI regression review — 2026-11-13

The API 35 emulator accepted the rebuilt APK, and ADB screenshots/UI hierarchy provided black-box evidence. Rotation now stays on the swipe screen at 2/20; Undo remains available and returns to 1/20. After returning to portrait, a different Like decision succeeds. Background/foreground within the same process also retains 2/20 and Undo history.

This closes the previously observed rotation loss for the tested single-device route. It does not establish process-death recovery. Landscape did preserve the card index and undo history, but the active card image was oversized/cropped and card text did not appear in the hierarchy while landscape. The tester did not inspect source.

Coverage still has material gaps: room context unavailable, directional transition not established by endpoint screenshots, next-pool loading boundary not exercised, and full metadata/description completeness not confirmed. Overall complete acceptance remains BLOCKED/incomplete until those mandatory portions are verified. No implementation failure was observed in this rerun.
