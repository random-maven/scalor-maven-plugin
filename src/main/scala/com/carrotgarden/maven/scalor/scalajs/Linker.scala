package com.carrotgarden.maven.scalor.scalajs

import java.io.File

import org.scalajs.core.tools.io.WritableFileVirtualJSFile
import org.scalajs.core.tools.linker.ClearableLinker
import org.scalajs.core.tools.linker.GenLinker
import org.scalajs.core.tools.linker.ModuleKind
import org.scalajs.core.tools.linker.Semantics
import org.scalajs.core.tools.linker.StandardLinker
import org.scalajs.core.tools.logging.Logger

import com.carrotgarden.maven.scalor.base
import com.carrotgarden.maven.scalor.eclipse
import com.carrotgarden.maven.scalor.base.Context.UpdateResult

/**
 * Incremental caching Scala.js linker.
 */
trait Linker {

  self : com.carrotgarden.maven.scalor.base.Context with base.Logging =>

  import Linker._
  import Logging._

  lazy val linkerLoggerBase = new LinkerLogger( logger, false )

  lazy val linkerLoggerTime = new LinkerLogger( logger, true )

  /**
   * Invoke single linker run.
   */
  def performLinker(
    context : Context
  ) : Unit = {
    import context._
    val cacherId = linkerCacherId()
    val linkerCacher = contextValue[ Cacher ]( cacherId ) {
      logger.dbug( s"Creating cacher: ${cacherId}" )
      newCacher()
    }
    val engineId = linkerEngineId( options )
    val linkerEngine = contextValue[ Engine ]( engineId ) {
      logger.dbug( s"Creating engine: ${engineId}" )
      newEngine( options )
    }
    val linkerLogger = if ( hasLogStats ) linkerLoggerTime else linkerLoggerBase
    linkerEngine.link( context, linkerLogger, linkerCacher )
    if ( hasLogStats ) {
      logger.info( s"Cacher stats: ${linkerCacher.report}" )
    }
  }

}

object Linker {

  /**
   * Linker invocation context.
   */
  case class Context(
    options :     Options,
    classpath :   Array[ File ],
    runtime :     File,
    updateList :  Array[ UpdateResult ],
    hasLogStats : Boolean
  ) {
    def hasUpdate = updateList.count( _.hasUpdate ) > 0
  }

  /**
   * Incremental caching Scala.js linker.
   */
  case class Engine(
    linker : ClearableLinker
  ) {

    /**
     * Single linker invocation.
     */
    def link(
      context : Context,
      logger :  Logger,
      cacher :  Cacher
    ) = {
      import context._
      logger.time( "Total invocation time" ) {
        val sjsirDirFiles = logger.time( s"Cacher: Process dirs" ) {
          cacher.cachedDirsFiles( classpath, updateList )
        }
        val sjsirJarFiles = logger.time( s"Cacher: Process jars" ) {
          cacher.cachedJarsFiles( classpath )
        }
        val sjsirFiles = sjsirDirFiles ++ sjsirJarFiles
        val output = WritableFileVirtualJSFile( runtime )
        linker.link( sjsirFiles, Seq(), output, logger )
      }
    }

  }

  /**
   * Linker engine configuration.
   */
  case class Options(
    checkIR :     Boolean = false,
    parallel :    Boolean = true,
    optimizer :   Boolean = false,
    batchMode :   Boolean = false,
    sourceMap :   Boolean = true,
    prettyPrint : Boolean = false
  )

  /**
   * Linker configuration parser.
   */
  object Options {
    import upickle._
    import upickle.default._
    implicit def optionsCodec : ReadWriter[ Options ] = macroRW
    def parse( options : String ) : Options = read[ Options ]( options )
    def unparse( options : Options ) : String = write( options )
  }

  def linkerCacherId() : String = {
    "scalor-linker-cacher"
  }

  def linkerEngineId( options : Options ) : String = {
    "scalor-linker-engine@" + options.toString
  }

  def newCacher() = {
    Cacher()
  }

  def newEngine( options : Options ) : Engine = {
    val linker = new ClearableLinker( () => newLinker( options ), options.batchMode )
    Engine( linker )
  }

  def newSemantics( options : Options ) : Semantics = {
    import options._
    if ( optimizer ) {
      Semantics.Defaults.optimized
    } else {
      Semantics.Defaults
    }
  }

  def newConfig( options : Options ) : StandardLinker.Config = {
    import options._
    StandardLinker.Config()
      .withCheckIR( checkIR )
      .withParallel( parallel )
      .withBatchMode( batchMode )
      .withOptimizer( optimizer )
      .withSourceMap( sourceMap )
      .withPrettyPrint( prettyPrint )
      .withModuleKind( ModuleKind.NoModule ) // TODO
      .withSemantics( newSemantics( options ) ) // TODO
  }

  def newLinker( options : Options ) : GenLinker = {
    StandardLinker( newConfig( options ) )
  }

}
