package com.carrotgarden.maven.scalor.util

import scala.util.DynamicVariable
import com.carrotgarden.maven.scalor.A

/**
 * Simple logging facade.
 *
 * Abstract away from Eclipse Console, Maven Console, Maven, Plexus, etc.
 */
trait Logging {

  val log : Logging.AnyLog

}

object Logging {

  /**
   * Simple logging facade.
   */
  trait AnyLog {

    /**
     *  Thread-local logger context.
     */
    val CTX = new DynamicVariable[ String ]( s"[${A.maven.name}] " )
    def context() : String = CTX.value
    def context( value : String ) : Unit = { CTX.value = s"[${A.maven.name}:${value}] " }

    def dbug( line : String ) : Unit = ()
    def info( line : String ) : Unit = ()
    def warn( line : String ) : Unit = ()
    def fail( line : String ) : Unit = ()
    def fail( line : String, error : Throwable ) : Unit = ()

  }

  /**
   * Empty logger.
   */
  case object NoopLogger extends AnyLog

  /**
   * Logger which uses context switch.
   */
  case class SwitchLogger( log : AnyLog, name : String ) extends AnyLog {
    override def dbug( line : String ) : Unit = { log.context( name ); log.dbug( line ) }
    override def info( line : String ) : Unit = { log.context( name ); log.info( line ) }
    override def warn( line : String ) : Unit = { log.context( name ); log.warn( line ) }
    override def fail( line : String ) : Unit = { log.context( name ); log.fail( line ) }
    override def fail( line : String, error : Throwable ) : Unit = { log.context( name ); log.fail( line, error ) }
  }

}
