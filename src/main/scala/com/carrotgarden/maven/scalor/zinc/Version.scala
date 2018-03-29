package com.carrotgarden.maven.scalor.zinc

import scala.tools.nsc.settings.ScalaVersion
import scala.tools.nsc.settings.SpecificScalaVersion

/**
 * Version verification support.
 */
object Version {

  import Module._

  /**
   * Scala artifact convention:
   * compiler-bridge_2.12
   * paradise_2.12.3
   */
  case class ArtifactVersion( artifacHead : String, versionTail : String ) {
    def unparse : String = s"${artifacHead}_${versionTail}"
  }

  /**
   * Scala convention: compiler-bridge_2.12, paradise_2.12.3, etc.
   */
  val atifactVersionRegex = """([^_]+)_([^_]+)""".r

  /**
   * Match epoch, i.e.: compiler-bridge_2.12
   */
  val scalaEpochRegex = s"""([^.]+)[.]([^.]+)""".r

  /**
   * Match release, i.e.: paradise_2.13.0-M2
   */
  val scalaReleaseRegex = """([^.]+)[.]([^.]+)[.]([^.]+)""".r

  def hasScalaEpoch( version : String ) = scalaEpochRegex.pattern.matcher( version ).matches

  def hasScalaRelease( version : String ) = scalaReleaseRegex.pattern.matcher( version ).matches

  /**
   * Extract version embedded in Maven artifactId.
   */
  def artifactVersion( artifactId : String ) : Option[ ArtifactVersion ] = {
    artifactId match {
      case atifactVersionRegex( artifactId, version ) =>
        Some( ArtifactVersion( artifactId, version ) )
      case _ => None
    }
  }

  /**
   * Extract Scala version embedded in Maven artifactId.
   */
  def artifactVersion( module : Module ) : Option[ ArtifactVersion ] = {
    artifactVersion( module.binaryArtifact.getArtifactId )
  }

  /**
   * Match version between epoch or release.
   */
  def hasScalaMatch(
    module : SpecificScalaVersion, library : SpecificScalaVersion,
    hasRelease : Boolean
  ) = {
    val hasMajor = module.major == library.major
    val hasMinor = module.minor == library.minor
    val hasMicro = module.rev == library.rev
    if ( hasRelease ) {
      hasMajor && hasMinor && hasMicro
    } else {
      hasMajor && hasMinor
    }
  }

  /**
   * Verify module version against the library.
   *
   * Assumptions:
   * - if version is present in artifactId use that
   * - if not, use module Maven artifact version
   */
  def assertModule( module : Module, library : Module ) = {
    val libraryVersion = scalaVersion( library )
    val moduleOption = artifactVersion( module )
    val moduleVersion = moduleOption
      .map( meta => scalaVersion( meta ) )
      .getOrElse( scalaVersion( module ) )
    val hasModuleRelease = moduleOption
      .map( meta => hasScalaRelease( meta.versionTail ) )
      .getOrElse( hasScalaRelease( module.binaryArtifact.getVersion ) )
    require(
      hasScalaMatch( moduleVersion, libraryVersion, hasModuleRelease ),
      s"Version mismatch: ${fileFrom( module )} vs ${fileFrom( library )}"
    )
  }

  /**
   * Verify version consistency between modules in Scala installation.
   */
  def assertVersion( install : ScalaInstall ) = {
    import install._
    assertModule( bridge, library )
    assertModule( compiler, library )
    pluginDefineList.foreach { plugin =>
      assertModule( plugin, library )
    }
  }

  def scalaVersion( artifactMeta : ArtifactVersion ) : SpecificScalaVersion = {
    ScalaVersion( artifactMeta.versionTail ).asInstanceOf[ SpecificScalaVersion ]
  }

  def scalaVersion( module : Module ) : SpecificScalaVersion = {
    ScalaVersion( module.binaryArtifact.getVersion ).asInstanceOf[ SpecificScalaVersion ]
  }

  def scalaVersionEpoch( scalaVersion : SpecificScalaVersion ) : String = {
    s"${scalaVersion.major}.${scalaVersion.minor}"
  }

  def scalaVersionRelease( scalaVersion : SpecificScalaVersion ) : String = {
    scalaVersion.unparse
  }

}
