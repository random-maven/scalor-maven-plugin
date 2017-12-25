#!/bin/bash
set -e -u

#
# squash commits after a point
#

source "${BASH_SOURCE%/*}/github-squash.conf"

git reset --soft $point
git add -A
git commit -m "develop"
git push --force
