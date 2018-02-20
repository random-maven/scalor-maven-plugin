
### Publish

Publish to Sonatype
* https://github.com/random-maven/scalor-maven-plugin/blob/master/.mvn/release-sonatype.sh

Verify release CI build
* https://travis-ci.org/random-maven/scalor-maven-plugin/builds
* https://ci.appveyor.com/project/random-maven/scalor-maven-plugin/history

Verify and promote release to Maven Central
* https://oss.sonatype.org/index.html#stagingRepositories

Verify artifact replication to Maven Central
* https://repo1.maven.org/maven2/com/carrotgarden/maven/scalor-maven-plugin_2.12/

### Update

Update plugin project release epoch
* https://github.com/random-maven/scalor-maven-plugin/blob/master/pom.xml 

Update demo project plugin to release version
* https://github.com/random-maven/scalor-maven-plugin/blob/master/demo/pom.xml

Update plugin project squash point to release commit
* https://github.com/random-maven/scalor-maven-plugin/blob/master/.mvn/github-squash.conf

### Commit

Commit and verify CI development build of demo project
* https://github.com/random-maven/scalor-maven-plugin/blob/master/.mvn/github-squash.sh

Merge development branch into master branch
