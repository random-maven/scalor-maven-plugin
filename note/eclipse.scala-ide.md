
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
Xmaxerrs=10
Xplugin=/home/work/repository/maven/org/scalamacros/paradise_2.12.4/2.1.1/paradise_2.12.4-2.1.1.jar
apiDiff=false
compileorder=JavaThenScala
deprecation=true
eclipse.preferences.version=1
encoding=UTF-8
feature=true
recompileOnMacroDef=true
relationsDebug=false
scala.compiler.additionalParams=
scala.compiler.installation=-832863704
scala.compiler.sourceLevel=2.12
scala.compiler.useProjectSettings=true
scalor.install.title=Scalor [981902fc7cb894f2d84d236d65b71372]
stopBuildOnError=true
target=jvm-1.8
unchecked=true
useScopesCompiler=true
withVersionClasspathValidator=true
```
