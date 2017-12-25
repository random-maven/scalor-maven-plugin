
### Scalor Maven Plugin

A builder for Java, Scala, Scala.macro, Scala.js, Eclipse and Maven

[![Apache License, Version 2.0, January 2004](https://img.shields.io/github/license/mojohaus/versions-maven-plugin.svg?label=License)](http://www.apache.org/licenses/)
[![Travis Status](https://travis-ci.org/random-maven/scalor-maven-plugin.svg?branch=master)](https://travis-ci.org/random-maven/scalor-maven-plugin/builds)

##### Scala-2.11
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.carrotgarden.maven/scalor-maven-plugin/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/com.carrotgarden.maven/scalor-maven-plugin_2.11)
[![Download](https://api.bintray.com/packages/random-maven/maven/scalor-maven-plugin_2.11/images/download.svg)](https://bintray.com/random-maven/maven/scalor-maven-plugin_2.11/_latestVersion)

##### Scala-2.12
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.carrotgarden.maven/scalor-maven-plugin/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/com.carrotgarden.maven/scalor-maven-plugin_2.12)
[![Download](https://api.bintray.com/packages/random-maven/maven/scalor-maven-plugin_2.12/images/download.svg)](https://bintray.com/random-maven/maven/scalor-maven-plugin_2.12/_latestVersion)

Similar plugins
* [scala-maven-plugin](https://github.com/davidB/scala-maven-plugin)
* [sbt-compiler-maven-plugin](https://github.com/sbt-compiler-maven-plugin/sbt-compiler-maven-plugin)

Plugin features
* incremental [Zinc 1.x](https://github.com/sbt/zinc)
* Eclipse configuration
* uses static [compiler-bridge](https://github.com/sbt/zinc/tree/1.x/internal/compiler-bridge)
* does auto-restart in Eclipse
* [same-project](https://stackoverflow.com/questions/21994764/scala-macros-and-separate-compilation-units) Scala macro build
* compiles Java and Scala sources
* compiles and [links Scala.js JavaScrpt](https://github.com/scala-js/scala-js-cli)
* compiles in 3 [scopes: macro, main, test](http://scala-ide.org/docs/current-user-doc/features/scalacompiler/index.html)
* auto-discovery of Scala compiler plugins
* [cross Scala](https://github.com/random-maven/scalor-maven-plugin/blob/master/.mvn/release-bintray-all.sh) version build with [flatten plugin](https://github.com/random-maven/flatten-maven-plugin)

Maven goals

* [scalor:register-macro](https://random-maven.github.io/scalor-maven-plugin/register-macro-mojo.html)
* [scalor:register-main](https://random-maven.github.io/scalor-maven-plugin/register-main-mojo.html)
* [scalor:register-test](https://random-maven.github.io/scalor-maven-plugin/register-test-mojo.html)

* [scalor:compile-macro](https://random-maven.github.io/scalor-maven-plugin/compile-macro-mojo.html)
* [scalor:compile-main](https://random-maven.github.io/scalor-maven-plugin/compile-main-mojo.html)
* [scalor:compile-test](https://random-maven.github.io/scalor-maven-plugin/compile-test-mojo.html)

* [scalor:prepack-macro](https://random-maven.github.io/scalor-maven-plugin/prepack-macro-mojo.html)
* [scalor:prepack-main](https://random-maven.github.io/scalor-maven-plugin/prepack-main-mojo.html)
* [scalor:prepack-test](https://random-maven.github.io/scalor-maven-plugin/prepack-test-mojo.html)

* [scalor:link-scala-js-main](https://random-maven.github.io/scalor-maven-plugin/link-scala-js-main-mojo.html)
* [scalor:link-scala-js-test](https://random-maven.github.io/scalor-maven-plugin/link-scala-js-test-mojo.html)

* [scalor:prepack-linker-main](https://random-maven.github.io/scalor-maven-plugin/prepack-linker-main-mojo.html)
* [scalor:prepack-linker-test](https://random-maven.github.io/scalor-maven-plugin/prepack-linker-test-mojo.html)

Usage exampl


Enable extensions to activate scalor plugin life cycle

```xml
        <profile>
            <id>scalor</id>
            <build>
                <plugins>

                   <!-- Enable life cycle -->
                    <plugin>
                        <groupId>com.carrotgarden.maven</groupId>
                        <artifactId>scalor-maven-plugin</artifactId>
                        <extensions>true</extensions>
                    </plugin>
                </plugins>
            </build>
        </profile>
```

Alternatively, use explicit activation for each individual goal: This approach is 
also required to activate goals in Eclipse M2E

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
                        <extensions>false</extensions>
                        <configuration>
                            <skip>false</skip>
                        </configuration>
                        <executions>
                            <execution>
                                <goals>
                                    <!-- Keep order. -->
                                    <goal>auto-conf</goal>
                                    <goal>eclipse-project</goal>
                                    <goal>eclipse-settings</goal>
                                    <goal>eclipse-classpath</goal>
                                    <goal>register-macro</goal>
                                    <goal>register-main</goal>
                                    <goal>register-test</goal>
                                    <goal>compile-macro</goal>
                                    <goal>prepack-macro</goal>
                                    <goal>compile-main</goal>
                                    <goal>prepack-main</goal>
                                    <goal>compile-test</goal>
                                    <goal>prepack-test</goal>
                                    <goal>link-scala-js-main</goal>
                                    <goal>prepack-linker-main</goal>
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
