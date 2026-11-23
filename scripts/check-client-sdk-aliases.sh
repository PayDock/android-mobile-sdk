#!/usr/bin/env bash

# Client SDK alias check (release gate).
#
# Without a pin, the Mobile SDK's widgets load the supported Client SDK line's stable alias in production and sandbox
# and its candidate alias in staging (ClientSDKConstants.SUPPORTED_LINE). A release must not be published while the
# stable alias is missing, so this script checks, for the stable alias of the line (staging is internal, so the
# candidate alias does not gate a release):
#
#   script  <alias>/widget.umd.min.js  status 200 and a JavaScript content type
#   record  <alias>/version.txt        status 200, content type text/plain and one line naming a release of the line
#
# The content types matter: the CDN answers a path that was never published with 200 and an HTML page (text/html),
# so a 200 status alone never proves an alias exists.
#
# The line is read from the Kotlin source (SUPPORTED_LINE in ClientSDKConstants.kt); there is no second copy.
# Each request is retried 3 times, 5 s apart, on transient network errors only; a wrong content type fails at once.
#
# Usage: scripts/check-client-sdk-aliases.sh
# Exit codes: 0 both checks pass; 1 otherwise, printing each failing URL with its status and content type.
# Self-test (stubbed curl, no network): scripts/check-client-sdk-aliases-test.sh

set -uo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
CONSTANTS_FILE="$PROJECT_ROOT/mobile-sdk/src/main/kotlin/com/paydock/core/ClientSDKConstants.kt"

# Same address scheme as ClientSDKConstants.libraryUrl(version).
CDN_BASE_URL="https://widget.paydock.com/sdk"
ALIASES=("stable")
JAVASCRIPT_CONTENT_TYPE='^(text|application)/(x-)?javascript'
TEXT_CONTENT_TYPE='^text/plain([[:space:]]*;|$)'

SUPPORTED_LINE="$(grep -E '^[[:space:]]*internal const val SUPPORTED_LINE[[:space:]]*=' "$CONSTANTS_FILE" 2>/dev/null |
    head -n 1 | sed -E 's/.*"([^"]*)".*/\1/')"
if [[ ! "$SUPPORTED_LINE" =~ ^v[0-9]+\.[0-9]+$ ]]; then
    echo "ERROR: could not read the supported line (vX.Y) from SUPPORTED_LINE in $CONSTANTS_FILE (got '$SUPPORTED_LINE')" >&2
    exit 1
fi
# The record names one release of the line, then an optional newline: ^v1\.150\.\d+\n?$
RECORD_PATTERN="^${SUPPORTED_LINE//./\\.}\\.[0-9]+"$'\n'"?$"

echo "Supported Client SDK line: $SUPPORTED_LINE"

BODY_FILE="$(mktemp)"
trap 'rm -f "$BODY_FILE"' EXIT

failures=0

# fetch <url>: sets STATUS, CONTENT_TYPE (lowercase) and BODY_FILE for the response.
fetch() {
    local response
    : > "$BODY_FILE"
    response="$(curl --silent --max-time 30 --retry 3 --retry-delay 5 \
        --output "$BODY_FILE" --write-out '%{http_code} %{content_type}' "$1")"
    STATUS="${response%% *}"
    CONTENT_TYPE="$(printf '%s' "${response#* }" | tr '[:upper:]' '[:lower:]')"
    [[ "$response" == *" "* ]] || CONTENT_TYPE=""
    [[ "$STATUS" =~ ^[0-9]{3}$ && "$STATUS" != "000" ]] || STATUS="no response"
}

fail() {
    echo "FAIL: $1 answered status $STATUS, content type '${CONTENT_TYPE:-none}': $2" >&2
    failures=$((failures + 1))
}

for alias in "${ALIASES[@]}"; do
    alias_url="$CDN_BASE_URL/$SUPPORTED_LINE.$alias"

    url="$alias_url/widget.umd.min.js"
    fetch "$url"
    if [[ "$STATUS" == "200" && "$CONTENT_TYPE" =~ $JAVASCRIPT_CONTENT_TYPE ]]; then
        echo "OK: $url answered 200 ($CONTENT_TYPE)"
    else
        fail "$url" "expected 200 with a JavaScript content type"
    fi

    url="$alias_url/version.txt"
    fetch "$url"
    body="$(cat "$BODY_FILE"; printf x)"
    body="${body%x}"
    if [[ "$STATUS" == "200" && "$CONTENT_TYPE" =~ $TEXT_CONTENT_TYPE && "$body" =~ $RECORD_PATTERN ]]; then
        echo "OK: $url answered 200 ($CONTENT_TYPE): ${body%$'\n'}"
    else
        fail "$url" "expected 200 with text/plain naming one release of $SUPPORTED_LINE (vX.Y.Z)"
    fi
done

if [[ "$failures" -ne 0 ]]; then
    echo "" >&2
    echo "The $SUPPORTED_LINE stable alias must be promoted before this Mobile SDK is released." >&2
    echo "Promote a $SUPPORTED_LINE release to stable in the client SDK pipeline, then re-run this job." >&2
    exit 1
fi

echo "The $SUPPORTED_LINE stable alias is promoted."
