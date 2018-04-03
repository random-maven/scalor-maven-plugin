package com.carrotgarden.maven.scalor.zinc

import com.carrotgarden.maven.scalor.util.Logging.AnyLog

import sbt.internal.inc.LoggedReporter
import sbt.util
import sbt.util.Level
import xsbti.Problem
import xsbti.compile.CompileProgress

/**
 * Zinc compiler logging features.
 */
object Logging {

  case class Logger(
    log :   AnyLog,
    level : Level.Value
  ) extends util.Logger {

    def hasLog( entry : Level.Value ) = entry.id >= level.id

    override def trace( error : => Throwable ) : Unit = {
      val report = Option( error.getMessage ).getOrElse( error )
      log.fail( s"[TRCE] Failure: ${report}", error )
    }

    override def success( message : => String ) : Unit = {
      log.info( s"[DONE] Success: ${message}" )
    }

    //    val EOL = "\n"
    //    if ( hasLog( Level.Error ) ) {
    //      if ( message.contains( EOL ) ) { // suppress stack dump
    //        val report = message.substring( 0, message.indexOf( EOL ) )
    //        log.fail( s"[FAIL] ${report}" )
    //      } else {
    //        log.fail( s"[FAIL] ${message}" )
    //      }
    //    }

    override def log( level : Level.Value, message : => String ) : Unit = {
      level match {
        case Level.Debug =>
          if ( hasLog( Level.Debug ) ) log.info( s"[DBUG] ${message}" )
        case Level.Info =>
          if ( hasLog( Level.Info ) ) log.info( s"[INFO] ${message}" )
        case Level.Warn =>
          if ( hasLog( Level.Warn ) ) log.info( s"[WARN] ${message}" )
        case Level.Error =>
          if ( hasLog( Level.Error ) ) log.fail( s"[FAIL] ${message}" )
      }
    }

  }

  case class Reporter(
    maxErrors : Int,
    logger :    util.Logger
  )
    extends LoggedReporter( maxErrors, logger ) {

    override def logInfo( problem : Problem ) : Unit = {
      logger.info( problem.toString )
    }

    override def logWarning( problem : Problem ) : Unit = {
      logger.warn( problem.toString )
    }

    override def logError( problem : Problem ) : Unit = {
      logger.error( problem.toString )
    }

  }

  case class Progress(
    log :     AnyLog,
    hasUnit : Boolean,
    hasRate : Boolean
  ) extends CompileProgress {

    override def startUnit( phase : String, unitPath : String ) : Unit = {
      if ( hasUnit ) log.info( s"[INIT] ${phase} / ${unitPath}" )
    }

    override def advance( current : Int, total : Int ) : Boolean = {
      if ( hasRate ) log.info( s"[STEP] ${current} / ${total}" )
      true
    }

  }

}
