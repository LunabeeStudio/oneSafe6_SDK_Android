#!/bin/sh

ARCHIVE_VERSIONS=([0]=1)

ARGUMENT_LIST=(
  "archive-version",
  "a"
)

opts=$(getopt \
  --long "$(printf "%s:," "${ARGUMENT_LIST[@]}")" \
  --name "$(basename "$0")" \
  --options "" \
  -- "$@"
)

while [[ ${#opts[@]} -gt 0 ]]; do
  case "$1" in
    --archive-version)
      ARCHIVE_VERSION=$2
      opts=("${opts[@]:2}")
      ;;
    -a)
      ARCHIVE_VERSION=$2
      opts=("${opts[@]:2}")
      ;;
    *)
      shift
      opts=("${opts[@]:1}")
      ;;
  esac
done

function join_by {
  local d=${1-} f=${2-}
  if shift 2; then
    printf %s "$f" "${@/#/$d}"
  fi
}

ARCHIVE_VERSION=${ARCHIVE_VERSION-1}

#TODO: Detect if version exists.
if [[ ! " ${ARCHIVE_VERSIONS[*]} " =~ " ${ARCHIVE_VERSION} " ]]; then
    echo "The provided archive version ($ARCHIVE_VERSION) is not supported."
    echo "Available version are: $(join_by "," ${ARCHIVE_VERSIONS[@]})"
    exit 1
fi

# Loading main code.
MAIN_CODE=$(cat main.swift)

# Loading Common code.
COMMON_CODE=$(find "./Sources/Common" -type f -name '*.swift' -exec cat {} +)

# Loading versionned Archive Code.
ARCHIVE_CODE=$(find "./Sources/ArchiveVersion/$ARCHIVE_VERSION" -type f -name '*.swift' -exec cat {} +)

# Injecting all the code into the swift command.
echo "${MAIN_CODE}\n${COMMON_CODE}\n${ARCHIVE_CODE}" | swift sh - "$@"
