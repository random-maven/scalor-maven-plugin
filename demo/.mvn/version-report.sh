#!/bin/bash
set -e -u

#
# report version updates
#

cd "${BASH_SOURCE%/*}/.."

./mvnw.sh -B \
    org.codehaus.mojo:versions-maven-plugin::display-plugin-updates \
    org.codehaus.mojo:versions-maven-plugin::display-property-updates \
    org.codehaus.mojo:versions-maven-plugin::display-dependency-updates \
    -D processDependencies=true \
    -D processDependencyManagement=true \
    -D verbose=false \
    -D generateBackupPoms=false \
    -D allowSnapshots=false \

