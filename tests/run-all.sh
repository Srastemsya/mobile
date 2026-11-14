#!/usr/bin/env bash
# Aggregates suite exit codes and report paths without replacing suite reports.
set -uo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT" || exit 1

OVERALL=0
for suite in tests/*/run.sh; do
  [ -x "$suite" ] || continue
  echo "== $suite"
  "$suite"
  CODE=$?
  echo "== $suite exit code: $CODE"
  [ "$CODE" -eq 0 ] || OVERALL=1
done

exit "$OVERALL"
