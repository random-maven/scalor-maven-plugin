#!/bin/bash
set -e -u

#
# validate and update dependencies
#

cd "${BASH_SOURCE%/*}/.."

./mvnw.sh clean validate -B -U
