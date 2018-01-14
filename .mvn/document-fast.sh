#!/bin/bash
set -e -u

#
# generate javadocs
#

cd "${BASH_SOURCE%/*}/.."

./mvnw.sh scalor:scaladoc-macro scalor:scaladoc-main scalor:scaladoc-test -B -P skip-test
