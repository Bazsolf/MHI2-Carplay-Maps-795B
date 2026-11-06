#!/bin/ksh
# CarPlay Now Playing Text Installer (MU1427)
# Prefer fresh controller on SD card if present, fallback to internal unit script
S=""
for V in /fs/sda0 /fs/sdb0 /net/mmx/fs/sda0 /net/mmx/fs/sdb0; do
    if [ -s "$V/Custom/carplay_now_playing/control.sh" ]; then
        S="$V/Custom/carplay_now_playing/control.sh"
        break
    fi
done
if [ -z "$S" ]; then
    S=/mnt/app/eso/hmi/engdefs/scripts/mqb/cpmap_audio.sh
    [ -s "$S" ] || S=/net/mmx/mnt/app/eso/hmi/engdefs/scripts/mqb/cpmap_audio.sh
fi

VOL=""
for V in /net/mmx/fs/sda0 /net/mmx/fs/sdb0 /fs/sda0 /fs/sdb0; do
    if [ -s "$V/Custom/carplay_now_playing/VERSION" ]; then VOL=$V; break; fi
done
[ -n "$VOL" ] || { echo "CPMAP_AUDIO_FAIL=SD_PAYLOAD_NOT_FOUND"; exit 1; }

/bin/ksh "$S" install "$VOL/Custom/carplay_now_playing"
