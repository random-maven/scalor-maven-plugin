package com.carrotgarden.maven.scalor.eclipse

import scala.reflect.ClassTag

import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.m2e.core.project.IMavenProjectFacade

import com.carrotgarden.maven.scalor.util.Classer.primitiveWrap
import com.carrotgarden.maven.scalor.util.Logging

/**
 * Extract plugin configuration parameters.
 */
trait Config {

  self : Monitor with Maven with Logging =>

  /**
   * Extract plugin configuration parameters.
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

    /**
     * Evaluate named parameter from this plugin configuration.
     */
    def paramValue( name : String, klaz : Class[ _ ] ) : Object = {
      subValue.split( 1 )
      // M2E expression evaluation needs java type
      val javaType = primitiveWrap( klaz )
      configValue( facade, name, monitor )( ClassTag( javaType ) )
    }

    config.update( paramValue )

    if ( config.eclipseLogParamsConfig ) {
      log.info( s"Eclipse companion plugin parameters:" )
      config.reportParams( reportValue )
    } else {
      subReport.split( config.paramsCount )
    }

    config
  }

}
