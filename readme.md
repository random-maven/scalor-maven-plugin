
### Scalor Maven Plugin

Build integrator for Java, Scala, Scala.macro, Scala.js, Scala.native, Eclipse and Maven.

[![Project License][licence_icon]][licence_link]
[![Travis Status][travis_icon]][travis_link]
[![Appvey Status][appvey_icon]][appvey_link]
[![Project Files][tokei_files_icon]][tokei_basic_link]
[![Project Lines][tokei_lines_icon]][tokei_basic_link]
[![Lines of Code][tokei_basic_icon]][tokei_basic_link]

| Install | Production Release | Development Release |
|---------|--------------------|---------------------|
| <h5>Scalor Plugin 1.X for Scala IDE 4.7</h5> | [![Central][central_212_icon]][central_212_link] | [![Bintray][bintray_212_icon]][bintray_212_link] | 

Similar plugins
* [scala-maven-plugin](https://github.com/davidB/scala-maven-plugin)
* [sbt-compiler-maven-plugin](https://github.com/sbt-compiler-maven-plugin/sbt-compiler-maven-plugin)

Getting started
* build and study [demo project](https://github.com/random-maven/scalor-maven-plugin/blob/master/demo)

### Plugin features

Scala
* new incremental [Zinc](https://github.com/sbt/zinc)
* uses static [compiler-bridge](https://github.com/sbt/zinc/tree/1.x/internal/compiler-bridge)
* auto-discovery of [Scala compiler plugins](https://random-maven.github.io/scalor-maven-plugin/2.12/eclipse-config-mojo.html#definePluginList)
* cross-scala-version [build with simple setup](https://github.com/random-maven/scalor-maven-plugin/tree/master/src/it/test-cross)
* limited Scala [Java 9 support](https://github.com/scala/scala-dev/issues/139) 

Scala.macro
* [same-project](https://stackoverflow.com/questions/21994764/scala-macros-and-separate-compilation-units) Scala macro build
* compiles in 3 scopes: [macro, main, test](http://scala-ide.org/docs/current-user-doc/features/scalacompiler/index.html)

Scala.js
* compiles and [links scala-js](https://github.com/scala-js/scala-js) JavaScrpt
* supports [JavaScrpt module initializers](https://random-maven.github.io/scalor-maven-plugin/2.12/scala-js-link-main-mojo.html#linkerMainInitializerList)
* same-project JS-VM + JVM [JUnit testing](https://github.com/random-scalor/scala-js-junit-tools)
* incremental cached JS linking in [Eclipse/M2E](https://random-maven.github.io/scalor-maven-plugin/2.12/scala-js-link-main-mojo.html)
* [auto-provisions](https://random-maven.github.io/scalor-maven-plugin/2.12/scala-js-env-prov-webjars-mojo.html) 
  Webjars [resources for testing](https://www.webjars.org/) 
* [auto-provisions](https://random-maven.github.io/scalor-maven-plugin/2.12/scala-js-env-prov-nodejs-mojo.html)
  JavaScript [JS-VM environments for testing](https://www.scala-js.org/doc/project/js-environments.html)

Scala.native
* compiles and links [scala-native](https://github.com/scala-native/scala-native) binary runtime
* supports embedding of [objcopy resources](http://www.linuxjournal.com/content/embedding-file-executable-aka-hello-world-version-5967)
* supports compiling of C/CPP user sources
* incremental cached Native linking in [Eclipse/M2E](https://random-maven.github.io/scalor-maven-plugin/2.12/scala-native-link-main-mojo.html)

Eclipse and Maven
* brings and installs companion [Eclipse plugin](https://github.com/random-maven/scalor-maven-plugin/blob/master/src/main/scala/com/carrotgarden/maven/scalor/EclipsePlugin.scala)
* creates custom [Scala installations for Scala IDE](http://scala-ide.org/docs/4.0.x/advancedsetup/scala-installations.html)
* exposes comprehensive [configuration and logging](https://random-maven.github.io/scalor-maven-plugin/2.12/eclipse-config-mojo.html)
* provides [identical compiler settings](https://random-maven.github.io/scalor-maven-plugin/2.12/eclipse-config-mojo.html#zincOptionsScala)
for Maven and Eclipse
* provides [identical formatter settings](https://random-maven.github.io/scalor-maven-plugin/2.12/format-mojo.html)
for Maven and Eclipse
* works around spurious crashes of [Scala IDE presentation compiler](https://random-maven.github.io/scalor-maven-plugin/2.12/eclipse-prescomp-mojo.html)
* auto-restarts test application [after full or incremental build in Eclipse](https://random-maven.github.io/scalor-maven-plugin/2.12/eclipse-restart-mojo.html)

Primary Maven goals

* [eclipse-config](https://random-maven.github.io/scalor-maven-plugin/2.12/eclipse-config-mojo.html)
* [register-main](https://random-maven.github.io/scalor-maven-plugin/2.12/register-main-mojo.html)
* [compile-main](https://random-maven.github.io/scalor-maven-plugin/2.12/compile-main-mojo.html)
* [scala-js-link-main](https://random-maven.github.io/scalor-maven-plugin/2.12/scala-js-link-main-mojo.html)
* [scala-native-link-main](https://random-maven.github.io/scalor-maven-plugin/2.12/scala-native-link-main-mojo.html)

Complete goals reference

* [Maven Goals 2.12](https://random-maven.github.io/scalor-maven-plugin/2.12/plugin-info.html)

### Eclipse setup

Prerequisites:
* [JDK-8 or JDK-9](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
* [Eclipse 4.7](http://www.eclipse.org/downloads/),
  [Maven M2E 1.8](http://www.eclipse.org/m2e/),
  [Scala IDE 4.7](http://scala-ide.org/).
* better, use
  [Maven M2E 1.9](https://repository.takari.io/content/sites/m2e.extras/m2e/1.9.0/N/LATEST/)
  and
  [Scala IDE 4.7 RC](http://scala-ide.org/download/milestone.html)
  .

Involves two steps:

1. declare `scalor-maven-plugin` in `pom.xml` editor  
   this makes plugin available for Maven and Eclipse M2E  
   make sure to provide Maven plugin goal `eclipse-config`  
  
2. invoke from menu `Eclipse -> Project -> { Auto, Clean, Build }`  
   this allows Maven plugin to install own Eclipse companion plugin  
   which in turn invokes M2E project configurator for Scala projects  

Project update tips:

* After an edit of `pom.xml`, propagate changes to Scala IDE  
  via context menu `Project -> Maven -> Update Project...`

* Activate M2E [Maven Console](https://www.ibm.com/support/knowledgecenter/SS8PJ7_9.1.0/com.ibm.etools.maven.doc/topics/troubleshooting.html)
to review  
Maven plugin and Eclipse [plugin messages](https://github.com/random-maven/scalor-maven-plugin/blob/master/note/install-log.md).

* If in doubt, review generated Eclipse descriptors:  
[.project](https://github.com/random-maven/scalor-maven-plugin/blob/master/note/eclipse.project.md),
[.classpath](https://github.com/random-maven/scalor-maven-plugin/blob/master/note/eclipse.classpath.md),
[.settings/scala-ide](https://github.com/random-maven/scalor-maven-plugin/blob/master/note/eclipse.scala-ide.md).

### Version mapping

Normally, Scala IDE itself runs 
on the latest stable Scala Library at the time frame.

For example:
* Scala IDE `4.7.0` -> Scala Library `2.12.3`

Eclipse companion plugin provided by this Maven plugin
needs to interact with Scala IDE and hence must run
on the Scala Library from the same epoch:
* Scala IDE `4.7.0` -> `scalor-maven-plugin_2.12`

However, `compiler-bridge` module provides an isolation gateway 
which allows `scalor-maven-plugin_2.12` to build projects 
with different Scala epoch, such as `2.11`, `2.12`, `2.13`

Required version mapping is provided via `scalor-maven-plugin` configuration entries:
```xml
<defineAuto>
<defineBridge>
<defineCompiler>
<definePluginList>
```

### Usage example

Project Examples:
* basic [demo project](https://github.com/random-maven/scalor-maven-plugin/blob/master/demo)
* test project for [Scala Native](https://github.com/random-maven/scalor-maven-plugin/tree/master/src/it/test-native)
* test project for cross Scala [2.11, 2.12, 2.13](https://github.com/random-maven/scalor-maven-plugin/tree/master/src/it/test-cross)
* `scalor-maven-plugin` project itself is a cross
[master](https://github.com/random-maven/scalor-maven-plugin/blob/master/pom.xml)
/
[module](https://github.com/random-maven/scalor-maven-plugin/blob/master/cross/2.12/pom.xml)
build

Command line invocation:

```bash
mvn clean install -P scalor
```

```xml
        <profile>
            <id>scalor</id>
            <build>
                <plugins>

                   <!-- Disable default compiler. -->
                   <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-compiler-plugin</artifactId>
                        <configuration>
                            <skip>true</skip>
                            <skipMain>true</skipMain>
                        </configuration>
                    </plugin>

                   <!-- Enable alternative compiler. -->
                    <plugin>
                        <groupId>com.carrotgarden.maven</groupId>
                        <artifactId>scalor-maven-plugin_2.12</artifactId>
                        <configuration>

                           <!-- Compiler bridge. -->
                            <defineBridge>
                                <dependency>
                                    <groupId>org.scala-sbt</groupId>
                                    <artifactId>compiler-bridge_${version.scala.epoch}</artifactId>
                                    <version>${version.scala.zinc}</version>
                                </dependency>
                            </defineBridge>

                           <!-- Scala compiler. -->
                            <defineCompiler>
                                <dependency>
                                    <groupId>org.scala-lang</groupId>
                                    <artifactId>scala-compiler</artifactId>
                                    <version>${version.scala.release}</version>
                                </dependency>
                            </defineCompiler>

                           <!-- Compiler plugins. -->
                            <definePluginList>
                                <dependency>
                                    <groupId>org.scalamacros</groupId>
                                    <artifactId>paradise_${version.scala.release}</artifactId>
                                    <version>${version.scala.plugin.macro}</version>
                                </dependency>
                                <dependency>
                                    <groupId>org.scala-js</groupId>
                                    <artifactId>scalajs-compiler_${version.scala.release}</artifactId>
                                    <version>${version.scalajs.release}</version>
                                </dependency>
                            </definePluginList>

                        </configuration>
                        <executions>
                            <execution>

                                <!-- Subset of available. -->
                                <goals>

                                    <!-- Setup Eclipse plugin. -->
                                    <goal>eclipse-config</goal>

                                    <!-- Transfer format settings. -->
                                    <goal>eclipse-format</goal>

                                    <!-- Manage test application. -->
                                    <goal>eclipse-restart</goal>

                                    <!-- Manage presentation compiler. -->
                                    <goal>eclipse-prescomp</goal>

                                    <!-- Add compilation sources. -->
                                    <goal>register-macro</goal>
                                    <goal>register-main</goal>
                                    <goal>register-test</goal>

                                    <!-- Compile sources. -->
                                    <goal>compile-macro</goal>
                                    <goal>compile-main</goal>
                                    <goal>compile-test</goal>

                                    <!-- Link runtime JS script. -->
                                    <goal>scala-js-link-main</goal>
                                    <goal>scala-js-link-test</goal>

                                    <!-- Provide JS-VM environment for testing. -->
                                    <goal>scala-js-env-prov-webjars</goal>
                                    <goal>scala-js-env-prov-nodejs</goal>
                                    <goal>scala-js-env-conf-nodejs</goal>

                                </goals>

                            </execution>
                        </executions>
                    </plugin>

                </plugins>
            </build>
        </profile>
```

### Build yourself

Linux
```bash
cd /tmp
git clone git@github.com:random-maven/scalor-maven-plugin.git
cd scalor-maven-plugin
./mvnw.sh clean install -B -P skip-test
```

Windows
* follow [windows build setup](https://github.com/random-maven/scalor-maven-plugin/blob/master/note/windows-setup.md)

[licence_icon]: https://img.shields.io/github/license/random-maven/scalor-maven-plugin.svg?label=License
[licence_link]: http://www.apache.org/licenses/

[travis_icon]: https://travis-ci.org/random-maven/scalor-maven-plugin.svg
[travis_link]: https://travis-ci.org/random-maven/scalor-maven-plugin/builds

[appvey_icon]: https://ci.appveyor.com/api/projects/status/5ena8xeyujneqqog?svg=true
[appvey_link]: https://ci.appveyor.com/project/random-maven/scalor-maven-plugin/history 

[tokei_files_icon]: https://tokei.rs/b1/github/random-maven/scalor-maven-plugin?category=files 
[tokei_lines_icon]: https://tokei.rs/b1/github/random-maven/scalor-maven-plugin?category=lines 
[tokei_basic_icon]: https://tokei.rs/b1/github/random-maven/scalor-maven-plugin
[tokei_basic_link]: https://github.com/random-maven/scalor-maven-plugin 

[central_212_icon]: https://img.shields.io/maven-central/v/com.carrotgarden.maven/scalor-maven-plugin_2.12.svg 
[central_212_link]: http://search.maven.org/#search%7Cga%7C1%7Cscalor-maven-plugin

[bintray_212_icon]: https://api.bintray.com/packages/random-maven/maven/scalor-maven-plugin_2.12/images/download.svg
[bintray_212_link]: https://bintray.com/random-maven/maven/scalor-maven-plugin_2.12

[gemnasium_icon]: https://gemnasium.com/random-maven/scalor-maven-plugin.svg
[gemnasium_link]: https://gemnasium.com/random-maven/scalor-maven-plugin
