#!/usr/bin/env bash
set -euo pipefail

usage() {
    cat <<EOF
Usage: $(basename "$0") <from-tag> <to-tag> [options]

Compare the Spring Batch builder packages that the Kotlin DSL wraps.
Options are passed through to spring-batch-diff.sh (e.g., --stat, -o <file>, -c <dir>).

Examples:
  $(basename "$0") 6.0.0 6.1.0
  $(basename "$0") 6.0.0 6.1.0 --stat
EOF
}

if [ $# -eq 0 ] || [ "$1" = "-h" ] || [ "$1" = "--help" ]; then
    usage
    exit 0
fi

CORE="spring-batch-core/src/main/java/org/springframework/batch/core"

exec "$(dirname "$0")/spring-batch-diff.sh" "$@" \
    -p "${CORE}/job/builder/**" \
    -p "${CORE}/step/builder/**"
