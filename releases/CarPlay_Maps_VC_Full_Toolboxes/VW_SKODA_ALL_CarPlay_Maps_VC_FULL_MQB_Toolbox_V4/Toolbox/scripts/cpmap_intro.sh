#!/bin/ksh
# CarPlay Maps VC - configure AID startup animation policy (v1.4.1+ / bridge v1.5.3).
# Usage: cpmap_intro.sh <once-per-boot|every-connect|off|status>

POLICY="$1"

fail() {
    echo "CPMAP_INTRO_FAIL=$1"
    exit 1
}

[ -n "$POLICY" ] || fail "NO_POLICY"

if [ -n "${CPMAP_TEST_ROOT:-}" ] && [ -d "$CPMAP_TEST_ROOT/mnt/app" ]; then
    MMX="$CPMAP_TEST_ROOT"
elif [ -d /mnt/app ] && [ -s /ifs/lsd.jxe ]; then
    MMX=""
elif [ -d /net/mmx/mnt/app ] && [ -s /net/mmx/ifs/lsd.jxe ]; then
    MMX="/net/mmx"
else
    fail "MMX_FILESYSTEM_NOT_FOUND"
fi

ROOT="$MMX/mnt/app/carplaymaps"
[ -d "$ROOT" ] || fail "CARPLAY_MAPS_VC_NOT_INSTALLED"

if [ "$POLICY" = "status" ]; then
    CUR="once-per-boot"
    if [ -s "$ROOT/intro-policy" ]; then
        CUR=`cat "$ROOT/intro-policy" 2>/dev/null`
    fi
    echo "CPMAP_INTRO_POLICY=$CUR"
    exit 0
fi

case "$POLICY" in
    once-per-boot|every-connect|off) ;;
    *) fail "INVALID_POLICY_$POLICY" ;;
esac

if [ -n "${CPMAP_TEST_ROOT:-}" ]; then
    :
elif [ -n "$MMX" ]; then
    on -f mmx mount -uw /mnt/app >/dev/null 2>&1 || fail "APP_REMOUNT_FAILED"
else
    mount -uw /mnt/app >/dev/null 2>&1 || fail "APP_REMOUNT_FAILED"
fi

echo "$POLICY" > "$ROOT/intro-policy.new" || fail "WRITE_FAILED"
chmod 644 "$ROOT/intro-policy.new"
mv "$ROOT/intro-policy.new" "$ROOT/intro-policy" || fail "ACTIVATE_FAILED"
sync 2>/dev/null || true

echo "CPMAP_INTRO_POLICY=$POLICY"
echo "CPMAP_INTRO_OK"
echo "Takes effect on next CarPlay connection / boot."
exit 0
