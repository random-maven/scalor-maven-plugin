package com.carrotgarden.maven.scalor.eclipse

import com.carrotgarden.maven.scalor.base
import com.carrotgarden.maven.scalor.util

import scala.collection.mutable.HashSet
import scala.tools.nsc
import scala.tools.nsc.settings.NoScalaVersion

import org.apache.maven.artifact.Artifact

import org.eclipse.core.runtime.IPath
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.Path
import org.eclipse.core.runtime.Status
import org.eclipse.core.runtime.jobs.Job
import org.eclipse.jdt.core.IClasspathContainer
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jface.preference.IPersistentPreferenceStore
import org.eclipse.m2e.core.project.IMavenProjectFacade
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest

import org.scalaide.core.SdtConstants
import org.scalaide.core.internal.project.CompileScope
import org.scalaide.core.internal.project.CustomScalaInstallationLabel
import org.scalaide.core.internal.project.LabeledScalaInstallation
import org.scalaide.core.internal.project.ScalaInstallation
import org.scalaide.core.internal.project.ScalaInstallationChoice
import org.scalaide.core.internal.project.ScalaInstallationLabel
import org.scalaide.core.internal.project.ScalaModule
import org.scalaide.core.internal.project.ScalaProject
import org.scalaide.ui.internal.preferences.IDESettings
import org.scalaide.util.eclipse.EclipseUtils
import org.eclipse.jdt.core.IClasspathAttribute
import com.carrotgarden.maven.scalor.util.Logging
import org.eclipse.core.resources.IFolder
import org.eclipse.core.resources.IResource

import java.nio.file.FileVisitOption._
import java.nio.file.FileVisitResult._
import java.nio.file.Files
import java.util.EnumSet
import java.nio.file.SimpleFileVisitor

import java.nio.file
import java.nio.file.FileVisitResult
import java.io.IOException
import java.nio.file.DirectoryStream

import scala.collection.JavaConverters._
import org.eclipse.core.runtime.IStatus
import org.eclipse.m2e.core.MavenPlugin

/**
 * Various workarounds.
 */
trait Hack {
  self : Logging with Monitor =>

  import Hack._

  /**
   * Work around Scala IDE mishandling of symbolic link paths.
   * Specifically, discover project folders which are symbolic links,
   * and explicitly declare them in the <code>.project</code> descriptor.
   */
  // http://help.eclipse.org/oxygen/index.jsp?topic=/org.eclipse.platform.doc.isv/guide/resInt_linked.htm
  def hackSymbolicLinks(
    request : ProjectConfigurationRequest,
    config :  ParamsConfig,
    monitor : IProgressMonitor
  ) : Unit = {
    import config._
    if ( eclipseHackSymbolicLinks ) {
      log.info( "Hacking .project symbolic links." )
      val project = request.getProject
      val workspace = project.getWorkspace
      val projectRoot = project.getLocation
      val projectFolder = projectRoot.toFile.toPath // absolute
      /**
       * Upgrade file system symlinks into Eclipse resource symlinks.
       */
      def processLink( path : file.Path ) : Unit = {
        val absolute = path.toAbsolutePath.normalize.toString
        val target = Path.fromOSString( absolute )
        val relative = target.makeRelativeTo( projectRoot )
        val source = project.getFolder( relative )
        val result = workspace.validateLinkLocation( source, target )
        val canLink = result.getSeverity != IStatus.ERROR
        if ( source.isLinked ) {
          log.info( s"   already linked: ${source} -> ${target}" )
        } else if ( canLink ) {
          log.info( s"   creating link : ${source} -> ${target}" )
          val update = IResource.FORCE | IResource.REPLACE
          source.createLink( target, update, monitor )
        } else {
          log.fail( s"   unable to link: ${source} -> ${target} : ${result}" )
        }
      }
      withFolderStream( projectFolder, SymlinkFilter(), processLink )
    }

  }

}

object Hack {

  import DirectoryStream.Filter

  case class SymlinkFilter() extends Filter[ file.Path ] {
    override def accept( entry : file.Path ) : Boolean = {
      Files.isDirectory( entry ) && Files.isSymbolicLink( entry )
    }
  }

  def withFolderStream(
    folder :  file.Path,
    filter :  DirectoryStream.Filter[ file.Path ],
    process : file.Path => Unit
  ) = {
    val iter = Files.newDirectoryStream( folder, filter ).iterator
    while ( iter.hasNext ) {
      process( iter.next )
    }
  }

}
