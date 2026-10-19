#!/usr/bin/env bash
# Verifies the thriftserver-0.17 artifact Gradle built/published is correct:
# checks the local build output (identical to what gets uploaded) against the
# group/version Gradle itself reports (no hardcoded duplicate truth), and,
# if a deployment ID is passed as $1 (shown in the Sonatype Central portal
# after publish-jar.sh runs), checks its state via the Central Portal's
# documented status API: POST /api/v1/publisher/status?id={deploymentId}
# (see https://central.sonatype.org/publish/publish-portal-api/). There is
# no documented endpoint for downloading a staged deployment's raw jar before
# it's actually published, so this only confirms deployment state, not bytes.
set -euo pipefail

DEPLOYMENT_ID="${1:-}"
MODULE=":thriftserver-0.17"
MODULE_DIR="thriftserver-0.17"
ARTIFACT="thriftserver-0.17"

PROPS=$(./gradlew "${MODULE}:properties" -q)
GROUP=$(echo "$PROPS" | awk -F': ' '/^group:/ {print $2}')
VERSION=$(echo "$PROPS" | awk -F': ' '/^version:/ {print $2}')

echo "Verifying ${GROUP}:${ARTIFACT}:${VERSION}"
FAIL=0

JAR="${MODULE_DIR}/build/libs/${ARTIFACT}-${VERSION}.jar"
SOURCES_JAR="${MODULE_DIR}/build/libs/${ARTIFACT}-${VERSION}-sources.jar"
JAVADOC_JAR="${MODULE_DIR}/build/libs/${ARTIFACT}-${VERSION}-javadoc.jar"
POM="${MODULE_DIR}/build/publications/mavenJava/pom-default.xml"

for f in "$JAR" "$SOURCES_JAR" "$JAVADOC_JAR" "$POM"; do
  if [ ! -f "$f" ]; then
    echo "MISSING: $f (run './gradlew ${MODULE}:publishToMavenLocal' first)" >&2
    FAIL=1
  fi
done
[ "$FAIL" -eq 1 ] && exit 1

echo "--- jar manifest ---"
MANIFEST="$(unzip -p "$JAR" META-INF/MANIFEST.MF | tr -d '\r')"
echo "$MANIFEST"

check_manifest() {
  local key="$1" expected="$2"
  if ! printf '%s\n' "$MANIFEST" | grep -q "^${key}: ${expected}$"; then
    echo "MANIFEST MISMATCH: expected ${key}: ${expected}" >&2
    FAIL=1
  fi
}

check_manifest "Implementation-Version" "$VERSION"
check_manifest "Implementation-Vendor-Id" "$GROUP"

echo "--- pom.xml coordinates ---"
grep -q "<groupId>${GROUP}</groupId>" "$POM" || { echo "POM MISMATCH: groupId" >&2; FAIL=1; }
grep -q "<artifactId>${ARTIFACT}</artifactId>" "$POM" || { echo "POM MISMATCH: artifactId" >&2; FAIL=1; }
grep -q "<version>${VERSION}</version>" "$POM" || { echo "POM MISMATCH: version" >&2; FAIL=1; }
echo "groupId/artifactId/version all present and match."

echo
echo "--- remote deployment status check ---"
if [ -z "$DEPLOYMENT_ID" ]; then
  echo "No deployment ID passed — skipping. Usage: ./verify-release.sh <deploymentId>"
  echo "(deploymentId is shown in the Sonatype Central portal after publish-jar.sh runs)"
elif [ -z "${OSSRH_LABS_TOKEN:-}" ]; then
  echo "OSSRH_LABS_TOKEN not set — skipping remote status check."
else
  RESPONSE=$(curl -s -X POST \
    "https://central.sonatype.com/api/v1/publisher/status?id=${DEPLOYMENT_ID}" \
    -H "Authorization: Bearer ${OSSRH_LABS_TOKEN}")
  echo "$RESPONSE"
  STATE=$(echo "$RESPONSE" | grep -o '"deploymentState"[[:space:]]*:[[:space:]]*"[A-Z]*"' | grep -o '"[A-Z]*"$' | tr -d '"')
  case "$STATE" in
    VALIDATED|PUBLISHED)
      echo "Deployment state: $STATE"
      ;;
    FAILED)
      echo "Deployment state: FAILED" >&2
      FAIL=1
      ;;
    PENDING|VALIDATING|PUBLISHING)
      echo "Deployment state: $STATE (still in progress — rerun in a bit)"
      ;;
    *)
      echo "Could not parse deploymentState from response above." >&2
      ;;
  esac
fi

echo
if [ "$FAIL" -eq 1 ]; then
  echo "VERIFY FAILED"
  exit 1
fi
echo "VERIFY PASSED — ${GROUP}:${ARTIFACT}:${VERSION} looks correct."
