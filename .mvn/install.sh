#!/bin/bash
set -e -u

#
# perform local install
#

cd "${BASH_SOURCE%/*}/.."

./mvnw.sh clean install -e -B -P skip-test
