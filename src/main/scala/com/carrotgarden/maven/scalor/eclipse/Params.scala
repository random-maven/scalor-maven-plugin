package com.carrotgarden.maven.scalor.eclipse

import java.io.File

import com.carrotgarden.maven.tools.Description
import org.apache.maven.plugins.annotations.Parameter

import com.carrotgarden.maven.scalor._

/**
 * Maven plugin parameters used to control companion Eclipse plugin.
 */
trait Params extends AnyRef
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
  with ParamsVersionScala {

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

trait ParamsLogger {

  @Description( """
  Enable to log persisting of configured project Scala IDE settings.
  """ )
  @Parameter(
    property     = "scalor.eclipseLogPersistSettings",
    defaultValue = "false"
  )
  var eclipseLogPersistSettings : Boolean = _

  @Description( """
  Enable to log persisting of selected project Scala compiler.
  """ )
  @Parameter(
    property     = "scalor.eclipseLogPersistCompiler",
    defaultValue = "false"
  )
  var eclipseLogPersistCompiler : Boolean = _

  @Description( """
  Enable to log persisting of custom Scala installation.
  """ )
  @Parameter(
    property     = "scalor.eclipseLogPersistInstall",
    defaultValue = "false"
  )
  var eclipseLogPersistInstall : Boolean = _

  @Description( """
  Enable to log class path re-ordering result entries.
  """ )
  @Parameter(
    property     = "scalor.eclipseLogClasspathOrder",
    defaultValue = "false"
  )
  var eclipseLogClasspathOrder : Boolean = _

  @Description( """
  Enable to log generated resolved custom Scala installation.
  """ )
  @Parameter(
    property     = "scalor.eclipseLogInstallResolve",
    defaultValue = "false"
  )
  var eclipseLogInstallResolve : Boolean = _

  @Description( """
  Report all available custom Scala installations persisted by Eclispe Scala IDE plugin.
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
  """ )
  @Parameter(
    property     = "scalor.eclipseLogParamsConfig",
    defaultValue = "false"
  )
  var eclipseLogParamsConfig : Boolean = _

  @Description( """
  Enable to log M2E build participant executions of Eclipse companion plugin.
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
  Ordering parameter: <a href="#eclipseBuilderOrdering"><b>eclipseBuilderOrdering</b></a>
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
item = path ;
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
    
    11 = org.eclipse.wst.+ ;
    12 = org.eclipse.dltk.+ ;
    
    41 = org.eclipse.jdt.+ ;
    42 = org.scala-ide.sdt.+ ; 
    
    71 = org.eclipse.pde.+ ;
    72 = org.eclipse.m2e.+ ;
    
    """
  )
  var eclipseBuilderOrdering : String = _

  @Description( """
  Enable to re-order Eclipse <code>.classpath</code> descriptor by
<pre>
  classpath/classpathentry/@path
</pre>
  Ordering parameter: <a href="#eclipseClasspathOrdering"><b>eclipseClasspathOrdering</b></a>
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
item = path ;
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
    
    11 = .*src/macr.*/java ;
    12 = .*src/macr.*/scala ;
    13 = .*src/macr.*/groovy ;
    14 = .*src/macr.*/res.* ;
    
    21 = .*src/main.*/java ;
    22 = .*src/main.*/scala ;
    23 = .*src/main.*/groovy ;
    24 = .*src/main.*/res.* ;
    
    31 = .*src/test.*/java ;
    32 = .*src/test.*/scala ;
    33 = .*src/test.*/groovy ;
    34 = .*src/test.*/res.* ;
    
    51 = .*target/gen[a-z-]*sources.* ; 
    52 = .*target/gen[a-z-]*test-.* ;
    53 = .*target/gen[a-z-]*.* ;
    
    81 = org.scala-ide.sdt.* ;
    82 = org.eclipse.jdt.* ;
    83 = org.eclipse.m2e.* ;
    84 = GROOVY_SUPPORT ;
    85 = GROOVY_DSL_SUPPORT ;
    
    91 = .*target/clas.* ;
    92 = .*target/test-clas.* ; 
    93 = .*target/scalor/clas.*/macr.* ; 
    94 = .*target/scalor/clas.*/main.* ; 
    95 = .*target/scalor/clas.*/test.* ; 
    
    """
  )
  var eclipseClasspathOrdering : String = _

  @Description( """
  Enable to re-order class path entires inside the<code>.classpath</code>Maven container.
  Class path entry order controls visual presentation in Eclipse UI.
  Ordering parameter: <a href="#eclipseMavenOrdering"><b>eclipseMavenOrdering</b></a>
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
  Ordering parameter: <a href="#eclipseNatureOrdering"><b>eclipseNatureOrdering</b></a>
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
item = path ;
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

    11 = org.scala-ide.sdt.+ ;
    21 = org.eclipse.jdt.+ ;
    31 = org.eclipse.pde.+ ;
    41 = org.eclipse.m2e.+ ;
    51 = org.eclipse.wst.+ ;
    61 = org.eclipse.dltk.+ ;

    """
  )
  var eclipseNatureOrdering : String = _

}

/**
 * Maven M2E version check.
 */
trait ParamsVersionMaven {

  @Description( """
  Verify version of
  <a href="http://www.eclipse.org/m2e/">
    Eclipse M2E plugin
  </a> 
  when running companion Eclipse plugin.
  Version range parameter: <a href="#eclipseMavenPluginVersionRange"><b>eclipseMavenPluginVersionRange</b></a> 
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
  Version range parameter: <a href="#eclipseMavenPluginVersionRange"><b>eclipseMavenPluginVersionRange</b></a> 
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
  Verify version of 
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

object Params extends Params {
  // Ensure variables have default values.
}

import meta.Macro._
import ParamsConfig._

/**
 * Expose updatable parameter values.
 */
case class ParamsConfig() extends Params
  with VariableCount
  with VariableReport
  with VariableUpdate {

  /**
   * Number of variable values.
   */
  override def paramsCount = variableCount[ Params ]

  /**
   * Report class variable values via macro.
   */
  override def reportParams( reportValue : ReportFun ) : Unit = {
    variableReportBlock[ Params ]( reportValue )
  }

  /**
   * Update class variable values via macro.
   */
  override def updateParams( paramValue : UpdateFun ) : Unit = {
    variableUpdateBlock[ Params ]( paramValue )
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
  def update( paramValue : UpdateFun ) : Unit = {
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

}
