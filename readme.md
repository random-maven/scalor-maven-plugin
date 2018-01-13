
### Scalor Maven Plugin

Build integrator for Java, Scala, Scala.macro, Scala.js, Eclipse and Maven.

[![Apache License, Version 2.0, January 2004](https://img.shields.io/github/license/mojohaus/versions-maven-plugin.svg?label=License)](http://www.apache.org/licenses/)
[![Travis Status](https://travis-ci.org/random-maven/scalor-maven-plugin.svg?branch=master)](https://travis-ci.org/random-maven/scalor-maven-plugin/builds)

##### Install Scalor Plugin 1.X for Scala IDE 4.7
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.carrotgarden.maven/scalor-maven-plugin_2.12/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/com.carrotgarden.maven/scalor-maven-plugin_2.12)
[![Download](https://api.bintray.com/packages/random-maven/maven/scalor-maven-plugin_2.12/images/download.svg)](https://bintray.com/random-maven/maven/scalor-maven-plugin_2.12/_latestVersion)

Similar plugins
* [scala-maven-plugin](https://github.com/davidB/scala-maven-plugin)
* [sbt-compiler-maven-plugin](https://github.com/sbt-compiler-maven-plugin/sbt-compiler-maven-plugin)

Plugin features
* new incremental [Zinc](https://github.com/sbt/zinc)
* uses static [compiler-bridge](https://github.com/sbt/zinc/tree/1.x/internal/compiler-bridge)
* [same-project](https://stackoverflow.com/questions/21994764/scala-macros-and-separate-compilation-units) Scala macro build
* compiles Java and Scala sources
* compiles and [links Scala.js JavaScrpt](https://github.com/scala-js/scala-js-cli)
* compiles in 3 scopes: [macro, main, test](http://scala-ide.org/docs/current-user-doc/features/scalacompiler/index.html)
* auto-discovery of [Scala compiler plugins](https://random-maven.github.io/scalor-maven-plugin/2.12/eclipse-config-mojo.html#definePluginList)
* cross-scala-version [build with simple setup](https://github.com/random-maven/scalor-maven-plugin/tree/master/src/it/test-cross)
* brings and installs its own companion [Eclipse plugin](https://github.com/random-maven/scalor-maven-plugin/blob/master/src/main/scala/com/carrotgarden/maven/scalor/EclipsePlugin.scala)
* creates custom [Scala installations for Scala IDE](http://scala-ide.org/docs/4.0.x/advancedsetup/scala-installations.html)
* comprehensive plugin [configuration and logging](https://random-maven.github.io/scalor-maven-plugin/2.12/eclipse-config-mojo.html)
* provides [identical compiler settings](https://random-maven.github.io/scalor-maven-plugin/2.12/eclipse-config-mojo.html#zincCompileOptions) 
  for Maven and Eclipse

Main Maven goals

* [eclipse-config](https://random-maven.github.io/scalor-maven-plugin/2.12/eclipse-config-mojo.html)
* [clean-main](https://random-maven.github.io/scalor-maven-plugin/2.12/clean-main-mojo.html)
* [register-main](https://random-maven.github.io/scalor-maven-plugin/2.12/register-main-mojo.html)
* [compile-main](https://random-maven.github.io/scalor-maven-plugin/2.12/compile-main-mojo.html)
* [prepack-main](https://random-maven.github.io/scalor-maven-plugin/2.12/prepack-main-mojo.html)
* [link-scala-js-main](https://random-maven.github.io/scalor-maven-plugin/2.12/link-scala-js-main-mojo.html)
* [prepack-linker-main](https://random-maven.github.io/scalor-maven-plugin/2.12/prepack-linker-main-mojo.html)

Complete goals reference

* [Maven Goals](https://random-maven.github.io/scalor-maven-plugin/2.12/plugin-info.html)

### Eclipse setup

Prerequisites:
* [Eclipse 4.7](http://www.eclipse.org/downloads/),
  [Maven M2E 1.8](http://www.eclipse.org/m2e/),
  [Scala IDE 4.7](http://scala-ide.org/).

Involves two steps:

1. declare `scalor-maven-plugin` in `pom.xml` editor  
   this makes plugin available for Maven and Eclipse M2E  
   make sure to provide Maven plugin goal `eclipse-config`  
  
2. invoke from menu `Eclipse -> Project -> { Clean, Build }`  
   this allows Maven plugin to install own Eclipse companion plugin  
   which in turn invokes M2E project configurator for Scala projects  

Project update tips:

* After an edit of `pom.xml`, propagate changes to Scala IDE  
  via context menu `Project -> Maven -> Update Project...`

* Activate M2E [Maven Console](https://www.ibm.com/support/knowledgecenter/SS8PJ7_9.1.0/com.ibm.etools.maven.doc/topics/troubleshooting.html)
to review  
Maven plugin and Eclipse [plugin messages](https://github.com/random-maven/scalor-maven-plugin/blob/master/note/install-log.md).

* When in doubt, review generated Eclipse descriptors:  
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
* Scala IDE `4.7.0` -> Scala Library `2.12.3` -> `scalor-maven-plugin_2.12`

However, `compiler-bridge` module provides an isolation gateway 
which allows `scalor-maven-plugin_2.12` to build projects 
with different Scala epoch, such as `2.11`, `2.12`, `2.13`

Required mapping is provided via `scalor-maven-plugin` configuration entries:
```xml
<defineBridge>
<defineCompiler>
<definePluginList>
```

Examples:
* test project for Scala [2.11, 2.12, 2.13](https://github.com/random-maven/scalor-maven-plugin/tree/master/src/it/test-cross)
* this project itself is a cross
[master](https://github.com/random-maven/scalor-maven-plugin/blob/master/pom.xml)
/
[module](https://github.com/random-maven/scalor-maven-plugin/blob/master/cross/2.12/pom.xml)
build

### Usage example

```
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
                            <skip>false</skip>

                           <!-- Required bridge. -->
                            <defineBridge>
                                <dependency>
                                    <groupId>org.scala-sbt</groupId>
                                    <artifactId>compiler-bridge_${version.scala.epoch}</artifactId>
                                    <version>${version.scala.zinc}</version>
                                </dependency>
                            </defineBridge>

                           <!-- Required compiler. -->
                            <defineCompiler>
                                <dependency>
                                    <groupId>org.scala-lang</groupId>
                                    <artifactId>scala-compiler</artifactId>
                                    <version>${version.scala.release}</version>
                                </dependency>
                            </defineCompiler>

                           <!-- Optional compiler plugins. -->
                            <definePluginList>
                                <dependency>
                                    <groupId>org.scalamacros</groupId>
                                    <artifactId>paradise_${version.scala.release}</artifactId>
                                    <version>${version.scala.plug.macro}</version>
                                </dependency>
                            </definePluginList>

                        </configuration>
                        <executions>
                            <execution>

                                <!-- Subset of available. -->
                                <goals>

                                    <!-- Setup Eclipse plugin. -->
                                    <goal>eclipse-config</goal>

                                    <!-- Remove build state cache. -->
                                    <goal>clean-macro</goal>
                                    <goal>clean-main</goal>
                                    <goal>clean-test</goal>

                                    <!-- Add compilation sources. -->
                                    <goal>register-macro</goal>
                                    <goal>register-main</goal>
                                    <goal>register-test</goal>

                                    <!-- Process scope=macro. -->
                                    <goal>compile-macro</goal>
                                    <goal>prepack-macro</goal>

                                    <!-- Process scope=main. -->
                                    <goal>compile-main</goal>
                                    <goal>prepack-main</goal>

                                    <!-- Process scope=test. -->
                                    <goal>compile-test</goal>
                                    <goal>prepack-test</goal>

                                    <!-- Link JS in scope=main. -->
                                    <goal>link-scala-js-main</goal>
                                    <goal>prepack-linker-main</goal>

                                    <!-- Link JS in scope=test. -->
                                    <goal>link-scala-js-test</goal>
                                    <goal>prepack-linker-test</goal>

                                </goals>

                            </execution>
                        </executions>
                    </plugin>

                </plugins>
            </build>
        </profile>
```

### Build yourself

```
cd /tmp
git clone git@github.com:random-maven/scalor-maven-plugin.git
cd scalor-maven-plugin
./mvnw.sh clean install -B -P skip-test
```
