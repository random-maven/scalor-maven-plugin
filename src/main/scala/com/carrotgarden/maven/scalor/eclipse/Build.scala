package com.carrotgarden.maven.scalor.eclipse

import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.m2e.core.project.configurator.MojoExecutionBuildParticipant

/**
 * M2E build support.
 */
object Build {

  /**
   * Project build participant.
   */
  case class Participant(
    context : Config.BuildContext
  ) extends MojoExecutionBuildParticipant( context.execution, true, true )
    with Format
    with Restart
    with Prescomp {
    import Participant._
    import context._
    import com.carrotgarden.maven.scalor.A.mojo._

    /** Current plugin goal. */
    def goal = getMojoExecution.getGoal

    /** Current plugin project. */
    def facade = getMavenProjectFacade

    /**
     * Enablement of Maven executions for Eclipse build type.
     */
    override def appliesToBuildKind( kind : Int ) : Boolean = {
      try {
        goal match {

          // add sources to project, during configuration
          case `register` if ( hasBuildConf( kind ) ) => true
          case `register-macro` if ( hasBuildConf( kind ) ) => true
          case `register-main` if ( hasBuildConf( kind ) ) => true
          case `register-test` if ( hasBuildConf( kind ) ) => true

          // generate runtime.js, during both full and incremental
          case `scala-js-link` if ( hasBuildMake( kind ) ) => true
          case `scala-js-link-main` if ( hasBuildMake( kind ) ) => true
          case `scala-js-link-test` if ( hasBuildMake( kind ) ) => true

          // set format to project, during configuration
          case `eclipse-format` if ( hasBuildConf( kind ) ) => true

          // manage test application, only during full build
          case `eclipse-restart` if ( hasBuildFull( kind ) ) => true

          // manage presentation compiler, only during full build
          case `eclipse-prescomp` if ( hasBuildFull( kind ) ) => true

          // always ignore the rest
          case _ => false
        }
      } catch {
        case error : Throwable =>
          logger.fail( s"Participant check error: ${error.getMessage}", error )
          throw error
      }
    }

    /**
     * Invocations of executions for build type.
     *
     * Each goal can execute in Eclipse or delegate to Maven.
     */
    override def build( kind : Int, monitor : IProgressMonitor ) : java.util.Set[ IProject ] = {
      import context.config._
      try {
        if ( eclipseLogBuildParticipant ) {
          val mode = renderMode( kind )
          val exec = appliesToBuildKind( kind )
          logger.info( s"participant=${goal} mode=${mode} exec=${exec}" )
        }
        goal match {
          // execute in Eclipse
          case `eclipse-format` if appliesToBuildKind( kind ) =>
            formatEnsure( context, monitor )
            null
          case `eclipse-restart` if appliesToBuildKind( kind ) =>
            restartEnsure( context, monitor )
            null
          case `eclipse-prescomp` if appliesToBuildKind( kind ) =>
            prescompEnsure( context, monitor )
            null
          // delegate to Maven
          case _ =>
            super.build( kind, monitor )
        }
      } catch {
        case error : Throwable =>
          logger.fail( s"Participant build error: ${error.getMessage}", error )
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
