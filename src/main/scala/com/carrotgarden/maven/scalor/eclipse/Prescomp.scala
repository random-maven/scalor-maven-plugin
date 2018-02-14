package com.carrotgarden.maven.scalor.eclipse

import scala.collection.mutable

import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.m2e.core.project.IMavenProjectFacade
import org.scalaide.core.IScalaProject
import org.scalaide.core.compiler.IScalaPresentationCompiler
import org.scalaide.core.internal.compiler.ScalaPresentationCompiler

import com.carrotgarden.maven.scalor.util.Logging.AnyLog
import com.carrotgarden.maven.scalor.util.Optioner.convert_Option_Value

/**
 * Manage Scala IDE presentation compiler.
 */
trait Prescomp {

  import Prescomp._

  /**
   * Build participant invocation.
   *
   * Setup or update presentation compiler management job.
   */
  def prescompEnsure(
    context : Config.BuildContext,
    monitor : IProgressMonitor
  ) : Unit = {
    import context._
    // always cancel
    Tasker.stopTask( prescomp.eclipsePrescompTaskName )
    // optionally reschedule
    if ( prescomp.eclipsePrescompEnable ) {
      val context = Context( facade, prescomp )
      val manager = Manager(
        context, logger.branch( "prescomp-manager" )
      )
      manager.init()
      logger.info( s"Manager running @ ${manager}" )
    }
  }

}

object Prescomp {

  val serverityError = 2 // See ScalaC.

  case class Context(
    name :             String,
    project :          IScalaProject,
    periodInvoke :     Long,
    hasLogErrorUnits : Boolean
  )

  object Context {
    def apply(
      facade :   IMavenProjectFacade,
      prescomp : ParamsPrescomp
    ) : Context = {
      import prescomp._
      val project = ScalaIDE.pluginProject( facade.getProject )
      new Context(
        name             = eclipsePrescompTaskName,
        project          = project,
        periodInvoke     = eclipsePrescompPeriodInvoke,
        hasLogErrorUnits = eclipsePrescompLogErrorUnits
      )
    }
  }

  /**
   * Presentation compiler management job.
   */
  case class Manager(
    context : Context,
    logger :  AnyLog
  ) extends Tasker.Periodic(
    context.name,
    logger,
    context.periodInvoke
  ) {
    import context._

    def hasProject = project.underlying.isOpen

    override def onSeriesSetup() = {
      logger.info( s"Manager setup @ ${this}" )
    }

    override def onSeriesShutdown() = {
      logger.info( s"Manager shutdown @ ${this}" )
    }

    override def runTask( monitor : IProgressMonitor ) : Unit = {
      if ( hasProject ) {
        val errorList = prescompErrorList( project )
        if ( errorList.isEmpty ) {
          // nothing to do
        } else {
          prescompRestart( project )
          if ( hasLogErrorUnits ) {
            logger.info( s"units with errors: ${errorList.mkString( ", " )}" )
          }
        }
      } else {
        // manager self cancel
        Tasker.stopTask( name )
      }
    }

  }

  /**
   * Issue presentation compiler restart request.
   */
  def prescompRestart( project : IScalaProject ) : Unit = {
    project.presentationCompiler.askRestart()
  }

  /**
   * Presentation compiler compilation units with errors.
   */
  def prescompErrorList( project : IScalaProject ) : Set[ String ] = {
    project.presentationCompiler.apply {
      prescompFace : IScalaPresentationCompiler =>
        val prescomImpl = prescompFace.asInstanceOf[ ScalaPresentationCompiler ]
        prescompErrorList( prescomImpl )
    }.getOrElse( Set() )
  }

  /**
   * Presentation compiler compilation units with errors.
   */
  def prescompErrorList( prescomp : ScalaPresentationCompiler ) : Set[ String ] = {
    val values = prescomp.unitOfFile.values
    if ( values.isEmpty ) {
      return Set()
    }
    val result = mutable.SortedSet[ String ]()
    val valuesIterator = values.iterator
    while ( valuesIterator.hasNext ) {
      val compilationUnit = valuesIterator.next
      val problemsIterator = compilationUnit.problems.iterator
      while ( problemsIterator.hasNext ) {
        val compilationProblem = problemsIterator.next
        if ( compilationProblem.severityLevel >= serverityError ) {
          result += compilationUnit.source.file.name
        }
      }
    }
    result.toSet
  }

}
