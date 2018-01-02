package com.carrotgarden.maven.scalor.eclipse

import java.io.File

import com.carrotgarden.maven.tools.Description
import org.apache.maven.plugins.annotations.Parameter

import com.carrotgarden.maven.scalor._

/**
 * Maven plugin parameters used to control companion Eclipse plugin.
 */
trait Params extends AnyRef
  with base.AnyPar
  with base.ParamsArtifact
  with base.BuildMacroSources
  with base.BuildMainSources
  with base.BuildTestSources
  with zinc.ParamsCompileOrder
  with zinc.ParamsOptionsJavaC
  with zinc.ParamsOptionsScalaC
  with zinc.ParamScalaInstall
  with zinc.ParamsPluginList
  with ParamsVersionMaven
  with ParamsVersionScala {

  @Description( """
  Enable to rename Scala Library container in Eclipse UI.
  Container description will reflect title of custom Scala installation. 
  """ )
  @Parameter(
    property     = "scalor.eclipseRenameLibraryContainer",
    defaultValue = "true"
  )
  var eclipseRenameLibraryContainer : Boolean = _

  @Description( """
  Reset all preferences to their default values before applying Maven configuration.
  Use this to remove manual user provided Eclipse UI configuration settins.
  """ )
  @Parameter(
    property     = "scalor.eclipseResetPreferences",
    defaultValue = "true"
  )
  var eclipseResetPreferences : Boolean = _

  @Description( """
  Report persisting of configured Scala IDE settings.
  """ )
  @Parameter(
    property     = "scalor.eclipseLogPersistSettings",
    defaultValue = "true"
  )
  var eclipseLogPersistSettings : Boolean = _

  @Description( """
  Report persisting of selected compiler.
  """ )
  @Parameter(
    property     = "scalor.eclipseLogPersistCompiler",
    defaultValue = "true"
  )
  var eclipseLogPersistCompiler : Boolean = _

  @Description( """
  Enable to log class path -re-ordering results.
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
  Report all available custom Scala installations persisted by Scala IDE.
  """ )
  @Parameter(
    property     = "scalor.eclipseLogCustomInstall",
    defaultValue = "true"
  )
  var eclipseLogCustomInstall : Boolean = _

  @Description( """
  Report all available custom Scala installations persisted by Scala IDE.
  """ )
  @Parameter(
    property     = "scalor.eclipseCustomInstallReport",
    defaultValue = "${project.build.directory}/custom-scala-install-report.txt"
  )
  var eclipseCustomInstallReport : File = _

  @Description( """
  Enable to apply comment in Scala IDE settings file.
  ${project.basedir}/.settings/org.scala-ide.sdt.core.prefs
  """ )
  @Parameter(
    property     = "scalor.eclipseScalaSettingsCommentApply",
    defaultValue = "true"
  )
  var eclipseScalaSettingsCommentApply : Boolean = _

  @Description( """
  Content of the comment in Scala IDE settings file.
  ${project.basedir}/.settings/org.scala-ide.sdt.core.prefs
  """ )
  @Parameter(
    property     = "scalor.eclipseScalaSettingsCommentString",
    defaultValue = "scalor-maven-plugin @ ${project.properties(release.stamp)}"
  )
  var eclipseScalaSettingsCommentString : String = _

  @Description( """
  Enable to apply comment in Eclipse .project file.
  """ )
  @Parameter(
    property     = "scalor.eclipseProjectCommentApply",
    defaultValue = "true"
  )
  var eclipseProjectCommentApply : Boolean = _

  @Description( """
  Content of the comment in eclipse .project file.
  """ )
  @Parameter(
    property     = "scalor.eclipseProjectCommentString",
    defaultValue = "scalor-maven-plugin @ ${project.properties(release.stamp)}"
  )
  var eclipseProjectCommentString : String = _

  @Description( """
  Enable to re-order Eclipse .project file by 'projectDescription/buildSpec/buildCommand/name' .
  """ )
  @Parameter(
    property     = "scalor.eclipseBuilderReorder",
    defaultValue = "true"
  )
  var eclipseBuilderReorder : Boolean = _

  @Description( """
  Order Eclipse .project file builder entries according to these rules.
  Rule format: item = path,
  where:
  'item' - relative sort order index,
  'path' - regular expression to match against 'projectDescription/buildSpec/buildCommand/name', 
  """ )
  @Parameter(
    property     = "scalor.eclipseBuilderOrdering",
    defaultValue = """
    
    11 = org.eclipse.wst.+
    12 = org.eclipse.dltk.+
    
    41 = org.eclipse.jdt.+ 
    42 = org.scala-ide.sdt.+ 
    
    71 = org.eclipse.pde.+ 
    72 = org.eclipse.m2e.+ 
    
    """
  )
  var eclipseBuilderOrdering : String = _

  @Description( """
  Enable to re-order Eclipse .classpath file by 'classpath/classpathentry/@path' .
  """ )
  @Parameter(
    property     = "scalor.eclipseClasspathReorder",
    defaultValue = "true"
  )
  var eclipseClasspathReorder : Boolean = _

  @Description( """
  Re-order Eclipse top level .classpath file entries according to these rules.
  Rule format: 
    item = path ;
  where:
    'item' - relative sort order index,
    'path' - regular expression to match against 'classpath/classpathentry/@path', 
  """ )
  @Parameter(
    property     = "scalor.eclipseClasspathOrdering",
    defaultValue = """
    
    11 = .*src/macr.*/java
    12 = .*src/macr.*/scala 
    13 = .*src/macr.*/groovy 
    14 = .*src/macr.*/res.*
    
    21 = .*src/main.*/java 
    22 = .*src/main.*/scala 
    23 = .*src/main.*/groovy 
    24 = .*src/main.*/res.*
    
    31 = .*src/test.*/java 
    32 = .*src/test.*/scala 
    33 = .*src/test.*/groovy 
    34 = .*src/test.*/res.*
    
    51 = .*target/gen[a-z-]*sources.* 
    52 = .*target/gen[a-z-]*test-.* 
    53 = .*target/gen[a-z-]*.*
    
    81 = org.scala-ide.sdt.*
    82 = org.eclipse.jdt.*
    83 = org.eclipse.m2e.*
    84 = GROOVY_SUPPORT
    85 = GROOVY_DSL_SUPPORT
    
    91 = .*target/macro-classes 
    92 = .*target/classes 
    93 = .*target/test-classes 
    
    """
  )
  var eclipseClasspathOrdering : String = _

  @Description( """
  Enable to re-order class path entires inside the .classpath Maven container.
  """ )
  @Parameter(
    property     = "scalor.eclipseMavenReorder",
    defaultValue = "true"
  )
  var eclipseMavenReorder : Boolean = _

  @Description( """
  Re-order class path entires inside the .classpath Maven container acording to these rules.
  Available rules are:
  """ )
  @Parameter(
    property     = "scalor.eclipseMavenOrdering",
    defaultValue = """
    artifactId = ascending
    """
  )
  var eclipseMavenOrdering : String = _

  @Description( """
  Eclipse Scala IDE plugin options. See: ScalaPluginSettings:
	https://github.com/scala-ide/scala-ide/blob/master/org.scala-ide.sdt.core/src/org/scalaide/ui/internal/preferences/IDESettings.scala
  Available custom interpolation properties:
    ${scalor.zincCompileOrder} ;
  """ )
  @Parameter(
    property     = "scalor.eclipseOptsScalaIDE",
    defaultValue = """
		-useScopesCompiler
		-compileorder:Mixed
		"""
  )
  var eclipseOptsScalaIDE : String = _

}

/**
 * Maven M2E version check.
 */
trait ParamsVersionMaven {

  @Description( """
  Verify version of Maven M2E when running companion Eclipse plugin.
  """ )
  @Parameter(
    property     = "scalor.eclipseMavenPluginVersionCheck",
    defaultValue = "true"
  )
  var eclipseMavenPluginVersionCheck : Boolean = _

  @Description( """
  Reaction of companion Eclipse plugin on Maven M2E version out of range error:
  true -> fail with error;
  false -> only log an error;
  """ )
  @Parameter(
    property     = "scalor.eclipseMavenPluginVersionError",
    defaultValue = "true"
  )
  var eclipseMavenPluginVersionError : Boolean = _

  @Description( """
  Version range of Maven M2E known to work with this release of companion Eclipse plugin. 
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
  Verify version of Scala IDE when running companion Eclipse plugin.
  """ )
  @Parameter(
    property     = "scalor.eclipseScalaPluginVersionCheck",
    defaultValue = "true"
  )
  var eclipseScalaPluginVersionCheck : Boolean = _

  @Description( """
  Reaction of companion Eclipse plugin on Scala IDE version out of range error:
  true -> fail with error;
  false -> only log an error;
  """ )
  @Parameter(
    property     = "scalor.eclipseScalaPluginVersionError",
    defaultValue = "true"
  )
  var eclipseScalaPluginVersionError : Boolean = _

  @Description( """
  Version range of Scala IDE known to work with this release of companion Eclipse plugin. 
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
object Params extends Params {

}

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
