#!/usr/bin/env bash
# Reproduce the Android build environment without adding SDK files to this repo.
set -euo pipefail
script_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
export NEOBASH_ANDROID_DEV_ROOT="${NEOBASH_ANDROID_DEV_ROOT:-/workspace/android-dev}"
root="$NEOBASH_ANDROID_DEV_ROOT"
if [[ "$root" != /* || "$root" == *[[:space:]]* ]]; then
  printf 'NEOBASH_ANDROID_DEV_ROOT must be an absolute path without whitespace.\n' >&2
  exit 1
fi
repo_root=$(cd "$script_dir/.." && pwd -P)
resolved_root=$(realpath -m "$root")
case "$resolved_root/" in
  "$repo_root/"*) printf 'Keep tools and downloads outside the checkout.\n' >&2; exit 1 ;;
esac
for tool in curl unzip python3 sha256sum sha1sum dpkg-deb; do
  command -v "$tool" >/dev/null || { printf 'Missing prerequisite: %s\n' "$tool" >&2; exit 1; }
done
[[ $(uname -s) == Linux && $(uname -m) == x86_64 ]] || {
  printf 'This installer supports Linux x86_64.\n' >&2; exit 1;
}
mkdir -p "$root/downloads" "$root/sdk/cmdline-tools"

fetch() {
  local url="$1" file="$2" checksum="$3" algorithm="$4"
  if ! [[ -f "$file" ]] || ! printf '%s  %s\n' "$checksum" "$file" | "${algorithm}sum" -c - >/dev/null 2>&1; then
    curl --fail --silent --show-error --location --retry 2 --connect-timeout 20 --max-time 600 "$url" -o "$file.part"
    printf '%s  %s\n' "$checksum" "$file.part" | "${algorithm}sum" -c -
    mv "$file.part" "$file"
  fi
  printf '%s  %s\n' "$checksum" "$file" | "${algorithm}sum" -c -
}

# Reuse a compatible full JDK when available. Gradle 8.11.1 supports Java 17/21.
jdk="$root/java-root/usr/lib/jvm/java-21-openjdk-amd64"
if [[ ! -x "$jdk/bin/javac" ]]; then
  candidate="${JAVA_HOME:-}"
  if [[ ! -x "$candidate/bin/javac" ]] && command -v javac >/dev/null; then
    candidate=$(dirname "$(dirname "$(readlink -f "$(command -v javac)")")")
  fi
  version=$("$candidate/bin/javac" -version 2>/dev/null || true)
  if [[ "$version" == 'javac 17.'* || "$version" == 'javac 21.'* ]]; then
    jdk="$candidate"
  else
    # Debian 13 runtime configuration and CA store are supplied by the cloud image.
    [[ -r /etc/java-21-openjdk/security/java.security && -r /etc/ssl/certs/java/cacerts ]] || {
      printf 'Provide a complete JDK 17/21 or use the Debian 13 Codex image with OpenJDK 21 runtime.\n' >&2
      exit 1
    }
    fetch 'https://deb.debian.org/debian/pool/main/o/openjdk-21/openjdk-21-jdk-headless_21.0.12.1+1-1~deb13u1_amd64.deb' "$root/downloads/jdk.deb" f3abafb6c644b03df042824e707cd211ea33254761d7f7b75be3f8dc0df97c7a sha256
    fetch 'https://deb.debian.org/debian/pool/main/o/openjdk-21/openjdk-21-jre-headless_21.0.12.1+1-1~deb13u1_amd64.deb' "$root/downloads/jre.deb" e95f36193e45464ac758e5436f940bf3eaecc77de67188275a42104f58aa7674 sha256
    mkdir -p "$root/java-root"
    dpkg-deb -x "$root/downloads/jre.deb" "$root/java-root"
    dpkg-deb -x "$root/downloads/jdk.deb" "$root/java-root"
  fi
fi
printf '%s\n' "$jdk" > "$root/java-home.txt"

if [[ ! -x "$root/sdk/cmdline-tools/19.0/bin/sdkmanager" ]]; then
  fetch https://dl.google.com/android/repository/commandlinetools-linux-13114758_latest.zip "$root/downloads/cmdline.zip" 5fdcc763663eefb86a5b8879697aa6088b041e70 sha1
  stage=$(mktemp -d "$root/downloads/cmdline.XXXXXX")
  trap 'rm -rf -- "$stage"' EXIT
  unzip -q "$root/downloads/cmdline.zip" -d "$stage"
  mv "$stage/cmdline-tools" "$root/sdk/cmdline-tools/19.0"
  rm -rf -- "$stage"
  trap - EXIT
fi
if [[ ! -x "$root/gradle-8.11.1/bin/gradle" ]]; then
  fetch https://downloads.gradle.org/distributions/gradle-8.11.1-bin.zip "$root/downloads/gradle.zip" f397b287023acdba1e9f6fc5ea72d22dd63669d59ed4a289a29b1a76eee151c6 sha256
  unzip -q "$root/downloads/gradle.zip" -d "$root"
fi
source "$script_dir/android-env.sh"
# sdkmanager checks the selected package archives against repository metadata.
# Noninteractive acceptance is limited to licenses for these requested packages.
python3 - <<'PY'
import os, subprocess
subprocess.run(['sdkmanager', '--sdk_root=' + os.environ['ANDROID_HOME'],
                'platforms;android-35', 'build-tools;35.0.0', 'platform-tools'],
               input='y\n' * 30, text=True, check=True)
PY
javac -version
gradle --version
sdkmanager --sdk_root="$ANDROID_HOME" --list_installed
test -f "$ANDROID_HOME/platforms/android-35/android.jar"
"$ANDROID_HOME/build-tools/35.0.0/aapt2" version
printf 'Activate with: source %s/android-env.sh\n' "$script_dir"
