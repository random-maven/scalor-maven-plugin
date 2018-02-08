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
  override lazy val log = Logging.Log( getLog, hasEclipseContext )

  lazy val logger = log.branch( mojoName )

  /**
   * Log file list.
   */
  def loggerReportFileList( title : String, fileList : Array[ File ] ) = {
    val array = fileList.map( _.getCanonicalPath ).sorted
    val report = util.Text.reportArray( array )
    logger.info( s"${title}\n${report}" )
  }

}

object Logging {

  case class Log(
    logger : org.apache.maven.plugin.logging.Log,
    hasM2E : Boolean
  ) extends util.Logging.AnyLog {
    override val founder = this
    override val context = A.maven.name
    override def text( line : String ) = if ( hasM2E ) {
      s"[${context}] ${line}"
    } else {
      line
    }
    override def dbug( line : String ) = logger.debug( text( line ) )
    override def info( line : String ) = logger.info( text( line ) )
    override def warn( line : String ) = logger.warn( text( line ) )
    override def fail( line : String ) = logger.error( text( line ) )
    override def fail( line : String, error : Throwable ) = logger.error( text( line ), error )
  }

}
