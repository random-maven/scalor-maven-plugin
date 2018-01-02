#!/bin/bash
set -e -u

#
# publish artifact via bintray
#

cd "${BASH_SOURCE%/*}/.."

#
# produce scala cross build
#

# inject shared build stamp (see arkon)
export RELEASE_STAMP=$(date -u +"%Y%m%d%H%M%S")
echo "RELEASE_STAMP=$RELEASE_STAMP"

./mvnw.sh clean deploy -B -P scala-2.12,distro-bintray,skip-test,attach-sources

#./mvnw.sh clean deploy -B -P scala-2.13,distro-bintray,skip-test,attach-sources
