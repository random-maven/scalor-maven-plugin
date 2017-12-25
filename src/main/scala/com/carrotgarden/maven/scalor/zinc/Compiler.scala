package com.carrotgarden.maven.scalor.zinc

import java.io.File
import java.nio.file.Paths
import java.net.JarURLConnection
import java.util.Optional
import java.util.ArrayList
import java.util.HashSet

import sbt.internal.inc.AnalyzingCompiler
import sbt.internal.inc.FileAnalysisStore
import sbt.internal.inc.IncrementalCompilerImpl
import sbt.internal.inc.Locate
import sbt.internal.inc.LoggedReporter
import sbt.internal.inc.ProblemStringFormats
import sbt.internal.inc.ScalaInstance
import sbt.util.InterfaceUtil
import sbt.util.Level
import sbt.util.Logger

import xsbt.CompilerInterface
import xsbti.Problem
import xsbti.compile.AnalysisContents
import xsbti.compile.AnalysisStore
import xsbti.compile.ClasspathOptionsUtil
import xsbti.compile.CompileAnalysis
import xsbti.compile.CompileProgress
import xsbti.compile.CompilerCache
import xsbti.compile.DefinesClass
import xsbti.compile.IncOptions
import xsbti.compile.MiniSetup
import xsbti.compile.PerClasspathEntryLookup
import xsbti.compile.PreviousResult
import xsbti.compile.ZincCompilerUtil

import com.carrotgarden.maven.scalor.base

import com.carrotgarden.maven.scalor.util.Folder._

/**
 * Compiler for scope=macro.
 */
trait CompilerMacro extends Compiler
  with base.BuildMacro
  with ParamsMacro {
  self : base.Build with base.Logging with base.Params =>

  def zincBuildCache = zincCacheMacro
}

/**
 * Compiler for scope=main.
 */
trait CompilerMain extends Compiler
  with base.BuildMain
  with ParamsMain {
  self : base.Build with base.Logging with base.Params =>

  def zincBuildCache = zincCacheMain
}

/**
 * Compiler for scope=test.
 */
trait CompilerTest extends Compiler
  with base.BuildTest
  with ParamsTest {
  self : base.Logging with base.Params =>

  def zincBuildCache = zincCacheTest
}

/**
 * Shared compiler interface.
 */
trait Compiler {
  self : Params with base.Build with base.Logging with base.Params =>

  import Compiler._

  /**
   *
   * Incremental compiler state for a scope.
   */
  def zincBuildCache : File

  /**
   * Compilation scope input source files.
   */
  def zincBuildSources : Array[ File ] =
    fileListByRegex( buildSourceFolders, zincRegexAnySource )

  /**
   * Compilation scope classes output directory.
   */
  def zincBuildTarget : File = buildTargetFolder

  /**
   * Configured project build dependencies.
   */
  def zincBuildClassPath : Array[ File ] =
    buildDependencyFolders ++ projectClassPath( buildDependencyScopes )

  /**
   * Dependencies of this maven plugin.
   */
  def zincPluginClassPath : Array[ File ] = pluginClassPath()

  /**
   * Verify if logging is enabled at a level.
   */
  def zincHasLog( level : Level.Value ) : Boolean = {
    level.id >= zincLogAtLevel
  }

  /**
   * Setup and invoke zinc incremental compiler.
   */
  def zincPerformCompile() : Unit = {

    // Verify compiler-bridge is present.
    val loader = new CompilerInterface().getClass.getClassLoader

    // Assemble required build context.
    val sources : Array[ File ] = zincBuildSources
    val classPath : Array[ File ] = zincBuildClassPath
    val cacheFile : File = zincBuildCache
    val classesDirectory : File = zincBuildTarget
    val pluginClassPath : Array[ File ] = zincPluginClassPath
    val pluginDiscoveryList : Array[ File ] = zincPluginDiscoveryList( loader )

    // Ensure output locations.
    ensureParent( cacheFile )
    ensureParent( classesDirectory )

    // Provide user reporting.
    if ( zincLogSourcesList ) {
      say.info( "Sources list:" )
      reportFileList( sources )
    }
    if ( zincLogBuildClassPath ) {
      say.info( "Build class path:" )
      reportFileList( classPath )
    }
    if ( zincLogPluginClassPath ) {
      say.info( "Plugin class path:" )
      reportFileList( pluginClassPath )
    }
    if ( zincLogPluginDiscoveryList ) {
      say.info( "Compiler plugin list:" )
      reportFileList( pluginDiscoveryList )
    }

    // Locate zinc dependencies.
    val resolve = for {
      bridge <- resolveJar( pluginClassPath, zincRegexCompilerBridge ).right
      library <- resolveJar( pluginClassPath, zincRegexScalaLibrary ).right
      reflect <- resolveJar( pluginClassPath, zincRegexScalaReflect ).right
      compiler <- resolveJar( pluginClassPath, zincRegexScalaCompiler ).right
    } yield {
      ( bridge, library, reflect, compiler )
    }
    val ( bridge, library, reflect, compiler ) = resolve match {
      case Right( tuple ) => tuple
      case Left( error )  => throw new RuntimeException( error )
    }

    // Verify dependency version consistency.
    if ( zincVerifyVersion ) {
      assertVersionTree( bridge, compiler )
      assertVersionTree( bridge, library )
      assertVersionTree( bridge, reflect )
      assertVersionExact( compiler, library )
      assertVersionExact( compiler, compiler )
    }

    // Declare scala version.
    val version = library.version

    // Describe ScalaC instance.
    val scalaInstance = new ScalaInstance(
      version        = version,
      loader         = loader,
      libraryJar     = library.file,
      compilerJar    = compiler.file,
      allJars        = pluginClassPath,
      explicitActual = Some( version )
    )

    // Provide static pre-built zinc compiler-bridge jar.
    val provider = ZincCompilerUtil.constantBridgeProvider( scalaInstance, bridge.file )

    // Configured ScalaC instance.
    val scalaCompiler = new AnalyzingCompiler(
      scalaInstance    = scalaInstance,
      provider         = provider,
      classpathOptions = ClasspathOptionsUtil.auto,
      onArgsHandler = _ => (),
      classLoaderCache = None // FIXME provide cache
    )

    // Use zinc incremental compiler.
    val incremental = new IncrementalCompilerImpl

    val compilers = incremental.compilers( scalaInstance, ClasspathOptionsUtil.boot, None, scalaCompiler )

    val lookup = new PerClasspathEntryLookup {
      override def analysis( classpathEntry : File ) : Optional[ CompileAnalysis ] = Optional.empty[ CompileAnalysis ]
      override def definesClass( classpathEntry : File ) : DefinesClass = Locate.definesClass( classpathEntry )
    }

    // Use zinc invocation logger.
    val logger = new Logger {
      override def trace( error : => Throwable ) : Unit =
        say.info( "[TRCE] " + Option( error.getMessage ).getOrElse( "error" ) )
      override def success( message : => String ) : Unit =
        say.info( "[DONE] " + s"Success: $message" )
      override def log( level : Level.Value, message : => String ) : Unit =
        level match {
          case Level.Debug => if ( zincHasLog( Level.Debug ) ) say.info( "[DBUG] " + message )
          case Level.Info  => if ( zincHasLog( Level.Info ) ) say.info( "[INFO] " + message )
          case Level.Warn  => if ( zincHasLog( Level.Warn ) ) say.info( "[WARN] " + message )
          case Level.Error => if ( zincHasLog( Level.Error ) ) {
            if ( message.contains( "\n" ) ) { // suppress stack dump
              say.error( "[FAIL] " + message.substring( 0, message.indexOf( "\n" ) ) )
            } else {
              say.error( "[FAIL] " + message )
            }
          }
        }
    }

    // Compilation problem reporter.
    val reporter = new LoggedReporter( zincMaximumErrors, logger ) {
      override def logInfo( problem : Problem ) : Unit =
        logger.info( problem.toString )
      override def logWarning( problem : Problem ) : Unit =
        logger.warn( problem.toString )
      override def logError( problem : Problem ) : Unit =
        logger.error( problem.toString )
    }

    // Compilation progress printer.
    val progress = new CompileProgress {
      override def startUnit( phase : String, unitPath : String ) : Unit = {
        if ( zincLogProgressUnit ) say.info( s"[I] ${phase} / ${unitPath}" )
      }
      override def advance( current : Int, total : Int ) : Boolean = {
        if ( zincLogProgressRate ) say.info( s"[S] ${current} / ${total}" )
        true
      }
    }

    // Incremental compiler setup.
    val setup = incremental.setup(
      lookup         = lookup,
      skip           = false,
      cacheFile      = cacheFile,
      cache          = CompilerCache.fresh,
      incOptions     = IncOptions.of(),
      reporter       = reporter,
      optionProgress = Some( progress ),
      extra          = Array.empty
    )

    // Final compilation options.
    val pluginOptions = pluginDiscoveryList.map( pluginStanza( _ ) ).flatten
    val scalacOptions = zincSettingsScalaC ++ pluginOptions
    val javacOptions = zincSettingsJavaC

    // Iterative inputs.
    val inputsPast = incremental.inputs(
      classpath             = classPath,
      sources               = sources,
      classesDirectory      = classesDirectory,
      scalacOptions         = scalacOptions,
      javacOptions          = javacOptions,
      maxErrors             = zincMaximumErrors,
      sourcePositionMappers = Array.empty,
      order                 = zincCompileOrder,
      compilers             = compilers,
      setup                 = setup,
      pr                    = incremental.emptyPreviousResult // FIXME read past state.
    )

    // State change analysis.
    val analysis = AnalysisStore.getCachedStore( FileAnalysisStore.binary( cacheFile ) )

    // Iterative inputs.
    val inputsNext = {
      InterfaceUtil.toOption( analysis.get() ) match {
        case Some( analysisContents ) =>
          val previousAnalysis = analysisContents.getAnalysis
          val previousSetup = analysisContents.getMiniSetup
          val previousResult = PreviousResult.of(
            Optional.of[ CompileAnalysis ]( previousAnalysis ),
            Optional.of[ MiniSetup ]( previousSetup )
          )
          inputsPast.withPreviousResult( previousResult )
        case _ =>
          inputsPast
      }
    }

    say.info( "Invoking zinc compiler:" )

    // Run compiler invocation.
    val result = incremental.compile( inputsNext, logger )

    // Persist state changes.
    analysis.set( AnalysisContents.create( result.analysis, result.setup ) )

  }

}

object Compiler extends ProblemStringFormats {

  def assertVersionTree( item1 : FileItem, item2 : FileItem ) = {
    if ( !versionTree( item1, item2 ) ) versionFailure( item1, item2 )
  }

  def assertVersionExact( item1 : FileItem, item2 : FileItem ) = {
    if ( !versionExact( item1, item2 ) ) versionFailure( item1, item2 )
  }

  /**
   * Report version mismatch error.
   */
  def versionFailure( file1 : FileItem, file2 : FileItem ) {
    throw new RuntimeException( s"Version mismatch: ${file1} vs ${file2}" );
  }

  /**
   * Verify version tree/branch match, i.e. 2.12 is-same-as 2.12.4
   */
  def versionTree( item_short : FileItem, item_long : FileItem ) = {
    item_long.version.startsWith( item_short.version )
  }

  /**
   * Verify exact version match.
   */
  def versionExact( item1 : FileItem, item2 : FileItem ) = {
    item2.version.equals( item1.version )
  }

  /**
   * Scala compiler plugin stanza: activate plugin by jar path.
   */
  def pluginStanza( file : File ) = Array[ String ]( "-Xplugin", file.getCanonicalPath )

}
