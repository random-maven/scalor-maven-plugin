package com.carrotgarden.maven.scalor.base

/**
 * Shared mojo behaviour.
 */
trait Skip {

  /**
   * Skip specific execution only.
   */
  def hasSkipMojo : Boolean

}
