#!/bin/bash
set -e -u

#
# publish artifact via sonatype, release revision
#

cd "${BASH_SOURCE%/*}/.."

./mvnw.sh clean deploy -B -D release-revision -P distro-sonatype,skip-test,attach-sources,attach-javadoc,sign-artifacts
