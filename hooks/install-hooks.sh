#!/usr/bin/env bash
# Installs the pre-commit hook for this repository.
# Run once after cloning: bash hooks/install-hooks.sh

set -e

GIT_DIR="$(git rev-parse --git-dir)"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

if [ "$(cd "$GIT_DIR/.." && pwd)" != "$(cd "$SCRIPT_DIR/.." && pwd)" ]; then
    echo "Refusing to install: \$(git rev-parse --git-dir) resolves outside this module"
    echo "  ($GIT_DIR)"
    exit 1
fi

HOOKS_DIR="$GIT_DIR/hooks"

cp "$SCRIPT_DIR/pre-commit" "$HOOKS_DIR/pre-commit"
chmod +x "$HOOKS_DIR/pre-commit"

echo "Pre-commit hook installed successfully."
echo "It will run automatically on every git commit in this repository."
