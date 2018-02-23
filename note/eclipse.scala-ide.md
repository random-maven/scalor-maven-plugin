
### Sample Sclala IDE `.settings/...scala-ide...` descriptor

Location:
* `${project.basedir}/.settings/org.scala-ide.sdt.core.prefs`

What to look for:
* `scalor.install.title` as expected
* `Xplugin` comes from `definePluginList`
* plugin-provided `zincCompileOptions` are present

Note:
* this file is a bag of options from different sources, ignore unfamiliar
* in this file ScalaC settings loose `-` command line prefix: `-Xplugin` -> `Xplugin`

```
//src/macro/java=macros
//src/macro/scala=macros
//src/main/java=main
//src/main/resources=main
//src/main/scala=main
//src/test/java=tests
//src/test/resources=tests
//src/test/scala=tests
P=scalajs\:sjsDefinedByDefault
Xplugin=/home/work/repository/maven/org/scala-js/scalajs-compiler_2.12.4/0.6.22/scalajs-compiler_2.12.4-0.6.22.jar,/home/work/repository/maven/org/scala-js/scalajs-junit-test-plugin_2.12.4/0.6.22/scalajs-junit-test-plugin_2.12.4-0.6.22.jar,/home/work/repository/maven/org/scalamacros/paradise_2.12.4/2.1.1/paradise_2.12.4-2.1.1.jar
apiDiff=false
compileorder=Mixed
deprecation=true
eclipse.preferences.version=1
encoding=UTF-8
feature=true
formatter.danglingCloseParenthesis=Force
formatter.firstArgumentOnNewline=Force
formatter.firstParameterOnNewline=Force
formatter.useProjectSpecificSettings=true
recompileOnMacroDef=true
relationsDebug=false
scala.compiler.additionalParams=
scala.compiler.installation=1162769519
scala.compiler.sourceLevel=2.12
scala.compiler.useProjectSettings=true
scalor.install.title=Scalor [fbb38a902d9f6ad4e24682cd5e1c3aab]
stopBuildOnError=true
target=jvm-1.8
unchecked=true
useScopesCompiler=true
withVersionClasspathValidator=true
```
