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
import org.apache.maven.reporting.MavenReport
import java.util.Locale
import org.codehaus.doxia.sink.Sink

import java.io.File

trait ScaladocAny {

  @Description( """
  Root name for the generated Scaladoc jar file.
  Full name will include <code>classifier</code> suffix.
  """ )
  @Parameter(
    property     = "scalor.scaladocFinalName",
    defaultValue = "${project.build.finalName}"
  )
  var scaladocFinalName : String = _

  def scaladocHasAttach : Boolean
  def scaladocClassifier : String
  def scaladocOutputFolder : File

  def scaladocArchiveName = s"${scaladocFinalName}-${scaladocClassifier}.jar"

}

trait ScaladocMacro extends ScaladocMain {

  // included in scope=main
  override def scaladocHasAttach = false

}

trait ScaladocMain extends ScaladocAny {

  @Description( """
  Artifact classifier for Scaladoc with scope=main.
  Using Java convension by default.
  Appended to <a href="#scaladocFinalName"><b>scaladocFinalName</b></a>.
  """ )
  @Parameter(
    property     = "scalor.scaladocMainClassifier",
    defaultValue = "javadoc"
  )
  var scaladocMainClassifier : String = _

  @Description( """
  Enable to attach generated Scaladoc 
  to the project as deployment artifact with scope=main.
  """ )
  @Parameter(
    property     = "scalor.scaladocMainAttach",
    defaultValue = "true"
  )
  var scaladocMainAttach : Boolean = _

  @Description( """
  Folder with generated Scaladoc content with scope=main. 
  """ )
  @Parameter(
    property     = "scalor.scaladocMainOutputFolder",
    defaultValue = "${project.reporting.outputDirectory}/scaladoc-main"
  )
  var scaladocMainOutputFolder : File = _

  override def scaladocHasAttach = scaladocMainAttach
  override def scaladocClassifier = scaladocMainClassifier
  override def scaladocOutputFolder = scaladocMainOutputFolder

}

trait ScaladocTest extends ScaladocAny {

  @Description( """
  Artifact classifier for Scaladoc with scope=test.
  Using Java convension by default.
  Appended to <a href="#scaladocFinalName"><b>scaladocFinalName</b></a>.
  """ )
  @Parameter(
    property     = "scalor.scaladocTestClassifier",
    defaultValue = "test-javadoc"
  )
  var scaladocTestClassifier : String = _

  @Description( """
  Enable to attach generated Scaladoc 
  to the project as deployment artifact with scope=test.
  """ )
  @Parameter(
    property     = "scalor.scaladocTestAttach",
    defaultValue = "true"
  )
  var scaladocTestAttach : Boolean = _

  @Description( """
  Folder with generated Scaladoc content with scope=test. 
  """ )
  @Parameter(
    property     = "scalor.scaladocTestOutputFolder",
    defaultValue = "${project.reporting.outputDirectory}/scaladoc-test"
  )
  var scaladocTestOutputFolder : File = _

  override def scaladocHasAttach = scaladocTestAttach
  override def scaladocClassifier = scaladocTestClassifier
  override def scaladocOutputFolder = scaladocTestOutputFolder

}
