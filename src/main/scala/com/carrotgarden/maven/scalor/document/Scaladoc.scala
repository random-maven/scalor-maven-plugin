package com.carrotgarden.maven.scalor.document

import java.io.File

import org.apache.maven.plugins.annotations.Parameter

import com.carrotgarden.maven.scalor.base
import com.carrotgarden.maven.tools.Description

trait ScaladocAny extends AnyRef {

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

trait ScaladocRegex extends base.BuildAnyRegex {

  @Description( """
  Regular expression for Java source file discovery via inclusion by match against absolute path.
  File match is defined as: <code>include.hasMatch && ! exclude.hasMatch</code>.
  Matches files with <code>java</code> extension by default.
<pre>
  """ )
  @Parameter(
    property     = "scalor.scaladocRegexJavaInclude",
    defaultValue = """.+[.]java"""
  )
  var scaladocRegexJavaInclude : String = _

  @Description( """
  Regular expression for Java source file discovery via exclusion by match against absolute path.
  File match is defined as: <code>include.hasMatch && ! exclude.hasMatch</code>.
  Matches no files when empty by default.
  """ )
  @Parameter(
    property = "scalor.scaladocRegexJavaExclude"
  )
  var scaladocRegexJavaExclude : String = _

  @Description( """
  Regular expression for Scala source file discovery via inclusion by match against absolute path.
  File match is defined as: <code>include.hasMatch && ! exclude.hasMatch</code>.
  Matches files with <code>scala</code> extension by default.
  """ )
  @Parameter(
    property     = "scalor.scaladocRegexScalaInclude",
    defaultValue = """.+[.]scala"""
  )
  var scaladocRegexScalaInclude : String = _

  @Description( """
  Regular expression for Scala source file discovery via exclusion by match against absolute path.
  File match is defined as: <code>include.hasMatch && ! exclude.hasMatch</code>.
  Matches no files when empty by default.
  """ )
  @Parameter(
    property = "scalor.scaladocRegexScalaExclude"
  )
  var scaladocRegexScalaExclude : String = _

  override def buildRegexJavaInclude = scaladocRegexJavaInclude
  override def buildRegexJavaExclude = scaladocRegexJavaExclude
  override def buildRegexScalaInclude = scaladocRegexScalaInclude
  override def buildRegexScalaExclude = scaladocRegexScalaExclude

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
