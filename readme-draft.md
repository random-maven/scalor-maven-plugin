
### Scalor Maven Plugin

Build integrator for Java, Scala, Scala.macro, Scala.js, Eclipse and Maven.

[![Apache License, Version 2.0, January 2004](https://img.shields.io/github/license/mojohaus/versions-maven-plugin.svg?label=License)](http://www.apache.org/licenses/)
[![Travis Status](https://travis-ci.org/random-maven/scalor-maven-plugin.svg?branch=master)](https://travis-ci.org/random-maven/scalor-maven-plugin/builds)

##### Scala IDE 4.7.X / Scala Library 2.12.X / Scalor Plugin 1.0.X
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.carrotgarden.maven/scalor-maven-plugin/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/com.carrotgarden.maven/scalor-maven-plugin_2.12)
[![Download](https://api.bintray.com/packages/random-maven/maven/scalor-maven-plugin_2.12/images/download.svg)](https://bintray.com/random-maven/maven/scalor-maven-plugin_2.12/_latestVersion)

Similar plugins
* [scala-maven-plugin](https://github.com/davidB/scala-maven-plugin)
* [sbt-compiler-maven-plugin](https://github.com/sbt-compiler-maven-plugin/sbt-compiler-maven-plugin)

Plugin features
* new incremental [zinc](https://github.com/sbt/zinc)
* uses static [compiler-bridge](https://github.com/sbt/zinc/tree/1.x/internal/compiler-bridge)
* [same-project](https://stackoverflow.com/questions/21994764/scala-macros-and-separate-compilation-units) Scala macro build
* compiles Java and Scala sources
* compiles and [links Scala.js JavaScrpt](https://github.com/scala-js/scala-js-cli)
* compiles in 3 [scopes: macro, main, test](http://scala-ide.org/docs/current-user-doc/features/scalacompiler/index.html)
* auto-discovery of Scala compiler plugins
* cross-scala-version [build with simple setup](https://github.com/random-maven/scalor-maven-plugin/tree/master/src/it/test-cross)
* brings and install its own companion [Eclipse plugin](https://github.com/random-maven/scalor-maven-plugin/blob/master/src/main/scala/com/carrotgarden/maven/scalor/EclipsePlugin.scala)
* creates custom [Scala installations for Scala IDE](http://scala-ide.org/docs/4.0.x/advancedsetup/scala-installations.html)
* provides *identical compiler settings* for Maven and Eclipse
* simple, modular, easy to maintain Maven/Eclipse plugin design 

Maven goals

* [scalor:eclipse-config](https://random-maven.github.io/scalor-maven-plugin/eclipse-config-mojo.html)

* [scalor:register-macro](https://random-maven.github.io/scalor-maven-plugin/register-macro-mojo.html)
* [scalor:register-main](https://random-maven.github.io/scalor-maven-plugin/register-main-mojo.html)
* [scalor:register-test](https://random-maven.github.io/scalor-maven-plugin/register-test-mojo.html)

* [scalor:compile-macro](https://random-maven.github.io/scalor-maven-plugin/compile-macro-mojo.html)
* [scalor:prepack-macro](https://random-maven.github.io/scalor-maven-plugin/prepack-macro-mojo.html)
  
* [scalor:compile-main](https://random-maven.github.io/scalor-maven-plugin/compile-main-mojo.html)
* [scalor:prepack-main](https://random-maven.github.io/scalor-maven-plugin/prepack-main-mojo.html)
  
* [scalor:compile-test](https://random-maven.github.io/scalor-maven-plugin/compile-test-mojo.html)
* [scalor:prepack-test](https://random-maven.github.io/scalor-maven-plugin/prepack-test-mojo.html)

* [scalor:link-scala-js-main](https://random-maven.github.io/scalor-maven-plugin/link-scala-js-main-mojo.html)
* [scalor:prepack-linker-main](https://random-maven.github.io/scalor-maven-plugin/prepack-linker-main-mojo.html)

* [scalor:link-scala-js-test](https://random-maven.github.io/scalor-maven-plugin/link-scala-js-test-mojo.html)
* [scalor:prepack-linker-test](https://random-maven.github.io/scalor-maven-plugin/prepack-linker-test-mojo.html)

### Version mapping

Normally, Scala IDE itself runs 
on the latest stable Scala library at the time frame.

For example:
* Scala IDE `4.7.0` -> Scala Library `2.12.3`

Eclipse companion plugin provided by this Maven plugin
needs to interact with Scala IDE and hence must run
on the Scala library from the same epoch:
* Scala IDE `4.7.0` -> Scala Library `2.12.3` -> `scalor-maven-plugin_2.12`

However, as it is usual in Scala tools ecosystem, 
`compiler-bridge` module provides an isolation gateway 
which allows `scalor-maven-plugin_2.12` to build projects 
with different epoch, such as `2.11`, `2.12`, `2.13`

Required mapping is provided via `scalor-maven-plugin` configuration entries:
```
<defineBridge>
<defineCompiler>
<definePluginList>
```

Examples:
* test project for Scala [2.11, 2.12, 2.13](https://github.com/random-maven/scalor-maven-plugin/tree/master/src/it/test-cross)

### Eclipse setup

Involves three steps:

1. declare `scalor-maven-plugin` in `pom.xml`  
   this makes plugin available for Maven and Eclipse  
   make sure to provide Maven plugin goal `eclipse-config`  
  
2. now, invoke `Eclipse -> Project -> Clean -> Build`  
   this allows Maven plugin to install Eclipse companion plugin  
   which in turn provides M2E project configurator for Scala projects  
  
3. finally, invoke `Project -> Maven -> Update Project...`  
   this executes M2E project configurator and changes Eclipse  
   Scala project properties to match Maven provided configuration  

Enable M2E `Maven Console` to review Maven and Eclispse plugin messages.  

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

                                <!-- Keep in order for M2E. -->
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
