#!/bin/ksh

SCRIPTS=/mnt/app/eso/hmi/engdefs/scripts/mqb
[ -s "$SCRIPTS/cpmap_intro.sh" ] || SCRIPTS=/net/mmx/mnt/app/eso/hmi/engdefs/scripts/mqb
/bin/ksh "$SCRIPTS/cpmap_intro.sh" status
exit $?
