package com.carrotgarden.maven.scalor.util

import scala.util.{ Either, Failure, Left, Right, Success, Try }

import scala.language.implicitConversions

object Error {

  object Throw {
    def apply( message : String ) = throw new RuntimeException( message )
  }

  implicit def Either_Try[ E <: Throwable, R ]( either : Either[ E, R ] ) : Try[ R ] = {
    either match {
      case Right( result ) => Success( result )
      case Left( erorr )   => Failure( erorr )

    }
  }

  implicit def Try_Either[ E ]( trial : Try[ E ] ) : Either[ Throwable, E ] = {
    trial match {
      case Success( result ) => Right( result )
      case Failure( error )  => Left( error )
    }
  }

  /**
   * Capture all exceptions, including "fatal" for standard Try.
   */
  object TryHard {
    def apply[ T ]( block : => T ) : Try[ T ] =
      try Success( block ) catch {
        case error : Throwable => Failure( error )
      }
  }

}
