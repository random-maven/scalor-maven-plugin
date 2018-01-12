package com.carrotgarden.maven.scalor.base

import com.carrotgarden.maven.scalor.util
import com.carrotgarden.maven.tools.Description

/**
 * Expose project base directory.
 */
trait Dir {

  self : Params =>

  @Description( """
  Project source/target folders are project-contained.
  """ )
  lazy val basedir = util.Folder( project.getBasedir.toPath )

}
