
### Project release steps

Publish to Sonatype
* https://github.com/random-maven/scalor-maven-plugin/blob/master/.mvn/release-sonatype.sh

Verify release travis build
* https://travis-ci.org/random-maven/scalor-maven-plugin/builds

Update plugin project release epoch
* https://github.com/random-maven/scalor-maven-plugin/blob/master/pom.xml 

Update demo project plugin to release version
* https://github.com/random-maven/scalor-maven-plugin/blob/master/demo/pom.xml

Update plugin project squash point to release commit
* https://github.com/random-maven/scalor-maven-plugin/blob/master/.mvn/github-squash.conf

Commit and verify development travis build of demo project
* https://github.com/random-maven/scalor-maven-plugin/blob/master/.mvn/github-squash.sh

Verify and promote release to Maven Central
* https://oss.sonatype.org/index.html#stagingRepositories

Merge development branch to master branch
