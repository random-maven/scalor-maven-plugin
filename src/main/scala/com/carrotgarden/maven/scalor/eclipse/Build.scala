package com.carrotgarden.maven.scalor.eclipse

import org.apache.maven.plugin.MojoExecution
import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.m2e.core.project.configurator.MojoExecutionBuildParticipant
import com.carrotgarden.maven.scalor.util.Logging

/**
 * M2E build support.
 */
object Build {

  /**
   * Project build participant.
   */
  class Participant(
    log :                Logging.AnyLog,
    config :             ParamsConfig,
    execution :          MojoExecution,
    runOnIncremental :   Boolean        = true,
    runOnConfiguration : Boolean        = true
  ) extends MojoExecutionBuildParticipant( execution, runOnIncremental, runOnConfiguration ) {

    import Participant._

    def goal = execution.getGoal

    /**
     * Enablement of executions for build type.
     */
    override def appliesToBuildKind( kind : Int ) : Boolean = {
      import com.carrotgarden.maven.scalor.A.mojo._
      goal match {
        // add sources to project model
        case `register-macro` if ( hasConf( kind ) ) => true
        case `register-main` if ( hasConf( kind ) ) => true
        case `register-test` if ( hasConf( kind ) ) => true
        // generate runtime.js
        case `link-scala-js-main` if ( !hasConf( kind ) ) => true
        case `link-scala-js-test` if ( !hasConf( kind ) ) => true
        // ignore the rest
        case _ => false
      }
    }

    /**
     * Invocations of executions for build type.
     */
    override def build( kind : Int, monitor : IProgressMonitor ) : java.util.Set[ IProject ] = {
      import config._
      if ( eclipseLogBuildParticipant ) {
        log.context( "build" )
        val mode = renderMode( kind )
        val exec = appliesToBuildKind( kind )
        log.info( s"participant=${goal} mode=${mode} exec=${exec}" )
      }
      super.build( kind, monitor )
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

    def hasConf( kind : Int ) = kind == PRECONFIGURE_BUILD
    def hasIncr( kind : Int ) = kind == INCREMENTAL_BUILD || kind == AUTO_BUILD
    def hasFull( kind : Int ) = kind == CLEAN_BUILD || kind == FULL_BUILD

  }

}
