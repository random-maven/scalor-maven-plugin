#!/bin/bash
set -e -u

#
# perform local install
#

cd "${BASH_SOURCE%/*}/.."

#
# produce scala cross build
#

# inject shared build stamp (see arkon)
export RELEASE_STAMP=$(date -u +"%Y%m%d%H%M%S")
echo "RELEASE_STAMP=$RELEASE_STAMP"

./mvnw.sh clean install -B -P scala-2.11,skip-test -e

./mvnw.sh clean install -B -P scala-2.12,skip-test -e
