#!/bin/sh


##### SPOTLESS HOOK START #####
SPOTLESS_EXECUTOR=C:/JavaProjects/DDD/rpm-ddd/mvnw.cmd
if ! $SPOTLESS_EXECUTOR spotless:check -DratchetFrom=origin/main; then
    echo 1>&2 "spotless found problems, running spotless:apply; commit the result and re-push"
    $SPOTLESS_EXECUTOR spotless:apply
    exit 1
fi
##### SPOTLESS HOOK END #####
