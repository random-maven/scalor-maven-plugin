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
 * Register Java, Scala, Resource folders for given compilation scope.
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
  with eclipse.Context {

  type RegistrationFunction[ T ] = ( T => Unit )

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
  def registerResourceRoot : RegistrationFunction[ Resource ]

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
  def registerSourceRoot : RegistrationFunction[ String ]

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
  def registerTargetRoot : RegistrationFunction[ String ]

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
          say.info( "Already registered: " + path )
        } else {
          say.info( "Registering root:   " + path )
          registerResourceRoot( resource )
        }
      }

    buildSourceFolders
      .map( file => basedir.absolute( file.toPath ) )
      .foreach { path : Path =>
        if ( hasSourceRoot( path ) ) {
          say.info( "Already registered: " + path )
        } else {
          say.info( "Registering root:   " + path )
          registerSourceRoot( path.toString )
        }
      }

    List( buildTargetFolder )
      .map( file => basedir.absolute( file.toPath ) )
      .foreach { path : Path =>
        if ( hasTargetRoot( path ) ) {
          say.info( "Already registered: " + path )
        } else {
          say.info( "Registering root:   " + path )
          // registerTargetRoot( path.toString )
        }
      }

    if ( buildEnsureFolders ) {
      say.info( "Ensuring build folders." )
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
Register Java, Scala, Resource folders for compilation scope=macro.
""" )
@Mojo(
  name                         = `register-macro`,
  defaultPhase                 = LifecyclePhase.INITIALIZE,
  requiresDependencyResolution = ResolutionScope.NONE
)
class RegisterMacroMojo extends RegisterAnyMojo
  with base.BuildMacroSources
  with base.BuildMacroTarget {

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

  // TODO support macro resources

  //  override def resourceRootList =     Collections.emptyList()
  //  override def registerResourceRoot =     ( Resource => () )
  //  override def sourceRootList =
  //    extractPropertyList( buildMacroSourceFoldersParam ).getOrElse( Collections.emptyList() )
  //
  //  override def registerSourceRoot =
  //    persistPropertyList( buildMacroSourceFoldersParam, _ )
  //
  //  override def targetRoot : String =
  //    extractProperty( buildMacroTargetParam ).getOrElse( "." )
  //
  //  override def registerTargetRoot =
  //    persistProperty( buildMacroTargetParam, _ )

  override def resourceRootList = project.getBuild.getResources
  override def registerResourceRoot = project.getBuild.addResource _
  override def sourceRootList = project.getCompileSourceRoots
  override def registerSourceRoot = project.addCompileSourceRoot _
  override def targetRoot : String = project.getBuild.getOutputDirectory
  override def registerTargetRoot = project.getBuild.setOutputDirectory _

}

@Description( """
Register Java, Scala, Resource folders for compilation scope=main.
""" )
@Mojo(
  name                         = `register-main`,
  defaultPhase                 = LifecyclePhase.INITIALIZE,
  requiresDependencyResolution = ResolutionScope.NONE
)
class RegisterMainMojo extends RegisterAnyMojo
  with base.BuildMain
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
  override def registerResourceRoot = project.getBuild.addResource _
  override def sourceRootList = project.getCompileSourceRoots
  override def registerSourceRoot = project.addCompileSourceRoot _
  override def targetRoot : String = project.getBuild.getOutputDirectory
  override def registerTargetRoot = project.getBuild.setOutputDirectory _

}

@Description( """
Register Java, Scala, Resource folders for compilation scope=test.
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
  override def registerResourceRoot = project.getBuild.addTestResource _
  override def sourceRootList = project.getTestCompileSourceRoots
  override def registerSourceRoot = project.addTestCompileSourceRoot _
  override def targetRoot : String = project.getBuild.getTestOutputDirectory
  override def registerTargetRoot = project.getBuild.setTestOutputDirectory _

}
