package com.carrotgarden.maven.scalor.eclipse

import java.io.File
import java.nio.file.Paths

import scala.collection.JavaConverters.asScalaBufferConverter

import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.Path
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.JavaCore
import org.scalaide.core.SdtConstants

import com.carrotgarden.maven.scalor.base
import com.carrotgarden.maven.tools.Description

import com.carrotgarden.maven.scalor.util.Optioner.convert_Option_Value

/**
 * Manage eclipse .classpath descriptor class path entries.
 */
trait Entry {

  self : Monitor with Maven with Base.Conf =>

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
    context :      Config.SetupContext,
    sourceFolder : String, // absolute
    targetFolder : String, // absolute
    monitor :      IProgressMonitor,
    attribMap :    Map[ String, String ] = Map(),
    generated :    Boolean               = false
  ) : Unit = {
    import context._
    val project = request.getProject
    val sourcePath = Maven.projectFolder( project, sourceFolder ).getFullPath
    val targetPath = Maven.projectFolder( project, targetFolder ).getFullPath
    logger.info( s"   ${sourcePath} -> ${targetPath}" )
    val entry = classpath.addSourceEntry( sourcePath, targetPath, generated )
    attribMap.foreach {
      case ( key, value ) => entry.setClasspathAttribute( key, value )
    }
  }

  @Description( """
  Path convention: resolve as absolute.
  """ )
  def ensureSourceRoots(
    context :    Config.SetupContext,
    sourceList : Seq[ String ], // absolute
    target :     String, // absolute
    attribMap :  Map[ String, String ],
    monitor :    IProgressMonitor
  ) : Unit = {
    import context._
    import com.carrotgarden.maven.scalor.util.Folder
    @Description( """
    Project source/target folders are project-contained.
    """ )
    val basedir = Folder( request.getProject.getLocation.toFile.toPath )
    val targetFolder = basedir.absolute( Paths.get( target ) ).toString
    sourceList.foreach { source =>
      val sourceFolder = basedir.absolute( Paths.get( source ) ).toString
      configurePathEntry( context, sourceFolder, targetFolder, monitor, attribMap )
    }
  }

  @Description( """
  Path convention: resolve as absolute.
  """ )
  def ensureSourceRoots(
    context :   Config.SetupContext,
    build :     base.Build,
    attribMap : Map[ String, String ],
    monitor :   IProgressMonitor
  ) : Unit = {
    import build._
    val resourceList = // absolute FIXME think again when symlinks here
      buildResourceFolders.map( path => new File( path.getDirectory ).getAbsolutePath )
    val sourceList = // absolute
      buildSourceFolders.map( path => path.getAbsolutePath )
    val target = // absolute
      buildTargetFolder.getAbsolutePath
    ensureSourceRoots( context, resourceList, target, attribMap, monitor )
    ensureSourceRoots( context, sourceList, target, attribMap, monitor )
  }

  /**
   * Configure source/target folder class path entries.
   *
   * Note: register-* goals must be executed before this.
   */
  def ensureSourceRoots(
    context : Config.SetupContext,
    monitor : IProgressMonitor
  ) : Unit = {
    import context._
    import com.carrotgarden.maven.scalor.base.Build.Param._
    val attribAny = Map(
      attrib.optional -> "true"
    )
    val attribMacro = attribAny + ( attrib.scope -> scope.`macro` )
    val attribMain = attribAny + ( attrib.scope -> scope.`main` )
    val attribTest = attribAny + ( attrib.scope -> scope.`test` )
    ensureSourceRoots( context, config.buildMacro, attribMacro, monitor )
    ensureSourceRoots( context, config.buildMain, attribMain, monitor )
    ensureSourceRoots( context, config.buildTest, attribTest, monitor )
  }

  /**
   * Re-create class path entry for given container id.
   */
  def ensureContainer(
    context :     Config.SetupContext,
    hasRemove :   Boolean,
    containerId : String,
    monitor :     IProgressMonitor
  ) = {
    import context._
    val entry = containerEntry( containerId )
    val path = entry.getPath
    if ( hasRemove ) {
      logger.info( s"Deleting container ${containerId}." )
      classpath.removeEntry( path )
    } else {
      logger.info( s"Creating container ${containerId}." )
      classpath.removeEntry( path )
      classpath.addEntry( entry )
    }
  }

  /**
   * Log class path entries.
   */
  def reportClassPath(
    context :   Config.SetupContext,
    useOutput : Boolean,
    monitor :   IProgressMonitor
  ) : Unit = {
    import context._
    import config._
    if ( eclipseLogClasspathOrder ) {
      val report = new StringBuffer()
      classpath.getEntryDescriptors.asScala.foreach { entry =>
        if ( useOutput ) {
          val output = Option( entry.getOutputLocation )
            .map( path => s" -> ${path}" ).getOrElse( "" )
          report.append( s"   ${entry.getPath}${output}\n" )
        } else {
          report.append( s"   ${entry.getPath}\n" )
        }
      }
      logger.info( s"\n${report}" )
    }
  }

  /**
   * Create/Delete Scala IDE scala-library container.
   */
  def ensureScalaLibrary(
    context : Config.SetupContext,
    monitor : IProgressMonitor
  ) = {
    import context._
    import config._
    ensureContainer(
      context,
      hasRemove   = eclipseRemoveLibraryContainer,
      containerId = SdtConstants.ScalaLibContId,
      monitor
    )
  }

}
