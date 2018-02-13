package com.carrotgarden.maven.scalor.scalajs

import com.carrotgarden.maven.scalor.util.Logging.AnyLog
import org.scalajs.core.tools.logging.Logger
import org.scalajs.core.tools.logging.Level

/**
 * Linker logging features.
 */
object Logging {

  class LinkerLogger(
    logger :     AnyLog,
    hasLogTime : Boolean = false
  ) extends Logger {

    override def log( level : Level, message : => String ) : Unit = {
      level match {
        case Level.Debug => logger.dbug( message )
        case Level.Info  => logger.info( message )
        case Level.Warn  => logger.warn( message )
        case Level.Error => logger.fail( message )
      }
    }

    override def success( message : => String ) : Unit = {
      info( message )
    }

    override def trace( error : => Throwable ) : Unit = {
      logger.fail( error.getMessage, error )
    }

    override def time( title : String, nanos : Long ) : Unit = {
      if ( hasLogTime ) {
        val micros = nanos / 1000
        val millis = micros / 1000
        val second = millis / 1000
        val output = "[time] %3d s %3d ms @ %s".format( second, ( millis % 1000 ), title )
        info( output )
      }
    }

  }

}
