#!/bin/sh

GODS_ROOT_DIR=$(cd `dirname "$0"`;pwd)

exec "$GODS_ROOT_DIR/scripts/gods_loader.sh" $*
