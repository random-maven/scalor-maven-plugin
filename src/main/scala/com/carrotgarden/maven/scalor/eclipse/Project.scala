package com.carrotgarden.maven.scalor.eclipse

import com.carrotgarden.maven.scalor.util.Logging

/**
 * Assemble M2E project configurator components.
 */
object Project {

  /**
   * Project configurator.
   */
  trait Configurator extends Base.Conf
    with Comment
    with Config
    with Entry
    with Hack
    with Logging
    with Maven
    with Monitor
    with Nature
    with Order
    with Props
    with Version
    with MavenM2E
    with ScalaIDE {

  }

}
