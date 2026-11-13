# FE-F03 acceptance criteria

Revision base: `50af9d72c70dc827ad798056e3e516799362aee5` (FE-F02). Scope: Android only. The user-specified feed page size is 10; other behavior follows the referenced contracts/spec. No iOS acceptance is required.

## AC-01 — Single-device first LIKE result

On the Android single-device feed, the first LIKE immediately leaves swipe mode and shows a full-screen chosen result with the selected item’s cover, title, parameters, and description. The action label matches kind (place/event); opening it fetches and displays details from the full catalog DTO.

Sources: `workflow/specs/feed-v1/FE-F03.md`; `docs/specs/feed-v1/implementation.md` § FE-F03; `docs/specs/feed-v1/acceptance.md` F03-01; `docs/contracts/discovery-v1.md` “Swipes on one device” and catalog detail endpoints.

## AC-02 — Automatic next page while candidates remain

The Android UI presents candidates in local pools of 10 and resets the counter at each pool. When the current server response has no queued candidates left but has a `nextCursor`, the app automatically requests the next API page with the existing filters and cursor. During that request, swipe actions are unavailable and the refresh placeholder communicates “Обновляем подборку” and “Ищем новые варианты по вашим фильтрам”; when candidates arrive, a new 10-card pool starts at its first item. The server may return up to 20 candidates per API page; the app can present those as two local pools of 10 before requesting another page.

Sources: `workflow/specs/feed-v1/FE-F03.md`; `docs/specs/feed-v1/implementation.md` § FE-F03; `docs/specs/feed-v1/acceptance.md` F03-02; `docs/contracts/discovery-v1.md` “Swipes on one device”.

## AC-03 — Exhausted feed recovery

When all candidates are skipped and the API returns no next cursor, Android shows “Больше вариантов нет” and an “Изменить фильтры” action. The action returns to filters in the current mode/type with existing values retained.

Sources: `workflow/specs/feed-v1/FE-F03.md`; `docs/specs/feed-v1/implementation.md` § FE-F03 and FE-F06; `docs/specs/feed-v1/acceptance.md` F03-03; `docs/contracts/discovery-v1.md` “Swipes on one device”.

## AC-04 — In-session continuity

Rotation and backgrounding/restoring the Android app during a page retain the current candidate, decisions, and available undo history. Process-death persistence is not required.

Sources: `workflow/specs/feed-v1/FE-F03.md`; `docs/specs/feed-v1/implementation.md` § FE-F03; `docs/specs/feed-v1/acceptance.md` F03-04.

## AC-05 — Boundaries and design

Implementation remains Android-only and follows the feature’s domain/data/presentation boundaries, UI Kit and theme tokens, and feature documentation requirements. Visual comparison should reasonably follow Page 2 in the supplied Figma link; pixel-level matching is not required.

Sources: `workflow/specs/feed-v1/FE-F03.md`; `workflow/rules/mobile-architecture.md`; `workflow/rules/feature-docs.md`; `workflow/rules/design.md`; supplied Page 2 Figma reference.
