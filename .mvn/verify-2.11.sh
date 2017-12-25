#!/bin/bash
set -e -u

#
# invoke integration test
#

cd "${BASH_SOURCE%/*}/.."

./mvnw.sh clean verify -B -P scala-2.11 -e
