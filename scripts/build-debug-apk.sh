#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

choose_java17() {
  for candidate in \
    /root/.local/share/mise/installs/java/17.0.2 \
    /root/.local/share/mise/installs/java/17 \
    /usr/lib/jvm/java-17-openjdk-amd64
  do
    if [[ -d "$candidate" ]]; then
      export JAVA_HOME="$candidate"
      export PATH="$JAVA_HOME/bin:$PATH"
      return 0
    fi
  done
  return 1
}

if ! choose_java17; then
  echo "Java 17 was not found. Please install JDK 17 and set JAVA_HOME." >&2
  exit 1
fi

echo "Using Java: $(java -version 2>&1 | head -n 1)"

gradle --no-daemon assembleDebug

APK_PATH="$ROOT_DIR/app/build/outputs/apk/debug/app-debug.apk"
if [[ -f "$APK_PATH" ]]; then
  echo
  echo "APK built successfully: $APK_PATH"
else
  echo
  echo "Build finished, but APK not found at expected location: $APK_PATH" >&2
  exit 1
fi
