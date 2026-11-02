#!/usr/bin/env bash
# Reads and writes the mod version in gradle.properties. The tag is the version the jars
# are built with: 0.0.1 is a release, and 0.0.1-alpha or 0.0.1-beta is that channel.
# A leading v is accepted. The following development version is the next patch, with no channel.
# Usage:
#   release-version.sh parse 0.0.1-alpha
#   release-version.sh channel 0.0.1-alpha   # prints alpha, beta, or release
#   release-version.sh next 0.0.1-alpha      # prints 0.0.2
#   release-version.sh read gradle.properties
#   release-version.sh write gradle.properties 0.0.1-alpha
#   release-version.sh cmp 0.0.1-alpha 0.0.1 # prints lt, eq, or gt
set -euo pipefail

die() { echo "$1" >&2; exit 1; }

part='(0|[1-9][0-9]*)'
ver_re="^${part}\\.${part}\\.${part}(-alpha|-beta)?$"
tag_re="^v?${part}\\.${part}\\.${part}(-alpha|-beta)?$"
version_line='^version=(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)(-alpha|-beta)?$'

parse() {
  local tag=$1
  [[ "$tag" =~ $tag_re ]] || die "Tag ${tag} has to look like 0.0.1 or 0.0.1-alpha"
  echo "${BASH_REMATCH[1]}.${BASH_REMATCH[2]}.${BASH_REMATCH[3]}${BASH_REMATCH[4]}"
}

channel() {
  local tag=$1
  [[ "$tag" =~ $tag_re ]] || die "Tag ${tag} has to look like 0.0.1 or 0.0.1-alpha"
  case "${BASH_REMATCH[4]}" in
    -alpha) echo alpha ;;
    -beta) echo beta ;;
    '') echo release ;;
    *) die "Tag ${tag} has to look like 0.0.1 or 0.0.1-alpha" ;;
  esac
}

next_version() {
  local version=$1
  [[ "$version" =~ $ver_re ]] || die "Version ${version} has to look like 0.0.1 or 0.0.1-alpha"
  echo "${BASH_REMATCH[1]}.${BASH_REMATCH[2]}.$((BASH_REMATCH[3] + 1))"
}

read_version() {
  local file=$1 line count
  line=$(grep -E "$version_line" "$file" || true)
  [[ -n "$line" ]] || die "${file} has no version=MAJOR.MINOR.PATCH line"
  count=$(printf '%s\n' "$line" | wc -l)
  [[ "$count" -eq 1 ]] || die "${file} has more than one version line"
  echo "${line#version=}"
}

write_version() {
  local file=$1 version=$2 tmp
  [[ "$version" =~ $ver_re ]] || die "Version ${version} has to look like 0.0.1 or 0.0.1-alpha"
  tmp=$(mktemp)
  awk -v version="$version" '
    /^version=(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)(-alpha|-beta)?$/ {
      if (seen++) exit 1
      print "version=" version
      next
    }
    { print }
    END { if (!seen) exit 1 }
  ' "$file" > "$tmp" && mv "$tmp" "$file" || {
    rm -f "$tmp"
    die "Could not replace the version line in ${file}"
  }
}

channel_rank() {
  case "$1" in
    -alpha) echo 1 ;;
    -beta) echo 2 ;;
    '') echo 3 ;;
    *) die "Version channel ${1} is not alpha, beta, or a release" ;;
  esac
}

cmp_version() {
  local left=$1 right=$2
  [[ "$left" =~ $ver_re ]] || die "Version ${left} has to look like 0.0.1 or 0.0.1-alpha"
  local l1=${BASH_REMATCH[1]} l2=${BASH_REMATCH[2]} l3=${BASH_REMATCH[3]} ls=${BASH_REMATCH[4]}
  [[ "$right" =~ $ver_re ]] || die "Version ${right} has to look like 0.0.1 or 0.0.1-alpha"
  local r1=${BASH_REMATCH[1]} r2=${BASH_REMATCH[2]} r3=${BASH_REMATCH[3]} rs=${BASH_REMATCH[4]}
  local lr rr
  if ((l1 != r1)); then ((l1 > r1)) && echo gt || echo lt; return; fi
  if ((l2 != r2)); then ((l2 > r2)) && echo gt || echo lt; return; fi
  if ((l3 != r3)); then ((l3 > r3)) && echo gt || echo lt; return; fi
  lr=$(channel_rank "$ls")
  rr=$(channel_rank "$rs")
  if ((lr != rr)); then ((lr > rr)) && echo gt || echo lt; return; fi
  echo eq
}

[[ $# -ge 1 ]] || die "Usage: release-version.sh parse|channel|next|read|write|cmp ..."
cmd=$1
shift
case "$cmd" in
  parse) [[ $# -eq 1 ]] || die "Usage: release-version.sh parse 0.0.1-alpha"; parse "$1" ;;
  channel) [[ $# -eq 1 ]] || die "Usage: release-version.sh channel 0.0.1-alpha"; channel "$1" ;;
  next) [[ $# -eq 1 ]] || die "Usage: release-version.sh next 0.0.1-alpha"; next_version "$1" ;;
  read) [[ $# -eq 1 ]] || die "Usage: release-version.sh read gradle.properties"; read_version "$1" ;;
  write) [[ $# -eq 2 ]] || die "Usage: release-version.sh write gradle.properties 0.2.0"; write_version "$1" "$2" ;;
  cmp) [[ $# -eq 2 ]] || die "Usage: release-version.sh cmp 0.2.0 0.2.1"; cmp_version "$1" "$2" ;;
  *) die "Unknown command: ${cmd}" ;;
esac
