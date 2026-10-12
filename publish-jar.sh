#!/usr/bin/env bash
set -euo pipefail

MODULE=":thriftserver-0.17"

if [ -z "${OSSRH_LABS_TOKEN:-}" ]; then
  echo "Missing required env var: OSSRH_LABS_TOKEN" >&2
  exit 1
fi

DECODED="$(printf '%s' "$OSSRH_LABS_TOKEN" | base64 -d)"
export SONATYPE_USERNAME="${DECODED%%:*}"
export SONATYPE_PASSWORD="${DECODED#*:}"

./gradlew "${MODULE}:publish"

curl -X POST \
  'https://ossrh-staging-api.central.sonatype.com/manual/upload/defaultRepository/com.newrelic.labs?publishing_type=user_managed' \
  -H "Authorization: Bearer $OSSRH_LABS_TOKEN" \
  -d ''
