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
 * Common file system operations.
 */
object Folder {

  def ensureFolder( dir : File ) : Unit = {
    if ( !dir.exists() ) {
      dir.mkdirs()
    }
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
      if ( file.isFile() && regex.matcher( file.getAbsolutePath ).matches() ) {
        fileList.add( file )
      } else if ( file.isDirectory() ) {
        fileCollectByRegex( file, regex, fileList )
      }
    }
  }

  def findJarByResource( loader : ClassLoader, resource : String ) : Array[ File ] = {
    val iter = loader.getResources( resource )
    val list = new HashSet[ File ]( 16 )
    while ( iter.hasMoreElements() ) {
      val url = iter.nextElement()
      val con = url.openConnection
      //      if(con.isInstanceOf[FileURLConnection]) {
      //
      //      }
      val fileUrl = if ( con.isInstanceOf[ JarURLConnection ] ) {
        val jarCon = con.asInstanceOf[ JarURLConnection ]
        jarCon.getJarFileURL
      } else {
        url
      }
      val path = Paths.get( fileUrl.toURI )
      list.add( path.toFile.getCanonicalFile )
    }
    list.toArray( Array[ File ]() )
  }

  trait TransferListener {
    def onFile( source : Path, target : Path, relative : Path ) : Unit = ()
    def onFolder( source : Path, target : Path, relative : Path ) : Unit = ()
  }

  object EmptyTransferListener extends TransferListener

  import java.nio.file.StandardCopyOption
  import java.nio.file.StandardCopyOption._

  def transferFolder(
    sourceFolder : Path,
    targetFolder : Path,
    listener :     TransferListener = EmptyTransferListener,
    options : Array[ StandardCopyOption ] = Array[ StandardCopyOption ](
      REPLACE_EXISTING, COPY_ATTRIBUTES
    )
  ) : Unit = {
    require( Files.isDirectory( sourceFolder ) )
    require( Files.isDirectory( targetFolder ) )
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
      case head :: tail => Left( "Duplicate file: " + regex )
      case Nil          => Left( "File not present: " + regex )
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

  def convertFileString( source : Array[ File ] ) : Array[ String ] = {
    val length = source.length
    val target = Array.ofDim[ String ]( length )
    var index = 0
    while ( index < length ) {
      target( index ) = source( index ).getCanonicalPath
      index += 1
    }
    target
  }

}
