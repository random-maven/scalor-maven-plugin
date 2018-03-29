package com.carrotgarden.maven.scalor.eclipse

import java.io.File

import org.apache.maven.plugins.annotations.Parameter

import com.carrotgarden.maven.scalor.base
import com.carrotgarden.maven.scalor.format
import com.carrotgarden.maven.scalor.zinc
import com.carrotgarden.maven.tools.Description

/**
 * Maven plugin parameters used to control companion Eclipse plugin.
 */
trait ParamsConfigBase extends AnyRef
  with base.ParamsAny
  with base.ParamsCompiler
  with base.BuildMacro
  with base.BuildMain
  with base.BuildTest
  with zinc.ParamsCompileOptions
  with zinc.ParamScalaInstall
  with ParamsOrder
  with ParamsLogger
  with ParamsComment
  with ParamsLibrary
  with ParamsPreferences
  with ParamsVersionMaven
  with ParamsVersionScala
  with ParamsHackSymlinks
  with ParamsLogConfig {

  override def paramsLogConfig = eclipseLogParamsConfig

}

/**
 * Manage test application restart after full or incremental build in Eclipse/M2E.
 */
trait ParamsRestartBase extends AnyRef
  with base.ParamsAny
  with base.BuildTest
  with ParamsRestartCore
  with ParamsLogConfig {

  override def paramsLogConfig = eclipseRestartLogParameters

}

/**
 * Manage Scala IDE Scala presentation compiler work-around process in Eclipse/M2E.
 */
trait ParamsPrescompBase extends AnyRef
  with ParamsPrescompCore
  with ParamsLogConfig {

  override def paramsLogConfig = eclipsePrescompLogParameters

}

/**
 * Apply source format settings from Maven to Eclipse.
 */
trait ParamsFormatBase extends AnyRef
  with ParamsFormatCore
  with ParamsLogConfig {

  override def paramsLogConfig = eclipseFormatLogParameters

}

/**
 * Apply source format settings from Maven to Eclipse.
 */
trait ParamsFormatCore extends AnyRef
  with format.ParamsSettings {

  @Description( """
  Enable to transfer source format settings from Maven to Eclipse.
  """ )
  @Parameter(
    property     = "scalor.eclipseFormatEnable",
    defaultValue = "true"
  )
  var eclipseFormatEnable : Boolean = _

  @Description( """
  Enable to log in Eclipse/M2E effective configuration parameters for this Maven execution.
  """ )
  @Parameter(
    property     = "scalor.eclipseFormatLogParameters",
    defaultValue = "false"
  )
  var eclipseFormatLogParameters : Boolean = _

}

/**
 * Manage test application restart after full or incremental build in Eclipse/M2E.
 */
trait ParamsRestartCore {

  @Description( """
  Enable test application with automatic restart management in Eclipse.
  Application parameter: <a href="#eclipseRestartMainClass"><b>eclipseRestartMainClass</b></a>.
  """ )
  @Parameter(
    property     = "scalor.eclipseRestartEnable",
    defaultValue = "true"
  )
  var eclipseRestartEnable : Boolean = _

  @Description( """
  Fully qualified class name which represents test application used for auto-restart.
  This class must be a Scala object with Java "main" contract, for example, in file <code>test/Main.scala</code>:
<pre>
package test
object Main {
  def main(args: Array[String]): Unit = {
    while(true) {
      println("test-main")
      Thread.sleep(5000)
    }
  }
}
</pre>
  Normally, this class should be placed in
  <a href="#buildTestSourceScalaFolders"><b>buildTestSourceScalaFolders</b></a>
  (<code>src/test/scala</code>) registered source root.
  Test application will be restarted after full or incremental build in Eclipse, 
  after resource change detection, following a settlement delay. 
  Test application will also be restarted when it exits or crashes.
  Test application is launched in a separate JVM.
  Enablement parameter: <a href="#eclipseRestartEnable"><b>eclipseRestartEnable</b></a>.
  Settlement parameter: <a href="#eclipseRestartPeriodSettle"><b>eclipseRestartPeriodSettle</b></a>.
  Detection parameter: <a href="#eclipseRestartRegexList"><b>eclipseRestartRegexList</b></a>.
  Java launch parameter: <a href="#eclipseRestartJavaArgs"><b>eclipseRestartJavaArgs</b></a>.
  """ )
  @Parameter(
    property     = "scalor.eclipseRestartMainClass",
    defaultValue = "test.Main"
  )
  var eclipseRestartMainClass : String = _

  @Description( """
  Working directory used to launch test application. Absolute path.
  """ )
  @Parameter(
    property     = "scalor.eclipseRestartWorkDir",
    defaultValue = "${project.build.directory}/scalor/test-main"
  )
  var eclipseRestartWorkDir : File = _

  @Description( """
  List of command line arguments for Java executable.
  See <a href="https://docs.oracle.com/javase/8/docs/technotes/tools/unix/java.html">options reference<a>.
  Separator parameter: <a href="#commonSequenceSeparator"><b>commonSequenceSeparator</b></a>.
  """ )
  @Parameter(
    property     = "scalor.eclipseRestartJavaArgs",
    defaultValue = """
    -Dscalor.test.app=${project.artifactId} ★
    -Xms1G ★
    -Xmx1G ★
    """
  )
  var eclipseRestartJavaArgs : String = _

  @Description( """
  List of environment variables for Java executable.
  Separator parameter: <a href="#commonSequenceSeparator"><b>commonSequenceSeparator</b></a>.
  Mapping parameter: <a href="#commonMappingPattern"><b>commonMappingPattern</b></a>.
  """ )
  @Parameter(
    property     = "scalor.eclipseRestartJavaVars",
    defaultValue = """
    HOME=${project.basedir} ★
    USER=scalor ★
    """
  )
  var eclipseRestartJavaVars : String = _

  @Description( """
  Eclipse background management job name representing running test application.
  Canceling this management job in Eclipse UI will terminate running test application.
  M2E project update or clean/build in Eclipse UI will re-create both management job and test application.
  Job name must be unique in Eclipse workspace.
  Application parameter: <a href="#eclipseRestartMainClass"><b>eclipseRestartMainClass</b></a>.
  To review current background jobs in Eclipse, navigate:
<pre>
Eclipse -> Window -> Show View -> Progress 
</pre>
  """ )
  @Parameter(
    property     = "scalor.eclipseRestartTaskName",
    defaultValue = "Scalor: application restart manager @ ${project.artifactId}"
  )
  var eclipseRestartTaskName : String = _

  @Description( """
  List of regular expressions used to detect test application restart condition.
  These resources are monitored in folders
  <a href="#buildTestDependencyFolders"><b>buildTestDependencyFolders</b></a> 
  included as effective project dependencies resolved by Eclipse/M2E,
  such as <code>target/classes</code>, <code>target/test-classes</code>,
  from current project as well as from accessible dependecy projects in the workspace.
  Normally matches Scala JVM classes, Scala.js IR classes, configuration files.
  Separator parameter: <a href="#commonSequenceSeparator"><b>commonSequenceSeparator</b></a>.
  Review actual class path with <a href="#eclipseRestartLogCommand"><b>eclipseRestartLogCommand</b></a>. 
  """ )
  @Parameter(
    property     = "scalor.eclipseRestartRegexList",
    defaultValue = """
    ^.+[.]class$ ★
    ^.+[.]sjsir$ ★
    ^.+[.]conf$ ★
    ^.+[.]js$ ★
    """
  )
  var eclipseRestartRegexList : String = _

  @Description( """
  Test application change detection settlement time window, milliseconds. 
  Actual restart will occur only when there are no more resource changes during this delay time window.
  Application parameter: <a href="#eclipseRestartMainClass"><b>eclipseRestartMainClass</b></a>.
  """ )
  @Parameter(
    property     = "scalor.eclipseRestartPeriodSettle",
    defaultValue = "3000"
  )
  var eclipseRestartPeriodSettle : Long = _

  @Description( """
  Test application wait-before-restart delay time window, milliseconds.
  Used as delay for application restart after an exit or crash, to prevent restart flood.  
  Application parameter: <a href="#eclipseRestartMainClass"><b>eclipseRestartMainClass</b></a>.
  """ )
  @Parameter(
    property     = "scalor.eclipseRestartPeriodPrevent",
    defaultValue = "3000"
  )
  var eclipseRestartPeriodPrevent : Long = _

  @Description( """
  Restart management task checks invocation period, milliseconds.
  Defines frequency of test application liveness checks and resource change detection checks. 
  Application parameter: <a href="#eclipseRestartMainClass"><b>eclipseRestartMainClass</b></a>.
  """ )
  @Parameter(
    property     = "scalor.eclipseRestartPeriodInvoke",
    defaultValue = "1000"
  )
  var eclipseRestartPeriodInvoke : Long = _

  @Description( """
  Limit number of records reported by loggers.
  Detector logger enablement: <a href="#eclipseRestartLogDetected"><b>eclipseRestartLogDetected</b></a>.
  """ )
  @Parameter(
    property     = "scalor.eclipseRestartLimitLogger",
    defaultValue = "20"
  )
  var eclipseRestartLimitLogger : Int = _

  @Description( """
  Enable to log command used to launch managed test application.
  Includes Java executable path, executable arguments, effective dependency class path, main class name. 
  """ )
  @Parameter(
    property     = "scalor.eclipseRestartLogCommand",
    defaultValue = "false"
  )
  var eclipseRestartLogCommand : Boolean = _

  @Description( """
  Enable to log list of changed resources which have triggred test application restart.
  Detector parameter: <a href="#eclipseRestartRegexList"><b>eclipseRestartRegexList</b></a>.
  Logs limit parameter: <a href="#eclipseRestartLimitLogger"><b>eclipseRestartLimitLogger</b></a>.
  """ )
  @Parameter(
    property     = "scalor.eclipseRestartLogDetected",
    defaultValue = "true"
  )
  var eclipseRestartLogDetected : Boolean = _

  @Description( """
  Enable to log all resource change events in the Eclipse workspace. 
  Generates execessive logging, use only for problem discovery.
  """ )
  @Parameter(
    property     = "scalor.eclipseRestartLogChanged",
    defaultValue = "false"
  )
  var eclipseRestartLogChanged : Boolean = _

  @Description( """
  Enable to log in Eclipse/M2E effective configuration parameters for this Maven execution.
  """ )
  @Parameter(
    property     = "scalor.eclipseRestartLogParameters",
    defaultValue = "false"
  )
  var eclipseRestartLogParameters : Boolean = _

}

/**
 * Manage Scala IDE Scala presentation compiler work-around process in Eclipse/M2E.
 */
trait ParamsPrescompCore {

  @Description( """
  Enable to work around spurious crashes of Scala IDE presentation compiler.
  Specifically, periodically analyze managed Scala IDE project,
  detect crashed presentation compiler instance, and issue restart.
  Periodicity parameter: <a href="#eclipsePrescompPeriodInvoke"><b>eclipsePrescompPeriodInvoke</b></a>.
  """ )
  @Parameter(
    property     = "scalor.eclipsePrescompEnable",
    defaultValue = "true"
  )
  var eclipsePrescompEnable : Boolean = _

  @Description( """
  Enable to log presentation compiler compilation units with problems at serverity <code>level=error</code>.
  These compilation units are monitored by mainenace task and trigger presentation compiler restart requests.
  Enablement parameter: <a href="#eclipsePrescompEnable"><b>eclipsePrescompEnable</b></a>.
  Periodicity parameter: <a href="#eclipsePrescompPeriodInvoke"><b>eclipsePrescompPeriodInvoke</b></a>.
  """ )
  @Parameter(
    property     = "scalor.eclipsePrescompLogErrorUnits",
    defaultValue = "true"
  )
  var eclipsePrescompLogErrorUnits : Boolean = _

  @Description( """
  Enable to log in Eclipse/M2E effective configuration parameters for this Maven execution.
  """ )
  @Parameter(
    property     = "scalor.eclipsePrescompLogParameters",
    defaultValue = "false"
  )
  var eclipsePrescompLogParameters : Boolean = _

  @Description( """
  Period of presentation compiler maintenance job invocations, milliseconds. 
  Defines frequency of presentation compiler liveness checks.
  Enablement parameter: <a href="#eclipsePrescompEnable"><b>eclipsePrescompEnable</b></a>.
  """ )
  @Parameter(
    property     = "scalor.eclipsePrescompPeriodInvoke",
    defaultValue = "5000"
  )
  var eclipsePrescompPeriodInvoke : Long = _

  @Description( """
  Name of Eclipse background job representing Scala IDE presentation compiler work-around process.
  Canceling this job in Eclipse UI will terminate presentation compiler work-around process.
  M2E project update or clean/build in Eclipse UI will re-create management job.
  Job name must be unique in Eclipse workspace.
  Enablement parameter: <a href="#eclipsePrescompEnable"><b>eclipsePrescompEnable</b></a>.
  To review current background jobs in Eclipse, navigate:
<pre>
Eclipse -> Window -> Show View -> Progress 
</pre>
  """ )
  @Parameter(
    property     = "scalor.eclipsePrescompTaskName",
    defaultValue = "Scalor: presenation compiler manager @ ${project.artifactId}"
  )
  var eclipsePrescompTaskName : String = _

}

trait ParamsHackSymlinks {

  @Description( """
  Work around Scala IDE mishandling of symbolic link paths.
  Specifically, discover project folders which are symbolic links,
  and explicitly declare them in the <code>.project</code> descriptor.
  Example result entry in the descriptor: 
<pre>
&lt;linkedResources&gt;
	&lt;link&gt;
		&lt;name&gt;src&lt;/name&gt;
		&lt;type&gt;2&lt;/type&gt;
		&lt;location&gt;/home/work/source/git/scalor-maven-plugin/src&lt;/location&gt;
	&lt;/link&gt;
&lt;/linkedResources&gt;
</pre>
  """ )
  @Parameter(
    property     = "scalor.eclipseHackSymbolicLinks",
    defaultValue = "false"
  )
  var eclipseHackSymbolicLinks : Boolean = _

}

trait ParamsPreferences {

  @Description( """
  Reset Eclipse Scala IDE project preferences 
  to their default values before applying Maven provided configuration.
  Use this feature to remove extraneous user-provided Eclipse UI configuration settings for Scala IDE.
  """ )
  @Parameter(
    property     = "scalor.eclipseResetPreferences",
    defaultValue = "true"
  )
  var eclipseResetPreferences : Boolean = _

}

trait ParamsLibrary {

  @Description( """
  Enable to remove Scala Library container from Eclipse build class path.
  Normally, Scala Library container is provided by default by Scala IDE plugin.
  Normally, scala-library artifact must also be provided as <code>pom.xml</code> dependency.
  That results in scala-library dependency listed twice on Eclipse build class path.
  To avoid "duplicate library on class path" warning, 
  use this parameter to remove Scala IDE provided container.
  Custom Scala installation configured by this plugin will use scala-library resolved from
    <a href="#defineCompiler"><b>defineCompiler</b></a>, 
  which normally should be configured as identical to the scala-library 
  provided as <code>pom.xml</code> project dependency.
  """ )
  @Parameter(
    property     = "scalor.eclipseRemoveLibraryContainer",
    defaultValue = "true"
  )
  var eclipseRemoveLibraryContainer : Boolean = _

  @Description( """
  Enable to rename Scala Library container in Eclipse UI.
  Container description will reflect title of the custom Scala installation. 
  Only effective when not removing scala-library container via parameter: 
    <a href="#eclipseRemoveLibraryContainer"><b>eclipseRemoveLibraryContainer</b></a>.
  """ )
  @Parameter(
    property     = "scalor.eclipseRenameLibraryContainer",
    defaultValue = "true"
  )
  var eclipseRenameLibraryContainer : Boolean = _

}

trait ParamsComment {

  //  @Description( """
  //  Enable to apply comment in Scala IDE settings file.
  //  <pre>${project.basedir}/.settings/org.scala-ide.sdt.core.prefs</pre>
  //  """ )
  //  @Parameter(
  //    property     = "scalor.eclipseScalaSettingsCommentApply",
  //    defaultValue = "true"
  //  )
  //  var eclipseScalaSettingsCommentApply : Boolean = _
  //
  //  @Description( """
  //  Content of the comment in Scala IDE settings file.
  //  <pre>${project.basedir}/.settings/org.scala-ide.sdt.core.prefs</pre>
  //  """ )
  //  @Parameter(
  //    property     = "scalor.eclipseScalaSettingsCommentString",
  //    defaultValue = "scalor-maven-plugin @ ${project.properties(release.stamp)}"
  //  )
  //  var eclipseScalaSettingsCommentString : String = _

  @Description( """
  Enable to apply comment in Eclipse <code>.project</code> descriptor.
  """ )
  @Parameter(
    property     = "scalor.eclipseProjectCommentApply",
    defaultValue = "true"
  )
  var eclipseProjectCommentApply : Boolean = _

  @Description( """
  Content of the comment in eclipse <code>.project</code> descriptor.
  """ )
  @Parameter(
    property     = "scalor.eclipseProjectCommentString",
    defaultValue = "scalor-maven-plugin @ ${project.properties(release.stamp)}"
  )
  var eclipseProjectCommentString : String = _

}

/**
 * Enabale logging of plugin configuration for given Eclipse goal.
 */
trait ParamsLogConfig {

  def paramsLogConfig : Boolean

}

trait ParamsLogger {

  @Description( """
  Enable to log persisting of configured project Scala IDE settings.
  Use to review actual stored per-project <code>key=value</code>.
  """ )
  @Parameter(
    property     = "scalor.eclipseLogPersistSettings",
    defaultValue = "false"
  )
  var eclipseLogPersistSettings : Boolean = _

  @Description( """
  Enable to log persisting of selected project Scala compiler.
  Use to review complier title with MD5.
  """ )
  @Parameter(
    property     = "scalor.eclipseLogPersistCompiler",
    defaultValue = "false"
  )
  var eclipseLogPersistCompiler : Boolean = _

  @Description( """
  Enable to log persisting of custom Scala installation.
  Use to review complier configuration for given project.
  """ )
  @Parameter(
    property     = "scalor.eclipseLogPersistInstall",
    defaultValue = "false"
  )
  var eclipseLogPersistInstall : Boolean = _

  @Description( """
  Enable to log class path re-ordering result entries.
  Use to review actual full class path dump.
  """ )
  @Parameter(
    property     = "scalor.eclipseLogClasspathOrder",
    defaultValue = "false"
  )
  var eclipseLogClasspathOrder : Boolean = _

  //  @Description( """
  //  Enable to log generated resolved custom Scala installation.
  //  Use to review installation details, including title, dependencies, etc.
  //  """ )
  //  @Parameter(
  //    property     = "scalor.eclipseLogInstallResolve",
  //    defaultValue = "false"
  //  )
  //  var eclipseLogInstallResolve : Boolean = _

  @Description( """
  Report all available custom Scala installations persisted by Eclipse Scala IDE plugin.
  Provides titles, dependencies, etc., for every custom Scala installation.
  Generated report output file parameter: 
    <a href="#eclipseInstallReportFile"><b>eclipseInstallReportFile</b></a>.
  """ )
  @Parameter(
    property     = "scalor.eclipseLogInstallReport",
    defaultValue = "false"
  )
  var eclipseLogInstallReport : Boolean = _

  @Description( """
  Report all available custom Scala installations persisted by Scala IDE.
  Provides titles, dependencies, etc., for every custom Scala installation.
  Enablement parameter:
    <a href="#eclipseLogInstallReport"><b>eclipseLogInstallReport</b></a>.
  """ )
  @Parameter(
    property     = "scalor.eclipseInstallReportFile",
    defaultValue = "${project.build.directory}/scalor/scala-install-report.txt"
  )
  var eclipseInstallReportFile : File = _

  @Description( """
  Enable to log effective configuration parameters of Eclipse companion plugin.
  Use to review full dump of settings transferred from Maven to Eclipse.
  """ )
  @Parameter(
    property     = "scalor.eclipseLogParamsConfig",
    defaultValue = "false"
  )
  var eclipseLogParamsConfig : Boolean = _

  @Description( """
  Enable to log M2E build participant executions of Eclipse companion plugin.
  Use to review actual build actions delegated from Eclipse to Maven plugin goals.
  """ )
  @Parameter(
    property     = "scalor.eclipseLogBuildParticipant",
    defaultValue = "false"
  )
  var eclipseLogBuildParticipant : Boolean = _

}

trait ParamsOrder {

  @Description( """
  Enable to re-order Eclipse <code>.project</code> descriptor by 
<pre>
  projectDescription/buildSpec/buildCommand/name
</pre>
  Builder order is important in Eclipse builds.
  Ordering parameter: <a href="#eclipseBuilderOrdering"><b>eclipseBuilderOrdering</b></a>.
  """ )
  @Parameter(
    property     = "scalor.eclipseBuilderReorder",
    defaultValue = "true"
  )
  var eclipseBuilderReorder : Boolean = _

  @Description( """
  Order Eclipse <code>.project</code> descriptor builder entries according to these rules.
  Rule format: 
<pre>
item = path
  where:
  item - relative sort order index,
  path - regular expression to match against 'projectDescription/buildSpec/buildCommand/name', 
</pre>  
  Separator parameter: <a href="#commonSequenceSeparator"><b>commonSequenceSeparator</b></a>.
  Enablement parameter: <a href="#eclipseBuilderReorder"><b>eclipseBuilderReorder</b></a>.
  """ )
  @Parameter(
    property     = "scalor.eclipseBuilderOrdering",
    defaultValue = """
    
    11 = org.eclipse.wst.+ ★
    12 = org.eclipse.dltk.+ ★
    
    41 = org.eclipse.jdt.+ ★
    42 = org.scala-ide.sdt.+ ★ 
    
    71 = org.eclipse.pde.+ ★
    72 = org.eclipse.m2e.+ ★
    
    """
  )
  var eclipseBuilderOrdering : String = _

  @Description( """
  Enable to re-order Eclipse <code>.classpath</code> descriptor by
<pre>
  classpath/classpathentry/@path
</pre>
  Ordering parameter: <a href="#eclipseClasspathOrdering"><b>eclipseClasspathOrdering</b></a>.
  """ )
  @Parameter(
    property     = "scalor.eclipseClasspathReorder",
    defaultValue = "true"
  )
  var eclipseClasspathReorder : Boolean = _

  @Description( """
  Re-order Eclipse top level <code>.classpath</code> descriptor entries according to these rules.
  Class path entry order controls visual presentation in Eclipse UI.
  Rule format:
<pre>
item = path
  where:
  item - relative sort order index,
  path - regular expression to match against 'classpath/classpathentry/@path', 
</pre>  
  Separator parameter: <a href="#commonSequenceSeparator"><b>commonSequenceSeparator</b></a>.
  Enablement parameter: <a href="#eclipseClasspathReorder"><b>eclipseClasspathReorder</b></a>.
  """ )
  @Parameter(
    property     = "scalor.eclipseClasspathOrdering",
    defaultValue = """
    
    11 = .*src/macr.*/java ★
    12 = .*src/macr.*/scala ★
    13 = .*src/macr.*/groovy ★
    14 = .*src/macr.*/res.* ★
    
    21 = .*src/main.*/java ★
    22 = .*src/main.*/scala ★
    23 = .*src/main.*/groovy ★
    24 = .*src/main.*/res.* ★
    
    31 = .*src/test.*/java ★
    32 = .*src/test.*/scala ★
    33 = .*src/test.*/groovy ★
    34 = .*src/test.*/res.* ★
    
    51 = .*target/gen[a-z-]*sources.* ★ 
    52 = .*target/gen[a-z-]*test-.* ★
    53 = .*target/gen[a-z-]*.* ★
    
    81 = org.scala-ide.sdt.* ★
    82 = org.eclipse.jdt.* ★
    83 = org.eclipse.m2e.* ★
    84 = GROOVY_SUPPORT ★
    85 = GROOVY_DSL_SUPPORT ★
    
    91 = .*target/clas.* ★
    92 = .*target/test-clas.* ★ 
    93 = .*target/scalor/clas.*/macr.* ★ 
    94 = .*target/scalor/clas.*/main.* ★ 
    95 = .*target/scalor/clas.*/test.* ★ 
    
    """
  )
  var eclipseClasspathOrdering : String = _

  @Description( """
  Enable to re-order class path entires inside the<code>.classpath</code>Maven container.
  Class path entry order controls visual presentation in Eclipse UI.
  Ordering parameter: <a href="#eclipseMavenOrdering"><b>eclipseMavenOrdering</b></a>.
  """ )
  @Parameter(
    property     = "scalor.eclipseMavenReorder",
    defaultValue = "true"
  )
  var eclipseMavenReorder : Boolean = _

  @Description( """
  Re-order class path entires inside the<code>.classpath</code>Maven container acording to these rules.
  Available rules are:
<pre>
    artifactId = ascending
</pre>
  Separator parameter: <a href="#commonSequenceSeparator"><b>commonSequenceSeparator</b></a>.
  Enablement parameter: <a href="#eclipseMavenReorder"><b>eclipseMavenReorder</b></a>.
  """ )
  @Parameter(
    property     = "scalor.eclipseMavenOrdering",
    defaultValue = """
    artifactId = ascending
    """
  )
  var eclipseMavenOrdering : String = _

  @Description( """
  Enable to re-order nature entires in <code>.project</code> descriptor.
  Nature order controls project presentation in Eclipse UI.
  Ordering parameter: <a href="#eclipseNatureOrdering"><b>eclipseNatureOrdering</b></a>.
  """ )
  @Parameter(
    property     = "scalor.eclipseNatureReorder",
    defaultValue = "true"
  )
  var eclipseNatureReorder : Boolean = _

  @Description( """
  Re-order nature entires in <code>.project</code> descriptor according to these rules.
  Nature order controls project presentation in Eclipse UI.
  In order to see <code>[S]<code> icon for Scala project, Scala nature must come first.
  Rule format: 
<pre>
item = path
  where:
  item - relative sort order index,
  path - regular expression to match against 'projectDescription/natures/nature', 
</pre>  
  Separator parameter: <a href="#commonSequenceSeparator"><b>commonSequenceSeparator</b></a>.
  Enablement parameter: <a href="#eclipseNatureReorder"><b>eclipseNatureReorder</b></a>.
  """ )
  @Parameter(
    property     = "scalor.eclipseNatureOrdering",
    defaultValue = """

    11 = org.scala-ide.sdt.+ ★
    21 = org.eclipse.jdt.+ ★
    31 = org.eclipse.pde.+ ★
    41 = org.eclipse.m2e.+ ★
    51 = org.eclipse.wst.+ ★
    61 = org.eclipse.dltk.+ ★

    """
  )
  var eclipseNatureOrdering : String = _

}

/**
 * Maven M2E version check.
 */
trait ParamsVersionMaven {

  @Description( """
  Enable to verify version of
  <a href="http://www.eclipse.org/m2e/">
    Eclipse M2E plugin
  </a> 
  when running companion Eclipse plugin.
  Version range parameter: <a href="#eclipseMavenPluginVersionRange"><b>eclipseMavenPluginVersionRange</b></a>.
  """ )
  @Parameter(
    property     = "scalor.eclipseMavenPluginVersionCheck",
    defaultValue = "true"
  )
  var eclipseMavenPluginVersionCheck : Boolean = _

  @Description( """
  Reaction of companion Eclipse plugin on Maven M2E version out of range error:
<pre>
  true  -> fail with error
  false -> only log an error
</pre>
  Version range parameter: <a href="#eclipseMavenPluginVersionRange"><b>eclipseMavenPluginVersionRange</b></a>.
  """ )
  @Parameter(
    property     = "scalor.eclipseMavenPluginVersionError",
    defaultValue = "true"
  )
  var eclipseMavenPluginVersionError : Boolean = _

  @Description( """
  Version range of Eclipse M2E plugin known to work with this release of companion Eclipse plugin.
  To verify Eclipse Platform M2E plugin version, navigate:
<pre>
  Eclipse -> Help -> About Eclipse -> Installation Details -> Installed Software -> M2E 
</pre>
  Enablement parameter: <a href="#eclipseMavenPluginVersionCheck"><b>eclipseMavenPluginVersionCheck</b></a>.
  Behavioral parameter: <a href="#eclipseMavenPluginVersionError"><b>eclipseMavenPluginVersionError</b></a>.
  """ )
  @Parameter(
    property     = "scalor.eclipseMavenPluginVersionRange",
    defaultValue = "[1.8.0,1.10.0)"
  )
  var eclipseMavenPluginVersionRange : String = _

}

/**
 * Scala IDE version check.
 */
trait ParamsVersionScala {

  @Description( """
  Enable to verify version of
  <a href="http://scala-ide.org/">
    Scala IDE plugin 
  </a> 
  when running companion Eclipse plugin.
  Version range parameter: <a href="#eclipseScalaPluginVersionRange"><b>eclipseScalaPluginVersionRange</b></a>. 
  """ )
  @Parameter(
    property     = "scalor.eclipseScalaPluginVersionCheck",
    defaultValue = "true"
  )
  var eclipseScalaPluginVersionCheck : Boolean = _

  @Description( """
  Reaction of companion Eclipse plugin on Scala IDE version out of range error:
<pre>
  true  -> fail with error
  false -> only log an error
</pre>
  Version range parameter: <a href="#eclipseScalaPluginVersionRange"><b>eclipseScalaPluginVersionRange</b></a>. 
  """ )
  @Parameter(
    property     = "scalor.eclipseScalaPluginVersionError",
    defaultValue = "true"
  )
  var eclipseScalaPluginVersionError : Boolean = _

  @Description( """
  Version range of Scala IDE known to work with this release of companion Eclipse plugin. 
  To verify Eclipse Platform Scala IDE plugin version, navigate:
<pre>
  Eclipse -> Help -> About Eclipse -> Installation Details -> Installed Software -> Scala IDE
</pre>
  Enablement parameter: <a href="#eclipseScalaPluginVersionCheck"><b>eclipseScalaPluginVersionCheck</b></a>.
  Behavioral parameter: <a href="#eclipseScalaPluginVersionError"><b>eclipseScalaPluginVersionError</b></a>.
  """ )
  @Parameter(
    property     = "scalor.eclipseScalaPluginVersionRange",
    defaultValue = "[4.7.0,4.7.2)"
  )
  var eclipseScalaPluginVersionRange : String = _

}

object ParamsConfigBase extends ParamsConfigBase {
  // Ensure variables have default values.
}

import com.carrotgarden.maven.scalor.meta.Macro.ReportFun
import com.carrotgarden.maven.scalor.meta.Macro.UpdateFun
import com.carrotgarden.maven.scalor.meta.Macro.VariableCount
import com.carrotgarden.maven.scalor.meta.Macro.VariableReport
import com.carrotgarden.maven.scalor.meta.Macro.VariableUpdate
import com.carrotgarden.maven.scalor.meta.Macro.variableCount
import com.carrotgarden.maven.scalor.meta.Macro.variableReportBlock
import com.carrotgarden.maven.scalor.meta.Macro.variableUpdateBlock

/**
 * Updatable plugin configuration parameters.
 */
trait ParamsUpdateVars extends AnyRef
  with VariableCount
  with VariableReport
  with VariableUpdate {

  def update( paramValue : UpdateFun ) : Unit

}

/**
 * Updatable plugin configuration parameters.
 */
trait ParamsConfigValue extends AnyRef
  with ParamsLogConfig
  with ParamsUpdateVars {

}

/**
 * Expose updatable parameter values for `eclipse-config`.
 */
case class ParamsConfig() extends AnyRef
  with ParamsConfigBase
  with ParamsConfigValue {

  import ParamsConfig._

  override def paramsCount = variableCount[ ParamsConfigBase ]

  override def reportParams( reportValue : ReportFun ) : Unit = {
    variableReportBlock[ ParamsConfigBase ]( reportValue )
  }

  override def updateParams( paramValue : UpdateFun ) : Unit = {
    variableUpdateBlock[ ParamsConfigBase ]( paramValue )
  }

  /**
   *  Build parameters for scope=macro.
   */
  val buildMacro = BuildMacro()

  /**
   *  Build parameters for scope=main.
   */
  val buildMain = BuildMain()

  /**
   *  Build parameters for scope=test.
   */
  val buildTest = BuildTest()

  /**
   * Update class variable values for all fields.
   */
  override def update( paramValue : UpdateFun ) : Unit = {
    this.updateParams( paramValue )
    buildMacro.updateParams( paramValue )
    buildMain.updateParams( paramValue )
    buildTest.updateParams( paramValue )
  }

}

object ParamsConfig {

  /**
   *  Build parameters for scope=macro.
   */
  case class BuildMacro() extends base.BuildMacro
    with VariableUpdate {
    override def updateParams( paramValue : UpdateFun ) : Unit = {
      variableUpdateBlock[ BuildMacro ]( paramValue )
    }
  }

  /**
   *  Build parameters for scope=main.
   */
  case class BuildMain() extends base.BuildMain
    with VariableUpdate {
    override def updateParams( paramValue : UpdateFun ) : Unit = {
      variableUpdateBlock[ BuildMain ]( paramValue )
    }
  }

  /**
   *  Build parameters for scope=test.
   */
  case class BuildTest() extends base.BuildTest
    with VariableUpdate {
    override def updateParams( paramValue : UpdateFun ) : Unit = {
      variableUpdateBlock[ BuildTest ]( paramValue )
    }
  }

  import upickle._
  import upickle.default._

  /**
   * Configuration parser.
   */
  implicit def codecParamsConfig : ReadWriter[ ParamsConfig ] = macroRW
  def parse( config : String ) : ParamsConfig = read[ ParamsConfig ]( config )
  def unparse( config : ParamsConfig ) : String = write( config )

}

/**
 * Expose updatable parameter values for `eclipse-restart`.
 */
case class ParamsRestart() extends AnyRef
  with ParamsRestartBase
  with ParamsConfigValue {

  override def paramsCount =
    variableCount[ ParamsRestartBase ]

  override def reportParams( reportValue : ReportFun ) : Unit = {
    variableReportBlock[ ParamsRestartBase ]( reportValue )
  }

  override def updateParams( paramValue : UpdateFun ) : Unit = {
    variableUpdateBlock[ ParamsRestartBase ]( paramValue )
  }

  override def update( paramValue : UpdateFun ) : Unit = {
    this.updateParams( paramValue )
  }

}

/**
 * Expose updatable parameter values for `eclipse-prescomp`.
 */
case class ParamsPrescomp() extends AnyRef
  with ParamsPrescompBase
  with ParamsConfigValue {

  override def paramsCount =
    variableCount[ ParamsPrescompBase ]

  override def reportParams( reportValue : ReportFun ) : Unit = {
    variableReportBlock[ ParamsPrescompBase ]( reportValue )
  }

  override def updateParams( paramValue : UpdateFun ) : Unit = {
    variableUpdateBlock[ ParamsPrescompBase ]( paramValue )
  }

  override def update( paramValue : UpdateFun ) : Unit = {
    this.updateParams( paramValue )
  }

}

/**
 * Expose updatable parameter values for `eclipse-format`.
 */
case class ParamsFormat() extends AnyRef
  with ParamsFormatBase
  with ParamsConfigValue {

  override def paramsCount =
    variableCount[ ParamsFormatBase ]

  override def reportParams( reportValue : ReportFun ) : Unit = {
    variableReportBlock[ ParamsFormatBase ]( reportValue )
  }

  override def updateParams( paramValue : UpdateFun ) : Unit = {
    variableUpdateBlock[ ParamsFormatBase ]( paramValue )
  }

  override def update( paramValue : UpdateFun ) : Unit = {
    this.updateParams( paramValue )
  }

}
