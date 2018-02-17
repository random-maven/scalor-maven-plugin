package com.carrotgarden.maven.scalor.base

import org.apache.maven.plugins.annotations.Parameter
import com.carrotgarden.maven.tools.Description

trait Skip {

  @Description( """
  Force to skip all executions of this plugin.
  """ )
  @Parameter(
    property     = "scalor.skip",
    defaultValue = "false"
  )
  var skip : Boolean = _

  @Description( """
  Enable logging of reason for skipping an execution.
  """ )
  @Parameter(
    property     = "scalor.skipLogReason",
    defaultValue = "false"
  )
  var skipLogReason : Boolean = _

  @Description( """
  List of packaging types, which are skipped by this plugin.
  """ )
  @Parameter(
    property     = "scalor.skipPackagingList",
    defaultValue = "pom"
  )
  var skipPackagingList : Array[ String ] = Array.empty
  
}

/**
 * Skip specific execution only.
 */
trait SkipMojo {

  /**
   * Skip specific execution only.
   */
  def hasSkipMojo : Boolean = false

}
