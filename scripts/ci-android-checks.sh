#!/usr/bin/env bash
# Keep partial downloads on the same runner; never upload after failed checks.
set -euo pipefail
log=$(mktemp)
trap 'rm -f "$log"' EXIT
for attempt in 1 2 3; do
    set +e
    ./gradlew --no-daemon assembleDebug testDebugUnitTest lintDebug 2>&1 | tee "$log"
    statuses=("${PIPESTATUS[@]}")
    set -e
    (( statuses[1] == 0 )) || exit "${statuses[1]}"
    (( statuses[0] != 0 )) || exit 0
    if (( attempt == 3 )) || ! python3 - "$log" <<'PY'
import pathlib, re, sys
text = pathlib.Path(sys.argv[1]).read_text()
network = r'(Received status code (429|5\d\d)|Server returned HTTP response code: (429|5\d\d)|Read timed out|Connect timed out|Connection reset)'
checks = r'(There were failing tests|Lint found|Compilation error|tests completed, [1-9]\d* failed|^e: file:)'
sys.exit(0 if re.search(network, text) and not re.search(checks, text, re.M) else 1)
PY
    then
        exit "${statuses[0]}"
    fi
    printf 'Transient dependency network failure; retry %s/3 on the same runner.\n' "$((attempt + 1))"
    sleep 10
done
