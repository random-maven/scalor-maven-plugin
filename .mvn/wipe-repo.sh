#!/bin/bash
set -e -u

#
# remove build artifacts on ci host.
#

base=$(cd "${BASH_SOURCE%/*}/.." && pwd)


path="$base/test-repo/com/carrotgarden/"

rm -r -f "$path"/* || true
