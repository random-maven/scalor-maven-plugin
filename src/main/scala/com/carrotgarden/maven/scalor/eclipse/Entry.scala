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
import com.carrotgarden.maven.scalor.base.Build.BuildParam

/**
 * Manage eclipse .classpath file class path entries.
 */
trait Entry {

  self : Logging with Monitor with Maven with Base.Conf =>

  /**
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
    assertCancel( monitor )
    val facade = request.getMavenProjectFacade
    val sourcePath = resolveFullPath( facade, sourceFolder )
    val targetPath = resolveFullPath( facade, targetFolder )
    val entry = classpath.addSourceEntry( sourcePath, targetPath, generated )
    attribMap.foreach {
      // IClasspathAttribute.IGNORE_OPTIONAL_PROBLEMS
      case ( key, value ) => entry.setClasspathAttribute( key, value )
    }
  }

  /**
   * Generate .classpath entry: source[path] -> target[output].
   * Use build class path configuration parameters for given execution.
   */
  def configureBuildParam(
    request :    ProjectConfigurationRequest,
    classpath :  IClasspathDescriptor,
    execution :  MojoExecution,
    buildParam : base.Build.BuildParam,
    monitor :    IProgressMonitor
  ) = {
    log.info( "Configure execution: " + execution )
    assertCancel( monitor )
    import buildParam._
    val project = request.getMavenProject
    val sourceFilesJava = configValue[ Array[ File ] ]( project, execution, javaSourceFolders, monitor )
    val sourceFilesScala = configValue[ Array[ File ] ]( project, execution, scalaSourceFolders, monitor )
    val targetFolder = configValue[ File ]( project, execution, buildTargetFolder, monitor )
    val hasEnsure = configValue[ java.lang.Boolean ]( project, execution, base.Build.paramEnsureFolders, monitor )
    if ( hasEnsure ) { util.Folder.ensureFolder( targetFolder ) }
    ( sourceFilesJava ++ sourceFilesScala ).foreach { sourceFolder =>
      if ( hasEnsure ) {
        util.Folder.ensureFolder( sourceFolder )
      }
      import BuildParam._
      /** Configure custom attribute for compilations scope. */
      val attribMap = Map(
        attrib.scope -> scope
      )
      configurePathEntry( request, classpath, sourceFolder, targetFolder, monitor, attribMap )
    }
  }

  /**
   * Configure source/target folder entries.
   * Find execution with matching goal.
   */
  def configureExecution(
    request :   ProjectConfigurationRequest,
    classpath : IClasspathDescriptor,
    execution : MojoExecution,
    monitor :   IProgressMonitor
  ) : Unit = {
    assertCancel( monitor )
    base.Build.descriptorMap.foreach {
      case ( goal, param ) => if ( goal == execution.getGoal ) {
        configureBuildParam( request, classpath, execution, param, monitor )
      }
    }
  }

  /**
   * Configure source/target folder entries.
   * Iterate all executions which can contribute roots.
   */
  def ensureRoots(
    request :   ProjectConfigurationRequest,
    classpath : IClasspathDescriptor,
    monitor :   IProgressMonitor
  ) : Unit = {
    baseExecutionList( request, monitor ).asScala.foreach { execution =>
      configureExecution( request, classpath, execution, monitor )
    }
  }

  /**
   * Re-create class path entry for given container id.
   */
  def ensureContainer(
    request :     ProjectConfigurationRequest,
    config :      ParamsConfig,
    classpath :   IClasspathDescriptor,
    containerId : String,
    monitor :     IProgressMonitor
  ) = {
    assertCancel( monitor )
    val containerEntry = classpathEntry( containerId )
    val containerPath = containerEntry.getPath
    if ( classpath.containsPath( containerPath ) ) {
      classpath.removeEntry( containerPath )
    }
    classpath.addEntry( containerEntry )
    reportWorked( monitor )
  }

}
