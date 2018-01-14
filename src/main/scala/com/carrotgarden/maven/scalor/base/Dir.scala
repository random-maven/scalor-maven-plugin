package com.carrotgarden.maven.scalor.base

import com.carrotgarden.maven.scalor.util
import com.carrotgarden.maven.tools.Description
import java.nio.file.Paths

/**
 * Expose project base directory.
 */
trait Dir {

  self : Params =>

  @Description( """
  Project source/target folders are project-contained.
  """ )
  lazy val basedir = util.Folder( project.getBasedir.toPath )

  def basedirOutput = basedir.absolute( Paths.get( project.getBuild.getDirectory ) )

  def basedirOutputMain = basedir.absolute( Paths.get( project.getBuild.getOutputDirectory ) )

  def basedirOutputTest = basedir.absolute( Paths.get( project.getBuild.getTestOutputDirectory ) )

}
