
### Demonstration project for Scalor Maven Plugin

Project features: 
* Scala.macro compile
* Scala.macro JUnit test
* Scala.js compile and link
* Scala.js module initializer
* Scala.js JUnit test in JS-VM
* Provision Node.js test JS-VM
* Provision Webjars for test JS-VM
* Setup Eclipse plugin in M2E
* Incremental Scala.js linking in M2E
* Transfer source format settings in M2E
* Manage test application process restart in M2E
* Manage presentation compiler work-around in M2E

### Verify full Maven build

```bash
cd /tmp
git clone git@github.com:random-maven/scalor-maven-plugin.git
cd scalor-maven-plugin/demo
./mvnw.sh verify -B
```

### Verify incremental Eclipse build

Import a copy of this project into Eclipse workspace and study 
both
[pom.xml](https://github.com/random-maven/scalor-maven-plugin/blob/master/demo/pom.xml)
and
[sources](https://github.com/random-maven/scalor-maven-plugin/tree/master/demo/src)
.

Enable various plugin logging options in `pom.xml` and observe results in M2E 
[Maven Console](https://www.ibm.com/support/knowledgecenter/SS8PJ7_9.1.0/com.ibm.etools.maven.doc/topics/troubleshooting.html)
. 
