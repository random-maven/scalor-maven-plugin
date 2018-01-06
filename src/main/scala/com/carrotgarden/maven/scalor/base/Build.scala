package com.carrotgarden.maven.scalor.base

import org.apache.maven.plugins.annotations._
import org.apache.maven.model.Resource

import java.io.File
import java.net.JarURLConnection
import java.nio.file.Paths
import java.nio.file.Files
import java.nio.file.FileVisitOption
import java.nio.file.Path

import java.util.stream.Collectors
import java.util.Arrays
import java.util.regex.Pattern
import java.util.HashSet
import java.util.List
import java.util.ArrayList

import com.carrotgarden.maven.scalor._
import com.carrotgarden.maven.tools.Description

import com.carrotgarden.maven.scalor.A._
//import com.github.dwickern.macros.NameOf._

import meta.Macro._
import util.Error._
import util.Folder._

/**
 * Build definition for compilation scope=macro.
 */
trait BuildMacro extends Build
  with BuildMacroDependency
  with BuildMacroSources
  with BuildMacroTarget

object BuildMacro extends BuildMacro

trait BuildMacroDependency extends BuildAnyDependency {

  @Description( """
  Project folders containing build classes
  which are dependency for compilation scope=macro.
  """ )
  @Parameter(
    property     = "scalor.buildMacroDependencyFolders",
    defaultValue = "" // empty
  )
  var buildMacroDependencyFolders : Array[ File ] = Array.empty

  @Description( """
  Maven dependency scopes which control selection
  of project dependency artifacts to be included
  in the classpath of compilation scope=macro.
  """ )
  @Parameter(
    property     = "scalor.buildMacroDependencyScopes",
    defaultValue = "compile,provided,system"
  )
  var buildMacroDependencyScopes : Array[ String ] = Array.empty

  override def buildDependencyFolders = buildMacroDependencyFolders
  override def buildDependencyScopes = buildMacroDependencyScopes

}

trait BuildMacroSources extends BuildAnySources {

  @Description( """
  Java source root folders to be included in compilation scope=macro.
  """ )
  @Parameter(
    property     = "scalor.buildMacroSourceJavaFolders",
    defaultValue = "${project.build.sourceDirectory}/../../macro/java"
  )
  var buildMacroSourceJavaFolders : Array[ File ] = Array[ File ]()

  @Description( """
  Scala source root folders to be included in compilation scope=macro.
  """ )
  @Parameter(
    property     = "scalor.buildMacroSourceScalaFolders",
    defaultValue = "${project.build.sourceDirectory}/../../macro/scala"
  )
  var buildMacroSourceScalaFolders : Array[ File ] = Array.empty

  override def buildSourceFolders = buildMacroSourceJavaFolders ++ buildMacroSourceScalaFolders

  /**
   * Custom project property used to store registered macro source folders.
   * Emulate 'project.getCompileSourceRoots' for scope=macro.
   */
  def buildMacroSourceFoldersParam = "scalor.buildMacroSourceRoots"

}

trait BuildMacroTarget extends BuildAnyTarget {

  @Description( """
  Build output folder with result classes of compilation scope=macro.
  """ )
  @Parameter(
    property     = "scalor.buildMacroTargetFolder",
    defaultValue = "${project.build.directory}/macro-classes"
  )
  var buildMacroTargetFolder : File = _

  /**
   * Custom project property used to store registered macro target folder.
   * Emulate 'project.getBuild.getOutputDirectory' for scope=macro.
   */
  def buildMacroTargetParam = "scalor.buildMacroOutputDirectory"

  override def buildTargetFolder = buildMacroTargetFolder

}

/**
 * Build definition for compilation scope=main.
 */
trait BuildMain extends Build
  with BuildMainDependency
  with BuildMainSources
  with BuildMainTarget

object BuildMain extends BuildMain

trait BuildMainDependency extends BuildAnyDependency {

  @Description( """
  Project folders containing build classes
  which are dependency for compilation scope=main.
  """ )
  @Parameter(
    property     = "scalor.buildMainDependencyFolders",
    defaultValue = "${project.build.directory}/macro-classes"
  )
  var buildMainDependencyFolders : Array[ File ] = Array.empty

  @Description( """
  Maven dependency scopes which control selection
  of project dependency artifacts to be included
  in the classpath of compilation scope=main.
  """ )
  @Parameter(
    property     = "scalor.buildMainDependencyScopes",
    defaultValue = "compile,provided,system"
  )
  var buildMainDependencyScopes : Array[ String ] = Array.empty

  override def buildDependencyFolders = buildMainDependencyFolders
  override def buildDependencyScopes = buildMainDependencyScopes

}

trait BuildMainSources extends BuildAnySources {

  @Description( """
  Java source root folders to be included in compilation scope=main.
  """ )
  @Parameter(
    property     = "scalor.buildMainSourceJavaFolders",
    defaultValue = "${project.build.sourceDirectory}"
  )
  var buildMainSourceJavaFolders : Array[ File ] = Array.empty

  @Description( """
  Scala source root folders to be included in compilation scope=main.
  """ )
  @Parameter(
    property     = "scalor.buildMainSourceScalaFolders",
    defaultValue = "${project.build.sourceDirectory}/../scala"
  )
  var buildMainSourceScalaFolders : Array[ File ] = Array.empty

  override def buildSourceFolders = buildMainSourceJavaFolders ++ buildMainSourceScalaFolders

}

trait BuildMainTarget extends BuildAnyTarget {

  @Description( """
  Build output folder with result classes of compilation scope=main.
  """ )
  @Parameter(
    property     = "scalor.buildMainTargetFolder",
    defaultValue = "${project.build.outputDirectory}"
  )
  var buildMainTargetFolder : File = _

  override def buildTargetFolder = buildMainTargetFolder

}

/**
 * Build definition for compilation scope=test.
 */
trait BuildTest extends Build
  with BuildTestDependency
  with BuildTestSources
  with BuildTestTarget

object BuildTest extends BuildTest

trait BuildTestDependency extends BuildAnyDependency {

  @Description( """
  Project folders containing build classes
  which are dependency for compilation scope=test.
  """ )
  @Parameter(
    property     = "scalor.buildTestDependencyFolders",
    defaultValue = "${project.build.directory}/macro-classes,${project.build.directory}/classes"
  )
  var buildTestDependencyFolders : Array[ File ] = Array.empty

  @Description( """
  Maven dependency scopes which control selection
  of project dependency artifacts to be included
  in the classpath of compilation scope=test.
  """ )
  @Parameter(
    property     = "scalor.buildTestDependencyScopes",
    defaultValue = "compile,provided,system,test,runtime"
  )
  var buildTestDependencyScopes : Array[ String ] = Array.empty

  override def buildDependencyFolders = buildTestDependencyFolders
  override def buildDependencyScopes = buildTestDependencyScopes

}

trait BuildTestSources extends BuildAnySources {

  @Description( """
  Java source root folders to be included in compilation scope=test.
  """ )
  @Parameter(
    property     = "scalor.buildTestSourceJavaFolders",
    defaultValue = "${project.build.testSourceDirectory}"
  )
  var buildTestSourceJavaFolders : Array[ File ] = Array.empty

  @Description( """
  Scala source root folders to be included in compilation scope=test.
  """ )
  @Parameter(
    property     = "scalor.buildTestSourceScalaFolders",
    defaultValue = "${project.build.testSourceDirectory}/../scala"
  )
  var buildTestSourceScalaFolders : Array[ File ] = Array.empty

  override def buildSourceFolders = buildTestSourceJavaFolders ++ buildTestSourceScalaFolders

}

trait BuildTestTarget extends BuildAnyTarget {

  @Description( """
  Build output folder with result classes of compilation scope=test.
  """ )
  @Parameter(
    property     = "scalor.buildTestTargetFolder",
    defaultValue = "${project.build.testOutputDirectory}"
  )
  var buildTestTargetFolder : File = _

  override def buildTargetFolder = buildTestTargetFolder

}

/**
 * Required build dependencies.
 */
trait BuildAnyDependency {
  /**
   * Required class dependency folders.
   */
  def buildDependencyFolders : Array[ File ]
  /**
   * Required artifact dependency scopes.
   */
  def buildDependencyScopes : Scope.Bucket
}

/**
 * Root source folder list.
 */
trait BuildAnySources {
  /**
   * Root source folder list.
   */
  def buildSourceFolders : Array[ File ]
}

/**
 * Compile output directory.
 */
trait BuildAnyTarget {
  /**
   * Compile output directory.
   */
  def buildTargetFolder : File
}

/**
 * Build resource definitions.
 * Provides composable build paramenter modules.
 */
trait Build extends AnyRef
  with BuildAnyDependency
  with BuildAnySources
  with BuildAnyTarget {

}

trait BuildEnsure {

  @Description( """
  Create source/target folders when missing.
  Controls both Maven plugin and Eclipse companion.
  """ )
  @Parameter(
    property     = "scalor.buildEnsureFolders",
    defaultValue = "true"
  )
  var buildEnsureFolders : Boolean = _

}

object Build extends Build with BuildEnsure {

  import BuildMacro._
  import BuildMain._
  import BuildTest._

  /**
   * Build parameter names.
   */

  val paramEnsureFolders = nameOf( buildEnsureFolders )

  /**
   * Describe build paramteres for give scope.
   * See [[org.scalaide.core.internal.project.CompileScope]]
   */
  case class BuildParam(
    scope :              String = "invalid",
    javaSourceFolders :  String = "invalid",
    scalaSourceFolders : String = "invalid",
    buildTargetFolder :  String = "invalid"
  )

  object BuildParam {
    object attrib {
      /**
       * Name of custom attribute for Eclipse class path entry.
       */
      val scope = "scalor.scope"
    }
  }

  lazy val paramMacro = BuildParam(
    "macro",
    nameOf( buildMacroSourceJavaFolders ),
    nameOf( buildMacroSourceScalaFolders ),
    nameOf( buildMacroTargetFolder )
  )

  lazy val paramMain = BuildParam(
    "main",
    nameOf( buildMainSourceJavaFolders ),
    nameOf( buildMainSourceScalaFolders ),
    nameOf( buildMainTargetFolder )
  )

  lazy val paramTest = BuildParam(
    "test",
    nameOf( buildTestSourceJavaFolders ),
    nameOf( buildTestSourceScalaFolders ),
    nameOf( buildTestTargetFolder )
  )

  /**
   * Publish parameter names for companion Eclipse plugin.
   * Provides source/target root folders for each build scope.
   */
  lazy val descriptorMap = Map[ String, BuildParam ](
    mojo.`register-macro` -> paramMacro,
    mojo.`register-main` -> paramMain,
    mojo.`register-test` -> paramTest,
    "" -> BuildParam()
  )

  def buildDependencyFolders : Array[ File ] = Throw( "not used" )
  def buildDependencyScopes : Scope.Bucket = Throw( "not used" )
  def buildSourceFolders : Array[ File ] = Throw( "not used" )
  def buildTargetFolder : File = Throw( "not used" )

}
