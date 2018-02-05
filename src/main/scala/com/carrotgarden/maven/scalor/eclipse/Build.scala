package com.carrotgarden.maven.scalor.eclipse

import org.apache.maven.plugin.MojoExecution
import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.m2e.core.project.configurator.MojoExecutionBuildParticipant
import com.carrotgarden.maven.scalor.util.Logging
import org.eclipse.core.runtime.CoreException
import org.eclipse.core.runtime.Status
import org.eclipse.core.runtime.IStatus
import org.eclipse.m2e.core.project.IMavenProjectFacade

/**
 * M2E build support.
 */
object Build {

  /**
   * Project build participant.
   */
  case class Participant(
    log :                Logging.AnyLog,
    context :            Config.Context,
    execution :          MojoExecution,
    runOnIncremental :   Boolean        = true,
    runOnConfiguration : Boolean        = true
  ) extends MojoExecutionBuildParticipant( execution, runOnIncremental, runOnConfiguration )
    with Restart {

    import Participant._
    import com.carrotgarden.maven.scalor.A.mojo._

    /** Current plugin goal. */
    def goal = execution.getGoal

    /** Current plugin project. */
     def facade = getMavenProjectFacade

    /**
     * Enablement of Maven executions for Eclispe build type.
     */
    override def appliesToBuildKind( kind : Int ) : Boolean = {
      try {
        goal match {
          // add sources to project, during configuration
          case `register-macro` if ( hasBuildConf( kind ) ) => true
          case `register-main` if ( hasBuildConf( kind ) ) => true
          case `register-test` if ( hasBuildConf( kind ) ) => true
          // generate runtime.js, during both full and incremental
          case `scala-js-link-main` if ( hasBuildMake( kind ) ) => true
          case `scala-js-link-test` if ( hasBuildMake( kind ) ) => true
          // configure test application, only during full build
          case `eclipse-restart` if ( hasBuildFull( kind ) ) => true
          // ignore the rest
          case _ => false
        }
      } catch {
        case error : Throwable =>
          log.fail( s"Participant check error", error )
          throw error
      }
    }

    /**
     * Invocations of executions for build type.
     */
    override def build( kind : Int, monitor : IProgressMonitor ) : java.util.Set[ IProject ] = {
      import context.config._
      try {
        if ( eclipseLogBuildParticipant ) {
          log.context( "build" )
          val mode = renderMode( kind )
          val exec = appliesToBuildKind( kind )
          log.info( s"participant=${goal} mode=${mode} exec=${exec}" )
        }
        // execute in Eclipse or delegate to Maven
        goal match {
          case `eclipse-restart` =>
            val restart = context.restart.get
            restartEnsure( kind, monitor, log, restart, facade, execution )
            null
          case _ =>
            // delegate to Maven mojo
            super.build( kind, monitor )
        }
      } catch {
        case error : Throwable =>
          log.fail( s"Participant build error", error )
          throw error
      }
    }

  }

  /**
   * Participant support.
   */
  object Participant {

    import org.eclipse.m2e.core.project.configurator.AbstractBuildParticipant._
    import org.eclipse.m2e.core.project.configurator.AbstractBuildParticipant2._

    /**
     * Render build mode as 'kind/type'
     * kind - recognized by M2E (5 kinds, see AbstractBuildParticipant)
     * type - recognized by Maven Build Context (2 types: full vs incr)
     */
    def renderMode( kind : Int ) : String = {
      kind match {
        // incremental
        case PRECONFIGURE_BUILD => "conf/incr" // empty delta
        // incremental
        case INCREMENTAL_BUILD  => "incr/incr" // valid delta
        case AUTO_BUILD         => "auto/incr" // valid delta
        // full build
        case CLEAN_BUILD        => "clea/full" // ignore delta
        case FULL_BUILD         => "full/full" // ignore delta
        // unknown
        case _                  => "none/none" // ignore delta
      }
    }

    /** Build type = configuration. */
    def hasBuildConf( kind : Int ) = ( kind == PRECONFIGURE_BUILD )

    /** Build type = incremental. */
    def hasBuildIncr( kind : Int ) = ( kind == INCREMENTAL_BUILD ) || ( kind == AUTO_BUILD )

    /** Build type = full. */
    def hasBuildFull( kind : Int ) = ( kind == CLEAN_BUILD ) || ( kind == FULL_BUILD )

    /** Build type = full or type = incremental. */
    def hasBuildMake( kind : Int ) = hasBuildFull( kind ) || hasBuildIncr( kind )

  }

}
