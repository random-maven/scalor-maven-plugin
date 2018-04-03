package com.carrotgarden.maven.scalor

import scala.collection.JavaConverters._

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.ResolutionScope

import com.carrotgarden.maven.tools.Description

import java.io.File

import com.carrotgarden.maven.scalor.util.Folder
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.LinkOption
import java.util.Comparator

trait SetupCrossAnyMojo extends AbstractMojo
  with base.Mojo
  with base.Params
  with base.Logging
  with base.SkipMojo
  with base.Context
  with setup.ParamsCross {

  @Description( """
  Flag to skip this execution: <code>setup-cross</code>.
  """ )
  @Parameter(
    property     = "scalor.skipSetupCross",
    defaultValue = "false"
  )
  var skipSetupCross : Boolean = _

  @Description( """
  List of packaging types, which activate this goal: <code>setup-cross</code>.
  Overrides effects of <a href="#skipPackagingList"><b>skipPackagingList</b></a>.
  """ )
  @Parameter(
    property     = "scalor.setupCrossPackagingList",
    defaultValue = "pom"
  )
  var setupCrossPackagingList : Array[ String ] = Array.empty

  override def hasSkipPackaging : Boolean = {
    !setupCrossPackagingList.contains( project.getPackaging )
  }

  def performInvocation() : Unit = {
    val root = project.getBasedir
    //
    logger.info( "Ensuring module folders." )
    val sourceSet = project.getModules.asScala.toSet
    val targetSet = crossModuleList.toSet
    val moduleList = sourceSet.intersect( targetSet ).toList.sorted
    moduleList.foreach { path =>
      val folder = new File( root, path )
      logger.info( s"   folder: ${folder}" )
      Folder.ensureFolder( folder )
    }
    //
    logger.info( "Ensuring resource symlinks." )
    val resourceList = crossResourceList.toList.sorted
    for {
      module <- moduleList
      resource <- resourceList
    } yield {
      val source = new File( root, s"${module}/${resource}" ).toPath.normalize
      val target = new File( root, resource ).toPath.normalize
      logger.info( s"   symlink: ${source} -> ${target}" )
      Folder.ensureSymlink( source, target )
    }
  }

  override def perform() : Unit = {
    if ( skipSetupCross || hasSkipMojo ) {
      reportSkipReason( "Skipping disabled goal execution." )
      return
    }
    performInvocation()
  }

}

@Description( """
Setup module structure for Scala cross version build.
Produce folders and symbolic links.
""" )
@Mojo(
  name                         = A.mojo.`setup-cross`,
  defaultPhase                 = LifecyclePhase.INITIALIZE,
  requiresDependencyResolution = ResolutionScope.NONE
)
class SetupCrossArkonMojo extends SetupCrossAnyMojo {

  override def mojoName = A.mojo.`setup-cross`

}
