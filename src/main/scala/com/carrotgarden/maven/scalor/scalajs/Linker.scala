package com.carrotgarden.maven.scalor.scalajs

import com.carrotgarden.maven.scalor._

import java.io.File

import org.scalajs.core.tools.linker.StandardLinker
import org.scalajs.core.tools.linker.Semantics
import org.scalajs.core.tools.linker.ModuleKind
import org.scalajs.core.tools.logging.ScalaConsoleLogger
import org.scalajs.core.tools.io.WritableFileVirtualJSFile
import org.scalajs.core.tools.io.IRFileCache
import org.scalajs.core.tools.linker.ClearableLinker
import org.scalajs.core.tools.linker.GenLinker
import org.scalajs.core.tools.logging.Logger
import org.scalajs.core.tools.logging.Level

/**
 * Incremental caching Scala.js linker.
 */
trait Linker {

  self : eclipse.Context with base.Logging =>

  import Linker._

  lazy val linkerLogger = new LinkerLogger( log )

  /**
   * Invoke single linker run.
   */
  def performLinker( options : Options, classpath : Array[ File ], runtime : File ) : Unit = {
    val id = linkerId( options )
    val engine = contextExtract[ Engine ]( id ).getOrElse {
      val engine = Linker( options ); contextPersist( id, Some( engine ) ); engine
    }
    engine.link( classpath, runtime, linkerLogger )
  }

}

object Linker {

  /**
   * Incremental caching Scala.js linker.
   */
  case class Engine(
    linker : ClearableLinker,
    cache :  IRFileCache#Cache
  ) {

    /**
     * Invoke single linker run.
     */
    def link( classpath : Array[ File ], runtime : File, logger : Logger ) = {
      val collected = IRFileCache.IRContainer.fromClasspath( classpath )
      val extracted = cache.cached( collected )
      val result = WritableFileVirtualJSFile( runtime )
      linker.link( extracted, Seq(), result, logger )
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

  def linkerId( options : Options ) = {
    "scala-js-linker@" + options.toString
  }

  def apply( options : Options ) : Engine = {
    val linker = new ClearableLinker( () => newLinker( options ), options.batchMode )
    val cache = new IRFileCache().newCache
    Engine( linker, cache )
  }

  def semantics( options : Options ) : Semantics = {
    import options._
    if ( optimizer ) {
      Semantics.Defaults.optimized
    } else {
      Semantics.Defaults
    }
  }

  def config( options : Options ) = {
    import options._
    StandardLinker.Config()
      .withCheckIR( checkIR )
      .withParallel( parallel )
      .withBatchMode( batchMode )
      .withOptimizer( optimizer )
      .withSourceMap( sourceMap )
      .withPrettyPrint( prettyPrint )
      .withModuleKind( ModuleKind.NoModule ) // TODO
      .withSemantics( semantics( options ) ) // TODO
  }

  def newLinker( options : Options ) : GenLinker = {
    StandardLinker( config( options ) )
  }

  class LinkerLogger( logger : util.Logging.AnyLog ) extends Logger {
    def log( level : Level, message : => String ) : Unit = {
      level match {
        case Level.Debug => logger.dbug( message )
        case Level.Info  => logger.info( message )
        case Level.Warn  => logger.warn( message )
        case Level.Error => logger.fail( message )
      }
    }
    def success( message : => String ) : Unit = info( message )
    def trace( error : => Throwable ) : Unit = {
      logger.fail( error.getMessage, error )
    }
  }

}
