package com.carrotgarden.maven.scalor.eclipse

import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.OperationCanceledException
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.SubMonitor
import scala.language.implicitConversions

/**
 * Eclipse job notification support.
 */
trait Monitor {

  /**
   * Produce a sub monitor with 100% work scope.
   */
  implicit class Enrich( monitor : IProgressMonitor ) {
    def toSub : SubMonitor = SubMonitor.convert( monitor, 100 )
  }

}
