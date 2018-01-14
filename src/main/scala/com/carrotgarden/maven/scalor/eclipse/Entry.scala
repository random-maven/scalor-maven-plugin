package com.carrotgarden.maven.scalor.eclipse

import scala.collection.JavaConverters._

import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest
import org.eclipse.m2e.jdt.IClasspathDescriptor
import java.io.File

import org.eclipse.core.runtime.IProgressMonitor

import Params._
import Maven._

import org.apache.maven.plugin.MojoExecution

import com.carrotgarden.maven.scalor.base
import com.carrotgarden.maven.scalor.util
import com.carrotgarden.maven.tools.Description

import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.JavaCore
import org.eclipse.core.runtime.Path
import org.eclipse.m2e.core.project.configurator.AbstractProjectConfigurator
import org.eclipse.jdt.core.IClasspathAttribute
import com.carrotgarden.maven.scalor.base.Build.Param
import org.scalaide.core.SdtConstants
import com.carrotgarden.maven.scalor.base.ParamsProjectUnit
import java.nio.file.Files
import java.nio.file.Paths
import org.eclipse.m2e.core.project.IMavenProjectFacade
import com.carrotgarden.maven.scalor.util.Logging

/**
 * Manage eclipse .classpath file class path entries.
 */
trait Entry {

  self : Logging with Monitor with Maven with Base.Conf =>

  /**
   * Provide container class path entry instance.
   */
  def containerEntry( path : String ) : IClasspathEntry = {
    JavaCore.newContainerEntry( Path.fromPortableString( path ) )
  }

  /**
   * Generate .classpath source[path] -> target[output] entry.
   */
  def configurePathEntry(
    request :      ProjectConfigurationRequest,
    classpath :    IClasspathDescriptor,
    sourceFolder : String, // absolute
    targetFolder : String, // absolute
    monitor :      IProgressMonitor,
    attribMap :    Map[ String, String ]       = Map(),
    generated :    Boolean                     = false
  ) = {
    import util.Folder._
    val project = request.getProject
    val sourcePath = projectFolder( project, sourceFolder ).getFullPath
    val targetPath = projectFolder( project, targetFolder ).getFullPath
    log.info( s"   ${sourcePath} -> ${targetPath}" )
    val entry = classpath.addSourceEntry( sourcePath, targetPath, generated )
    attribMap.foreach {
      case ( key, value ) => entry.setClasspathAttribute( key, value )
    }
  }

  @Description( """
  Path convention: resolve as absolute.
  """ )
  def ensureSourceRoots(
    request :    ProjectConfigurationRequest,
    classpath :  IClasspathDescriptor,
    sourceList : Seq[ String ],
    target :     String,
    attribMap :  Map[ String, String ],
    monitor :    IProgressMonitor
  ) : Unit = {
    import util.Folder
    @Description( """
    Project source/target folders are project-contained.
    """ )
    val basedir = Folder( request.getProject.getLocation.toFile.toPath )
    val targetFolder = basedir.absolute( Paths.get( target ) ).toString
    sourceList.foreach { source =>
      val sourceFolder = basedir.absolute( Paths.get( source ) ).toString
      configurePathEntry( request, classpath, sourceFolder, targetFolder, monitor, attribMap )
    }
  }

  def ensureSourceRoots(
    request :   ProjectConfigurationRequest,
    classpath : IClasspathDescriptor,
    build :     base.Build,
    attribMap : Map[ String, String ],
    monitor :   IProgressMonitor
  ) : Unit = {
    import build._
    val resourceList = buildResourceFolders.map( _.getDirectory )
    val sourceList = buildSourceFolders.map( _.getAbsolutePath )
    val target = buildTargetFolder.getAbsolutePath
    ensureSourceRoots( request, classpath, resourceList, target, attribMap, monitor )
    ensureSourceRoots( request, classpath, sourceList, target, attribMap, monitor )
  }

  /**
   * Configure source/target folder class path entries.
   *
   * Note: register-* goals must be executed before this.
   */
  def ensureSourceRoots(
    request :   ProjectConfigurationRequest,
    config :    ParamsConfig,
    classpath : IClasspathDescriptor,
    monitor :   IProgressMonitor
  ) : Unit = {
    import Param._
    val attribAny = Map(
      attrib.optional -> "true"
    )
    import base.Build._
    val attribMacro = attribAny + ( attrib.scope -> scope.`macro` )
    val attribMain = attribAny + ( attrib.scope -> scope.`main` )
    val attribTest = attribAny + ( attrib.scope -> scope.`test` )
    ensureSourceRoots( request, classpath, config.buildMacro, attribMacro, monitor )
    ensureSourceRoots( request, classpath, config.buildMain, attribMain, monitor )
    ensureSourceRoots( request, classpath, config.buildTest, attribTest, monitor )
  }

  /**
   * Re-create class path entry for given container id.
   */
  def ensureContainer(
    request :     ProjectConfigurationRequest,
    classpath :   IClasspathDescriptor,
    hasRemove :   Boolean,
    containerId : String,
    monitor :     IProgressMonitor
  ) = {
    val entry = containerEntry( containerId )
    val path = entry.getPath
    if ( hasRemove ) {
      log.info( s"Deleting container ${containerId}." )
      classpath.removeEntry( path )
    } else {
      log.info( s"Creating container ${containerId}." )
      classpath.removeEntry( path )
      classpath.addEntry( entry )
    }
  }

  /**
   * Log class path entries.
   */
  def reportClassPath(
    facade :    IMavenProjectFacade,
    config :    ParamsConfig,
    classpath : IClasspathDescriptor,
    monitor :   IProgressMonitor
  ) : Unit = {
    classpath.getEntryDescriptors.asScala.foreach { entry =>
      log.info( s"XXX ${entry.getPath} -> ${entry.getOutputLocation}" )
    }
  }

  /**
   * Create/Delete Scala IDE scala-library container.
   */
  def ensureScalaLibrary(
    request :   ProjectConfigurationRequest,
    config :    ParamsConfig,
    classpath : IClasspathDescriptor,
    monitor :   IProgressMonitor
  ) = {
    import config._
    ensureContainer(
      request,
      classpath,
      hasRemove   = eclipseRemoveLibraryContainer,
      containerId = SdtConstants.ScalaLibContId,
      monitor
    )
  }

}
