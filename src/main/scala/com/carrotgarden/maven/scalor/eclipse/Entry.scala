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

import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.JavaCore
import org.eclipse.core.runtime.Path
import org.eclipse.m2e.core.project.configurator.AbstractProjectConfigurator
import com.carrotgarden.maven.scalor.eclipse.Logging.AnyLog
import org.eclipse.jdt.core.IClasspathAttribute
import com.carrotgarden.maven.scalor.base.Build.Param
import org.scalaide.core.SdtConstants
import com.carrotgarden.maven.scalor.base.ParamsProjectUnit

/**
 * Manage eclipse .classpath file class path entries.
 */
trait Entry {

  self : Logging with Monitor with Maven with Base.Conf =>

  /**
   * Provide class path entry instance.
   */
  def classpathEntry( path : String ) : IClasspathEntry = {
    JavaCore.newContainerEntry( Path.fromPortableString( path ) )
  }

  /**
   * Generate .classpath source[path] -> target[output] entry.
   */
  def configurePathEntry(
    request :      ProjectConfigurationRequest,
    classpath :    IClasspathDescriptor,
    sourceFolder : File,
    targetFolder : File,
    monitor :      IProgressMonitor,
    attribMap :    Map[ String, String ]       = Map(),
    generated :    Boolean                     = false
  ) = {
    log.info( s"   entry: ${relativePath( request, sourceFolder )} -> ${relativePath( request, targetFolder )}" )
    val facade = request.getMavenProjectFacade
    val sourcePath = resolveFullPath( facade, sourceFolder )
    val targetPath = resolveFullPath( facade, targetFolder )
    val entry = classpath.addSourceEntry( sourcePath, targetPath, generated )
    attribMap.foreach {
      case ( key, value ) => entry.setClasspathAttribute( key, value )
    }
  }

  def ensureSourceRoots(
    request :    ProjectConfigurationRequest,
    classpath :  IClasspathDescriptor,
    sourceList : Seq[ String ],
    target :     String,
    attribMap :  Map[ String, String ],
    monitor :    IProgressMonitor
  ) : Unit = {
    val targetFolder = new File( target ).getCanonicalFile
    sourceList.foreach { source =>
      val sourceFolder = new File( source ).getCanonicalFile
      configurePathEntry( request, classpath, sourceFolder, targetFolder, monitor, attribMap )
    }
  }

  /**
   * Configure source/target folder entries.
   * 
   * Note: register-* goals must be executed before this.
   */
  def ensureSourceRoots(
    request :   ProjectConfigurationRequest,
    classpath : IClasspathDescriptor,
    monitor :   IProgressMonitor
  ) : Unit = {

    val project = request.getMavenProject
    val projectUnit = ParamsProjectUnit( project )
    import Param._
    val attribMap = Map(
      attrib.optional -> "true"
    )
    import base.Build._
    for { // see register-macro
      sourceList <- projectUnit.extractPropertyList( buildMacroSourceFoldersParam )
      target <- projectUnit.extractProperty( buildMacroTargetParam )
    } yield {
      val attribMacro = attribMap + ( attrib.scope -> scope.`macro` )
      ensureSourceRoots( request, classpath, sourceList.asScala, target, attribMacro, monitor )
    }
    { // see register-main
      val sourceList = project.getCompileSourceRoots.asScala
      val target = project.getBuild.getOutputDirectory
      val attribMain = attribMap + ( attrib.scope -> scope.`main` )
      ensureSourceRoots( request, classpath, sourceList, target, attribMain, monitor )
    }
    { // see register-main
        // TODO absolute vs relative
      val sourceList = project.getBuild.getResources.asScala.map( _.getDirectory )
      val target = project.getBuild.getOutputDirectory
      val attribMain = attribMap + ( attrib.scope -> scope.`main` )
      ensureSourceRoots( request, classpath, sourceList, target, attribMain, monitor )
    }
    { // see register-test
      val sourceList = project.getTestCompileSourceRoots.asScala
      val target = project.getBuild.getTestOutputDirectory
      val attribTest = attribMap + ( attrib.scope -> scope.`test` )
      ensureSourceRoots( request, classpath, sourceList, target, attribTest, monitor )
    }
    { // see register-test
        // TODO absolute vs relative
      val sourceList = project.getBuild.getTestResources.asScala.map( _.getDirectory )
      val target = project.getBuild.getTestOutputDirectory
      val attribTest = attribMap + ( attrib.scope -> scope.`test` )
      ensureSourceRoots( request, classpath, sourceList, target, attribTest, monitor )
    }
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
    val containerEntry = classpathEntry( containerId )
    val containerPath = containerEntry.getPath
    if ( hasRemove ) {
      log.info( s"Deleting container ${containerId}." )
      classpath.removeEntry( containerPath )
    } else {
      log.info( s"Creating container ${containerId}." )
      classpath.removeEntry( containerPath )
      classpath.addEntry( containerEntry )
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
