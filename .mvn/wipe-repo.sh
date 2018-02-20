#!/bin/bash
set -e -u

#
# remove build artifacts on ci host
#

wipe_repo() {
   local path="$1"
   rm -r -f "$path"/* || true
}

base=$(cd "${BASH_SOURCE%/*}/.." && pwd)

name="com/carrotgarden/maven"

wipe_repo "$base/test-repo/$name"

wipe_repo "$HOME/.m2/repository/$name"
