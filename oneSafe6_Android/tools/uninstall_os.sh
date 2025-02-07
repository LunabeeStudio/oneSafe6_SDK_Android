adb shell pm list packages studio.lunabee.onesafe |
cut -d ':' -f 2 |
tr -d '\r' |
xargs -L1 -t adb uninstall

adb shell settings put global window_animation_scale 1
adb shell settings put global transition_animation_scale 1
adb shell settings put global animator_duration_scale 1
