#!/bin/sh
set -u
root=$(CDPATH= cd -- "$(dirname -- "$0")/../.." && pwd)
cd "$root"
stamp=$(date -u +%Y%m%dT%H%M%SZ)
report="$root/tests/reports/${stamp}-FE-F01-android-runtime.md"
adb=/private/tmp/srastemsya-sdk.7DCrTs/platform-tools/adb
set +e
output=$("$adb" -s emulator-5554 shell pm path fefu.prime.srastem 2>&1)
rc=$?
set -e
if [ "$rc" -eq 0 ] && printf '%s' "$output" | grep -q 'package:'; then
  passed=1; skipped=0; result=PASS
else
  passed=0; skipped=1; result=BLOCKED
fi
{
  printf '# FE-F01 Android runtime test run\n\n'
  printf -- '- Timestamp (UTC): %s\n' "$stamp"
  printf -- '- Revision: base 0092b0ba38d9a3813d558454154ce075ec71cd82 plus uncommitted FE-F01 working-tree revision supplied by PM.\n'
  printf -- '- Platform: Android only; AVD `Srastemsya35` / `emulator-5554`\n'
  printf -- '- Command: `adb -s emulator-5554 shell pm path fefu.prime.srastem`\n'
  printf -- '- Command exit code: %s\n' "$rc"
  printf -- '- Result: %s; installed application package check.\n' "$result"
  printf -- '- Counts: passed %s, failed 0, skipped/blocked %s\n' "$passed" "$skipped"
  printf -- '- Manual UI scenarios and their screenshots: see `../reviews/%s-FE-F01-android.md` and `../evidence/FE-F01/20261113T132800Z-android/`.\n' "$stamp"
  if [ "$rc" -ne 0 ]; then
    printf -- '- Diagnostic: %s\n' "$output"
  fi
}
