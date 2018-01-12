package com.carrotgarden.maven.scalor.base

import Context._

/**
 * Plugins context for (plugin, project) as configuration store.
 *
 * Persisted for the duration of Maven session.
 *
 * Available for Maven and Eclipse.
 */
case class Context( context : PluginContext ) {

}

object Context {

  type PluginContext = java.util.Map[ String, Object ]
  
  object key {
    
  }

}
