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

trait Params {

  @Description( """
  Maven archive builder.
  """ )
  @Component( role = classOf[ Archiver ], hint = "jar" )
  var archiveBuilder : JarArchiver = _

  @Description( """
  Maven archive configuration.
  """ )
  @Parameter()
  var archiveConfig : MavenArchiveConfiguration = new MavenArchiveConfiguration()

  @Description( """
  Maven project helper.
  """ )
  @Component()
  var projectHelper : MavenProjectHelper = _

  @Description( """
  The directory where the generated archive file will be put.
  """ )
  @Parameter( defaultValue = "${project.build.directory}" )
  var sourcesOutputDirectory : File = _

  @Description( """
  Contains the full list of projects in the reactor.
  """ )
  @Parameter( defaultValue = "${reactorProjects}", readonly = true )
  var reactorProjects : java.util.List[ MavenProject ] = _

}

trait ParamsAnySources {

  def sourcesClassifier : String

}

trait ParamsMacroSources extends AnyRef
  with ParamsAnySources {

  @Description( """
  Classifier of sources in compilation scope=macro.
  """ )
  @Parameter(
    property     = "scalor.sourcesMacroClassifier",
    defaultValue = "macro-sources"
  )
  var sourcesMacroClassifier : String = _

  override def sourcesClassifier = sourcesMacroClassifier

}

trait ParamsMainSources extends AnyRef
  with ParamsAnySources {

  @Description( """
  Classifier of sources in compilation scope=main.
  """ )
  @Parameter(
    property     = "scalor.sourcesMainClassifier",
    defaultValue = "sources"
  )
  var sourcesMainClassifier : String = _

  override def sourcesClassifier = sourcesMainClassifier

}

trait ParamsTestSources extends AnyRef
  with ParamsAnySources {

  @Description( """
  Classifier of sources in compilation scope=test.
  """ )
  @Parameter(
    property     = "scalor.sourcesTestClassifier",
    defaultValue = "test-sources"
  )
  var sourcesTestClassifier : String = _

  override def sourcesClassifier = sourcesTestClassifier

}
