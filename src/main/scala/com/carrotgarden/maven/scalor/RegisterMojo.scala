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
import java.util.Arrays
import java.util.Collections
import org.apache.maven.model.Resource
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.Path

/**
 * Shared register mojo interface.
 * Register Java, Scala, resource root folders for given compilation scope.
 */
trait RegisterAnyMojo extends AbstractMojo
  with base.Dir
  with base.Mojo
  with base.Params
  with base.Logging
  with base.SkipMojo
  with base.BuildEnsure
  with base.BuildAnySources
  with base.BuildAnyTarget
  with com.carrotgarden.maven.scalor.base.Context {

  type RegisterFun[ T ] = ( T => Unit )

  @Description( """
  Flag to skip goal execution: <code>register-*</code>.
  """ )
  @Parameter(
    property     = "scalor.skipRegister",
    defaultValue = "false"
  )
  var skipRegister : Boolean = _

  /**
   * Detect if given resource root is already registered.
   */
  def hasResourceRoot( root : Resource ) = {
    val rootPath = Paths.get( root.getDirectory )
    resourceRootList.asScala
      .map( resource => Paths.get( resource.getDirectory ) )
      .find( testPath => basedir.isSamePath( testPath, rootPath ) )
      .isDefined
  }

  /**
   * Provide currently registered project resource roots.
   */
  def resourceRootList : java.util.List[ Resource ]

  /**
   * Register resource root for given compilation scope with the project.
   */
  def registerResource : RegisterFun[ Resource ]

  /**
   * Detect if given source root is already registered.
   */
  def hasSourceRoot( rootPath : Path ) = {
    sourceRootList.asScala
      .map( entry => Paths.get( entry ) )
      .find( testPath => basedir.isSamePath( testPath, rootPath ) )
      .isDefined
  }

  /**
   * Report currently registered project source roots.
   */
  def sourceRootList : java.util.List[ String ]

  /**
   * Register source root for given compilation scope with the project.
   */
  def registerSource : RegisterFun[ String ]

  /**
   * Detect if given target root is already registered.
   */
  def hasTargetRoot( rootPath : Path ) : Boolean = {
    val testPath = Paths.get( targetRoot )
    basedir.isSamePath( rootPath, testPath )
  }

  /**
   * Report currently registered project target root.
   */
  def targetRoot : String

  /**
   * Register target root for given compilation scope with the project.
   */
  def registerTarget : RegisterFun[ String ]

  def reportPathOld( path : Path ) = s"Already registered:   ${path}"
  
  def reportPathNew( path : Path ) = s"Registering new root: ${path}"

  /**
   * Business logic of adding root folders to the project model.
   */
  @Description( """
  Path convention: resolve as absolute.
  """ )
  def perfromRegister() : Unit = {

    buildResourceFolders
      .foreach { resource : Resource =>
        val path = basedir.absolute( Paths.get( resource.getDirectory ) )
        if ( hasResourceRoot( resource ) ) {
          logger.info( reportPathOld( path ) )
        } else {
          logger.info( reportPathNew( path ) )
          registerResource( resource )
        }
      }

    buildSourceFolders
      .map( file => basedir.absolute( file.toPath ) )
      .foreach { path : Path =>
        if ( hasSourceRoot( path ) ) {
          logger.info( reportPathOld( path ) )
        } else {
          logger.info( reportPathNew( path ) )
          registerSource( path.toString )
        }
      }

    List( buildTargetFolder )
      .map( file => basedir.absolute( file.toPath ) )
      .foreach { path : Path =>
        if ( hasTargetRoot( path ) ) {
          logger.info( reportPathOld( path ) )
        } else {
          logger.info( reportPathNew( path ) )
          registerTarget( path.toString )
        }
      }

    if ( buildEnsureFolders ) {
      logger.info( "Ensuring build folders." )
      buildResourceFolders
        .foreach { resource : Resource =>
          val path = basedir.absolute( Paths.get( resource.getDirectory ) )
          ensureFolder( path )
        };
      ( buildSourceFolders ++ List( buildTargetFolder ) )
        .foreach { file : File =>
          val path = basedir.absolute( file.toPath )
          ensureFolder( path )
        }
    }

  }

  override def perform() : Unit = {
    if ( skipRegister || hasSkipMojo ) {
      reportSkipReason( "Skipping disabled goal execution." )
      return
    }
    perfromRegister()
  }

}

@Description( """
Register Java, Scala, resource root folders for compilation scope=macro.
""" )
@Mojo(
  name                         = `register-macro`,
  defaultPhase                 = LifecyclePhase.INITIALIZE,
  requiresDependencyResolution = ResolutionScope.NONE
)
class RegisterMacroMojo extends RegisterAnyMojo
  with base.BuildMacroSources
  with base.BuildMainTarget {

  import base.Build._

  override def mojoName = `register-macro`

  @Description( """
  Flag to skip goal execution: <code>register-macro</code>.
  """ )
  @Parameter(
    property     = "scalor.skipRegisterMacro",
    defaultValue = "false"
  )
  var skipRegisterMacro : Boolean = _

  override def hasSkipMojo = skipRegisterMacro

  override def resourceRootList = project.getBuild.getResources
  override def registerResource = project.getBuild.addResource _
  override def sourceRootList = project.getCompileSourceRoots
  override def registerSource = project.addCompileSourceRoot _
  override def targetRoot : String = project.getBuild.getOutputDirectory
  override def registerTarget = project.getBuild.setOutputDirectory _

}

@Description( """
Register Java, Scala, resource root folders for compilation scope=main.
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
  Flag to skip goal execution: <code>register-main</code>.
  """ )
  @Parameter(
    property     = "scalor.skipRegisterMain",
    defaultValue = "false"
  )
  var skipRegisterMain : Boolean = _

  override def hasSkipMojo = skipRegisterMain
  override def resourceRootList = project.getBuild.getResources
  override def registerResource = project.getBuild.addResource _
  override def sourceRootList = project.getCompileSourceRoots
  override def registerSource = project.addCompileSourceRoot _
  override def targetRoot : String = project.getBuild.getOutputDirectory
  override def registerTarget = project.getBuild.setOutputDirectory _

}

@Description( """
Register Java, Scala, resource root folders for compilation scope=test.
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
  Flag to skip goal execution: <code>register-test</code>.
  """ )
  @Parameter(
    property     = "scalor.skipRegisterTest",
    defaultValue = "false"
  )
  var skipRegisterTest : Boolean = _

  override def hasSkipMojo = skipRegisterTest
  override def resourceRootList = project.getBuild.getTestResources
  override def registerResource = project.getBuild.addTestResource _
  override def sourceRootList = project.getTestCompileSourceRoots
  override def registerSource = project.addTestCompileSourceRoot _
  override def targetRoot : String = project.getBuild.getTestOutputDirectory
  override def registerTarget = project.getBuild.setTestOutputDirectory _

}
