#!/usr/bin/env bash
# CI cache helper, used by the `.setup_bundler` template in .gitlab-ci.yml.
#
#   ci_cache.sh restore   (before_script) put cached Android SDK platforms back into the image's SDK
#   ci_cache.sh save      (after_script)  stash newly installed SDK platforms and tidy the Gradle home
#
# The CI image doesn't ship every SDK platform we compile against (e.g. android-37 for :sample), so AGP
# downloads it on every job (~1.5 min). Only platforms installed during the job are cached, never the
# ones the image already ships.
set -uo pipefail

SDK="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-/opt/android-sdk}}"
PLATFORMS_CACHE="${CI_PROJECT_DIR:-$PWD}/.android-platforms"
PREINSTALLED_LIST="/tmp/ci-cache-preinstalled-platforms"

case "${1:-}" in
  restore)
    mkdir -p "$PLATFORMS_CACHE" "$SDK/platforms"
    ls "$SDK/platforms" > "$PREINSTALLED_LIST" 2>/dev/null || true
    for dir in "$PLATFORMS_CACHE"/*/; do
      [ -d "$dir" ] || continue
      name="$(basename "$dir")"
      if [ ! -e "$SDK/platforms/$name" ]; then
        cp -a "$dir" "$SDK/platforms/$name" && echo "Restored SDK platform $name from cache"
      fi
    done
    ;;
  save)
    mkdir -p "$PLATFORMS_CACHE"
    for dir in "$SDK/platforms"/*/; do
      [ -d "$dir" ] || continue
      name="$(basename "$dir")"
      grep -qx "$name" "$PREINSTALLED_LIST" 2>/dev/null && continue
      [ -e "$PLATFORMS_CACHE/$name" ] && continue
      cp -a "$dir" "$PLATFORMS_CACHE/$name" && echo "Cached SDK platform $name"
    done
    # Files Gradle rewrites on every run; dropping them avoids re-uploading an unchanged cache.
    if [ -n "${GRADLE_USER_HOME:-}" ]; then
      rm -f "$GRADLE_USER_HOME/caches/modules-2/modules-2.lock"
      rm -rf "$GRADLE_USER_HOME"/caches/*/plugin-resolution/
    fi
    ;;
  *)
    echo "usage: $0 restore|save" >&2
    exit 1
    ;;
esac
exit 0
