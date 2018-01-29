#!/bin/bash
set -e -u

#
# perform quick package
#

cd "${BASH_SOURCE%/*}/.."

./mvnw.sh clean package -B -P skip-test
