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

    val context : String
    val founder : AnyLog

    def text( line : String ) = s"[${context}] ${line}"

    def dbug( line : String ) : Unit = founder.dbug( text( line ) )
    def info( line : String ) : Unit = founder.info( text( line ) )
    def warn( line : String ) : Unit = founder.warn( text( line ) )
    def fail( line : String ) : Unit = founder.fail( text( line ) )
    def fail( line : String, error : Throwable ) : Unit = founder.fail( text( line ), error )

    def branch( context : String ) : AnyLog = ContextLogger( founder, context )

  }

  /**
   * Logger which defines separate context.
   */
  case class ContextLogger(
    override val founder : AnyLog,
    override val context : String
  ) extends AnyLog {
  }

}
