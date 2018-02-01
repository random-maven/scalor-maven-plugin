package com.carrotgarden.maven.scalor.eclipse

import org.eclipse.core.runtime.jobs.Job
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.IStatus
import org.eclipse.core.runtime.Status
import com.carrotgarden.maven.scalor.util.Logging.AnyLog

/**
 * Eclipse job execution support.
 */
object Tasker {

  class Periodic(
    name :     String,
    block :    => Unit,
    logger :   AnyLog,
    period :   Long    = 3000, // milliseconds
    priority : Int     = Job.BUILD // background
  ) extends Job( name ) {

    {
      setPriority( priority )
      schedule( period )
      logger.info( s"Periodic job scheduled: ${this}" )
    }

    override def run( monitor : IProgressMonitor ) : IStatus = {
      logger.context( name )
      try {
        block
      } catch {
        case error : Throwable =>
          logger.fail( s"Periodic job failure: ${this}", error )
      }
      if ( monitor.isCanceled ) {
        logger.info( s"Periodic job canceled: ${this}" )
      } else {
        schedule( period )
      }
      Status.OK_STATUS
    }

  }

  def findTask( name : String ) : Option[ Job ] = {
    val tasList = Job.getJobManager.find( null )
    tasList.find( task => name == task.getName )
  }

  def stopTask( name : String ) : Unit = {
    findTask( name ).map { task => task.cancel() }
  }

}
