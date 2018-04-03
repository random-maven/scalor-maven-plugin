#!/bin/bash
set -e -u

#
# invoke integration test
#

cd "${BASH_SOURCE%/*}/.."

#./mvnw.sh clean verify -B -D invoker.test=test-any

#./mvnw.sh clean verify -B -D invoker.test=test-envjs

#./mvnw.sh clean verify -B -D invoker.test=test-cross

#./mvnw.sh clean verify -B -D invoker.test=test-define

#./mvnw.sh clean verify -B -D invoker.test=test-format

#./mvnw.sh clean verify -B -e -D invoker.test=test-m2e-1.8
#./mvnw.sh clean verify -B -e -D invoker.test=test-m2e-1.9

#./mvnw.sh clean verify -B -e -D invoker.test=test-tc-1.8
#./mvnw.sh clean verify -B -e -D invoker.test=test-tc-1.9

./mvnw.sh clean verify -B -e -D invoker.test=test-setup

#./mvnw.sh clean verify -B -e -D invoker.test=test-native
