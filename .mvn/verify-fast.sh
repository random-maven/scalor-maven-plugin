#!/bin/bash
set -e -u

#
# invoke integration test
#

cd "${BASH_SOURCE%/*}/.."

./mvnw.sh clean verify -B -D invoker.test=test-envjs

#./mvnw.sh clean verify -B -D invoker.test=test-cross
