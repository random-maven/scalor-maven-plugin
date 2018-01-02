package com.carrotgarden.maven.scalor.eclipse

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
    def info( line : String ) = ()
    def warn( line : String ) = ()
    def fail( line : String ) = ()
    def fail( line : String, error : Throwable ) = ()
  }

}
