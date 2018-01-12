package com.carrotgarden.maven.scalor.base

import org.apache.maven.plugin.AbstractMojo

import com.carrotgarden.maven.scalor.A
import com.carrotgarden.maven.scalor.eclipse
import com.carrotgarden.maven.scalor.util

import java.io.File

/**
 * Shared logging features.
 */
trait Logging {

  self : eclipse.Context with AbstractMojo =>

  def mojoName : String

  object say {
    /**  Work around lack of logging source in M2E "Maven Console". */
    private lazy val prefix =
      if ( m2e.isPresent ) "[" + A.maven.name + ":" + mojoName + "] " else ""
    //
    def debug( line : String ) = getLog().debug( prefix + line );
    def info( line : String ) = getLog().info( prefix + line );
    def warn( line : String ) = getLog().warn( prefix + line );
    def error( line : String ) = getLog().error( prefix + line );
  }

  /**
   * Log file list.
   */
  def reportFileList( fileList : Array[ File ] ) = {
    import util.Folder._
    fileList.sorted.foreach {
      file => say.info( "   " + ensureCanonicalFile( file ) )
    }
  }

}
