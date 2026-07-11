#!/bin/bash
set -o pipefail
rsync -a --delete --exclude .git --exclude local.properties \
  "$HOME/reaction-racer/" tycho:builds/reaction-racer/ || { echo "RSYNC FAILED"; exit 2; }
ssh tycho 'cd ~/builds/reaction-racer && ~/gradle-8.9/bin/gradle test 2>&1'
