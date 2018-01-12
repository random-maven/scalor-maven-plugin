#!/bin/bash
set -e -u

#
# produce cross version project symlinks
#

base=$(cd "${BASH_SOURCE%/*}/.." && pwd)

cross212="$base/cross/2.12"
cross213="$base/cross/2.13"

echo "base     $base"
echo "cross212 $cross212"
echo "cross213 $cross213"

make_link() {
    ln -v -f -r -s -T "$1" "$2"
}

# sources
make_link "$base/src" "$cross212/src"
make_link "$base/src" "$cross213/src"

# repository
make_link "$base/test-repo" "$cross212/test-repo"
make_link "$base/test-repo" "$cross213/test-repo"
