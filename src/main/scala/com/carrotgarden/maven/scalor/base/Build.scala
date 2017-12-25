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

import com.carrotgarden.maven.scalor.util
import com.carrotgarden.maven.tools.Description

import com.carrotgarden.maven.scalor.A._
import com.github.dwickern.macros.NameOf._

import util.Error._
import util.Folder._

// https://maven.apache.org/guides/introduction/introduction-to-the-pom.html
// <sourceDirectory>src/main/java</sourceDirectory>
// <testSourceDirectory>src/test/java</testSourceDirectory>
// <directory>target</directory>
// <outputDirectory>target/classes</outputDirectory>
// <testOutputDirectory>target/test-classes</testOutputDirectory>

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
  var buildMacroDependencyFolders : Array[ File ] = Array[ File ]()

  @Description( """
  Maven dependency scopes which control selection
  of project dependency artifacts to be included
  in the classpath of compilation scope=macro.
  """ )
  @Parameter(
    property     = "scalor.buildMacroDependencyScopes",
    defaultValue = "compile,provided,system"
  )
  var buildMacroDependencyScopes : Array[ String ] = Array[ String ]()

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
  var buildMacroSourceScalaFolders : Array[ File ] = Array[ File ]()

  override def buildSourceFolders = buildMacroSourceJavaFolders ++ buildMacroSourceScalaFolders

  /**
   * Internal storage parameter.
   */
  def buildMacroSourceFoldersParam = "scalor.buildMacroSourceFolders"

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

  def buildMacroTargetParam = param.of( nameOf( buildMacroTargetFolder ) )

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
  var buildMainDependencyFolders : Array[ File ] = Array[ File ]()

  @Description( """
  Maven dependency scopes which control selection
  of project dependency artifacts to be included
  in the classpath of compilation scope=main.
  """ )
  @Parameter(
    property     = "scalor.buildMainDependencyScopes",
    defaultValue = "compile,provided,system"
  )
  var buildMainDependencyScopes : Array[ String ] = Array[ String ]()

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
  var buildMainSourceJavaFolders : Array[ File ] = Array[ File ]()

  @Description( """
  Scala source root folders to be included in compilation scope=main.
  """ )
  @Parameter(
    property     = "scalor.buildMainSourceScalaFolders",
    defaultValue = "${project.build.sourceDirectory}/../scala"
  )
  var buildMainSourceScalaFolders : Array[ File ] = Array[ File ]()

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
  var buildTestDependencyFolders : Array[ File ] = Array[ File ]()

  @Description( """
  Maven dependency scopes which control selection
  of project dependency artifacts to be included
  in the classpath of compilation scope=test.
  """ )
  @Parameter(
    property     = "scalor.buildTestDependencyScopes",
    defaultValue = "compile,provided,system,test,runtime"
  )
  var buildTestDependencyScopes : Array[ String ] = Array[ String ]()

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
  var buildTestSourceJavaFolders : Array[ File ] = Array[ File ]()

  @Description( """
  Scala source root folders to be included in compilation scope=test.
  """ )
  @Parameter(
    property     = "scalor.buildTestSourceScalaFolders",
    defaultValue = "${project.build.testSourceDirectory}/../scala"
  )
  var buildTestSourceScalaFolders : Array[ File ] = Array[ File ]()

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
 * Text encoding for sources and resources.
 */
trait BuildEncoding {

  @Description( """
  Text encoding for sources and resources.
  """ )
  @Parameter(
    property     = "scalor.buildSourceEncoding",
    defaultValue = "${project.build.sourceEncoding}"
  )
  var buildSourceEncoding : String = "UTF-8"

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
  """ )
  @Parameter(
    property     = "scalor.buildEnsureFolders",
    defaultValue = "true"
  )
  var buildEnsureFolders : Boolean = _

}

object Build extends Build {

  import BuildMacro._
  import BuildMain._
  import BuildTest._

  case class BuildParam(
    javaSourceFolders :  String = "scalor.invalid",
    scalaSourceFolders : String = "scalor.invalid",
    buildTargetFolder :  String = "scalor.invalid"
  )

  lazy val paramMacro = BuildParam(
    nameOf( buildMacroSourceJavaFolders ),
    nameOf( buildMacroSourceScalaFolders ),
    nameOf( buildMacroTargetFolder )
  )

  lazy val paramMain = BuildParam(
    nameOf( buildMainSourceJavaFolders ),
    nameOf( buildMainSourceScalaFolders ),
    nameOf( buildMainTargetFolder )
  )

  lazy val paramTest = BuildParam(
    nameOf( buildTestSourceJavaFolders ),
    nameOf( buildTestSourceScalaFolders ),
    nameOf( buildTestTargetFolder )
  )

  lazy val descriptorMap = Map[ String, BuildParam ](
    mojo.`register-macro` -> paramMacro,
    mojo.`register-main` -> paramMain,
    mojo.`register-test` -> paramTest,
    //    mojo.`compile-macro` -> paramMacro,
    //    mojo.`compile-main` -> paramMain,
    //    mojo.`compile-test` -> paramTest,
    //    mojo.`prepack-macro` -> paramMacro,
    //    mojo.`prepack-main` -> paramMain,
    //    mojo.`prepack-test` -> paramTest
    "" -> BuildParam()
  )

  def buildDependencyFolders : Array[ File ] = Throw( "not used" )
  def buildDependencyScopes : Scope.Bucket = Throw( "not used" )
  def buildSourceFolders : Array[ File ] = Throw( "not used" )
  def buildTargetFolder : File = Throw( "not used" )

}
