#!/usr/bin/env bash
# Prereleases publish Unreleased only. Stable releases also include the prerelease
# sections since the previous stable heading. Bumping archives only Unreleased.
# Usage:
#   release-changelog.sh extract CHANGELOG.md        # may be empty; used to compare notes
#   release-changelog.sh notes CHANGELOG.md 0.2.0
#   release-changelog.sh bump CHANGELOG.md 0.2.0
set -euo pipefail

die() { echo "$1" >&2; exit 1; }

semver='^(0|[1-9][0-9]*)[.](0|[1-9][0-9]*)[.](0|[1-9][0-9]*)(-alpha|-beta)?$'

extract() {
  local file=$1 body
  body=$(awk '
    /^## Unreleased$/ { in_section = 1; found = 1; next }
    /^## / && in_section { exit }
    in_section { lines[++n] = $0 }
    END {
      if (!found) exit 1
      start = 1
      while (start <= n && lines[start] ~ /^[[:space:]]*$/) start++
      end = n
      while (end >= start && lines[end] ~ /^[[:space:]]*$/) end--
      for (i = start; i <= end; i++) print lines[i]
    }
  ' "$file") || die "${file} has no ## Unreleased section"
  printf '%s\n' "$body"
}

notes() {
  local file=$1 version=$2 body history
  [[ "$version" =~ $semver ]] || die "Version ${version} has to look like 0.0.1 or 0.0.1-alpha"
  body=$(extract "$file")
  case "$version" in
    *-alpha|*-beta)
      printf '%s\n' "$body" | grep '^[[:space:]]*[-*] ' >/dev/null || die "${file} has no bullets under ## Unreleased"
      ;;
    *)
      # Headings are newest first. A stable heading is the exclusive cutoff; beta
      # headings are included just like alpha headings. Reject unknown headings.
      history=$(awk -v semver="$semver" '
        /^## Unreleased$/ { found = 1; next }
        /^## / && found {
          version = substr($0, 4)
          if (version !~ semver) exit 1
          if (version !~ /-(alpha|beta)$/) exit
          in_history = 1
        }
        in_history { lines[++n] = $0 }
        END {
          while (n > 0 && lines[n] ~ /^[[:space:]]*$/) n--
          for (i = 1; i <= n; i++) print lines[i]
        }
      ' "$file") || die "${file} has an invalid version heading"
      if [[ -n "$body" ]]; then
        body=$(printf '## %s\n\n%s' "$version" "$body")
      fi
      if [[ -n "$history" ]]; then
        if [[ -n "$body" ]]; then
          body=$(printf '%s\n\n%s' "$body" "$history")
        else
          body=$history
        fi
      fi
      printf '%s\n' "$body" | grep '^[[:space:]]*[-*] ' >/dev/null || die "${file} has no changes since the previous stable release"
      ;;
  esac
  printf '%s\n' "$body"
}

bump() {
  local file=$1 version=$2 tmp
  [[ "$version" =~ $semver ]] || die "Version ${version} has to look like 0.0.1 or 0.0.1-alpha"
  grep -Fxq "## ${version}" "$file" && die "${file} already has a ## ${version} section"
  # A stable promotion can have an empty Unreleased section. Still insert its
  # heading so it becomes the cutoff for the next stable release.
  notes "$file" "$version" >/dev/null
  tmp=$(mktemp)
  awk -v version="$version" '
    /^## Unreleased$/ && !done {
      print "## Unreleased"
      print ""
      print "## " version
      done = 1
      next
    }
    { print }
    END { if (!done) exit 1 }
  ' "$file" > "$tmp" && mv "$tmp" "$file" || {
    rm -f "$tmp"
    die "Could not rename ## Unreleased in ${file}"
  }
}

[[ $# -ge 1 ]] || die "Usage: release-changelog.sh extract|notes|bump ..."
cmd=$1
shift
case "$cmd" in
  extract) [[ $# -eq 1 ]] || die "Usage: release-changelog.sh extract CHANGELOG.md"; extract "$1" ;;
  notes) [[ $# -eq 2 ]] || die "Usage: release-changelog.sh notes CHANGELOG.md 0.2.0"; notes "$1" "$2" ;;
  bump) [[ $# -eq 2 ]] || die "Usage: release-changelog.sh bump CHANGELOG.md 0.2.0"; bump "$1" "$2" ;;
  *) die "Unknown command: ${cmd}" ;;
esac
