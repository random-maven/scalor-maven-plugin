package com.carrotgarden.maven.scalor.scalajs

import java.io.File

import org.apache.maven.plugins.annotations.Parameter

import com.carrotgarden.maven.scalor.base
import com.carrotgarden.maven.tools.Description
import com.carrotgarden.maven.scalor.util.Folder
import com.carrotgarden.maven.scalor.util.Error.Throw

/**
 * Linker build resource definitions for any scope.
 */
trait Build extends AnyRef
  with base.BuildAnyTarget
  with base.BuildAnyDependency {

  /**
   * Build mode for runtime JavaScript.
   */
  def linkerModeBuild : String

  /**
   * Build mode for runtime JavaScript.
   */
  def linkerModeBuildMin : String

  /**
   * Name of the generated runtime JavaScript.
   */
  def linkerRuntimeJS : String

  /**
   * Name of the generated runtime JavaScript.
   */
  def linkerRuntimeMinJS : String

  /**
   * Build mode for runtime JavaScript.
   */
  def linkerBuildMode : Build.Mode = {
    Build.buildMode( linkerModeBuild )
  }

  /**
   * Build mode for runtime JavaScript.
   */
  def linkerBuildModeMin : Build.Mode = {
    Build.buildMode( linkerModeBuildMin )
  }

  def linkerHasBuild( incremental : Boolean ) : Boolean = {
    Build.hasBuildEnabled( linkerBuildMode, incremental )
  }

  def linkerHasBuildMin( incremental : Boolean ) : Boolean = {
    Build.hasBuildEnabled( linkerBuildModeMin, incremental )
  }

  /**
   * Full path of the generated runtime JavaScript.
   */
  def linkerRuntimeFile : File = {
    val file = new File( buildTargetFolder, linkerRuntimeJS ).getCanonicalFile
    Folder.ensureParent( file )
    file
  }

  /**
   * Full path of the generated runtime JavaScript.
   */
  def linkerRuntimeMinFile : File = {
    val file = new File( buildTargetFolder, linkerRuntimeMinJS ).getCanonicalFile
    Folder.ensureParent( file )
    file
  }

}

object Build {

  sealed trait Mode {
    def name : String
  }
  case object BuildAlways extends Mode {
    override val name = "always"
  }
  case object BuildNever extends Mode {
    override val name = "never"
  }
  case object BuildFull extends Mode {
    override val name = "full"
  }
  case object BuildIncr extends Mode {
    override val name = "incr"
  }

  def buildMode( name : String ) : Mode = {
    name match {
      case BuildAlways.name => BuildAlways
      case BuildNever.name  => BuildNever
      case BuildFull.name   => BuildFull
      case BuildIncr.name   => BuildIncr
      case _                => Throw( s"Invalid build mode: ${name}" )
    }
  }

  def hasBuildEnabled( mode : Mode, incremental : Boolean ) : Boolean = {
    mode match {
      case BuildAlways => true
      case BuildNever  => false
      case BuildFull   => !incremental
      case BuildIncr   => incremental
    }
  }

}

/**
 * Scala.js linker build parameters for scope=main.
 */
trait BuildMain extends Build
  with BuildMainDependency
  with BuildMainTarget {

  @Description( """
  Build mode for non-optimized <a href="#linkerMainRuntimeJs"><b>linkerMainRuntimeJs</b></a>.
  Normally uses <code>always</code>, to link during both Maven full build and Eclipse incremental build.
  Available build modes:
<pre>
  always - link during both full and incremental build
  never  - do not produce runtime at all
  full   - link only during full build
  incr   - link only during incremental build
</pre>
  """ )
  @Parameter(
    property     = "scalor.linkerMainBuildMode",
    defaultValue = "always"
  )
  var linkerMainBuildMode : String = _

  @Description( """
  Build mode for optimized/minified <a href="#linkerMainRuntimeMinJs"><b>linkerMainRuntimeMinJs</b></a>.
  Normally uses <code>full</code>, to link only during Maven full build and skip Eclipse incremental build.
  Available build modes:
<pre>
  always - link during both full and incremental build
  never  - do not produce runtime at all
  full   - link only during full build
  incr   - link only during incremental build
</pre>
  """ )
  @Parameter(
    property     = "scalor.linkerMainBuildModeMin",
    defaultValue = "full"
  )
  var linkerMainBuildModeMin : String = _

  @Description( """
  Non-optimized runtime script.
  Relative path of the generated runtime JavaScript file for scope=main, mode=development.
  File is packaged inside <a href="#linkerMainTargetFolder"><b>linkerMainTargetFolder</b></a>
  Normally follows webjars convention.
  """ )
  @Parameter(
    property     = "scalor.linkerMainRuntimeJs",
    defaultValue = "${project.artifactId}/${project.version}/runtime.js"
  )
  var linkerMainRuntimeJs : String = _

  @Description( """
  Linker optimized/minified runtime script.
  Relative path of the generated runtime JavaScript file for scope=main, mode=production.
  File is packaged inside <a href="#linkerMainTargetFolder"><b>linkerMainTargetFolder</b></a>
  Normally follows webjars convention.
  """ )
  @Parameter(
    property     = "scalor.linkerMainRuntimeMinJs",
    defaultValue = "${project.artifactId}/${project.version}/runtime.min.js"
  )
  var linkerMainRuntimeMinJs : String = _

  override def linkerModeBuild = linkerMainBuildMode
  override def linkerModeBuildMin = linkerMainBuildModeMin
  override def linkerRuntimeJS = linkerMainRuntimeJs
  override def linkerRuntimeMinJS = linkerMainRuntimeMinJs

}

trait BuildMainDependency extends base.BuildAnyDependency {

  @Description( """
  Folders with classes generated by current project and included in linker class path.
  Normally includes build output from scope=[macro,main]
  (<code>target/classes</code>).
  """ )
  @Parameter(
    property     = "scalor.linkerMainDependencyFolders",
    defaultValue = "${project.build.outputDirectory}"
  )
  var linkerMainDependencyFolders : Array[ File ] = Array.empty

  @Description( """
  Provide linker class path from project dependency artifacts based on these scopes.
  Scopes <a href="https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html">reference</a>.
  """ )
  @Parameter(
    property     = "scalor.linkerMainDependencyScopes",
    defaultValue = "provided"
  )
  var linkerMainDependencyScopes : Array[ String ] = Array.empty

  override def buildDependencyFolders = linkerMainDependencyFolders
  override def buildDependencyScopes = linkerMainDependencyScopes

}

trait BuildMainTarget extends base.BuildAnyTarget {

  @Description( """
  Build target directory for the generated runtime JavaScript file with scope=main.
  Normally packaged inside <code>target/classes</code>.
  Normally follows webjars convention.
  """ )
  @Parameter(
    property     = "scalor.linkerMainTargetFolder",
    defaultValue = "${project.build.outputDirectory}/META-INF/resources/webjars"
  )
  var linkerMainTargetFolder : File = _

  override def buildTargetFolder = linkerMainTargetFolder

}

/**
 * Scala.js linker build parameters for scope=test.
 */
trait BuildTest extends Build
  with BuildTestTarget
  with BuildTestDependency {

  @Description( """
  Build mode for non-optimized <a href="#linkerTestRuntimeJs"><b>linkerTestRuntimeJs</b></a>.
  Normally uses <code>always</code>, to link during both Maven full build and Eclipse incremental build.
  Available build modes:
<pre>
  always - link during both full and incremental build
  never  - do not produce runtime at all
  full   - link only during full build
  incr   - link only during incremental build
</pre>
  """ )
  @Parameter(
    property     = "scalor.linkerTestBuildMode",
    defaultValue = "always"
  )
  var linkerTestBuildMode : String = _

  @Description( """
  Build mode for optimized/minified <a href="#linkerTestRuntimeMinJs"><b>linkerTestRuntimeMinJs</b></a>.
  Normally uses <code>never</code>, since test runtime is not intended as deployment artifact.
  Available build modes:
<pre>
  always - link during both full and incremental build
  never  - do not produce runtime at all
  full   - link only during full build
  incr   - link only during incremental build
</pre>
  """ )
  @Parameter(
    property     = "scalor.linkerTestBuildModeMin",
    defaultValue = "never"
  )
  var linkerTestBuildModeMin : String = _

  @Description( """
  Non-optimized runtime script.
  Relative path of the generated runtime JavaScript file for scope=test, mode=development.
  File is packaged inside <a href="#linkerTestTargetFolder"><b>linkerTestTargetFolder</b></a>
  Normally follows webjars convention.
  """ )
  @Parameter(
    property     = "scalor.linkerTestRuntimeJs",
    defaultValue = "${project.artifactId}/${project.version}/runtime-test.js"
  )
  var linkerTestRuntimeJs : String = _

  @Description( """
  Linker optimized/minified runtime script.
  Relative path of the generated runtime JavaScript file for scope=test, mode=production.
  File is packaged inside <a href="#linkerTestTargetFolder"><b>linkerTestTargetFolder</b></a>
  Normally follows webjars convention.
  """ )
  @Parameter(
    property     = "scalor.linkerTestRuntimeMinJs",
    defaultValue = "${project.artifactId}/${project.version}/runtime-test.min.js"
  )
  var linkerTestRuntimeMinJs : String = _

  override def linkerModeBuild = linkerTestBuildMode
  override def linkerModeBuildMin = linkerTestBuildModeMin
  override def linkerRuntimeJS = linkerTestRuntimeJs
  override def linkerRuntimeMinJS = linkerTestRuntimeMinJs

}

trait BuildTestDependency extends base.BuildAnyDependency {

  @Description( """
  Folders with classes generated by current project and included in linker class path.
  Normally includes build output from scope=[macro,main,test] 
  (<code>target/test-classes</code>, <code>target/classes</code>).
  """ )
  @Parameter(
    property     = "scalor.linkerTestDependencyFolders",
    defaultValue = "${project.build.testOutputDirectory},${project.build.outputDirectory}"
  )
  var linkerTestDependencyFolders : Array[ File ] = Array.empty

  @Description( """
  Provide linker class path from project dependencies selected by these scopes.
  Scopes <a href="https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html">reference</a>.
  """ )
  @Parameter(
    property     = "scalor.linkerTestDependencyScopes",
    defaultValue = "provided,test"
  )
  var linkerTestDependencyScopes : Array[ String ] = Array.empty

  override def buildDependencyFolders = linkerTestDependencyFolders
  override def buildDependencyScopes = linkerTestDependencyScopes

}

trait BuildTestTarget extends base.BuildAnyTarget {

  @Description( """
  Build target directory for the generated runtime JavaScript file with scope=test.
  Normally packaged inside <code>target/test-classes</code>.
  Normally follows webjars convention.
  """ )
  @Parameter(
    property     = "scalor.linkerTestTargetFolder",
    defaultValue = "${project.build.testOutputDirectory}/META-INF/resources/webjars"
  )
  var linkerTestTargetFolder : File = _

  override def buildTargetFolder = linkerTestTargetFolder

}
