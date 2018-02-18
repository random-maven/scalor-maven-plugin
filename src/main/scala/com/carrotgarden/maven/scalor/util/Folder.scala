package com.carrotgarden.maven.scalor.util

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.Arrays
import java.util.ArrayList
import java.util.regex.Pattern
import java.util.HashSet
import java.net.JarURLConnection
import java.nio.file.Paths
import java.util.function.Consumer
import scala.Left
import scala.Right
import java.nio.charset.Charset

import scala.language.implicitConversions

/**
 * Operations against base folder.
 */
case class Folder( root : Path ) {

  require( root.isAbsolute, "Expecting absolute base." )

  lazy val base = root.toAbsolutePath.normalize

  /**
   * Match absolute normalized path.
   */
  def isSamePath( path1 : Path, path2 : Path ) : Boolean =
    Folder.isSamePath( base, path1, path2 )

  /**
   * String absolute normalized path.
   */
  def textPath( path : Path ) : String =
    Folder.textPath( base, path )

  /**
   * Absolute normalized path.
   */
  def absolute( path : Path ) : Path =
    if ( path.isAbsolute ) path.normalize
    else base.resolve( path ).toAbsolutePath.normalize

  /**
   * Relative normalized path.
   */
  def relative( path : Path ) : Path =
    base.relativize( path ).normalize

}

/**
 * Common file system operations.
 */
object Folder {

  def hasExec( file : File ) = {
    file.exists && file.isFile && file.canRead && file.canExecute
  }

  def textPath( base : Path, path : Path ) : String = {
    if ( path.isAbsolute() ) {
      path.normalize().toString()
    } else {
      base.resolve( path ).toAbsolutePath.normalize.toString
    }
  }

  /**
   * Match absolute normalized path.
   */
  def isSamePath( base : Path, path1 : Path, path2 : Path ) : Boolean = {
    textPath( base, path1 ) == textPath( base, path2 )
  }

  /**
   * Enforce path resolution.
   * Absolute path does not resolve symbolic links.
   */
  //  def ensureAbsoluteFile( file : File ) : File = {
  //    file.getCanonicalFile
  //  }

  /**
   * Enforce path resolution.
   * Absolute path does not resolve symbolic links.
   */
  //  def ensureAbsolutePath( file : File ) : String = {
  //    file.getCanonicalPath
  //  }

  /**
   * Enforce path resolution.
   * Canonical path resolves symbolic links.
   */
  def ensureCanonicalFile( file : File ) : File = {
    file.getCanonicalFile
  }

  /**
   * Enforce path resolution.
   * Canonical path resolves symbolic links.
   */
  def ensureCanonicalPath( file : File ) : String = {
    file.getCanonicalPath
  }

  def ensureFolder( dir : File ) : Unit = {
    if ( !dir.exists() ) {
      dir.mkdirs()
    }
  }

  def ensureFolder( dir : Path ) : Unit = {
    ensureFolder( dir.toFile ) // work around travis ci
  }

  def ensureParent( file : File ) : Unit = {
    val parent = file.getParentFile
    if ( !parent.exists() ) {
      parent.mkdirs()
    }
  }

  def fileListByRegex( rootList : Array[ File ], regex : String ) : Array[ File ] = {
    val pattern = regex.r.pattern
    val fileList = new ArrayList[ File ]( 256 )
    val iter = Arrays.asList( rootList : _* ).iterator()
    while ( iter.hasNext() ) {
      val root = iter.next()
      if ( root.isDirectory() ) fileCollectByRegex( root, pattern, fileList )
    }
    fileList.toArray( Array[ File ]() )
  }

  def fileCollectByRegex( root : File, regex : Pattern, fileList : ArrayList[ File ] ) : Unit = {
    val list = root.listFiles()
    var index = 0
    var limit = list.length
    while ( index < limit ) {
      val file = list( index ); index += 1
      if ( file.isFile() && regex.matcher( file.getCanonicalPath ).matches() ) {
        fileList.add( file )
      } else if ( file.isDirectory() ) {
        fileCollectByRegex( file, regex, fileList )
      }
    }
  }

  //  def findJarByResource( loader : ClassLoader, resource : String ) : Array[ File ] = {
  //    val iter = loader.getResources( resource )
  //    val list = new HashSet[ File ]( 16 )
  //    while ( iter.hasMoreElements() ) {
  //      val url = iter.nextElement()
  //      val con = url.openConnection
  //      //      if(con.isInstanceOf[FileURLConnection]) {
  //      //
  //      //      }
  //      val fileUrl = if ( con.isInstanceOf[ JarURLConnection ] ) {
  //        val jarCon = con.asInstanceOf[ JarURLConnection ]
  //        jarCon.getJarFileURL
  //      } else {
  //        url
  //      }
  //      val path = Paths.get( fileUrl.toURI )
  //      list.add( path.toFile ) // XXX
  //    }
  //    list.toArray( Array[ File ]() )
  //  }

  trait TransferListener {
    def onFile( source : Path, target : Path, relative : Path ) : Unit = ()
    def onFolder( source : Path, target : Path, relative : Path ) : Unit = ()
  }

  object EmptyTransferListener extends TransferListener

  import java.nio.file.StandardCopyOption
  import java.nio.file.StandardCopyOption._

  // FIXME copy only delta
  def transferFolder(
    sourceFolder : Path,
    targetFolder : Path,
    listener :     TransferListener = EmptyTransferListener,
    options : Array[ StandardCopyOption ] = Array[ StandardCopyOption ](
      REPLACE_EXISTING, COPY_ATTRIBUTES
    )
  ) : Unit = {
    require( Files.isDirectory( sourceFolder ), s"Expecting folder ${sourceFolder}" )
    require( Files.isDirectory( targetFolder ), s"Expecting folder ${targetFolder}" )
    val consumer = new Consumer[ Path ] {
      override def accept( sourcePath : Path ) = {
        val relative = sourceFolder.relativize( sourcePath )
        val targetPath = targetFolder.resolve( relative )
        if ( Files.isDirectory( sourcePath ) ) {
          if ( !Files.exists( targetPath ) ) {
            Files.createDirectory( targetPath )
            listener.onFolder( sourcePath, targetPath, relative )
          }
        } else {
          Files.copy( sourcePath, targetPath, options : _* )
          listener.onFile( sourcePath, targetPath, relative )
        }
      }
    }
    Files.walk( sourceFolder ).forEach( consumer )
  }

  /**
   * Descriptor: file and its version.
   */
  case class FileItem( file : File, version : String )

  def findFileByName( classpath : Array[ File ], regex : String ) : Either[ String, File ] = {
    classpath.collect {
      case file if file.getName.matches( regex ) => file
    }.toList match {
      case head :: Nil  => Right( head )
      case head :: tail => Left( s"Duplicate file: ${regex}" )
      case Nil          => Left( s"File not present: ${regex}" )
    }
  }

  /**
   * Locate file and extract its version by regex.
   */
  def resolveJar( classPath : Array[ File ], regex : String ) : Either[ String, FileItem ] = {
    for {
      file <- findFileByName( classPath, regex ).right
      version <- extractVersion( file, regex ).right
    } yield {
      FileItem( file, version )
    }
  }

  /**
   * Extract version form file name via regex.
   */
  def extractVersion( file : File, regex : String ) : Either[ String, String ] = {
    file.getName match {
      case regex.r( version ) => Right( version )
      case _                  => Left( s"Missing version: ${regex} @ ${file.getName}" )
    }
  }

  private case class Arkon()

  /**
   * Extract resource from plugin class path into file.
   */
  def provideResource( source : String, target : File ) = {
    val loader = Arkon().getClass.getClassLoader
    val input = loader.getResourceAsStream( source )
    if ( input == null ) {
      throw new RuntimeException( s"Plugin class path missing ${source}" )
    }
    ensureParent( target )
    Files.copy( input, target.toPath )
  }

  /**
   */
  def persistString(
    file : File, text : String,
    charset : Charset = Charset.forName( "UTF-8" )
  ) = {
    Files.write( file.toPath, text.getBytes( charset ) )
  }

  //  def convertFileString( source : Array[ File ] ) : Array[ String ] = {
  //    val length = source.length
  //    val target = Array.ofDim[ String ]( length )
  //    var index = 0
  //    while ( index < length ) {
  //      target( index ) = source( index ) // XXX
  //      index += 1
  //    }
  //    target
  //  }

}
