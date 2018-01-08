#!/bin/bash
set -e -u

#
# publish artifact via sonatype
#

cd "${BASH_SOURCE%/*}/.."

#
# produce scala cross build
#

# inject shared build stamp (see arkon)
export RELEASE_STAMP=$(date -u +"%Y%m%d%H%M%S")
echo "RELEASE_STAMP=$RELEASE_STAMP"

./mvnw.sh clean deploy -B -P scala-2.12,distro-sonatype,skip-test,attach-sources,attach-javadoc,sign-artifacts

#./mvnw.sh clean deploy -B -P scala-2.13,distro-sonatype,skip-test,attach-sources,attach-javadoc,sign-artifacts
