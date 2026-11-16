#!/usr/bin/env bash

# Self-test of scripts/check-client-sdk-aliases.sh with a stubbed curl (no network).
#
# Each case stubs what the CDN answers for the supported line's alias scripts and version records, runs the check and
# compares its exit code with the expected one.
#
# Usage: scripts/check-client-sdk-aliases-test.sh
# Exit codes: 0 every case behaves as expected; 1 otherwise.

set -uo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CHECK="$SCRIPT_DIR/check-client-sdk-aliases.sh"
STUB_DIR="$(mktemp -d)"
trap 'rm -rf "$STUB_DIR"' EXIT

# The stub answers every script request with STUB_SCRIPT_STATUS / STUB_SCRIPT_TYPE and every version record request
# with STUB_RECORD_STATUS / STUB_RECORD_TYPE / STUB_RECORD_BODY. STUB_FAIL_ALIAS (candidate or stable) answers that
# alias's requests with 200 text/html, like the CDN for a path that was never published.
cat > "$STUB_DIR/curl" <<'STUB'
#!/usr/bin/env bash
output=""
url=""
while [[ $# -gt 0 ]]; do
    case "$1" in
        --output) output="$2"; shift 2 ;;
        --write-out|--max-time|--retry|--retry-delay) shift 2 ;;
        -*) shift ;;
        *) url="$1"; shift ;;
    esac
done
status="${STUB_SCRIPT_STATUS}"
type="${STUB_SCRIPT_TYPE}"
body="/* client sdk */"
if [[ "$url" == */version.txt ]]; then
    status="${STUB_RECORD_STATUS}"
    type="${STUB_RECORD_TYPE}"
    body="${STUB_RECORD_BODY}"
fi
if [[ -n "${STUB_FAIL_ALIAS:-}" && "$url" == *".${STUB_FAIL_ALIAS}/"* ]]; then
    status="200"
    type="text/html"
    body="<html></html>"
fi
printf '%s' "$body" > "$output"
printf '%s %s' "$status" "$type"
STUB
chmod +x "$STUB_DIR/curl"

failures=0

# run_case <name> <expected exit code> <VAR=value>...
run_case() {
    local name="$1" expected="$2" actual
    shift 2
    env PATH="$STUB_DIR:$PATH" \
        STUB_SCRIPT_STATUS=200 STUB_SCRIPT_TYPE="application/javascript" \
        STUB_RECORD_STATUS=200 STUB_RECORD_TYPE="text/plain" STUB_RECORD_BODY=$'v1.150.1\n' \
        STUB_FAIL_ALIAS="" \
        "$@" bash "$CHECK" > "$STUB_DIR/out.txt" 2>&1
    actual=$?
    if [[ "$actual" == "$expected" ]]; then
        echo "PASS: $name (exit $actual)"
    else
        echo "FAIL: $name: expected exit $expected, got $actual" >&2
        sed 's/^/    /' "$STUB_DIR/out.txt" >&2
        failures=$((failures + 1))
    fi
}

run_case "JavaScript 200 and a record of the line pass" 0
run_case "text/javascript with a charset passes" 0 STUB_SCRIPT_TYPE="text/javascript; charset=utf-8"
run_case "a record without a trailing newline passes" 0 STUB_RECORD_BODY="v1.150.2"
run_case "text/html 200 for the script fails" 1 STUB_SCRIPT_TYPE="text/html"
run_case "a stable alias that was never promoted fails" 1 STUB_FAIL_ALIAS="stable"
run_case "a missing candidate alias is not checked (widgets never load it)" 0 STUB_FAIL_ALIAS="candidate"
run_case "a missing script fails" 1 STUB_SCRIPT_STATUS=404 STUB_SCRIPT_TYPE="text/html"
run_case "a missing record fails" 1 STUB_RECORD_STATUS=404 STUB_RECORD_TYPE="text/html" STUB_RECORD_BODY="<html></html>"
run_case "another line's record fails" 1 STUB_RECORD_BODY=$'v1.151.0\n'
run_case "a pre-release record fails" 1 STUB_RECORD_BODY=$'v1.150.2-beta\n'
run_case "a record that is not text/plain fails" 1 STUB_RECORD_TYPE="application/octet-stream"
run_case "no response fails" 1 STUB_SCRIPT_STATUS=000 STUB_SCRIPT_TYPE=""

if [[ "$failures" -ne 0 ]]; then
    echo "$failures self-test case(s) failed." >&2
    exit 1
fi
echo "All self-test cases passed."
