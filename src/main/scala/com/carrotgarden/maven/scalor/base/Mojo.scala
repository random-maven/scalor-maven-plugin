package com.carrotgarden.maven.scalor.base

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoFailureException

import com.carrotgarden.maven.scalor.eclipse

/**
 * Shared mojo execution process steps.
 */
trait Mojo extends AbstractMojo
  with Params
  with Logging
  with Skip
  with SkipMojo
  with eclipse.Context {

  /**
   * Mojo execution name.
   */
  def mojoName : String

  def hasSkip : Boolean = {
    skip
  }

  def hasSkipPackaging : Boolean = {
    skipPackagingList.contains( project.getPackaging )
  }

  def reportSkipReason( line : String ) : Unit = {
    if ( skipLogReason ) { log.info( line ) }
  }

  /**
   * Actually perform goal execution.
   */
  def perform() : Unit

  override def execute() : Unit = {
    try {
      contextReset()
      if ( hasSkip ) {
        reportSkipReason( "Skipping plugin execution." )
        return
      }
      if ( hasSkipPackaging ) {
        reportSkipReason( s"Skipping plugin execution for packaging '${project.getPackaging}'." )
        return
      }
      perform()
    } catch {
      case error : Throwable =>
        val message = "Execution failure:"
        contextError( message, error );
        log.fail( message + " " + error.getMessage )
        throw new MojoFailureException( message, error )
    }
  }

}
