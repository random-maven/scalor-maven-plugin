package com.carrotgarden.maven.scalor.eclipse

import java.nio.file
import java.nio.file.DirectoryStream
import java.nio.file.Files

import org.eclipse.core.resources.IResource
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.IStatus
import org.eclipse.core.runtime.Path

import com.carrotgarden.maven.scalor.util.Optioner.convert_Option_Value

/**
 * Various workarounds.
 */
trait Hack {

  self : Monitor =>

  import Hack._

  /**
   * Work around Scala IDE mishandling of symbolic link paths.
   * Specifically, discover project folders which are symbolic links,
   * and explicitly declare them in the <code>.project</code> descriptor.
   */
  // http://help.eclipse.org/oxygen/index.jsp?topic=/org.eclipse.platform.doc.isv/guide/resInt_linked.htm
  def hackSymbolicLinks(
    context : Config.SetupContext,
    monitor : IProgressMonitor
  ) : Unit = {
    import context._
    import config._
    if ( eclipseHackSymbolicLinks ) {
      logger.info( "Hacking .project symbolic links." )
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
        val origin = source.getFullPath
        if ( source.isLinked ) {
          logger.info( s"   already linked: ${origin} -> ${target}" )
        } else if ( canLink ) {
          logger.info( s"   creating link : ${origin} -> ${target}" )
          val update = IResource.FORCE | IResource.REPLACE
          source.createLink( target, update, monitor )
        } else {
          logger.fail( s"   unable to link: ${origin} -> ${target} : ${result}" )
        }
      }
      withFolderStream( projectFolder, SymlinkFilter(), processLink )
    }
  }

}

object Hack {

  import java.nio.file.DirectoryStream.Filter

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
