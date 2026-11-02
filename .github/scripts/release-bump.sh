#!/usr/bin/env bash
# Points main at the patch after a published tag. The published number itself is never
# committed: the tag's jars already used it, and this only writes the following version.
# Usage: release-bump.sh 0.2.0 0.2.1 CHANGELOG.md tag-changelog.md gradle.properties
# Prints a summary. Exits 1 when the change would move the version backwards.
set -euo pipefail

die() { echo "$1" >&2; exit 1; }

[[ $# -eq 5 ]] || die "Usage: release-bump.sh 0.2.0 0.2.1 CHANGELOG.md tag-changelog.md gradle.properties"
released=$1
next=$2
changelog=$3
tagged=$4
properties=$5
here=$(dirname "$0")

order=$(bash "$here/release-version.sh" cmp "$(bash "$here/release-version.sh" read "$properties")" "$next")
case "$order" in
  gt) die "gradle.properties is already past ${next}, so this would move the version backwards" ;;
  eq) version_note="\`gradle.properties\` is already \`${next}\`." ;;
  lt)
    bash "$here/release-version.sh" write "$properties" "$next"
    version_note="\`gradle.properties\` now says \`${next}\`."
    ;;
esac

if grep -Fxq "## ${released}" "$changelog"; then
  changelog_note="\`## ${released}\` is already in the changelog."
elif cmp -s \
  <(bash "$here/release-changelog.sh" extract "$changelog") \
  <(bash "$here/release-changelog.sh" extract "$tagged"); then
  bash "$here/release-changelog.sh" bump "$changelog" "$released"
  changelog_note="The new notes are under \`## ${released}\`, and \`## Unreleased\` is empty again. Earlier version sections stay in place."
else
  changelog_note="\`## Unreleased\` on main does not match the notes that were published, so the heading was left as it is."
fi

cat <<EOF
\`${released}\` is published. ${version_note}

${changelog_note}
EOF
