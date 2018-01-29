#!/bin/bash
set -e -u

#
# produce documentation site
#

cd "${BASH_SOURCE%/*}/.."

./mvnw.sh plugin:report site -B
