#!/bin/sh


HEAP_SIZE=100m

if [ "$1" = "-h" ] ; then
   echo "Options: "
   echo "  -no-intro: skips introduction"
   echo "  -full-screen: full screen display"
   exit
fi

PROGDIR=`dirname "$0"`
GODS_ROOT_DIR=$(cd "$PROGDIR/..";pwd)

java -Djava.library.path="$GODS_ROOT_DIR/bin" -classpath "$GODS_ROOT_DIR/bin" -DROOT_DIR="$GODS_ROOT_DIR" -Xmx$HEAP_SIZE $*

