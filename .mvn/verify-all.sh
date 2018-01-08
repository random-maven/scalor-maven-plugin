#!/bin/bash
set -e -u

#
# invoke integration test
#

"${BASH_SOURCE%/*}"/verify-2.12.sh

#"${BASH_SOURCE%/*}"/verify-2.13.sh
