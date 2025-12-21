#!/bin/bash

LOCAL_DIR="./forge/build/libs/"
REMOTE_USER="user"
REMOTE_HOST="100.117.104.86"
REMOTE_DIR="/new-space/apps/prism/instances/1.20.1 devel forge/minecraft/mods"

echo "Watching $LOCAL_DIR for changes..."
fswatch --latency 2 -o "$LOCAL_DIR" | xargs -n1 -I{} sh -c \
  "rsync -avz --progress --protect-args \
    --exclude '*-sources.jar' \
    --exclude '*-dev.jar' \
    --exclude '*-slim.jar' \
    --include '*.jar' \
    --exclude '*' \
    $LOCAL_DIR*.jar \
    $REMOTE_USER@$REMOTE_HOST:'$REMOTE_DIR' \
    && echo 'Synced!'"
