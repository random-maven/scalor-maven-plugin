package com.carrotgarden.maven.scalor.eclipse

import scala.reflect.ClassTag

import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.m2e.core.project.IMavenProjectFacade

import com.carrotgarden.maven.scalor._
import org.apache.maven.plugin.MojoExecution
import org.eclipse.m2e.core.lifecyclemapping.model.IPluginExecutionMetadata
import org.eclipse.m2e.core.project.configurator.MojoExecutionBuildParticipant
import org.eclipse.core.resources.IProject
import org.eclipse.m2e.core.project.configurator.AbstractBuildParticipant
import org.eclipse.m2e.core.project.configurator.AbstractBuildParticipant2
import org.eclipse.core.runtime.SubMonitor

/**
 * Assemble M2E project configurator components.
 */
object Project {

  import Logging._

  /**
   * Project configurator.
   */
  trait Configurator extends Base.Conf
    with Comment
    with Entry
    with Logging
    with Maven
    with Monitor
    with Nature
    with Order
    with Props
    with Version
    with MavenM2E
    with ScalaIDE {

    import util.Classer._

    /**
     * Cache shared values during project configurator life cycle.
     */
    lazy val cached = meta.Cached()

    /**
     * Extract plugin configuration values.
     */
    def paramsConfig(
      facade :  IMavenProjectFacade,
      monitor : IProgressMonitor
    ) : ParamsConfig = {
      val config = ParamsConfig()
      val subMon = monitor.toSub
      val subValue = subMon.split( 50 ).setWorkRemaining( config.paramsCount )
      val subReport = subMon.split( 50 ).setWorkRemaining( config.paramsCount )
      /**
       * Evaluate named parameter from this plugin configuration.
       */
      def paramValue( name : String, klaz : Class[ _ ] ) : Object = {
        subValue.split( 1 )
        // M2E expression evaluation needs java type
        val javaType = primitiveWrap( klaz )
        configValue( facade, name, monitor )( ClassTag( javaType ) )
      }
      /**
       * Report named parameter from this plugin configuration.
       */
      def reportValue( name : String, value : Any ) : Unit = {
        subReport.split( 1 )
        val text = if ( value.isInstanceOf[ Array[ _ ] ] ) {
          value.asInstanceOf[ Array[ _ ] ].mkString( "[ ", " , ", " ]" )
        } else {
          value
        }
        log.info( s"   ${name}=${text}" )
      }
      config.updateParams( paramValue )
      if ( config.eclipseLogParamsConfig ) {
        log.info( s"Eclipse companion plugin parameters:" )
        config.reportParams( reportValue )
      } else {
        subReport.split( config.paramsCount )
      }
      config
    }

    /**
     * Provide M2E build participant for mojo execution.
     */
    override def getBuildParticipant(
      facade :    IMavenProjectFacade,
      execution : MojoExecution,
      metadata :  IPluginExecutionMetadata
    ) : AbstractBuildParticipant = {
      // NullProgressMonitor
      val subMon = SubMonitor.convert( null )
      // Already cached by other steps.
      val config = cached( paramsConfig( facade, subMon ) )
      new Participant( log, config, execution )
    }

  }

  import AbstractBuildParticipant._
  import AbstractBuildParticipant2._

  /**
   * Render build mode as 'kind/type'
   * kind - recognized by M2E (5 kinds, see AbstractBuildParticipant)
   * type - recognized by Maven Build Context (2 types: full vs incr)
   */
  def renderMode( kind : Int ) : String = {
    kind match {
      // incremental: skip
      case PRECONFIGURE_BUILD => "conf/incr" // empty delta
      // incremental: exec
      case INCREMENTAL_BUILD  => "incr/incr" // valid delta
      case AUTO_BUILD         => "auto/incr" // valid delta
      // full build: exec
      case CLEAN_BUILD        => "clea/full" // ignore delta
      case FULL_BUILD         => "full/full" // ignore delta
      // unknown: skip
      case _                  => "none/none" // ignore delta
    }
  }

  def hasConf( kind : Int ) = kind == PRECONFIGURE_BUILD
  def hasIncr( kind : Int ) = kind == INCREMENTAL_BUILD || kind == AUTO_BUILD
  def hasFull( kind : Int ) = kind == CLEAN_BUILD || kind == FULL_BUILD

  /**
   * Project build participant.
   */
  class Participant(
    log :                AnyLog,
    config :             ParamsConfig,
    execution :          MojoExecution,
    runOnIncremental :   Boolean       = true,
    runOnConfiguration : Boolean       = true
  ) extends MojoExecutionBuildParticipant( execution, runOnIncremental, runOnConfiguration ) {

    import config._
    import A.mojo._

    def goal = execution.getGoal

    /**
     * Enablement of executions for build type.
     */
    override def appliesToBuildKind( kind : Int ) : Boolean = {
      goal match {
        // add sources to project model
        case `register-macro` if ( hasConf( kind ) ) => true
        case `register-main` if ( hasConf( kind ) ) => true
        case `register-test` if ( hasConf( kind ) ) => true
        // generate runtime.js
        case `link-scala-js-main` if ( !hasConf( kind ) ) => true
        case `link-scala-js-test` if ( !hasConf( kind ) ) => true
        // ignore rest
        case _ => false
      }
    }

    /**
     * Invocations of executions for build type.
     */
    override def build( kind : Int, monitor : IProgressMonitor ) : java.util.Set[ IProject ] = {
      if ( eclipseLogBuildParticipant ) {
        log.context( "build" )
        val mode = renderMode( kind )
        val exec = appliesToBuildKind( kind )
        log.info( s"participant=${goal} mode=${mode} exec=${exec}" )
      }
      super.build( kind, monitor )
    }

  }

}
