package com.carrotgarden.maven.scalor.eclipse

import com.carrotgarden.maven.scalor.base
import com.carrotgarden.maven.scalor.util.Error.TryHard
import com.carrotgarden.maven.tools.Description

/**
 * Eclipse wiring context.
 */
@Description( """
Note: controlled OSGI environment.
""" )
trait Context {

  self : base.Context =>

  /**
   * Connect this Maven plugin with host Eclipse plugins.
   * Only used when running inside Eclipse platform with M2E.
   */
  // Lazy, for plexus injector.
  lazy val wiringHandle = TryHard {
    Wiring( buildContext ).setup
  }

}
