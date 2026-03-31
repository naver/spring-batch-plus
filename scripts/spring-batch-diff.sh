#!/usr/bin/env bash
set -euo pipefail

UPSTREAM_URL="https://github.com/spring-projects/spring-batch.git"
DEFAULT_CACHE_DIR="${TMPDIR:-/tmp}/spring-batch-upstream"

# Color support
if [ -t 1 ] && [ -z "${NO_COLOR:-}" ]; then
    BOLD='\033[1m'
    RED='\033[0;31m'
    GREEN='\033[0;32m'
    RESET='\033[0m'
else
    BOLD='' RED='' GREEN='' RESET=''
fi

usage() {
    cat <<EOF
Usage: $(basename "$0") <from-tag> <to-tag> [options]
       $(basename "$0") --list-tags [options]

Compare two Spring Batch version tags and show the diff.

Arguments:
  from-tag              Source version (e.g., 5.2.0)
  to-tag                Target version (e.g., 6.0.0)

Options:
  -s, --stat            Show diffstat summary only
  -p, --path <glob>     Filter to specific path (repeatable)
  -o, --output <file>   Write output to file
  -l, --list-tags       List available release tags and exit
  -c, --cache-dir <dir> Cache directory (default: ${DEFAULT_CACHE_DIR})
  -n, --no-cache        Remove cache after completion
  -h, --help            Show this help

Examples:
  $(basename "$0") 5.2.0 6.0.0
  $(basename "$0") 5.2.0 6.0.0 --stat
  $(basename "$0") 5.2.0 6.0.0 --stat -p 'spring-batch-core/src/main/java/**'
  $(basename "$0") --list-tags
EOF
}

die() {
    echo -e "${RED}Error: $1${RESET}" >&2
    exit 1
}

info() {
    echo -e "${GREEN}$1${RESET}" >&2
}

normalize_tag() {
    local tag="$1"
    # Strip leading 'v' and trailing '.RELEASE' for uniform input
    tag="${tag#v}"
    tag="${tag%.RELEASE}"
    local major="${tag%%.*}"

    if [ "$major" -ge 5 ] 2>/dev/null; then
        # 5.x+: tags use 'v' prefix (e.g., v5.2.0)
        echo "v${tag}"
    else
        # 4.x: try plain first (4.3.x), then .RELEASE suffix (4.2.x and below)
        echo "${tag}"
    fi
}

# Parse arguments
stat_only=false
no_cache=false
list_tags=false
cache_dir="${DEFAULT_CACHE_DIR}"
output_file=""
paths=()
positional=()

while [[ $# -gt 0 ]]; do
    case "$1" in
        -s|--stat)
            stat_only=true
            shift
            ;;
        -p|--path)
            [[ $# -lt 2 ]] && die "--path requires an argument"
            paths+=("$2")
            shift 2
            ;;
        -o|--output)
            [[ $# -lt 2 ]] && die "--output requires an argument"
            output_file="$2"
            shift 2
            ;;
        -l|--list-tags)
            list_tags=true
            shift
            ;;
        -c|--cache-dir)
            [[ $# -lt 2 ]] && die "--cache-dir requires an argument"
            cache_dir="$2"
            shift 2
            ;;
        -n|--no-cache)
            no_cache=true
            shift
            ;;
        -h|--help)
            usage
            exit 0
            ;;
        -*)
            die "Unknown option: $1"
            ;;
        *)
            positional+=("$1")
            shift
            ;;
    esac
done

# Show help when no arguments given
if [ "$list_tags" = false ] && [ ${#positional[@]} -eq 0 ]; then
    usage
    exit 0
fi

# Validate arguments
if [ "$list_tags" = false ]; then
    if [ ${#positional[@]} -lt 2 ]; then
        die "Both <from-tag> and <to-tag> are required. Use --help for usage."
    fi
    from_tag=$(normalize_tag "${positional[0]}")
    to_tag=$(normalize_tag "${positional[1]}")
fi

command -v git >/dev/null 2>&1 || die "git is required but not found"

# Cleanup trap
if [ "$no_cache" = true ]; then
    trap 'rm -rf "${cache_dir}"' EXIT INT TERM
fi

# Clone or update bare repository
repo_dir="${cache_dir}/spring-batch.git"

if [ -d "${repo_dir}" ]; then
    info "Updating cached repository..."
    git -C "${repo_dir}" fetch --tags --prune --quiet || die "Failed to fetch tags. Check network connection."
else
    info "Cloning Spring Batch repository (bare + blobless, ~50MB)..."
    mkdir -p "${cache_dir}"
    git clone --bare --filter=blob:none --quiet "${UPSTREAM_URL}" "${repo_dir}" || die "Failed to clone repository. Check network connection."
fi

# List tags mode
if [ "$list_tags" = true ]; then
    echo -e "${BOLD}Available Spring Batch release tags:${RESET}"
    {
        # 5.x+: v-prefixed tags
        git -C "${repo_dir}" tag --list 'v[0-9]*' --sort=-version:refname \
            | grep -E '^v[0-9]+\.[0-9]+\.[0-9]+$'
        # 4.x: no prefix (4.3.x plain, 4.2.x and below with .RELEASE)
        git -C "${repo_dir}" tag --list '[0-9]*' --sort=-version:refname \
            | grep -E '^[0-9]+\.[0-9]+\.[0-9]+(\.RELEASE)?$'
    }
    exit 0
fi

# Resolve tag, trying .RELEASE suffix fallback for 4.2.x and below
resolve_tag() {
    local tag="$1"
    if git -C "${repo_dir}" rev-parse --verify "refs/tags/${tag}" >/dev/null 2>&1; then
        echo "$tag"
        return 0
    fi
    # Try .RELEASE suffix fallback
    if git -C "${repo_dir}" rev-parse --verify "refs/tags/${tag}.RELEASE" >/dev/null 2>&1; then
        echo "${tag}.RELEASE"
        return 0
    fi
    echo -e "${RED}Error: tag '${tag}' not found${RESET}" >&2
    echo "" >&2
    echo "Similar tags:" >&2
    local version_part="${tag#v}"
    local major="${version_part%%.*}"
    {
        git -C "${repo_dir}" tag --list "v${major}.*" --sort=-version:refname \
            | grep -E '^v[0-9]+\.[0-9]+\.[0-9]+$'
        git -C "${repo_dir}" tag --list "${major}.*" --sort=-version:refname \
            | grep -E '^[0-9]+\.[0-9]+\.[0-9]+(\.RELEASE)?$'
    } | head -10 >&2 || true
    exit 1
}

from_tag=$(resolve_tag "$from_tag")
to_tag=$(resolve_tag "$to_tag")

# Build diff command
diff_args=()
if [ "$stat_only" = true ]; then
    diff_args+=("--stat")
fi
diff_args+=("${from_tag}" "${to_tag}")
if [ ${#paths[@]} -gt 0 ]; then
    diff_args+=("--")
    diff_args+=("${paths[@]}")
fi

# Header
print_header() {
    echo -e "${BOLD}Spring Batch diff: ${from_tag} -> ${to_tag}${RESET}"
    echo "Source: https://github.com/spring-projects/spring-batch"
    if [ ${#paths[@]} -gt 0 ]; then
        echo "Paths: ${paths[*]}"
    fi
    echo ""
}

# Generate diff
if [ -n "$output_file" ]; then
    local_bold="$BOLD" local_reset="$RESET"
    BOLD='' RESET=''
    print_header > "$output_file"
    BOLD="$local_bold" RESET="$local_reset"
    git -C "${repo_dir}" diff "${diff_args[@]}" >> "$output_file"
    info "Output written to ${output_file}"
elif [ -t 1 ]; then
    {
        print_header
        git -C "${repo_dir}" diff --color=always "${diff_args[@]}"
    } | ${PAGER:-less -R}
else
    print_header
    git -C "${repo_dir}" diff "${diff_args[@]}"
fi
