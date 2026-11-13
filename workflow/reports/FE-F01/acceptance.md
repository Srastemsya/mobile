# FE-F01 acceptance criteria

Scope: Android only per dispatcher instruction; iOS excluded. FE-F01 filter flow in single-device mode. Test against the configured discovery-v1 staging service; backend behavior is outside implementation scope.

- AC-01 (F01-01): From single-device entry, user can choose exactly one kind (PLACE/EVENT), category from server categories valid for that kind or “All” (no categoryId), optional date for EVENT only, budget and party size. Submitting valid filters opens a feed matching filters. Sources: `docs/specs/feed-v1/acceptance.md` F01-01; `docs/specs/feed-v1/implementation.md` FE-F01; `docs/contracts/discovery-v1.md` BE-02/04.
- AC-02 (F01-01): Invalid negative/non-integer budget or party size below 1 when budget is supplied blocks submission and shows a field-associated error. Sources: same, plus `docs/specs/feed-v1/implementation.md` validation paragraph.
- AC-03 (F01-02): From swipe screen, “Change filters” opens filter screen in current single-device mode with current values restored; changing a filter and submitting starts a fresh feed request with updated filters. Sources: `docs/specs/feed-v1/acceptance.md` F01-02; `docs/specs/feed-v1/implementation.md` FE-F01/F02.
- AC-04: Screen presents “What are we looking for?” with single-device context, uses UI Kit components/tokens, and hides/disables date for PLACE. Source: `docs/specs/feed-v1/implementation.md` FE-F01 and shared UI Kit requirements.
- AC-05: Feature documentation updated to match implemented behavior and API boundary. Sources: `workflow/rules/feature-docs.md`; `workflow/rules/mobile-architecture.md`.

Platform restriction (task instruction): validate Android only; no iOS work or acceptance claim.
