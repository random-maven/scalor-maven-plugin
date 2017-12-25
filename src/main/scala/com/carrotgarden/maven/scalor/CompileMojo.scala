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

@Description( """
Compile Scala sources in compilation scope=macro.
""" )
@Mojo(
  name                         = `compile-macro`,
  defaultPhase                 = LifecyclePhase.COMPILE,
  requiresDependencyResolution = ResolutionScope.COMPILE
)
class CompileMacroMojo extends CompileAnyMojo
  with zinc.CompilerMacro {

  override def mojoName = `compile-macro`

  @Description( """
    Flag to skip goal execution: compile-macro.
    """ )
  @Parameter(
    property     = "scalor.skipCompileMacro", //
    defaultValue = "false"
  )
  var skipCompileMacro : Boolean = _

  def hasSkipMojo = skipCompileMacro

}

@Description( """
Compile Java and Scala sources in compilation scope=main.
""" )
@Mojo(
  name                         = `compile-main`,
  defaultPhase                 = LifecyclePhase.COMPILE,
  requiresDependencyResolution = ResolutionScope.COMPILE
)
class CompileMainMojo extends CompileAnyMojo
  with zinc.CompilerMain {

  override def mojoName = `compile-main`

  @Description( """
  Flag to skip goal execution: compile-main.
  """ )
  @Parameter(
    property     = "scalor.skipCompileMain", //
    defaultValue = "false"
  )
  var skipCompileMain : Boolean = _

  def hasSkipMojo = skipCompileMain

}

@Description( """
Compile Java and Scala sources in compilation scope=test.
""" )
@Mojo(
  name                         = `compile-test`,
  defaultPhase                 = LifecyclePhase.TEST_COMPILE,
  requiresDependencyResolution = ResolutionScope.TEST
)
class CompileTestMojo extends CompileAnyMojo
  with zinc.CompilerTest {

  override def mojoName = `compile-test`

  @Description( """
  Flag to skip goal execution: compile-test.
  """ )
  @Parameter(
    property     = "scalor.skipCompileTest", //
    defaultValue = "false"
  )
  var skipCompileTest : Boolean = _

  def hasSkipMojo = skipCompileTest

}

/**
 * Shared compiler mojo interface.
 */
trait CompileAnyMojo extends AbstractMojo
  with base.Mojo
  with base.Params
  with base.Build
  with base.Logging
  with base.Skip
  with eclipse.Build
  with zinc.Params
  with zinc.Compiler {

  @Description( """
  Flag to skip compile execution: compile-*.
  """ )
  @Parameter(
    property     = "scalor.skipCompile", //
    defaultValue = "false"
  )
  var skipCompile : Boolean = _

  @Description( """
  Flag to skip compile execution in Eclipse: compile-*.
  """ )
  @Parameter(
    property     = "scalor.skipCompileEclipse", //
    defaultValue = "true"
  )
  var skipCompileEclipse : Boolean = _

  override def perform() : Unit = {
    if ( skipCompile || hasSkipMojo ) {
      say.info( "Skipping disabled goal execution." )
      return
    }
    if ( hasEclipse && skipCompileEclipse ) {
      say.info( "Skipping eclipse build invocation." )
      return
    }
    if ( hasIncremental ) {
      say.info( "Skipping incremental build invocation." )
      return
    }
    zincPerformCompile()
  }

}
