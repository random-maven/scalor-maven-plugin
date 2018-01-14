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

trait ReportAny {

  self : MavenReport =>

  import MavenReport._

  //  @Description( """
  //  """ )
  //  @Parameter(
  //    property     = "scalor.reportDestinationFolder",
  //    defaultValue = "scaladoc"
  //  )
  //  var reportDestinationFolder : String = _

  override def isExternalReport : Boolean = true

  override def getCategoryName : String = CATEGORY_PROJECT_REPORTS

  override def canGenerateReport : Boolean = {
    //        var back = ((project.isExecutionRoot() || forceAggregate) && canAggregate() && project.getCollectedProjects().size() > 0);
    //        back = back || (findSourceFiles().size() != 0);
    //        return back;
    true
  }

  override def generate( sink : Sink, locale : Locale ) : Unit = {
    ???
  }

}

trait ReportMain extends AnyRef
  with ReportAny {

  self : MavenReport =>

  @Description( """
  """ )
  @Parameter(
    property     = "scalor.reportMainReportName",
    defaultValue = "ScalaDoc"
  )
  var reportMainReportName : String = _

  @Description( """
  """ )
  @Parameter(
    property     = "scalor.reportMainReportDescription",
    defaultValue = "Scala Documentation"
  )
  var reportMainReportDescription : String = _

  @Description( """
  """ )
  @Parameter(
    property     = "scalor.reportMainReportOutputDirectory",
    defaultValue = "${project.reporting.outputDirectory}/scaladoc-main"
  )
  var reportMainReportOutputDirectory : File = _

  @Description( """
  """ )
  @Parameter(
    property     = "scalor.reportMainReportRelativeFolder",
    defaultValue = "scaladoc-main"
  )
  var reportMainReportRelativeFolder : String = _

  override def getName( locale : Locale ) : String = reportMainReportName
  override def getDescription( locale : Locale ) : String = reportMainReportDescription
  override def getOutputName : String = reportMainReportRelativeFolder + "/index.html"
  override def getReportOutputDirectory : File = reportMainReportOutputDirectory
  override def setReportOutputDirectory( file : File ) : Unit = {
    if ( reportMainReportOutputDirectory.getAbsolutePath.endsWith( reportMainReportRelativeFolder ) ) {
      reportMainReportOutputDirectory = file
    } else {
      reportMainReportOutputDirectory = new File( file, reportMainReportRelativeFolder )
    }
  }

}

trait ReportTest extends AnyRef
  with ReportAny {

  self : MavenReport =>

  @Description( """
  """ )
  @Parameter(
    property     = "scalor.reportTestReportName",
    defaultValue = "ScalaDoc [TEST]"
  )
  var reportTestReportName : String = _

  @Description( """
  """ )
  @Parameter(
    property     = "scalor.reportTestReportDescription",
    defaultValue = "Scala Documentation [TEST]"
  )
  var reportTestReportDescription : String = _

  @Description( """
  """ )
  @Parameter(
    property     = "scalor.reportTestReportOutputDirectory",
    defaultValue = "${project.reporting.outputDirectory}/scaladoc-test"
  )
  var reportTestReportOutputDirectory : File = _

  @Description( """
  """ )
  @Parameter(
    property     = "scalor.reportMainReportRelativeFolder",
    defaultValue = "scaladoc-test"
  )
  var reportTestReportRelativeFolder : String = _

  override def getName( locale : Locale ) : String = reportTestReportName
  override def getDescription( locale : Locale ) : String = reportTestReportDescription
  override def getReportOutputDirectory : File = reportTestReportOutputDirectory
  override def getOutputName : String = reportTestReportRelativeFolder + "/index.html"
  override def setReportOutputDirectory( file : File ) : Unit = {
    if ( reportTestReportOutputDirectory.getAbsolutePath.endsWith( reportTestReportRelativeFolder ) ) {
      reportTestReportOutputDirectory = file
    } else {
      reportTestReportOutputDirectory = new File( file, reportTestReportRelativeFolder )
    }
  }

}
