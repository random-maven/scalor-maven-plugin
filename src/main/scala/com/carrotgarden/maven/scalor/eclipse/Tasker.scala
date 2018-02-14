package com.carrotgarden.maven.scalor.eclipse

import java.util.concurrent.atomic.AtomicBoolean

import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.IStatus
import org.eclipse.core.runtime.Status
import org.eclipse.core.runtime.jobs.IJobChangeEvent
import org.eclipse.core.runtime.jobs.IJobChangeListener
import org.eclipse.core.runtime.jobs.Job

import com.carrotgarden.maven.scalor.util.Logging.AnyLog

/**
 * Eclipse job execution support.
 */
object Tasker {

  type BlockFun = IProgressMonitor => Unit

  trait SeriesLifecycle extends IJobChangeListener {
    //
    val hasLife = new AtomicBoolean( false )
    def hasLifeCreate = hasLife.compareAndSet( false, true )
    def hasLifeDelete = hasLife.compareAndSet( true, false )
    //
    override def scheduled( event : IJobChangeEvent ) = {}
    override def sleeping( event : IJobChangeEvent ) = {}
    override def awake( event : IJobChangeEvent ) = {}
    override def aboutToRun( event : IJobChangeEvent ) = {
      if ( hasLifeCreate ) {
        onSeriesSetup()
      }
    }
    override def running( event : IJobChangeEvent ) = {}
    override def done( event : IJobChangeEvent ) = {
      val task = event.getJob
      val hasNone = task.getState == Job.NONE
      val hasCancel = event.getResult.getSeverity == IStatus.CANCEL
      if ( hasNone && hasCancel && hasLifeDelete ) {
        task.removeJobChangeListener( this )
        onSeriesShutdown()
      }
    }
    /** Before first run in series. */
    def onSeriesSetup() = ()
    /** After the final run in series. */
    def onSeriesShutdown() = ()
  }

  class Periodic(
    val name : String,
    logger :   AnyLog,
    period :   Long   = 3000, // milliseconds
    priority : Int    = Job.BUILD // background
  ) extends Job( name ) with SeriesLifecycle {

    def init() = {
      addJobChangeListener( this )
      setPriority( priority )
      schedule( period )
      logger.info( s"Periodic job scheduled: ${this}" )
    }

    /**
     * Periodic task invoked by this job.
     */
    def runTask( monitor : IProgressMonitor ) : Unit = ()

    /**
     * Core implementation of periodic job.
     */
    override def run( monitor : IProgressMonitor ) : IStatus = {
      // logger.context( name )
      try {
        runTask( monitor )
      } catch {
        case error : Throwable =>
          logger.fail( s"Periodic job failure: ${this}", error )
      }
      if ( monitor.isCanceled ) {
        logger.info( s"Periodic job canceled: ${this}" )
        Status.CANCEL_STATUS
      } else {
        schedule( period ) // restart
        Status.OK_STATUS
      }
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
