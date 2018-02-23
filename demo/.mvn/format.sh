#!/bin/bash
set -e -u

#
# format project sources
#

cd "${BASH_SOURCE%/*}/.."

./mvnw.sh scalor:format -B
