#!/usr/bin/env bash
# Run unchanged against each APK on the same physical device. Does not select HOME.
set -euo pipefail
out=${1:?Uso: scripts/measure-drawer.sh DIRECTORIO_SALIDA [REPETICIONES]}
repeats=${2:-20}
[[ "$repeats" =~ ^[1-9][0-9]*$ ]] || { echo 'Repeticiones debe ser un entero positivo.' >&2; exit 1; }
mkdir -p "$out"
adb get-state > "$out/device-state.txt"
adb shell getprop ro.build.fingerprint > "$out/fingerprint.txt"
adb shell dumpsys package site.neobash.launcher > "$out/package.txt"
adb shell wm size > "$out/display.txt"
adb shell settings get global animator_duration_scale > "$out/animator-scale.txt"
adb shell settings get global transition_animation_scale > "$out/transition-scale.txt"
size=$(adb shell wm size | tr -d '\r' | tail -1 | awk '{print $NF}')
width=${size%x*}; height=${size#*x}
[[ "$width" =~ ^[0-9]+$ && "$height" =~ ^[0-9]+$ ]] || exit 1
adb logcat -T 1 -v monotonic -s NeobashPerf:D '*:S' > "$out/events.log" &
logger_pid=$!
trap 'kill "$logger_pid" 2>/dev/null || true' EXIT
adb shell am force-stop site.neobash.launcher
for ((i=1; i<=repeats; i++)); do
    adb shell am start -W -a android.intent.action.MAIN -c android.intent.category.HOME \
        -n site.neobash.launcher/.MainActivity > "$out/home-$i.txt"
    sleep 2
    adb shell dumpsys gfxinfo site.neobash.launcher reset > /dev/null
    adb shell input swipe "$((width * 8 / 10))" "$((height / 2))" "$((width * 2 / 10))" "$((height / 2))" 250
    sleep 1
    adb shell dumpsys gfxinfo site.neobash.launcher framestats > "$out/frames-$i.txt"
done
printf 'Captura guardada en %s. La primera apertura usa proceso frío; las siguientes, catálogo caliente.\n' "$out"
