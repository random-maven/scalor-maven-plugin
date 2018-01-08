package com.carrotgarden.maven.scalor.eclipse

import scala.util.DynamicVariable
import com.carrotgarden.maven.scalor.A

/**
 * Simple logging facade.
 * Abstract away from Eclipse Console, Maven Console, etc.
 */
trait Logging {
  import Logging._

  val log : AnyLog

}

object Logging {

  /**
   * Simple logging facade.
   */
  trait AnyLog {
    /**
     *  Thread-local logger context.
     */
    val CTX = new DynamicVariable[ String ]( s"[${A.eclipse.name}] " )
    def context = CTX.value
    def context( value : String ) = { CTX.value = s"[${A.eclipse.name}:${value}] " }

    def info( line : String ) = ()
    def warn( line : String ) = ()
    def fail( line : String ) = ()
    def fail( line : String, error : Throwable ) = ()

  }

}
