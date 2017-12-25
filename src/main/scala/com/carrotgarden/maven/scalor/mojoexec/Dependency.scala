package com.carrotgarden.maven.scalor.mojoexec

import org.twdata.maven.mojoexecutor.MojoExecutor._

import org.apache.maven.plugins.annotations._
import java.io.File

import com.carrotgarden.maven.scalor.base

/**
 * Invoke maven-dependency-plugin.
 */
trait Dependency {
  self : base.Params with base.Logging =>

  /**
   * artifact scope descriptor to include in the dependency resolution
   */
  def dependencyIncludeScope : String

  /**
   * exclude transitive dependency artifacts from the resolution result
   */
  @Parameter(
    property     = "scalor.dependencyExcludeTransitive", //
    defaultValue = "true"
  )
  var dependencyExcludeTransitive : Boolean = _

  /**
   *
   */
  @Parameter(
    property     = "scalor.dependencyOutputProperty", //
    defaultValue = "scalor.DependencyClassPath"
  )
  var dependencyOutputProperty : String = _

  /**
   *
   */
  @Parameter(
    property     = "scalor.dependencyOutputListFile", //
    defaultValue = "${project.build.directory}/scalor/dependency.list"
  )
  var dependencyOutputListFile : File = _

  /**
   * Result of "build-classpath".
   */
  def dependencyClassPathResult : String = {
    project.getProperties.getProperty( dependencyOutputProperty, "" );
  }

  /**
   * Inject dependency class path into a project variable.
   */
  def dependencyBuildClassPath() = {
    say.info( "Building dependency classpath." )
    executeMojo( //
      plugin( //
        groupId( "org.apache.maven.plugins" ), //
        artifactId( "maven-dependency-plugin" ) //
      ), //
      goal( "build-classpath" ), //
      configuration( //
        element( "silent", true.toString ),
        element( "includeScope", dependencyIncludeScope ), //
        element( "excludeTransitive", dependencyExcludeTransitive.toString ), //
        element( "outputProperty", dependencyOutputProperty ) //
      ), //
      executionEnvironment( project, session, manager ) //
    )
  }

  /**
   * Produce dependency resolution report.
   */
  def dependencyResolve() = {
    say.info( "Producing dependency report." )
    executeMojo( //
      plugin( //
        groupId( "org.apache.maven.plugins" ), //
        artifactId( "maven-dependency-plugin" ) //
      ), //
      goal( "resolve" ), //
      configuration( //
        element( "includeScope", dependencyIncludeScope ), //
        element( "excludeTransitive", dependencyExcludeTransitive.toString() ), //
        element( "outputFile", dependencyOutputListFile.getAbsolutePath ) //
      ), //
      executionEnvironment( project, session, manager ) //
    )
  }

}
