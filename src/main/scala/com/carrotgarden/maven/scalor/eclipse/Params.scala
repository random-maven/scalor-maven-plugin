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
  with base.BuildMacroSources
  with base.BuildMainSources
  with base.BuildTestSources
  with zinc.ParamsCompileOptions
  with zinc.ParamScalaInstall
  with ParamsOrder
  with ParamsLogger
  with ParamsVersionMaven
  with ParamsVersionScala {

  @Description( """
  Enable to remove Scala Library container from Eclipse build class path.
  Normally, Scala Library container is provided by default by Scala IDE plugin.
  Normally, scala-library artifact must also be provided as <code>pom.xml</code> dependnecy.
  That results in scala-library dependency listed twice on Eclipse build class path.
  To avoid "duplicate library on class path" warning, 
  use this parameter to remove Scala IDE provided container.
  Custom Scala installation configured by this plugin will use scala-library resolved from
    <a href="#defineCompiler"><b>defineCompiler</b></a>, 
  which normally should be configured as identical to the scala-library 
  provided as <code>pom.xml</code> project dependnecy.
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

  @Description( """
  Reset Eclipse Scala IDE project preferences 
  to their default values before applying Maven provided configuration.
  Use this to remove manual user-provided Eclipse UI configuration settins for Scala IDE.
  """ )
  @Parameter(
    property     = "scalor.eclipseResetPreferences",
    defaultValue = "true"
  )
  var eclipseResetPreferences : Boolean = _

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
  Report persisting of configured project Scala IDE settings.
  """ )
  @Parameter(
    property     = "scalor.eclipseLogPersistSettings",
    defaultValue = "true"
  )
  var eclipseLogPersistSettings : Boolean = _

  @Description( """
  Report persisting of selected Scala compiler installation.
  """ )
  @Parameter(
    property     = "scalor.eclipseLogPersistCompiler",
    defaultValue = "true"
  )
  var eclipseLogPersistCompiler : Boolean = _

  @Description( """
  Enable to log class path re-ordering results.
  """ )
  @Parameter(
    property     = "scalor.eclipseLogClasspathOrder",
    defaultValue = "true"
  )
  var eclipseLogClasspathOrder : Boolean = _

  @Description( """
  Report plugin-generated resolved custom Scala installation.
  """ )
  @Parameter(
    property     = "scalor.eclipseLogInstallResolve",
    defaultValue = "true"
  )
  var eclipseLogInstallResolve : Boolean = _

  @Description( """
  Report all available custom Scala installations persisted by Eclispe Scala IDE plugin.
  Generated report output file parameter: 
    <a href="#eclipseCustomInstallReport"><b>eclipseCustomInstallReport</b></a>.
  """ )
  @Parameter(
    property     = "scalor.eclipseLogCustomInstall",
    defaultValue = "true"
  )
  var eclipseLogCustomInstall : Boolean = _

  @Description( """
  Report all available custom Scala installations persisted by Scala IDE.
  Enablement parameter:
    <a href="#eclipseLogCustomInstall"><b>eclipseLogCustomInstall</b></a>.
  """ )
  @Parameter(
    property     = "scalor.eclipseCustomInstallReport",
    defaultValue = "${project.build.directory}/scalor/scala-install-report.txt"
  )
  var eclipseCustomInstallReport : File = _
  
    @Description( """
  Enable to log effective parameters configured for Eclipse companion plugin.
  """ )
  @Parameter(
    property     = "scalor.eclipseLogParamsConfig",
    defaultValue = "false"
  )
  var eclipseLogParamsConfig : Boolean = _

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
    
    91 = .*target/macro-classes ; 
    92 = .*target/classes ;
    93 = .*target/test-classes ; 
    
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
  Verify version of Maven M2E when running companion Eclipse plugin.
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
  Verify version of Scala IDE plugin when running companion Eclipse plugin.
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

/**
 * Expose static parameter names.
 */
object Params extends Params

/**
 * Expose updatable parameter values.
 */
case class ParamsConfig() extends Params {

  import meta.Macro._

  /**
   * Update class variable values via macro.
   */
  def updateParams( paramValue : UpdateFun ) : ParamsConfig = {
    variableUpdateBlock[ Params ]( paramValue )
    this
  }

  /**
   * Report class variable values via macro.
   */
  def reportParams( reportValue : ReportFun ) : Unit = {
    variableReportBlock[ Params ]( reportValue )
  }

}
