#!/usr/bin/env bash
# Suite: shared (KMP commonTest unit tests: feed domain use cases + FeedModeViewModel).
# Runs only this unit and writes one unique timestamped report per invocation.
set -uo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
REPORT_DIR="$ROOT/tests/reports"
mkdir -p "$REPORT_DIR"
STAMP="$(date +%Y%m%d-%H%M%S)"
REPORT="$REPORT_DIR/$STAMP-shared.md"

cd "$ROOT" || exit 1

# Drop previous results so the invocation always produces a fresh run, not UP-TO-DATE.
rm -rf shared/build/test-results

CMD="./gradlew :shared:testAndroid :shared:iosSimulatorArm64Test --console=plain --no-build-cache"
echo "running: $CMD"
OUTPUT="$($CMD 2>&1)"
CODE=$?

COUNTS="$(find shared/build/test-results -name 'TEST-*.xml' -print0 2>/dev/null | xargs -0 python3 -c '
import sys, xml.etree.ElementTree as ET
tests = failures = skipped = 0
for path in sys.argv[1:]:
    try:
        root = ET.parse(path).getroot()
    except Exception:
        continue
    tests += int(root.get("tests", 0) or 0)
    failures += int(root.get("failures", 0) or 0) + int(root.get("errors", 0) or 0)
    skipped += int(root.get("skipped", 0) or 0)
print(f"{tests} {failures} {skipped}")
' 2>/dev/null)"
read -r PASSED_TOTAL FAILED SKIPPED <<<"${COUNTS:-0 0 0}"
PASSED=$((PASSED_TOTAL - FAILED))

PER_TARGET="$(python3 - "$ROOT" <<'PY'
import glob, os, sys, xml.etree.ElementTree as ET
root = sys.argv[1]
for directory in sorted(glob.glob(os.path.join(root, "shared/build/test-results/*"))):
    tests = failures = skipped = 0
    for path in glob.glob(os.path.join(directory, "TEST-*.xml")):
        try:
            element = ET.parse(path).getroot()
        except Exception:
            continue
        tests += int(element.get("tests", 0) or 0)
        failures += int(element.get("failures", 0) or 0) + int(element.get("errors", 0) or 0)
        skipped += int(element.get("skipped", 0) or 0)
    print(f"- target {os.path.basename(directory)}: passed={tests - failures}, failed={failures}, skipped={skipped}")
PY
)"

{
  echo "# Suite report: shared unit tests"
  echo
  echo "- stamp: $STAMP"
  echo "- unit: shared/src/commonTest (KMP unit tests: Android host JVM + iOS Simulator ARM64)"
  echo "- command: \`$CMD\`"
  echo "- exit code: $CODE"
  echo "- passed: $PASSED"
  echo "- failed: $FAILED"
  echo "- skipped: $SKIPPED"
  echo
  echo '## per target'
  echo "$PER_TARGET"
  echo
  echo '## tail of command output'
  echo '```'
  echo "$OUTPUT" | tail -25
  echo '```'
} > "$REPORT"

echo "report: $REPORT"
exit "$CODE"
