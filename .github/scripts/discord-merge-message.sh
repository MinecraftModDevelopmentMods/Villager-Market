#!/usr/bin/env bash
# Prints the Discord webhook payload for a merged pull request: its title as a link, then the CHANGELOG.md bullets
# that this merge added. GitHub's own preview card is switched off (flags 4) and no one gets pinged.
# Usage: discord-merge-message.sh <merge commit> <repo name> <pr number> <title> <url>
set -euo pipefail

merge=$1 repo=$2 number=$3 title=$4 url=$5
# Discord rejects messages over 2000 characters; leave room for the "more" line.
limit=1900

# The first parent is main before the merge, so this diff is exactly what the pull request changed.
bullets=$(git diff --unified=0 "$merge^1" "$merge" -- CHANGELOG.md | sed -n 's/^+\([[:space:]]*[-*] \)/\1/p')

content="✅ **[${repo} #${number}: ${title}](${url})**"
if [ -n "$bullets" ]; then
  while IFS= read -r line; do
    if [ $(( ${#content} + ${#line} + 1 )) -gt "$limit" ]; then
      content+=$'\n'"…and more in CHANGELOG.md"
      break
    fi
    content+=$'\n'"$line"
  done <<< "$bullets"
fi

jq -n --arg content "$content" '{content: $content, flags: 4, allowed_mentions: {parse: []}}'
