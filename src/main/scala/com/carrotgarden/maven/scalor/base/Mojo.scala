package com.carrotgarden.maven.scalor.base

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoFailureException

import com.carrotgarden.maven.scalor.eclipse

/**
 * Shared mojo execution process steps.
 */
trait Mojo {
  self : eclipse.Build with Params with Logging with AbstractMojo =>

  /**
   * Actually perform goal execution.
   */
  def perform() : Unit

  override def execute() : Unit = {
    try {
      contextReset()
      if ( skip ) {
        say.info( "Skipping all plugin executions." )
        return
      }
      perform()
    } catch {
      case error : Throwable =>
        val message = "Execution failure:"
        contextError( message, error );
        say.error( message + " " + error.getMessage )
        throw new MojoFailureException( message, error )
    }
  }

}
