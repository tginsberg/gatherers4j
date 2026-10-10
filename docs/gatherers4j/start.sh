#!/usr/bin/env bash

#
# Copyright 2025 Todd Ginsberg
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# Run the docs site locally with live reload. Hugo watches content, layouts,
# assets, data, and hugo.toml; the browser refreshes when any of them change.
#
# Usage: ./start.sh [extra hugo server args]
#   HUGO=/path/to/hugo ./start.sh      use a specific Hugo binary
#   PORT=1314 ./start.sh               use a different port (default 1313)
#   POLL=500ms ./start.sh              poll interval; POLL=off for native events
#                                      (default: 1s on /mnt/<drive>, off elsewhere)

set -euo pipefail

MIN_HUGO="0.160.1"

cd "$(dirname "${BASH_SOURCE[0]}")"

hugo_version() {
  "$1" version 2>/dev/null | sed -nE 's/^hugo v([0-9]+\.[0-9]+\.[0-9]+).*\+extended.*/\1/p'
}

# True when $1 >= $MIN_HUGO
meets_minimum() {
  [ -n "$1" ] && [ "$(printf '%s\n%s\n' "$MIN_HUGO" "$1" | sort -V | head -n1)" = "$MIN_HUGO" ]
}

# Find an extended Hugo that is new enough: $HUGO, then ../hugo/hugo (see
# ../local_install_hugo.sh), then whatever is on PATH.
find_hugo() {
  local candidate version
  for candidate in "${HUGO:-}" ../hugo/hugo "$(command -v hugo || true)"; do
    [ -n "$candidate" ] && [ -x "$candidate" ] || continue
    version="$(hugo_version "$candidate")"
    if meets_minimum "$version"; then
      echo "$candidate"
      return 0
    fi
  done
  return 1
}

if ! HUGO_BIN="$(find_hugo)"; then
  echo "No extended Hugo >= ${MIN_HUGO} found." >&2
  echo "Run: (cd .. && ./local_install_hugo.sh 0.166.0), or set HUGO=/path/to/hugo" >&2
  exit 1
fi

# Install npm dependencies (Bootstrap, Font Awesome, Dart Sass) only when any are
# missing. This is slow on /mnt/c, so it is not repeated on every start. After
# package-lock.json changes, run `npm ci --ignore-scripts` yourself.
for dependency in sass-embedded bootstrap @fortawesome/fontawesome-free; do
  if [ ! -f "node_modules/${dependency}/package.json" ]; then
    echo "Installing npm dependencies (missing ${dependency})..."
    npm ci --ignore-scripts
    break
  fi
done

# Hugo shells out to `sass --embedded`. npm can link the pure-JS `sass` package
# over sass-embedded's bin, which Hugo cannot use, so point the link back.
chmod +x node_modules/sass-embedded/dist/bin/sass.js
ln -sf ../sass-embedded/dist/bin/sass.js node_modules/.bin/sass
export PATH="$PWD/node_modules/.bin:$PATH"

PROJECT_VERSION="$(tr -d '\r\n' < ../../VERSION.txt)"
export PROJECT_VERSION

echo "Using $("$HUGO_BIN" version | cut -d' ' -f2) from $HUGO_BIN"
# inotify events are not delivered for Windows drives (/mnt/c) under WSL, so
# watch by polling there. Set POLL=off to use native events, or POLL=500ms etc.
POLL="${POLL:-}"
if [ -z "$POLL" ]; then
  case "$PWD" in /mnt/[a-z]/*) POLL=1s ;; *) POLL=off ;; esac
fi
poll_args=()
[ "$POLL" != "off" ] && poll_args=(--poll "$POLL")

exec "$HUGO_BIN" server \
  --port "${PORT:-1313}" \
  --navigateToChanged \
  --noHTTPCache \
  ${poll_args[@]+"${poll_args[@]}"} \
  "$@"
