#!/bin/sh

GODS_ROOT_DIR=$(cd `dirname "$0"`;pwd)

"$GODS_ROOT_DIR"/scripts/gods_loader.sh gods.game.GodsGame $*
