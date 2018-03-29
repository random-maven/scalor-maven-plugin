#!/usr/bin/env bash

set -e

#
# remove appveyor cache
# https://www.appveyor.com/docs/build-cache/
#

configFile="$HOME/.appveyor/config.sh"
source "$configFile"

accountName="random-maven"
projectSlug="scalor-maven-plugin"

cacheUrl="https://ci.appveyor.com/api/projects/$accountName/$projectSlug/buildcache"

tokenHeader="Authorization: Bearer $tokenValue"

curl -H "$tokenHeader" -X "DELETE" "$cacheUrl"
