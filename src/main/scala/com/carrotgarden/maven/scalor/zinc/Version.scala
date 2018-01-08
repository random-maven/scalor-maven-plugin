package com.carrotgarden.maven.scalor.zinc

import com.carrotgarden.maven.scalor.util.Error._
import com.carrotgarden.maven.scalor.util.Folder._

/**
 * Version verification support.
 */
object Version {

  import Module._

  /**
   * Fail on version mismatch error.
   */
  def assertVersionTree( item1 : FileItem, item2 : FileItem ) = {
    if ( !versionTree( item1, item2 ) ) versionFailure( item1, item2 )
  }

  /**
   * Fail on version mismatch error.
   */
  def assertVersionExact( item1 : FileItem, item2 : FileItem ) = {
    if ( !versionExact( item1, item2 ) ) versionFailure( item1, item2 )
  }

  /**
   * Report version mismatch error.
   */
  def versionFailure( file1 : FileItem, file2 : FileItem ) {
    throw new RuntimeException( s"Version mismatch: ${file1} vs ${file2}" );
  }

  /**
   * Verify version tree/branch match, i.e. 2.12 is-same-as 2.12.4
   */
  def versionTree( item_short : FileItem, item_long : FileItem ) = {
    item_long.version.startsWith( item_short.version )
  }

  /**
   * Verify exact version match.
   */
  def versionExact( item1 : FileItem, item2 : FileItem ) = {
    item2.version.equals( item1.version )
  }

  /**
   * Convert module to item.
   */
  def itemFrom( module : Module, useArtifactVersion : Boolean = false ) : FileItem = {
    if ( useArtifactVersion ) {
      val entry = artifactVersion( module ).getOrElse(
        Throw( "Can not extract verson from artifactId: " + module )
      )
      FileItem( fileFrom( module ), entry.version )
    } else {
      FileItem( fileFrom( module ), versionFrom( module ) )
    }
  }

  case class ArtifactVersion( artifacId : String, version : String )

  /**
   * Scala convention:
   * compiler-bridge_2.12
   * paradise_2.12.3
   */
  val atifactVersionRegex = """([^_]+)_([^_]+)""".r

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
   * Extract version embedded in Maven artifactId.
   */
  def artifactVersion( module : Module ) : Option[ ArtifactVersion ] = {
    artifactVersion( module.binaryArtifact.getArtifactId )
  }

  /**
   * Verify version consistency between modules in Scala installation.
   */
  def assertVersion( install : ScalaInstall ) = {

    val bridge = itemFrom( install.bridge, true )

    val compiler = itemFrom( install.compiler )
    val library = itemFrom( install.library )
    val reflect = itemFrom( install.reflect )

    assertVersionTree( bridge, compiler )
    assertVersionTree( bridge, library )
    assertVersionTree( bridge, reflect )

    assertVersionExact( compiler, library )
    assertVersionExact( compiler, reflect )

    install.pluginDefineList.foreach { module =>
      val plugin = itemFrom( module, true )
      assertVersionExact( compiler, plugin )
    }

  }

}
