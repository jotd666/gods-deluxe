#!/bin/sh

#
#  Gods run script
#
#  written by JOTD
#

GODS_ROOT_DIR=$(cd `dirname "$0"`;pwd)

exec java -Djava.library.path="$GODS_ROOT_DIR/bin" -DGODS_ASSETS_DIR="$GODS_ROOT_DIR" -jar "$GODS_ROOT_DIR/bin/gods-deluxe.jar" $*
