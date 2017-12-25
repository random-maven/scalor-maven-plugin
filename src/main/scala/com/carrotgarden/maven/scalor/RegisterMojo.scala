package com.carrotgarden.maven.scalor

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations._
import org.apache.maven.execution.MavenSession
import org.apache.maven.project.MavenProject
import org.apache.maven.plugin.BuildPluginManager
import org.apache.maven.plugin.MojoFailureException
import org.sonatype.plexus.build.incremental.BuildContext

import scala.collection.JavaConverters._

import java.util.Date
import java.io.File

import A.mojo._
import util.Folder._

import com.carrotgarden.maven.tools.Description

@Description( """
Register project sources for compilation scope=macro.
""" )
@Mojo(
  name                         = `register-macro`,
  defaultPhase                 = LifecyclePhase.INITIALIZE,
  requiresDependencyResolution = ResolutionScope.NONE
)
class RegisterMacroMojo extends RegisterAnyMojo
  with base.BuildMacroSources
  with base.BuildMacroTarget {

  override def mojoName = `register-macro`

  @Description( """
  Flag to skip goal execution: register-macro.
  """ )
  @Parameter(
    property     = "scalor.skipRegisterMacro", //
    defaultValue = "false"
  )
  var skipRegisterMacro : Boolean = _

  override def hasSkipMojo = skipRegisterMacro
  override def sourceRootList = extractPropertyList( buildMacroSourceFoldersParam ).asJava
  override def registerSourceRoot : RegistrationFunction = persistPropertyList( buildMacroSourceFoldersParam, _ )
  override def targetRoot : String = extractProperty( buildMacroTargetParam )
  override def registerTargetRoot : RegistrationFunction = persistProperty( buildMacroTargetParam, _ )
  
}

@Description( """
Register project sources for compilation scope=main.
""" )
@Mojo(
  name                         = `register-main`,
  defaultPhase                 = LifecyclePhase.INITIALIZE,
  requiresDependencyResolution = ResolutionScope.NONE
)
class RegisterMainMojo extends RegisterAnyMojo
  with base.BuildMainSources
  with base.BuildMainTarget {

  override def mojoName = `register-main`

  @Description( """
  Flag to skip goal execution: register-main.
  """ )
  @Parameter(
    property     = "scalor.skipRegisterMain", //
    defaultValue = "false"
  )
  var skipRegisterMain : Boolean = _

  override def hasSkipMojo = skipRegisterMain
  override def sourceRootList = project.getCompileSourceRoots
  override def registerSourceRoot : RegistrationFunction = project.addCompileSourceRoot _
  override def targetRoot : String = project.getBuild.getOutputDirectory
  override def registerTargetRoot : RegistrationFunction = project.getBuild.setOutputDirectory _

}

@Description( """
Register project sources for compilation scope=test.
""" )
@Mojo(
  name                         = `register-test`,
  defaultPhase                 = LifecyclePhase.INITIALIZE,
  requiresDependencyResolution = ResolutionScope.NONE
)
class RegisterTestMojo extends RegisterAnyMojo
  with base.BuildTestSources
  with base.BuildTestTarget {

  override def mojoName = `register-test`

  @Description( """
  Flag to skip goal execution: register-test.
  """ )
  @Parameter(
    property     = "scalor.skipRegisterTest", //
    defaultValue = "false"
  )
  var skipRegisterTest : Boolean = _

  override def hasSkipMojo = skipRegisterTest
  override def sourceRootList = project.getTestCompileSourceRoots
  override def registerSourceRoot : RegistrationFunction = project.addTestCompileSourceRoot _
  override def targetRoot : String = project.getBuild.getTestOutputDirectory
  override def registerTargetRoot : RegistrationFunction = project.getBuild.setTestOutputDirectory _

}

trait RegisterAnyMojo extends AbstractMojo
  with base.Mojo
  with base.BuildEnsure
  with base.Params
  with base.Logging
  with base.Skip
  with base.BuildAnySources
  with base.BuildAnyTarget
  with eclipse.Build {

  type RegistrationFunction = ( String => Unit )

  @Description( """
  Flag to skip goal execution: register-*.
  """ )
  @Parameter(
    property     = "scalor.skipRegister", //
    defaultValue = "false"
  )
  var skipRegister : Boolean = _

  /**
   * Detect if given source root is already registered.
   */
  def hasSourceRoot( root : String ) = sourceRootList.contains( root )

  /**
   * Declare project source roots.
   */
  def sourceRootList : java.util.List[ String ]

  /**
   * Register source root for given compilation scope with the project.
   */
  def registerSourceRoot : RegistrationFunction

  /**
   */
  def hasTargetRoot( root : String ) : Boolean = root == targetRoot

  /**
   */
  def targetRoot : String

  /**
   */
  def registerTargetRoot : RegistrationFunction

  /**
   */
  def perfromRegister() : Unit = {

    buildSourceFolders.map( _.getCanonicalPath ).foreach { path =>
      if ( hasSourceRoot( path ) ) {
        say.info( "Already registered:  " + path )
      } else {
        say.info( "Register source root: " + path )
        registerSourceRoot( path )
      }
    }

    List( buildTargetFolder.getCanonicalPath ).foreach { path =>
      if ( hasTargetRoot( path ) ) {
        say.info( "Already registered:  " + path )
      } else {
        say.info( "Register target root: " + path )
        registerTargetRoot( path )
      }
    }

    if ( buildEnsureFolders ) {
      say.info( "Ensuring build folders." )
      buildSourceFolders.foreach { folder =>
        ensureFolder( folder )
      }
      ensureFolder( buildTargetFolder )
    }
  }

  override def perform() : Unit = {
    if ( skipRegister || hasSkipMojo ) {
      say.info( "Skipping disabled goal execution." )
      return
    }
    if ( hasIncremental ) {
      say.info( "Skipping incremental build invocation." )
      return
    }
    perfromRegister()
  }

}
