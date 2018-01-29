#!/bin/bash
set -e -u

#
# publish artifact via bintray
#

cd "${BASH_SOURCE%/*}/.."

./mvnw.sh clean deploy -B -P distro-bintray,skip-test,attach-sources
