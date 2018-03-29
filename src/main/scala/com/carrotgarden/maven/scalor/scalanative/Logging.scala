package com.carrotgarden.maven.scalor.scalanative

//import sbt.util
//import sbt.util.Level

import scala.scalanative.build
import scala.scalanative.linker
import scala.scalanative.optimizer
import scala.scalanative.nir
import scala.scalanative.nir.Global

import scala.sys.process.ProcessLogger

import com.carrotgarden.maven.scalor.util.Logging.AnyLog

import java.util.function.Supplier

import com.carrotgarden.maven.scalor.util.Java8
import Java8._

object Logging {

  object SilentNativeLogger extends build.Logger {
    override def debug( msg : String ) : Unit = {}
    override def info( msg : String ) : Unit = {}
    override def warn( msg : String ) : Unit = {}
    override def error( msg : String ) : Unit = {}
  }

  object SilentProcessLogger extends ProcessLogger {
    override def out( s : => String ) : Unit = ()
    override def err( s : => String ) : Unit = ()
    override def buffer[ T ]( f : => T ) : T = f
  }

  case class LinkerLogger( logger : AnyLog )
    extends xsbti.Logger with ProcessLogger {

    override def debug( message : Supplier[ String ] ) : Unit = {
      logger.dbug( message.get )
    }

    override def info( message : Supplier[ String ] ) : Unit = {
      logger.info( message.get )
    }

    override def warn( message : Supplier[ String ] ) : Unit = {
      logger.warn( message.get )
    }

    override def error( message : Supplier[ String ] ) : Unit = {
      logger.fail( message.get )
    }

    override def trace( error : Supplier[ Throwable ] ) : Unit = {
      val failure = error.get
      logger.fail( failure.getMessage, failure )
    }

    def stat( notice : String ) : Unit = {
      val output = "[stat] %s".format( notice )
      info( output )
    }

    def time( notice : String, nanos : Long ) : Unit = {
      val micros = nanos / 1000
      val millis = micros / 1000
      val second = millis / 1000
      val output = "[time] %3d s %3d ms @ %s".format( second, ( millis % 1000 ), notice )
      info( output )
    }

    def time( nanos : Long ) : Unit = {
      val micros = nanos / 1000
      val millis = micros / 1000
      val second = millis / 1000
      val output = "[time] %3d s %3d ms".format( second, ( millis % 1000 ) )
      info( output )
    }

    def time[ T ]( notice : Supplier[ String ] )( command : => T ) : T = {
      logger.info( notice.get )
      val time1 = System.nanoTime()
      val result = command
      val time2 = System.nanoTime()
      val nanos = time2 - time1
      time( notice.get, nanos )
      result
    }

    def running( command : String ) : Unit = {
      logger.info( s"[proc] ${command}" )
    }

    override def buffer[ T ]( body : => T ) : T = body

    override def out( message : => String ) : Unit = {
      logger.info( message )

    }

    override def err( message : => String ) : Unit = {
      logger.fail( message )
    }

  }

  case class OptimizerReporter( logger : AnyLog ) extends optimizer.Reporter {

    import scalanative.optimizer.Pass
    import scalanative.nir.Defn

    /** Gets called whenever optimizations starts. */
    override def onStart( batchId : Int, batchDefns : Seq[ Defn ] ) : Unit = {
      logger.info( s"onStart" )
    }

    /** Gets called right after pass transforms the batchDefns. */
    override def onPass(
      batchId :    Int,
      passId :     Int,
      pass :       Pass,
      batchDefns : Seq[ nir.Defn ]
    ) : Unit = {

    }

    /** Gets called with final result of optimization. */
    override def onComplete( batchId : Int, batchDefns : Seq[ Defn ] ) : Unit = {
      logger.info( s"onComplete" )
    }

  }

  case class LinkerReporter( logger : AnyLog ) extends linker.Reporter {

    /** Gets called whenever linking starts. */
    override def onStart() : Unit = {
      logger.info( s"onStart" )
    }

    /** Gets called whenever a new entry point is discovered. */
    override def onEntry( global : Global ) : Unit = {

    }

    /** Gets called whenever a new definition is loaded from nir path. */
    override def onResolved( global : Global ) : Unit = {

    }

    /** Gets called whenever linker fails to resolve a global. */
    override def onUnresolved( globa : Global ) : Unit = {

    }

    /** Gets called whenever a new direct dependency is discovered. */
    override def onDirectDependency( from : Global, to : Global ) : Unit = {

    }

    /** Gets called whenever a new conditional dependency is discovered. */
    override def onConditionalDependency( from : Global, to : Global, cond : Global ) : Unit = {

    }

    /** Gets called whenever linking is complete. */
    override def onComplete() : Unit = {
      logger.info( s"onComplete" )
    }

  }

}
