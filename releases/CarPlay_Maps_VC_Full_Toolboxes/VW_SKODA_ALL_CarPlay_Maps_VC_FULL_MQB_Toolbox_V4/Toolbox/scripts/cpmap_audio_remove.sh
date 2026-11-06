#!/bin/ksh
# CarPlay Now Playing Text Uninstaller
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

/bin/ksh "$S" uninstall
