package com.carrotgarden.maven.scalor.document

import java.io.File
import java.util.ArrayList
import java.util.Arrays
import java.util.Collections

import org.apache.maven.artifact.Artifact
import org.apache.maven.execution.MavenSession
import org.apache.maven.model.Dependency
import org.apache.maven.plugin.MojoExecution
import org.apache.maven.plugins.annotations.Component
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject
import com.carrotgarden.maven.tools.Description
import org.codehaus.plexus.archiver.Archiver
import org.codehaus.plexus.archiver.jar.JarArchiver
import org.apache.maven.archiver.MavenArchiveConfiguration
import org.apache.maven.project.MavenProjectHelper

/**
 * Provides components injected by Maven runtime.
 */
trait Params {

//  @Description( """
//  Maven archive builder.
//  """ )
//  @Component( role = classOf[ Archiver ], hint = "jar" )
//  var archiveBuilder : JarArchiver = _

  @Description( """
  Configuration of Scaladoc archive jar. 
  Normally used with provided default values.
  Component reference:
<a href="https://maven.apache.org/shared/maven-archiver/index.html">
  MavenArchiveConfiguration
</a>
  """ )
  @Parameter()
  var scaladocArchiveConfig : MavenArchiveConfiguration = new MavenArchiveConfiguration()

  @Description( """
  Maven project helper.
  """ )
  @Component()
  var projectHelper : MavenProjectHelper = _

  @Description( """
  Contains the full list of projects in the build.
  """ )
  @Parameter( defaultValue = "${reactorProjects}", readonly = true )
  var reactorProjects : java.util.List[ MavenProject ] = _

}
