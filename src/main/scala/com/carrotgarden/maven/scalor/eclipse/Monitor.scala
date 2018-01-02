package com.carrotgarden.maven.scalor.eclipse

import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.OperationCanceledException
import org.eclipse.core.runtime.IProgressMonitor

/**
 * Eclipse background job support.
 */
trait Monitor {

  /**
   * Exit job on request.
   */
  def assertCancel( monitor : IProgressMonitor ) = {
    if ( monitor != null && monitor.isCanceled ) throw new OperationCanceledException()
  }

  /**
   * Update job progress.
   */
  def reportWorked( monitor : IProgressMonitor, work : Int = 1 ) {
    if ( monitor != null ) monitor.worked( work )
  }

}
