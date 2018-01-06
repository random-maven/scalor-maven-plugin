package com.carrotgarden.maven.scalor

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations._
import org.apache.maven.execution.MavenSession
import org.apache.maven.project.MavenProject
import org.apache.maven.plugin.BuildPluginManager
import org.apache.maven.plugin.MojoFailureException
import org.sonatype.plexus.build.incremental.BuildContext

import com.carrotgarden.maven.tools.Description

import A.mojo._
import org.apache.maven.reporting.MavenReport
import java.io.File
import java.util.Locale
import org.codehaus.doxia.sink.Sink

trait ScaladocAnyMojo extends AbstractMojo with MavenReport
  with base.Mojo
  with base.Params
  with base.Logging
  with base.SkipMojo {

  import MavenReport._

  override def isExternalReport() : Boolean = true
  override def getCategoryName : String = CATEGORY_PROJECT_REPORTS
  override def getOutputName() : String = ""
  override def getReportOutputDirectory() : File = ???
  override def setReportOutputDirectory( file : File ) : Unit = ???
  override def getName( locale : Locale ) : String = ""
  override def getDescription( locale : Locale ) : String = ""

  override def canGenerateReport() : Boolean = {
    //        var back = ((project.isExecutionRoot() || forceAggregate) && canAggregate() && project.getCollectedProjects().size() > 0);
    //        back = back || (findSourceFiles().size() != 0);
    //        return back;
    ???
  }

  def generate( sink : Sink, locale : Locale ) : Unit = {
    ???
  }

  override def perform() : Unit = {
    say.info( s"TODO" )
  }

}

@Description( """
Produce project scaladoc for compilation scope=macro.
""" )
@Mojo(
  name                         = `scaladoc-macro`,
  defaultPhase                 = LifecyclePhase.GENERATE_SOURCES,
  requiresDependencyResolution = ResolutionScope.COMPILE
)
class ScaladocMacroMojo extends ScaladocAnyMojo {

  override def mojoName : String = `scaladoc-macro`

}

@Description( """
Produce project scaladoc for compilation scope=main.
""" )
@Mojo(
  name                         = `scaladoc-main`,
  defaultPhase                 = LifecyclePhase.GENERATE_SOURCES,
  requiresDependencyResolution = ResolutionScope.COMPILE
)
class ScaladocMainMojo extends ScaladocAnyMojo {

  override def mojoName : String = `scaladoc-main`

}

@Description( """
Produce project scaladoc for compilation scope=test.
""" )
@Mojo(
  name                         = `scaladoc-test`,
  defaultPhase                 = LifecyclePhase.GENERATE_TEST_SOURCES,
  requiresDependencyResolution = ResolutionScope.TEST
)
class ScaladocTestMojo extends ScaladocAnyMojo {

  override def mojoName : String = `scaladoc-test`

}

trait SourcesAnyMojo extends AbstractMojo
  with base.Mojo
  with base.Params
  with base.Logging
  with base.SkipMojo
  with document.ParamsAnySources {

  override def perform() : Unit = {
    say.info( s"TODO" )
  }

}

@Description( """
Produce project sources for compilation scope=macro.
""" )
@Mojo(
  name                         = `sources-macro`,
  defaultPhase                 = LifecyclePhase.PACKAGE,
  requiresDependencyResolution = ResolutionScope.NONE
)
class SourcesMacroMojo extends SourcesAnyMojo
  with document.ParamsMacroSources {

  override def mojoName : String = `sources-macro`

}

@Description( """
Produce project sources for compilation scope=main.
""" )
@Mojo(
  name                         = `sources-main`,
  defaultPhase                 = LifecyclePhase.PACKAGE,
  requiresDependencyResolution = ResolutionScope.NONE
)
class SourcesMainMojo extends SourcesAnyMojo
  with document.ParamsMainSources {

  override def mojoName : String = `sources-main`

}

@Description( """
Produce project sources for compilation scope=test.
""" )
@Mojo(
  name                         = `sources-test`,
  defaultPhase                 = LifecyclePhase.PACKAGE,
  requiresDependencyResolution = ResolutionScope.NONE
)
class SourcesTestMojo extends SourcesAnyMojo
  with document.ParamsTestSources {

  override def mojoName : String = `sources-test`

}
