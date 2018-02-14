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
 * Build resource definitions for any scope.
 */
trait Build extends AnyRef
  with BuildAnyDependency
  with BuildAnySources
  with BuildAnyTarget {
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
   * Root resource folder list.
   */
  def buildResourceFolders : Array[ Resource ]

  /**
   * Root source folder list.
   */
  def buildSourceFolders : Array[ File ]

}

/**
 * Build results directory.
 */
trait BuildAnyTarget {

  /**
   * Compile target directory.
   */
  def buildTargetFolder : File

}

/**
 * Build definition for compilation scope=macro.
 */
trait BuildMacro extends Build
  with BuildMacroDependency
  with BuildMacroSources
  with BuildMainTarget

object BuildMacro extends BuildMacro {
  // Ensure variables have default values.
}

trait BuildMacroDependency extends BuildAnyDependency {

  @Description( """
  Project folders containing build classes
  which are dependency for compilation scope=macro.
  Normally is empty.
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
  Scopes <a href="https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html">reference</a>.
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
  Normally uses <code>src/macro/java</code>.
  """ )
  @Parameter(
    property     = "scalor.buildMacroSourceJavaFolders",
    defaultValue = "${project.build.sourceDirectory}/../../macro/java"
  )
  var buildMacroSourceJavaFolders : Array[ File ] = Array[ File ]()

  @Description( """
  Scala source root folders to be included in compilation scope=macro.
  Normally uses <code>src/macro/scala</code>.
  """ )
  @Parameter(
    property     = "scalor.buildMacroSourceScalaFolders",
    defaultValue = "${project.build.sourceDirectory}/../../macro/scala"
  )
  var buildMacroSourceScalaFolders : Array[ File ] = Array.empty

  @Description( """
  Resource root folders to be included in compilation scope=macro.
  There are no macro resource folders by default.
  Component reference: 
<a href="https://maven.apache.org/pom.html#Resources">
  Resource
</a>.
  Example entry in <code>pom.xml</code>:
<pre>
&lt;buildMacroResourceFolders&gt;
  &lt;resource&gt;
    &lt;directory&gt;${project.basedir}/src/macro/resources&lt;/directory&gt;
  &lt;/resource&gt;  
&lt;/buildMacroResourceFolders&gt;
</pre>
  """ )
  @Parameter(
    property     = "scalor.buildMacroResourceFolders",
    defaultValue = ""
  )
  var buildMacroResourceFolders : Array[ Resource ] = Array.empty

  override def buildSourceFolders = buildMacroSourceJavaFolders ++ buildMacroSourceScalaFolders
  override def buildResourceFolders = buildMacroResourceFolders

}

/**
 * Build definition for compilation scope=main.
 */
trait BuildMain extends Build
  with BuildMainDependency
  with BuildMainSources
  with BuildMainTarget

object BuildMain extends BuildMain {
  // Ensure variables have default values.
}

trait BuildMainDependency extends BuildAnyDependency {

  @Description( """
  Project folders containing build classes
  which are dependency for compilation scope=main.
  Normally includes folder scope=[macro] (<code>target/classes</code>).
  """ )
  @Parameter(
    property     = "scalor.buildMainDependencyFolders",
    defaultValue = "${project.build.outputDirectory}"
  )
  var buildMainDependencyFolders : Array[ File ] = Array.empty

  @Description( """
  Maven dependency scopes which control selection
  of project dependency artifacts to be included
  in the classpath of compilation scope=main.
  Scopes <a href="https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html">reference</a>.
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
  Normally uses <code>src/main/java</code>.
  """ )
  @Parameter(
    property     = "scalor.buildMainSourceJavaFolders",
    defaultValue = "${project.build.sourceDirectory}"
  )
  var buildMainSourceJavaFolders : Array[ File ] = Array.empty

  @Description( """
  Scala source root folders to be included in compilation scope=main.
  Normally uses <code>src/main/scala</code>.
  """ )
  @Parameter(
    property     = "scalor.buildMainSourceScalaFolders",
    defaultValue = "${project.build.sourceDirectory}/../scala"
  )
  var buildMainSourceScalaFolders : Array[ File ] = Array.empty

  @Description( """
  Resource root folders to be included in compilation scope=main.
  Normally uses <code>src/main/resources</code>.
  Component reference: 
<a href="https://maven.apache.org/pom.html#Resources">
  Resource
</a>.
  Example entry in <code>pom.xml</code>:
<pre>
&lt;buildMainResourceFolders&gt;
  &lt;resource&gt;
    &lt;directory&gt;${project.basedir}/src/main/resources&lt;/directory&gt;
  &lt;/resource&gt;  
&lt;/buildMainResourceFolders&gt;
</pre>
  """ )
  @Parameter(
    property     = "scalor.buildMainResourceFolders",
    defaultValue = "${project.build.resources}"
  )
  var buildMainResourceFolders : Array[ Resource ] = Array.empty

  override def buildSourceFolders = buildMainSourceJavaFolders ++ buildMainSourceScalaFolders
  override def buildResourceFolders = buildMainResourceFolders

}

trait BuildMainTarget extends BuildAnyTarget {

  @Description( """
  Build target folder with result classes of compilation scope=[macro,main].
  Normally uses <code>target/classes</code>.
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

object BuildTest extends BuildTest {
  // Ensure variables have default values.
}

trait BuildTestDependency extends BuildAnyDependency {

  @Description( """
  Project folders containing build classes
  which are dependency for compilation scope=test.
  Normally includes folder scope=[macro,main] (<code>target/classes</code>).
  """ )
  @Parameter(
    property     = "scalor.buildTestDependencyFolders",
    defaultValue = "${project.build.outputDirectory}"
  )
  var buildTestDependencyFolders : Array[ File ] = Array.empty

  @Description( """
  Maven dependency scopes which control selection
  of project dependency artifacts to be included
  in the classpath of compilation scope=test.
  Scopes <a href="https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html">reference</a>.
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
  Normally uses <code>src/test/java</code>.
  """ )
  @Parameter(
    property     = "scalor.buildTestSourceJavaFolders",
    defaultValue = "${project.build.testSourceDirectory}"
  )
  var buildTestSourceJavaFolders : Array[ File ] = Array.empty

  @Description( """
  Scala source root folders to be included in compilation scope=test.
  Normally uses <code>src/test/scala</code>.
  """ )
  @Parameter(
    property     = "scalor.buildTestSourceScalaFolders",
    defaultValue = "${project.build.testSourceDirectory}/../scala"
  )
  var buildTestSourceScalaFolders : Array[ File ] = Array.empty

  @Description( """
  Resource root folders to be included in compilation scope=test.
  Normally uses <code>src/test/resources</code>.
  Component reference: 
<a href="https://maven.apache.org/pom.html#Resources">
  Resource
</a>.
  Example entry in <code>pom.xml</code>:
<pre>
&lt;buildTestResourceFolders&gt;
  &lt;resource&gt;
    &lt;directory&gt;${project.basedir}/src/test/resources&lt;/directory&gt;
  &lt;/resource&gt;  
&lt;/buildTestResourceFolders&gt;
</pre>
  """ )
  @Parameter(
    property     = "scalor.buildTestResourceFolders",
    defaultValue = "${project.build.testResources}"
  )
  var buildTestResourceFolders : Array[ Resource ] = Array.empty

  override def buildSourceFolders = buildTestSourceJavaFolders ++ buildTestSourceScalaFolders
  override def buildResourceFolders = buildTestResourceFolders

}

trait BuildTestTarget extends BuildAnyTarget {

  @Description( """
  Build target folder with result classes of compilation scope=test.
  Normally uses <code>target/test-classes</code>.
  """ )
  @Parameter(
    property     = "scalor.buildTestTargetFolder",
    defaultValue = "${project.build.testOutputDirectory}"
  )
  var buildTestTargetFolder : File = _

  override def buildTargetFolder = buildTestTargetFolder

}

trait BuildEnsure {

  @Description( """
  Create build source root and target root folders when missing.
  """ )
  @Parameter(
    property     = "scalor.buildEnsureFolders",
    defaultValue = "true"
  )
  var buildEnsureFolders : Boolean = _

}

object Build {

  /**
   * Build constants.
   */
  object Param {

    /**
     * Define plugin scope names.
     */
    object scope {
      val `macro` = "macro"
      val `main` = "main"
      val `test` = "test"
    }

    /**
     * Define class path entry attributes.
     */
    object attrib {
      /**
       * Name of custom compilation scope attribute for Eclipse class path entry.
       */
      val scope = "scalor.scope"
      /**
       * Eclipse "optional" class path entry does not complain when folder is missing.
       */
      val optional = "optional"
    }

  }

}
