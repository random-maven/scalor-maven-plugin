package com.carrotgarden.maven.scalor.base

import org.apache.maven.plugin.AbstractMojo

import com.carrotgarden.maven.scalor.A
import com.carrotgarden.maven.scalor.eclipse
import com.carrotgarden.maven.scalor.util

import java.io.File

/**
 * Shared logging features.
 */
trait Logging extends util.Logging {

  self : Mojo with eclipse.Context =>

  // Lazy, for plexus injector.
  override lazy val log = Logging.Log( getLog, mojoName, m2e.isPresent )

  /**
   * Log file list.
   */
  def reportFileList( fileList : Array[ File ] ) = {
    import util.Folder._
    fileList.sorted.foreach {
      file => log.info( "   " + ensureCanonicalFile( file ) )
    }
  }

}

object Logging {

  case class Log(
    logger :   org.apache.maven.plugin.logging.Log,
    mojoName : String,
    hasM2E :   Boolean
  ) extends util.Logging.AnyLog {
    /**  Work around lack of logging source in M2E "Maven Console". */
    override val context = if ( hasM2E ) "[" + A.maven.name + ":" + mojoName + "] " else ""
    override def info( line : String ) = logger.info( context + line )
    override def warn( line : String ) = logger.warn( context + line )
    override def fail( line : String ) = logger.error( context + line )
    override def fail( line : String, error : Throwable ) = logger.error( context + line, error )
  }

}
