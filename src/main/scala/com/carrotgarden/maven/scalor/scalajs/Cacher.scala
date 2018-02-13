package com.carrotgarden.maven.scalor.scalajs

import java.io.File
import java.util.jar.JarFile

import scala.collection.concurrent.TrieMap

import org.scalajs.core.tools.io.FileVirtualBinaryFile
import org.scalajs.core.tools.io.FileVirtualScalaJSIRFile
import org.scalajs.core.tools.io.IRFileCache.IRContainer
import org.scalajs.core.tools.io.IRFileCache.VirtualRelativeIRFile
import org.scalajs.core.tools.io.VirtualJarFile
import com.carrotgarden.maven.scalor.base.Context.UpdateResult

/**
 * Incremental linker cacher.
 */
case class Cacher() {

  import Cacher._

  val linkerCache = new TrieMap[ LinkerKey, LinkerRoot ]()

  /**
   * Static cache for jars.
   */
  def cachedJarsFiles(
    classpath : Array[ File ]
  ) : Seq[ LinkerFile ] = {
    classpath.toSeq.filter( _.isFile ).flatMap { path =>
      val key = LinkerKey( path.getAbsolutePath )
      val root = linkerCache.getOrElseUpdate( key, LinkerRoot( path, extractJar( path ) ) )
      root.sjsirFiles
    }
  }

  /**
   * Updatable cache for dirs.
   */
  def cachedDirsFiles(
    classpath :  Array[ File ],
    updateList : Array[ UpdateResult ]
  ) : Seq[ LinkerFile ] = {
    classpath.toSeq.filter( _.isDirectory ).flatMap { path =>
      val key = LinkerKey( path.getAbsolutePath )
      val rootPast = linkerCache.getOrElseUpdate( key, LinkerRoot( path, extractDir( path ) ) )
      val updateResult = updateList.find( result => result.hasUpdate && rootPast.hasBase( result.basedir ) )
      val rootNext = updateResult.map( result => updateDirFiles( rootPast, result ) ).getOrElse( rootPast )
      linkerCache.put( key, rootNext )
      rootNext.sjsirFiles
    }
  }

  def report = {
    val values = linkerCache.values
    val dirs = values.filter( _.isDir )
    val dirsNum = dirs.size
    val dirsFiles = dirs.map( _.sjsirFiles.size ).sum
    val jars = values.filter( _.isJar )
    val jarsNum = jars.size
    val jarsFiles = jars.map( _.sjsirFiles.size ).sum
    s"dirs/files=${dirsNum}/${dirsFiles} jars/files=${jarsNum}/${jarsFiles}"
  }

}

object Cacher {

  type LinkerFile = VirtualRelativeIRFile

  /**
   * Cache root identity.
   */
  case class LinkerKey( key : String ) extends AnyVal

  /**
   * Cache root resource: jar or dir.
   */
  case class LinkerRoot(
    // Original resource path.
    path : File,
    // Linker files extracted from path.
    sjsirFiles : Seq[ LinkerFile ]
  ) {
    def isDir = path.isDirectory
    def isJar = path.isFile
    def hasBase( base : File ) = path.getAbsolutePath == base.getAbsolutePath
  }

  /**
   * Apply incremental changes to dir cache.
   */
  def updateDirFiles(
    root :   LinkerRoot,
    update : UpdateResult
  ) : LinkerRoot = {
    import update._
    import scala.collection.mutable
    require( hasDir( root.path ) )
    require( root.hasBase( basedir ) )
    val resultMap = mutable.Map[ LinkerKey, LinkerFile ]()
    resultMap ++= root.sjsirFiles.map( sjsir => ( LinkerKey( sjsir.path ), sjsir ) )
    deleted.foreach { file =>
      resultMap.remove( LinkerKey( file.getAbsolutePath ) )
    }
    changed.foreach { file =>
      resultMap.put( LinkerKey( file.getAbsolutePath ), linkerFile( basedir, file ) )
    }
    root.copy( sjsirFiles = resultMap.values.toSeq )
  }

  def hasDir( file : File ) : Boolean = {
    file.isDirectory
  }

  def hasJar( file : File ) : Boolean = {
    try { new JarFile( file ); true } catch { case error : Throwable => false }
  }

  def linkerJar( file : File ) : VirtualJarFile = {
    new FileVirtualBinaryFile( file ) with VirtualJarFile
  }

  def extractContainers( classpath : Seq[ File ] ) : Seq[ IRContainer ] = {
    IRContainer.fromClasspath( classpath )
  }

  def extractLinkerFiles( file : IRContainer ) : Seq[ LinkerFile ] = file match {
    case IRContainer.File( file ) => file :: Nil
    case IRContainer.Jar( jar )   => jar.sjsirFiles
  }

  def extractJar( file : File ) : Seq[ LinkerFile ] = {
    require( hasJar( file ) )
    linkerJar( file ).sjsirFiles
  }

  def extractDir( dir : File ) : Seq[ LinkerFile ] = {
    require( hasDir( dir ) )
    val baseDir = dir.getAbsoluteFile
    def walkForIR( dir : File ) : Seq[ File ] = {
      val ( subdirs, files ) = dir.listFiles().partition( _.isDirectory )
      subdirs.flatMap( walkForIR ) ++ files.filter( _.getName.endsWith( ".sjsir" ) )
    }
    for ( ir <- walkForIR( baseDir ) ) yield {
      linkerFile( baseDir, ir )
    }
  }

  def linkerFile( baseDir : File, ir : File ) : LinkerFile = {
    val relDir = ir.getPath.stripPrefix( baseDir.getPath )
    FileVirtualScalaJSIRFile.relative( ir, relDir )
  }

}
