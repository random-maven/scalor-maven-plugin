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

/**
 * Shared compiler mojo interface.
 */
trait CompileAnyMojo extends AbstractMojo
  with base.Mojo
  with base.Build
  with base.ParamsCompiler
  with resolve.Maven
  with zinc.Params
  with zinc.Compiler
  with zinc.Resolve {

  @Description( """
  Flag to skip compile execution: <code>compile-*</code>.
  """ )
  @Parameter(
    property     = "scalor.skipCompile", //
    defaultValue = "false"
  )
  var skipCompile : Boolean = _

  def performCompile() : Unit = {
    zincPerformCompile()
  }

  override def perform() : Unit = {
    if ( skipCompile || hasSkipMojo ) {
      reportSkipReason( "Skipping disabled goal execution." )
      return
    }
    performCompile()
  }

}

@Description( """
Compile Java and Scala sources in all compilation scopes.
Invokes goals: compile-*
""" )
@Mojo(
  name                         = A.mojo.`compile`,
  defaultPhase                 = LifecyclePhase.COMPILE,
  requiresDependencyResolution = ResolutionScope.TEST
)
class CompileArkonMojo extends CompileAnyMojo
  with zinc.CompilerMacro
  with zinc.CompilerMain
  with zinc.CompilerTest {

  override def mojoName = A.mojo.`compile`

  override def zincBuildCache = throwNotUsed

  override def performCompile() : Unit = {
    executeSelfMojo( A.mojo.`compile-macro` )
    executeSelfMojo( A.mojo.`compile-main` )
    executeSelfMojo( A.mojo.`compile-test` )
  }

}

@Description( """
Compile Java and Scala sources in compilation scope=macro.
A member of goal=compile.
""" )
@Mojo(
  name                         = A.mojo.`compile-macro`,
  defaultPhase                 = LifecyclePhase.COMPILE,
  requiresDependencyResolution = ResolutionScope.COMPILE
)
class CompileMacroMojo extends CompileAnyMojo
  with zinc.CompilerMacro {

  override def mojoName = A.mojo.`compile-macro`

  @Description( """
    Flag to skip goal execution: <code>compile-macro</code>.
    """ )
  @Parameter(
    property     = "scalor.skipCompileMacro", //
    defaultValue = "false"
  )
  var skipCompileMacro : Boolean = _

  override def hasSkipMojo = skipCompileMacro

}

@Description( """
Compile Java and Scala sources in compilation scope=main.
A member of goal=compile.
""" )
@Mojo(
  name                         = A.mojo.`compile-main`,
  defaultPhase                 = LifecyclePhase.COMPILE,
  requiresDependencyResolution = ResolutionScope.COMPILE
)
class CompileMainMojo extends CompileAnyMojo
  with zinc.CompilerMain {

  override def mojoName = A.mojo.`compile-main`

  @Description( """
  Flag to skip goal execution: <code>compile-main</code>.
  """ )
  @Parameter(
    property     = "scalor.skipCompileMain", //
    defaultValue = "false"
  )
  var skipCompileMain : Boolean = _

  override def hasSkipMojo = skipCompileMain

}

@Description( """
Compile Java and Scala sources in compilation scope=test.
A member of goal=compile.
""" )
@Mojo(
  name                         = A.mojo.`compile-test`,
  defaultPhase                 = LifecyclePhase.TEST_COMPILE,
  requiresDependencyResolution = ResolutionScope.TEST
)
class CompileTestMojo extends CompileAnyMojo
  with zinc.CompilerTest {

  override def mojoName = A.mojo.`compile-test`

  @Description( """
  Flag to skip goal execution: <code>compile-test</code>.
  """ )
  @Parameter(
    property     = "scalor.skipCompileTest", //
    defaultValue = "false"
  )
  var skipCompileTest : Boolean = _

  override def hasSkipMojo = skipCompileTest

}
