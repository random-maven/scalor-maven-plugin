package com.carrotgarden.maven.scalor.eclipse

import java.util.Properties

import org.eclipse.core.internal.registry.ExtensionRegistry
import org.eclipse.core.runtime
import org.eclipse.core.runtime.ContributorFactoryOSGi
import org.eclipse.core.runtime.CoreException
import org.eclipse.core.runtime.IStatus
import org.eclipse.core.runtime.Platform
import org.eclipse.core.runtime.Status
import org.slf4j.LoggerFactory

import com.carrotgarden.maven.scalor.util
import com.carrotgarden.maven.scalor.util.Logging
import com.carrotgarden.maven.scalor.A

object Plugin {

  import Logging._

  trait Log extends AnyLog {
    /**  Work around lack of logging source in M2E "Maven Console". */
    override def context( value : String ) = { CTX.value = s"[${A.eclipse.name}:${value}] " }
    context( A.mojo.`eclipse-config` )
  }

  /**
   * Eclipse companion plugin installed by this Maven plugin.
   */
  trait Activator extends runtime.Plugin
    with Project.Tracker
    with Logging {

    import Plugin._
    import Plugin.Config._
    import com.carrotgarden.maven.scalor.util.OSGI._
    import com.carrotgarden.maven.scalor.util.Props._

    /**
     * Eclipse plugin self-descriptor.
     */
    lazy val properties : Properties = {
      propertiesFrom( getBundle.getEntry( plugin.properties ) )
    }

    /**
     * Eclipse plugin self-descriptor.
     */
    def property( key : String ) = properties.getProperty( key )

    lazy val pluginId = property( key.pluginId )
    lazy val mavenGroupId = property( key.mavenGroupId )
    lazy val mavenArtifactId = property( key.mavenArtifactId )
    lazy val mavenVersion = property( key.mavenVersion )
    lazy val eclipseDecorator = property( key.eclipseDecorator )
    lazy val projectConfigurator = property( key.projectConfigurator )

    lazy val logId = mavenArtifactId + "-" + mavenVersion

    /**
     * Log to M2E "Maven Console".
     */
    object slf4jLog extends Plugin.Log {
      private lazy val logger = LoggerFactory.getLogger( logId );
      override def dbug( line : String ) = logger.debug( context() + line )
      override def info( line : String ) = logger.info( context() + line )
      override def warn( line : String ) = logger.warn( context() + line )
      override def fail( line : String ) = logger.error( context() + line )
      override def fail( line : String, error : Throwable ) = logger.error( context() + line, error )
    }

    /**
     * Log to Eclipse "Error Log".
     */
    object eclipseLog extends Plugin.Log {
      import org.eclipse.core.runtime.IStatus._
      override def dbug( line : String ) = getLog.log( new Status( OK, logId, context() + line ) )
      override def info( line : String ) = getLog.log( new Status( INFO, logId, context() + line ) )
      override def warn( line : String ) = getLog.log( new Status( WARNING, logId, context() + line ) )
      override def fail( line : String ) = getLog.log( new Status( ERROR, logId, context() + line ) )
      override def fail( line : String, error : Throwable ) = getLog.log( new Status( ERROR, logId, context() + line, error ) )
    }

    /**
     * Detect optional M2E logging plugins.
     */
    def hasLogback = {
      // M2E provides logback appender which prints to "Maven Console"
      val hasAppender = discoverBundle( getBundle, m2e.logback.appender ).isDefined
      // M2E logback appender must be configured by this configuration bundle
      val hasConfiguration = discoverBundle( getBundle, m2e.logback.configuration ).isDefined
      // Multiple alternative Slf4J implementation bundles can make M2E appender inoperable
      val hasImplementation = LoggerFactory.getILoggerFactory.getClass.getName.startsWith( "ch.qos.logback" )
      //
      hasAppender && hasConfiguration && hasImplementation
    }

    /**
     * Bind plugin logger to detected destination.
     */
    override lazy val log : AnyLog =
      if ( hasLogback ) {
        eclipseLog.info( "Plugin logger is using M2E [Maven Console]." )
        slf4jLog
      } else {
        eclipseLog.info( "Plugin logger is using Eclipse [Error Log]." )
        eclipseLog
      }

    /**
     * Provides extension points from plugin.xml.
     */
    lazy val contributor = ContributorFactoryOSGi.createContributor( getBundle )

    // FIXME duplicate detection
    def ensureContribution = {
      //    registerContribution
    }

    // FIXME duplicate detection
    def unensureContribution = {
      //    unregisterContribution
    }

    /**
     * Activate extension points from plugin.xml.
     */
    def registerContribution = {
      log.info( "Registering contribution." )
      val registry = Platform.getExtensionRegistry.asInstanceOf[ ExtensionRegistry ]
      val pluginInput = pluginXmlUrl.openStream
      val persist = false
      val name = null
      val translator = null
      val token = registry.getTemporaryUserToken
      val hasReg = registry.addContribution( pluginInput, contributor, persist, name, translator, token )
      if ( hasReg ) {
        log.info( "Contribution registraton success." )
      } else {
        log.fail( "Contribution registraton failure." )
      }
    }

    /**
     * Deactivate extension points from plugin.xml.
     */
    def unregisterContribution = {
      val registry = Platform.getExtensionRegistry.asInstanceOf[ ExtensionRegistry ]
      val token = registry.getTemporaryUserToken
      registry.removeContributor( contributor, token )
    }

    /**
     * Common execution wrapper with error reporting.
     */
    def tryCore[ T ]( name : String )( block : => T ) : T = try block catch {
      case error : Throwable =>
        val text = s"${name} ${error.getMessage}"
        val line = util.Text.preview( text, 80 )
        log.fail( line, error )
        throw new CoreException( new Status( IStatus.ERROR, logId, name, error ) )
    }

  }
  /**
   * Shared Maven/Eclipse settings.
   *
   * Ensure not loading OSGI classes here.
   */
  object Config extends util.JarRes {

    /**
     * Eclipse plugin self-descriptor files names.
     */
    object plugin {
      val xml = "plugin.xml"
      val properties = "plugin.properties"
    }

    /**
     * Eclipse 'plugin.properties' keys names.
     */
    object key {
      val pluginId = "pluginId"
      val mavenGroupId = "mavenGroupId"
      val mavenArtifactId = "mavenArtifactId"
      val mavenVersion = "mavenVersion"
      val eclipseDecorator = "eclipseDecorator"
      val projectConfigurator = "projectConfigurator"
    }

    /**
     * Known M2E plugin id.
     */
    object m2e {
      object logback {
        val appender = "org.eclipse.m2e.logback.appender"
        val configuration = "org.eclipse.m2e.logback.configuration"
      }
    }

    /**
     * Configuration "plugin.properties" resource in the jar file.
     */
    def pluginPropertiesUrl = resourceURL( plugin.properties )

    /**
     * Configuration "plugin.xml" resource in the plugin jar file.
     */
    def pluginXmlUrl = resourceURL( plugin.xml )

  }

}
