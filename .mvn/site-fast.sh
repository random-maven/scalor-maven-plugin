#!/bin/bash
set -e -u

#
# produce documentation site
#

cd "${BASH_SOURCE%/*}/.."

./mvnw.sh clean install site:site site:stage site:deploy -B -P skip-test
