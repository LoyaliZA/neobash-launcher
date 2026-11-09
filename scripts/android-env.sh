#!/usr/bin/env bash
# Source this file in each shell that needs the Android toolchain.
export NEOBASH_ANDROID_DEV_ROOT="${NEOBASH_ANDROID_DEV_ROOT:-/workspace/android-dev}"
if [[ -f "$NEOBASH_ANDROID_DEV_ROOT/java-home.txt" ]]; then
  IFS= read -r JAVA_HOME < "$NEOBASH_ANDROID_DEV_ROOT/java-home.txt"
elif [[ -x "$NEOBASH_ANDROID_DEV_ROOT/java-root/usr/lib/jvm/java-21-openjdk-amd64/bin/javac" ]]; then
  JAVA_HOME="$NEOBASH_ANDROID_DEV_ROOT/java-root/usr/lib/jvm/java-21-openjdk-amd64"
else
  printf 'Run scripts/setup-android.sh first.\n' >&2
  return 1 2>/dev/null || exit 1
fi
export JAVA_HOME
export ANDROID_HOME="$NEOBASH_ANDROID_DEV_ROOT/sdk"
export ANDROID_SDK_ROOT="$ANDROID_HOME"
export ANDROID_USER_HOME="$NEOBASH_ANDROID_DEV_ROOT/android-user"
export GRADLE_USER_HOME="$NEOBASH_ANDROID_DEV_ROOT/gradle-home"
export PATH="$JAVA_HOME/bin:$NEOBASH_ANDROID_DEV_ROOT/gradle-8.11.1/bin:$ANDROID_HOME/cmdline-tools/19.0/bin:$ANDROID_HOME/platform-tools:$PATH"
mkdir -p "$ANDROID_USER_HOME" "$NEOBASH_ANDROID_DEV_ROOT/user-home"

# Java does not inherit HTTP proxy settings automatically. Keep TLS verification.
if [[ -n "${HTTPS_PROXY:-}" && "${JAVA_TOOL_OPTIONS:-}" != *'-Dhttps.proxyHost='* ]]; then
  neobash_proxy_options=$(python3 - <<'PY'
import os, urllib.parse
p = urllib.parse.urlsplit(os.environ['HTTPS_PROXY'])
if not p.hostname or any(c.isspace() for c in p.hostname):
    raise SystemExit('Invalid HTTPS_PROXY hostname')
print(f'-Dhttps.proxyHost={p.hostname} -Dhttps.proxyPort={p.port or 80} '
      f'-Dhttp.proxyHost={p.hostname} -Dhttp.proxyPort={p.port or 80} '
      '-Dhttp.nonProxyHosts=localhost|127.*|[::1]')
PY
  ) || { return 1 2>/dev/null || exit 1; }
  export JAVA_TOOL_OPTIONS="${JAVA_TOOL_OPTIONS:+$JAVA_TOOL_OPTIONS }$neobash_proxy_options"
  unset neobash_proxy_options
fi
if [[ "${JAVA_TOOL_OPTIONS:-}" != *'-Duser.home='* ]]; then
  export JAVA_TOOL_OPTIONS="${JAVA_TOOL_OPTIONS:+$JAVA_TOOL_OPTIONS }-Duser.home=$NEOBASH_ANDROID_DEV_ROOT/user-home"
fi
