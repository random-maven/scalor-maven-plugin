package com.carrotgarden.maven.scalor.eclipse

import scala.reflect.ClassTag

import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.m2e.core.project.IMavenProjectFacade

import com.carrotgarden.maven.scalor._

object Project {

  /**
   * Assemble M2E project configurator components.
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

    import Logging._
    import util.Classer._

    /**
     * Provide plugin configuration values.
     */
    def paramsConfig(
      log :     AnyLog,
      facade :  IMavenProjectFacade,
      monitor : IProgressMonitor
    ) : ParamsConfig = {
      /**
       * Evaluate named parameter from this plugin configuration.
       */
      def paramValue( name : String, klaz : Class[ _ ] ) : Object = {
        // M2E expression evaluation needs java type
        val javaType = primitiveWrap( klaz )
        configValue( facade, name, monitor )( ClassTag( javaType ) )
      }
      /**
       * Report named parameter from this plugin configuration.
       */
      def reportValue( name : String, value : Any ) : Unit = {
        log.info( s"   ${name}=${value}" )
      }

      val config = ParamsConfig()
      config.updateParams( paramValue )
      // config.reportParams( reportValue )
      config
    }

  }

}
