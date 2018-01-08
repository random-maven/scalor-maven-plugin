#!/bin/bash
set -e -u

#
# produce documentation site
#

cd "${BASH_SOURCE%/*}/.."

./mvnw.sh clean install site -B -P skip-test
