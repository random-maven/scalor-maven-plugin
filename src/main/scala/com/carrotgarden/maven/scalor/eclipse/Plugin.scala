package com.carrotgarden.maven.scalor.eclipse

import java.util.Properties

import scala.collection.concurrent.TrieMap

import org.eclipse.core.internal.registry.ExtensionRegistry
import org.eclipse.core.runtime
import org.eclipse.core.runtime.ContributorFactoryOSGi
import org.eclipse.core.runtime.CoreException
import org.eclipse.core.runtime.IStatus
import org.eclipse.core.runtime.Platform
import org.eclipse.core.runtime.Status
import org.slf4j.LoggerFactory

import com.carrotgarden.maven.scalor.A
import com.carrotgarden.maven.scalor.util
import com.carrotgarden.maven.scalor.util.Logging
import com.carrotgarden.maven.scalor.base.Self

object Plugin {

  import com.carrotgarden.maven.scalor.util.Logging._

  val loggerMemento = TrieMap[ String, AnyLog ]()

  trait Log extends AnyLog {

    override val founder = this

    override val context = A.eclipse.name

    override def branch( context : String ) : AnyLog = {
      loggerMemento.getOrElseUpdate( context, ContextLogger( founder, context ) )
    }

  }

  /**
   * Eclipse companion plugin installed by this Maven plugin.
   */
  trait Activator extends runtime.Plugin
    with Tracker
    with Logging {

    import Plugin._
    import Self._
    import Wiring._
    import com.carrotgarden.maven.scalor.util.Props._

    /**
     * Eclipse plugin self-descriptor.
     */
    lazy val properties : Properties = {
      propertiesFrom( getBundle.getEntry( Eclipse.pluginProps ) )
    }

    /**
     * Eclipse plugin self-descriptor.
     */
    def property( key : String ) = properties.getProperty( key )

    lazy val pluginId = property( Key.pluginId )
    lazy val mavenGroupId = property( Key.mavenGroupId )
    lazy val mavenArtifactId = property( Key.mavenArtifactId )
    lazy val mavenVersion = property( Key.mavenVersion )
    lazy val eclipseDecorator = property( Key.eclipseDecorator )
    lazy val projectConfigurator = property( Key.projectConfigurator )

    lazy val logId = mavenArtifactId + "-" + mavenVersion

    /**
     * Log to M2E "Maven Console".
     */
    object slf4jLog extends Plugin.Log {
      private lazy val logger = LoggerFactory.getLogger( logId );
      override def dbug( line : String ) = logger.debug( text( line ) )
      override def info( line : String ) = logger.info( text( line ) )
      override def warn( line : String ) = logger.warn( text( line ) )
      override def fail( line : String ) = logger.error( text( line ) )
      override def fail( line : String, error : Throwable ) = logger.error( text( line ), error )
    }

    /**
     * Log to Eclipse "Error Log".
     */
    object eclipseLog extends Plugin.Log {
      import org.eclipse.core.runtime.IStatus._
      override def dbug( line : String ) = getLog.log( new Status( OK, logId, text( line ) ) )
      override def info( line : String ) = getLog.log( new Status( INFO, logId, text( line ) ) )
      override def warn( line : String ) = getLog.log( new Status( WARNING, logId, text( line ) ) )
      override def fail( line : String ) = getLog.log( new Status( ERROR, logId, text( line ) ) )
      override def fail( line : String, error : Throwable ) = getLog.log( new Status( ERROR, logId, text( line ), error ) )
    }

    /**
     * Detect optional M2E logging plugins.
     */
    def hasLogback = {
      // M2E provides logback appender which prints to "Maven Console"
      val hasAppender = discoverBundleFrom( getBundle, M2E.logback.appender ).isDefined
      // M2E logback appender must be configured by this configuration bundle
      val hasConfiguration = discoverBundleFrom( getBundle, M2E.logback.configuration ).isDefined
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
      val pluginInput = Eclipse.pluginXmlURL.openStream
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
      pluginInput.close()
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

}
