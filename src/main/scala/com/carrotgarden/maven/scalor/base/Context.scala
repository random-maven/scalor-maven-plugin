package com.carrotgarden.maven.scalor.base

import org.apache.maven.plugin.AbstractMojo

/**
 * Plugin state storage during Maven session.
 *
 * Available in Maven and Eclispe.
 *
 * Maven session duration:
 * - Maven: single build invocation
 * - Eclipse: between project configuration updates
 */
trait Context {

  self : AbstractMojo =>

  /**
   *  Plugin container context.
   */
  def pluginContext = getPluginContext.asInstanceOf[ java.util.Map[ String, Object ] ]

  /**
   * Extract plugin build state.
   */
  def pluginExtract[ T <: Object ]( key : String ) : Option[ T ] = {
    Option( pluginContext.get( key ).asInstanceOf[ T ] )
  }

  /**
   * Persist plugin build state.
   */
  def pluginPersist[ T <: Object ]( key : String, option : Option[ T ] = None ) : Unit = {
    option match {
      case Some( value ) => pluginContext.put( key, value )
      case None          => pluginContext.remove( key )
    }
  }

}
