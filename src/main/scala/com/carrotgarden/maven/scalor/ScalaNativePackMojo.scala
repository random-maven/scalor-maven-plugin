package com.carrotgarden.maven.scalor

import scala.collection.JavaConverters._

import org.apache.commons.lang3.SystemUtils

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.ResolutionScope

import com.carrotgarden.maven.scalor.base.Context.UpdateResult
import com.carrotgarden.maven.scalor.scalanative.Linker
import com.carrotgarden.maven.tools.Description

import java.io.File
import com.carrotgarden.maven.scalor.util.Folder
import org.apache.maven.archiver.MavenArchiver
import org.codehaus.plexus.archiver.jar.JarArchiver
import org.apache.maven.artifact.Artifact

trait ScalaNativePackAnyMojo extends AbstractMojo
  with base.Dir
  with base.Mojo
  with base.Params
  with base.Logging
  with base.SkipMojo
  with base.Context
  with scalanative.ParamsPackAny
  with scalanative.ParamsOperatingSystem {

  @Description( """
  Flag to skip this execution: <code>scala-native-pack-*</code>.
  """ )
  @Parameter(
    property     = "scalor.skipNativePack",
    defaultValue = "false"
  )
  var skipNativePack : Boolean = _

  /**
   * Discover Scala.native library on project class path.
   */
  lazy val libraryArtifactOption : Option[ Artifact ] = {
    util.Maven.locateArtifact( project, nativeScalaLibRegex )
  }

  lazy val nativeArchive : File = {
    val nativeTarget = basedirOutput.toFile
    Folder.ensureFolder( nativeTarget )
    new File( nativeTarget, nativeArchiveName )
  }

  /**
   * Package Scala.native content into an archive jar.
   */
  def performPackage() : Unit = {
    logger.info( s"Packaging Scala.native: ${nativeArchive}" )
    if ( nativeArchive.exists ) { nativeArchive.delete }
    val packager = new MavenArchiver()
    val archiveBuilder = new JarArchiver()
    packager.setArchiver( archiveBuilder )
    packager.setOutputFile( nativeArchive )
    val includes = Array[ String ]( "**/**" ) // FIXME to config
    val excludes = Array[ String ]()
    packager.getArchiver.addDirectory( nativeOutputFolder, includes, excludes )
    packager.createArchive( session, project, nativeArchiveConfig )
  }

  /**
   * Attach Scala.native artifact to the project as deployment artifact.
   */
  def performAttach() : Unit = {
    if ( nativeHasAttach ) {
      logger.info( s"Attaching Scala.native: ${nativeArchive}" )
      projectHelper.attachArtifact( project, nativeArchive, nativeClassifier )
    }
  }

  def performInvocation() : Unit = {
    if ( nativeLibraryDetect && libraryArtifactOption.isEmpty ) {
      reportSkipReason( s"Skipping execution: Scala.native library missing: ${nativeScalaLibRegex}." )
      return
    }
    performPackage()
    performAttach()
  }

  override def perform() : Unit = {
    if ( skipNativePack || hasSkipMojo ) {
      reportSkipReason( "Skipping disabled goal execution." )
      return
    }
    if ( nativeSystemDetect && !nativeHasOperatingSystem ) {
      reportSkipReason( "Skipping unsupported operating system." )
      return
    }
    performInvocation()
  }

}

@Description( """
Package Scala.native runtime artifact for all scopes.
Invokes goals: scala-native-pack-*.
""" )
@Mojo(
  name                         = A.mojo.`scala-native-pack`,
  defaultPhase                 = LifecyclePhase.PACKAGE,
  requiresDependencyResolution = ResolutionScope.NONE
)
class ScalaNativePackArkonMojo extends ScalaNativePackAnyMojo
  with scalanative.ParamsPackMain
  with scalanative.ParamsPackTest {

  override def mojoName = A.mojo.`scala-native-pack`

  override def performInvocation() : Unit = {
    executeSelfMojo( A.mojo.`scala-native-pack-main` )
    executeSelfMojo( A.mojo.`scala-native-pack-test` )
  }

}

@Description( """
Package Scala.native runtime artifact for scope=main.
A member of goal=scala-native-pack.
""" )
@Mojo(
  name                         = A.mojo.`scala-native-pack-main`,
  defaultPhase                 = LifecyclePhase.PACKAGE,
  requiresDependencyResolution = ResolutionScope.COMPILE
)
class ScalaNativePackMainMojo extends ScalaNativePackAnyMojo
  with scalanative.ParamsPackMain {

  override def mojoName = A.mojo.`scala-native-pack-main`

  @Description( """
  Flag to skip this goal execution: <code>scala-native-pack-main</code>.
  """ )
  @Parameter(
    property     = "scalor.skipNativePackMain",
    defaultValue = "false"
  )
  var skipNativePackMain : Boolean = _

  override def hasSkipMojo = skipNativePackMain

}

@Description( """
Package Scala.native runtime artifact for scope=test.
A member of goal=scala-native-pack.
""" )
@Mojo(
  name                         = A.mojo.`scala-native-pack-test`,
  defaultPhase                 = LifecyclePhase.PACKAGE,
  requiresDependencyResolution = ResolutionScope.TEST
)
class ScalaNativePackTestMojo extends ScalaNativePackAnyMojo
  with scalanative.ParamsPackTest {

  override def mojoName = A.mojo.`scala-native-pack-test`

  @Description( """
  Flag to skip this goal execution: <code>scala-native-pack-test</code>.
  """ )
  @Parameter(
    property     = "scalor.skipNativePackTest",
    defaultValue = "false"
  )
  var skipNativePackTest : Boolean = _

  override def hasSkipMojo = skipNativePackTest

}
