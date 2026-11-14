#!/bin/sh
set -u

ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/../.." && pwd)
cd "$ROOT"
STAMP=$(date '+%Y%m%d-%H%M%S')
REPORT_DIR="$ROOT/tests/fe-f00-blackbox/tests/reports"
mkdir -p "$REPORT_DIR"
REPORT="$REPORT_DIR/$STAMP.md"
TMP_ANDROID=$(mktemp)
TMP_IOS=$(mktemp)

ANDROID_CMD='./gradlew :androidApp:assembleDebug'
IOS_CMD="xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' -derivedDataPath build/ios-derived build"

./gradlew :androidApp:assembleDebug >"$TMP_ANDROID" 2>&1
ANDROID_EXIT=$?
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' -derivedDataPath build/ios-derived build >"$TMP_IOS" 2>&1
IOS_EXIT=$?

PASSED=0
FAILED=0
SKIPPED=0
if [ "$ANDROID_EXIT" -eq 0 ]; then PASSED=$((PASSED + 1)); else FAILED=$((FAILED + 1)); fi
if [ "$IOS_EXIT" -eq 0 ]; then PASSED=$((PASSED + 1)); else FAILED=$((FAILED + 1)); fi

{
  printf '# FE-F00 black-box build run\n\n'
  printf -- '- Timestamp: %s\n' "$STAMP"
  printf -- '- Revision: %s\n' "$(git rev-parse HEAD)"
  printf -- '- Android command: `%s` — exit %s\n' "$ANDROID_CMD" "$ANDROID_EXIT"
  printf -- '- iOS command: `%s` — exit %s\n' "$IOS_CMD" "$IOS_EXIT"
  printf -- '- Passed: %s; failed: %s; skipped: %s\n' "$PASSED" "$FAILED" "$SKIPPED"
  printf -- '- App-level UI checks are recorded separately in `workflow/reports/FE-F00/test_report.md`; build failures alone do not establish product defects.\n'
} >"$REPORT"
rm -f "$TMP_ANDROID" "$TMP_IOS"
printf '%s\n' "$REPORT"
printf 'Android exit=%s; iOS exit=%s; passed=%s failed=%s skipped=%s\n' "$ANDROID_EXIT" "$IOS_EXIT" "$PASSED" "$FAILED" "$SKIPPED"
if [ "$FAILED" -gt 0 ]; then exit 1; fi
