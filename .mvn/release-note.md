--- 1.5.1 ---

#### Improvements
*

#### Bug fixes
*

--- 1.5.0 ---

#### Improvements
* java 11 support
* provide Eclipse Scala IDE Setup
* use eclipse 2019-03
* use m2e 1.10
* use scala 2.12.7
* use zinc 1.2.5
* use llvm 5.0 toolchain
* use scala js 0.6.27
* use scala native 0.3.9
* use scala native gc=immix
* use xenial on travis
* support m2e .classpath/attribute name="test" value="true"

#### Bug fixes
* Better advanced usage link #25
* Bump zinc version and some other plugins #23

--- 1.4.4 ---

#### Improvements
* 

#### Bug fixes
*

--- 1.4.3 ---

#### Improvements
* dependencies update 
* include `target/test-classes` in `compile-text`

#### Bug fixes
* use `Bundle-ActivationPolicy: lazy`

--- 1.4.2 ---

#### Improvements
* dependency updates

--- 1.4.1 ---

#### Improvements
* document `buildMainSourceJavaFolders`, etc
* document `scaladocRegexScalaExclude`, etc #13
* update `flatten-maven-plugin` #16

#### Bug fixes
* fix zinc log truncate of multi-line
* fix `setup-cross` properties
* document windows build setup #18

--- 1.4.0 ---

#### New features
* new goal: `setup-cross`
* initial Scala.native support

#### Breaking change
* change some `scalor.*` parameter names

#### Improvements
* update `maven-wrapper` 
* introduce `.gitattributes`
* update `scala-js-junit-tools`
* explicit JavaC compiler options for Zinc invocation
* enable `env-conf-*` and `env-prov-*` goals in Eclipse
* use constant default options for JavaC and ScalaC #14
* use separate source regex for compile and scaladoc #13

#### Bug fixes
* ensure `encoding="UTF-8"` for all `pom.xml`
* inject `scala-js-junit-tools` basedir config
* relax bundle version of `org.scala-lang.modules.scala-xml`
* relax plugin version verification rules #10
* work around nsc.Settings ignoring `-D` user-set state #11
* proper implementation for `scala.compiler.additionalParams` #12

--- 1.3.0 ---

#### Bug fixes
* some `scalor.*` parameter names

#### Improvements
* code cleanup
* new arkon goals: `clean, compile, register, ...`
* new formatter goals: `format, format-macro, format-main, format-test`

--------------
