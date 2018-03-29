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
import java.util.regex.Matcher
import java.nio.charset.StandardCharsets
import java.util.Comparator

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
    val matcher = regex.r.pattern.matcher( "" )
    val fileList = new ArrayList[ File ]( 256 )
    var index = 0
    var limit = rootList.length
    while ( index < limit ) {
      val root = rootList( index ); index += 1
      if ( root.isDirectory ) {
        fileCollectByRegex( root, matcher, fileList )
      }
    }
    fileList.toArray( Array[ File ]() )
  }

  def fileCollectByRegex( root : File, matcher : Matcher, fileList : ArrayList[ File ] ) : Unit = {
    val list = root.listFiles
    var index = 0
    var limit = list.length
    while ( index < limit ) {
      val file = list( index ); index += 1
      if ( file.isFile && matcher.reset( file.getCanonicalPath ).matches ) {
        fileList.add( file )
      } else if ( file.isDirectory ) {
        fileCollectByRegex( file, matcher, fileList )
      }
    }
  }

  def fileHasMatch(
    path :          String,
    includeOption : Option[ Matcher ], excludeOption : Option[ Matcher ]
  ) : Boolean = {
    val hasInclude = includeOption.map( _.reset( path ).matches ).getOrElse( true )
    val hasExclude = excludeOption.map( _.reset( path ).matches ).getOrElse( false )
    hasInclude && !hasExclude
  }

  def fileHasMatch(
    file :          File,
    includeOption : Option[ Matcher ], excludeOption : Option[ Matcher ]
  ) : Boolean = {
    fileHasMatch( file.getCanonicalPath, includeOption, excludeOption )
  }

  def fileCollectByRegex(
    root :          File,
    includeOption : Option[ Matcher ], excludeOption : Option[ Matcher ],
    fileList : ArrayList[ File ]
  ) : Unit = {
    val list = root.listFiles
    var index = 0
    var limit = list.length
    while ( index < limit ) {
      val file = list( index ); index += 1
      if ( file.isFile && fileHasMatch( file, includeOption, excludeOption ) ) {
        fileList.add( file )
      } else if ( file.isDirectory ) {
        fileCollectByRegex( file, includeOption, excludeOption, fileList )
      }
    }
  }

  def fileListByRegex(
    rootList :      Array[ File ],
    includeOption : Option[ String ], excludeOption : Option[ String ]
  ) : Array[ File ] = {
    val matcherInclude = includeOption.map( _.r.pattern.matcher( "" ) )
    val matcherExclude = excludeOption.map( _.r.pattern.matcher( "" ) )
    val fileList = new ArrayList[ File ]( 256 )
    var index = 0
    var limit = rootList.length
    while ( index < limit ) {
      val root = rootList( index ); index += 1
      if ( root.isDirectory ) {
        fileCollectByRegex( root, matcherInclude, matcherExclude, fileList )
      }
    }
    fileList.toArray( Array[ File ]() )
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
    charset : Charset = StandardCharsets.UTF_8
  ) = {
    Files.write( file.toPath, text.getBytes( charset ) )
  }

  /**
   * Remove file or folder.
   */
  def deletePath( entry : Path ) : Unit = {
    import Files._
    if ( exists( entry ) ) {
      if ( isSymbolicLink( entry ) ) {
        delete( entry )
      } else {
        walk( entry )
          .sorted( Comparator.reverseOrder() )
          .forEach( path => { delete( path ) } )
      }
    }
  }

  /**
   * Verify symbolic link is present and valid.
   */
  def hasSymlink( source : Path, target : Path ) : Boolean = {
    import Files._
    true &&
      exists( source ) &&
      isSymbolicLink( source ) &&
      exists( target ) &&
      isSameFile( source.toRealPath(), target.toRealPath() )
  }

  /**
   * Ensure symbolic link is present and valid.
   */
  def ensureSymlink( source : Path, target : Path, useAbsolute : Boolean = false ) : Unit = {
    import Files._
    if ( hasSymlink( source, target ) ) {
      return
    }
    require( exists( target ), s"Missing symlink target: ${target}" )
    deletePath( source )
    require( !exists( source ), s"Failed to delete source: ${source}" )
    val result = if ( useAbsolute ) {
      target
    } else {
      source.getParent.relativize( target )
    }
    createSymbolicLink( source, result )
    require( hasSymlink( source, target ), s"Failed to symlink: ${source} -> ${target}" )
  }

}
