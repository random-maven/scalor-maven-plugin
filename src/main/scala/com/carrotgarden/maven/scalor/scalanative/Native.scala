package com.carrotgarden.maven.scalor.scalanative

import java.io.File

import com.carrotgarden.maven.scalor.util.Java8
import com.carrotgarden.maven.scalor.meta.Macro.nameOf

import scala.scalanative
import scala.scalanative.nir
import scala.scalanative.build
import scala.scalanative.linker
import scala.scalanative.optimizer
import scala.scalanative.Access

import scala.language.implicitConversions
import scala.util.Try

import sbt.io.IO
import sbt.io.Hash
import sbt.io.syntax._

import scala.sys.process.Process
import scala.collection.concurrent.TrieMap

import Native._
import Logging._
import Java8._
import com.carrotgarden.maven.scalor.util.Folder
import java.nio.file.Path
import java.nio.file.Files

/**
 * Native build pipeline provider.
 *
 * https://github.com/scala-native/scala-native/issues/1177
 *
 * https://github.com/scala-native/scala-native/blob/master/sbt-scala-native/src/main/scala/scala/scalanative/sbtplugin/ScalaNativePluginInternal.scala
 */
case class Native(
  context :    Context,
  buildCache : TrieMap[ String, AnyRef ]
) {

  import context._

  /**
   * Extract cross-build-session cached value.
   */
  def cached[ T <: AnyRef ]( key : String )( value : => T ) : T = {
    buildCache.getOrElseUpdate( key, value ).asInstanceOf[ T ]
  }

  /**
   * Invoke external command and measure invocation duration.
   */
  def measure[ T ]( notice : String )( command : => T ) : T = {
    logger.info( notice )
    val time1 = System.nanoTime
    val result = command
    val time2 = System.nanoTime
    val nanos = time2 - time1
    if ( options.logTime ) {
      logger.time( nanos )
    }
    result
  }

  /**
   * Optionally report command stats.
   */
  def report( notice : String ) : Unit = {
    if ( options.logStat ) {
      logger.stat( notice )
    }
  }

  /**
   * Invoke command in a separate process.
   */
  def invoke( command : Seq[ String ], workDir : File = identityBuildDir ) : Unit = {
    if ( options.logProc ) {
      logger.running( render( command ) )
    }
    val result = Process( command, workDir ) ! logger
    if ( result != 0 ) {
      sys.error( s"Failed to invoke command: ${render( command )}" )
    }
  }

  /**
   * Render shell command for printing.
   */
  def render( command : Seq[ String ] ) : String = {
    val prefix = if ( options.logVert ) "\n" else " "
    val middle = if ( options.logVert ) "\n" else " "
    val suffix = if ( options.logVert ) "\n" else " "
    command.mkString( prefix, middle, suffix )
  }

  /**
   * Discover an external binary tool.
   */
  def discover( binaryName : String, workDir : File = identityBuildDir ) : File = {
    val command = Seq( "which", binaryName )
    Process( command, workDir )
      .lineStream_!( SilentProcessLogger )
      .map( file( _ ) )
      .headOption
      .getOrElse { sys.error( s"Missing binary: ${binaryName}" ) }
  }

  /**
   * Compile source C/CPP into object file O.
   */
  def compile( source : String, target : String, opts : Seq[ String ] ) = {
    val hasCpp = source.endsWith( ".cpp" )
    val compiler = if ( hasCpp ) discoverClangPP else discoverClang
    val flags = if ( hasCpp ) options.optsClangPP.toSeq else Seq.empty
    val command = Seq( compiler.abs, "-c", source, "-o", target ) ++ flags ++ opts
    invoke( command )
  }

  /**
   * Convert binary file into linkable object file.
   * note: requires work dir and relative source / target
   * https://stackoverflow.com/questions/15594988/objcopy-prepends-directory-pathname-to-symbol-name
   */
  def objcopy( workDir : File, source : String, target : String, objType : ObjType, opts : Seq[ String ] ) = {
    val objTool = discoverObjCopy.abs
    val objOpts = Seq(
      "--input", "binary",
      "--output", objType.output,
      "--binary-architecture", objType.arch
    )
    val command = Seq( objTool ) ++ objOpts ++ opts ++ Seq( source, target )
    invoke( command, workDir )
  }

  lazy val identity = {
    params.workDir + options.mode
  }

  lazy val buildOutput : File = {
    params.runtime
  }

  /**
   * Discovering LLVM clang
   */
  lazy val discoverClang : File = cached( nameOf( discoverClang ) ) {
    measure( "Discovering LLVM clang" ) {
      val path = build.Discover.clang()
      report( s"clang: ${path}" )
      path.toFile
    }
  }

  /**
   * Discovering LLVM clang++
   */
  lazy val discoverClangPP : File = cached( nameOf( discoverClangPP ) ) {
    measure( "Discovering LLVM clang++" ) {
      val path = build.Discover.clangpp()
      report( s"clang++: ${path}" )
      path.toFile
    }
  }

  /**
   * Discovering GCC objcopy
   */
  lazy val discoverObjCopy : File = cached( nameOf( discoverObjCopy ) ) {
    measure( "Discovering GCC objcopy" ) {
      val objcopy = discover( "objcopy" )
      report( s"objcopy: ${objcopy}" )
      objcopy
    }
  }

  /**
   * Discovering LLVM compile options
   */
  lazy val discoverCompileOptions : Seq[ String ] = cached( nameOf( discoverCompileOptions ) ) {
    measure( "Discovering LLVM compile options" ) {
      val options = build.Discover.compileOptions()
      report( s"compile options: ${options.mkString( " " )}" )
      options
    }
  }

  /**
   * Discovering LLVM linking options
   */
  lazy val discoverLinkingOptions : Seq[ String ] = cached( nameOf( discoverLinkingOptions ) ) {
    measure( "Discovering LLVM linking options" ) {
      val options = build.Discover.linkingOptions()
      report( s"linking options: ${options.mkString( " " )}" )
      options
    }
  }

  /**
   * LLVM discover platform: <arch><sub>-<vendor>-<sys>-<abi>
   */
  lazy val discoverTargetTriplet : String = cached( nameOf( discoverTargetTriplet ) + identity ) {
    val workDir = identityBuildDir
    val clang = discoverClang
    val tripletDir = tripletProbeDir
    measure( "Discovering LLVM triple" ) {
      val source = tripletDir / "probe.c.file"
      val target = tripletDir / "probe.ll.file"
      val probe = "int probe;"
      IO.write( source, probe )
      val command = Seq(
        clang.abs, "-S", "-xc", "-emit-llvm", "-o", target.abs, source.abs
      )
      invoke( command )
      // parse generated *.ll entry:
      // target triple = "x86_64-unknown-linux-gnu"
      val triple = IO.readLines( target ).collectFirst {
        case line if line.contains( "target triple" ) =>
          line.split( """["]""" ).apply( 1 )
      }.getOrElse( sys.error( "Failed to parse probe: ${command}" ) )
      report( s"triple: ${triple}" )
      triple
    }
  }

  lazy val nativeCrossDir : File = {
    val folder = params.workDir / options.mode
    IO.createDirectory( folder )
    folder
  }

  /**
   * Build location for sources from org.scala-native:nativelib
   */
  lazy val interopLibDir : File = {
    val folder = identityBuildDir / "native-lib"
    IO.createDirectory( folder )
    folder
  }

  /**
   * Build location for resources from current project
   */
  lazy val projectBuildDirCdata : File = {
    val folder = identityBuildDir / "project-data"
    IO.createDirectory( folder )
    folder
  }

  /**
   * Build location for C/CPP sources from current project
   */
  lazy val projectBuildDirClang : File = {
    val folder = identityBuildDir / "project-lang"
    IO.createDirectory( folder )
    folder
  }

  /**
   * Build location for sources from triplet probe
   */
  lazy val tripletProbeDir : File = {
    val folder = identityBuildDir / "triplet-probe"
    IO.createDirectory( folder )
    folder
  }

  /**
   * Working directory for intermediate build files
   */
  lazy val identityBuildDir : File = cached( nameOf( identityBuildDir ) + identity ) {
    measure( "Creating build dir" ) {
      val folder = nativeCrossDir / "build"
      IO.delete( folder )
      IO.createDirectory( folder )
      report( s"path: ${folder}" )
      folder
    }
  }

  lazy val buildLinkStubs : Boolean = {
    options.linkStubs
  }

  lazy val buildEntryClass : String = {
    params.entryClass + "$" // must be object
  }

  lazy val buildClassPath : Seq[ Path ] = {
    params.classPath.toSeq.map( _.toPath )
  }

  /**
   * Aggregate config object that's used for tools
   */
  lazy val buildConfig = {
    val config = build.Config.empty
      .withMode( buildMode )
      .withLinkStubs( buildLinkStubs )
      .withMainClass( buildEntryClass )
      .withClassPath( buildClassPath )
      .withWorkdir( identityBuildDir.toPath )
      .withNativelib( interopLibJar.toPath )
      .withClang( discoverClang.toPath )
      .withClangPP( discoverClangPP.toPath )
      .withCompileOptions( discoverCompileOptions )
      .withLinkingOptions( discoverLinkingOptions )
      .withTargetTriple( discoverTargetTriplet )
      .withLogger( SilentNativeLogger )
    config
  }

  /**
   * Pass manager for the optimizer
   */
  lazy val nativeDriver : optimizer.Driver = {
    optimizer.Driver.default( buildConfig.mode )
  }

  /**
   * Link NIR using Scala Native linker
   */
  lazy val nirLinkIR : linker.Result = {
    val config = buildConfig
    val driver = nativeDriver
    measure( "Linking NIR" ) {
      val result = Access.scanatLink( config, driver )
      Access.resultAssert( result )
      report( Access.resultReport( result ) )
      result
    }
  }

  /**
   * Optimize NIR produced after linking
   */
  lazy val nirOptimizeIR : Seq[ nir.Defn ] = {
    val config = buildConfig
    val driver = nativeDriver
    val result = nirLinkIR
    measure( "Optimizing NIR" ) {
      val optimized = Access.scanatOptimize( config, driver, result )
      report( s"optimized ${optimized.size} entries" )
      optimized
    }
  }

  /**
   * Generate LLVM IR based on the optimized NIR
   */
  lazy val nirGenerateLL : Seq[ File ] = {
    val config = buildConfig
    val optimized = nirOptimizeIR
    val buildDir = identityBuildDir
    measure( "Generating LL from NIR" ) {
      Access.codegenProduce( config, optimized )
      val moduleList = ( buildDir ** "*.ll" ).get
      report( s"produced ${moduleList.length} modules" )
      moduleList.toSeq
    }
  }

  /**
   * Compile LLVM IR to native object files
   */
  lazy val nirCompileLL : Seq[ File ] = {
    val buildDir = identityBuildDir
    val compileOpts = discoverCompileOptions
    val generated = nirGenerateLL
    val optimizeOpts = settingsCompile
    val opts = optimizeOpts ++ compileOpts
    measure( "Compiling LL to native O" ) {
      val objectList = generated.par.map { ll =>
        val source = ll.abs
        val target = source + ".o"
        compile( source, target, opts )
        new File( target ).getAbsoluteFile
      }.seq.toSeq
      report( s"produced ${objectList.size} objects" )
      objectList
    }
  }

  /**
   * User provided compile options
   */
  lazy val settingsCompile : Seq[ String ] = {
    Access.selectOption( buildMode, options.optsCompDebug, options.optsCompRelease )
  }

  /**
   * User provided linking options
   */
  lazy val settingsLinking : Seq[ String ] = {
    Access.selectOption( buildMode, options.optsLinkDebug, options.optsLinkRelease )
  }

  /**
   * Copy src/main/clang -> build/project-clang
   */
  lazy val projectCopyClangFiles : Seq[ File ] = {
    val target = projectBuildDirClang
    params.clangFolders.filter( _.exists ).map( _.getAbsoluteFile ).foreach { source =>
      IO.copyDirectory(
        source, target,
        overwrite            = true, preserveLastModified = true, preserveExecutable = true
      )
    }
    nativeSourceList( target )
  }

  /**
   * Copy src/main/cdata -> build/project-cdata
   */
  lazy val projectCopyCdataFiles : Seq[ File ] = {
    def zipper = IO.createUniqueDirectory( identityBuildDir / "project-zipper" ) // XXX
    import params._
    val target = projectBuildDirCdata
    val output = if ( cdataZipEnable ) zipper else target
    params.cdataFolders.filter( _.exists ).map( _.getAbsoluteFile ).foreach { source =>
      IO.copyDirectory(
        source, output,
        overwrite            = true, preserveLastModified = true, preserveExecutable = true
      )
    }
    if ( cdataZipEnable ) {
      val zipfile = target / cdataZipFileName
      produceDirZip( output, zipfile )
    }
    Folder.fileListByRegex( Array( target ), None, Some( ".+[.]o" ) )
  }

  /**
   * Compile project C/CPP sources to native objects
   */
  lazy val projectCompileClang : Seq[ File ] = if ( params.clangEnable ) {
    val workDir = identityBuildDir
    val compileOpts = discoverCompileOptions
    val provided = projectCopyClangFiles
    val optimizeOpts = settingsCompile
    val opts = optimizeOpts ++ compileOpts
    measure( "Compiling project C lang to native O" ) {
      val objectList = provided.par.map { cc =>
        val source = cc.abs
        val target = source + ".o"
        compile( source, target, opts )
        new File( target ).getAbsoluteFile
      }.seq.toSeq
      report( s"produced ${objectList.size} objects in folder ${projectBuildDirClang}" )
      objectList
    }
  } else {
    Seq.empty
  }

  /**
   * Convert project resources to native objects
   */
  lazy val projectConvertCdata : Seq[ File ] = if ( params.cdataEnable ) {
    val triplet = discoverTargetTriplet
    val provided = projectCopyCdataFiles
    val dataDir = projectBuildDirCdata
    val dataPath = dataDir.toPath
    val objType = convertTripletObjCopy( triplet, options.mapsObjCopy )
    measure( "Converting project C data to native O" ) {
      report( s"mapping: triplet=${triplet} -> objType=${objType}" )
      val objectList = provided.par.map { dd =>
        val base = dataPath.relativize( dd.toPath ).toString
        val source = base
        val target = base + ".o"
        objcopy( dataDir, source, target, objType, options.optsObjCopy )
        new File( dataDir, target ).getAbsoluteFile
      }.seq.toSeq
      report( s"produced ${objectList.size} objects in folder ${projectBuildDirCdata}" )
      objectList
    }
  } else {
    Seq.empty
  }

  lazy val interopLibJar : File = {
    params.nativeLib
  }

  /**
   * Unpack scala-native inerop lib
   */
  lazy val interopUnpackLib : File = cached( nameOf( interopUnpackLib ) + identity ) {
    val workDir = identityBuildDir
    measure( "Unpacking scala-native:nativelib" ) {
      val libDir = interopLibDir
      val libFile = interopLibJar
      val libHashCode = Hash( libFile ).toSeq
      val libHashFile = libDir / "jarhash"
      def hasUnpack =
        libDir.exists &&
          libHashFile.exists &&
          libHashCode == IO.readBytes( libHashFile ).toSeq
      if ( !hasUnpack ) {
        IO.delete( libDir )
        IO.unzip( libFile, libDir )
        IO.write( libHashFile, Hash( libFile ) )
      }
      val sourceList = nativeSourceList( libDir )
      report( s"produced ${sourceList.size} sources in folder ${libDir}" )
      libDir
    }
  }

  lazy val buildGC : String = {
    params.gcType
  }

  lazy val buildMode : build.Mode = {
    build.Mode( options.mode )
  }

  /**
   * Compile C/C++ code in scala-native interop lib
   */
  lazy val interopCompileLib : File = cached( nameOf( interopCompileLib ) + identity ) {

    val gcType = buildGC
    val clang = discoverClang
    val clangpp = discoverClangPP
    val compileOpts = settingsCompile ++ discoverCompileOptions

    val interopDir = interopUnpackLib

    val linkerResult = nirLinkIR

    measure( "Compiling scala-native:nativelib" ) {

      val sourceList = nativeSourceList( interopDir )

      // predicate to check if given file path shall be compiled
      val sep = java.io.File.separator
      val optPath = interopDir + sep + "optional" // XXX hardcode
      val gcPath = interopDir + sep + "gc" // XXX hardcode
      val gcSelPath = gcPath + sep + gcType

      val linkNameList = Access.runtimeLinks( buildConfig, linkerResult )

      def hasInclude( path : String ) = {
        if ( path.contains( optPath ) ) {
          val name = file( path ).getName.split( "\\." ).head
          linkNameList.contains( name )
        } else if ( path.contains( gcPath ) ) {
          path.contains( gcSelPath )
        } else {
          true
        }
      }

      // delete .o files for all excluded source files
      sourceList.par.foreach { entry =>
        val source = entry.abs
        if ( !hasInclude( source ) ) {
          val target = file( source + ".o" )
          if ( target.exists ) {
            IO.delete( target )
          }
        }
      }

      // generate .o files for all included source files
      sourceList.par.foreach { entry =>
        val source = entry.abs
        val target = source + ".o"
        if ( hasInclude( source ) && !file( target ).exists ) {
          compile( source, target, compileOpts )
        }
      }

      report( s"compiled ${sourceList.size} sources" )

    }

    interopDir

  }

  /**
   * Define O/S-dependent required runtime libraries
   */
  lazy val systemRuntimeLinks : Seq[ String ] = cached( nameOf( systemRuntimeLinks ) ) {
    val target = discoverTargetTriplet
    val os = Option( sys.props( "os.name" ) ).getOrElse( "" )
    val arch = target.split( "-" ).head
    val librt = os match {
      case "Linux" => Seq( "rt" )
      case _       => Seq.empty
    }
    val libunwind = os match {
      case "Mac OS X" => Seq.empty
      case _          => Seq( "unwind", "unwind-" + arch )
    }
    librt ++ libunwind
  }

  /**
   * Invoke custom build pipeline
   */
  lazy val invokeBuildCustom : File = {

    val dir1 = identityBuildDir

    val clang = discoverClang
    val clangpp = discoverClangPP
    val objcopy = discoverObjCopy
    val compileOpts = discoverCompileOptions
    val linkingOpts = discoverLinkingOptions
    val targetTriple = discoverTargetTriplet

    val nir1 = nirLinkIR
    val nir2 = nirOptimizeIR
    val nir3 = nirGenerateLL
    val nir4 = nirCompileLL

    val lib1 = interopUnpackLib
    val lib2 = interopCompileLib

    val proj1 = projectConvertCdata
    val proj2 = projectCompileClang

    val nirObjs = nirCompileLL.map( _.abs )
    val cdataObjs = projectConvertCdata.map( _.abs )
    val clangObjs = projectCompileClang.map( _.abs )
    val interopObjs = nativeObjectList( interopLibDir ).map( _.abs )

    val runtimeLinks = systemRuntimeLinks ++ Access.runtimeLinks( buildConfig, nirLinkIR )

    val linkerOpts = runtimeLinks.map( link => s"-l${link}" ) ++ linkingOpts
    val tripleOpts = Seq( "-target", targetTriple )
    val outputOpts = Seq( "-o", buildOutput.abs )

    val buildOpts = outputOpts ++ linkerOpts ++ tripleOpts ++ settingsLinking
    val objectList = nirObjs ++ cdataObjs ++ clangObjs ++ interopObjs
    val command = Seq( clangpp.abs ) ++ buildOpts ++ objectList

    measure( "Linking output runtime binary" ) {
      invoke( command )
      val size = "%,d".format( buildOutput.length )
      val path = buildOutput.getCanonicalPath
      report( s"size: ${size} path: ${path}" )
    }

    buildOutput

  }

  /**
   * Invoke default build pipeline
   */
  lazy val invokeBuildDefault : File = {
    val config = buildConfig
    val outpath = buildOutput.toPath
    build.Build.build( config, outpath ).toFile
  }

}

object Native {

  /**
   * Native pipeline invocation context.
   */
  case class Context(
    params :  Linker.Params,
    options : Linker.Options,
    logger :  LinkerLogger
  )

  /**
   * Extract C/CPP files form a directory.
   */
  def nativeSourceList( dir : File ) : Seq[ File ] = {
    val listC = ( dir ** "*.c" ).get
    val listCPP = ( dir ** "*.cpp" ).get
    ( listC ++ listCPP )
  }

  /**
   * Extract O files form a directory.
   */
  def nativeObjectList( dir : File ) : Seq[ File ] = {
    val listC = ( dir ** "*.o" ).get
    ( listC )
  }

  def produceDirZip( sourceFolder : File, outputZip : File ) : Unit = {
    import scala.collection.mutable._
    val root = sourceFolder.toPath
    val sourceList = Buffer[ ( File, String ) ]()
    Files.walk( root )
      // .filter( path => { Files.isRegularFile( path ) } )
      .forEach( path => {
        val relative = root.relativize( path )
        val sourceEntry = ( path.toFile, relative.toString )
        sourceList += sourceEntry
      } )
    IO.zip( sourceList, outputZip )
  }

  /**
   * Descriptor for objcopy <binary-architecture/output>.
   */
  case class ObjType( arch : String, output : String )

  /**
   * Convert from LLVM <triplet> to objcopy <binary-architecture/output>
   */
  def convertTripletObjCopy( triplet : String, mappings : Map[ String, String ] ) : ObjType = {
    val mappingOption = mappings.find {
      case ( regex, entry ) => regex.r.pattern.matcher( triplet ).matches
    }
    require(
      mappingOption.isDefined,
      s"Unsupported triplet: ${triplet} in mappins: ${mappings.mkString( " " )}"
    )
    val separator = "/"
    val ( regex, entry ) = mappingOption.get
    require(
      entry.contains( separator ),
      s"Missing field separator=${separator} in objcopy mapping entry: ${regex}=${entry}"
    )
    val termList = entry.split( "/" )
    require(
      termList.size == 2,
      s"Wrong term list size=${termList.size} in objcopy mapping entry: ${regex}=${entry}"
    )
    val arch = termList( 0 )
    val output = termList( 1 )
    ObjType( arch, output )
  }

  implicit class RichFile( file : File ) {
    def abs : String = file.getAbsolutePath
  }

}
