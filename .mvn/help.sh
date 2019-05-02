#!/bin/bash
set -e -u

#
# report maven configuration
#

cd "${BASH_SOURCE%/*}/.."

./mvnw.sh help:active-profiles -B

./mvnw.sh dependency:tree -B
