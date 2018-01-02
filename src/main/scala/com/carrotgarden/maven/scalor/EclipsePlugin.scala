package com.carrotgarden.maven.scalor

import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.jdt.launching.JavaRuntime
import org.eclipse.jface.viewers.IDecoration
import org.eclipse.jface.viewers.ILightweightLabelDecorator
import org.eclipse.jface.viewers.LabelProvider
import org.eclipse.m2e.core.project.IMavenProjectFacade
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest
import org.eclipse.m2e.jdt.IClasspathDescriptor
import org.eclipse.m2e.jdt.IJavaProjectConfigurator
import org.osgi.framework.BundleContext
import org.scalaide.core.SdtConstants

import com.carrotgarden.maven.tools.Description

/**
 * Eclipse companion plugin installed by this Maven plugin.
 */
object EclipsePlugin {

  @volatile private var instance : Activator = _

  /**
   * Expose plugin instance.
   */
  def apply() : Activator = instance

  /**
   * Eclipse companion plugin installed by this Maven plugin.
   */
  class Activator extends eclipse.Plugin.Activator {

    /**
     * Eclipse plugin lifecycle.
     */
    override def start( context : BundleContext ) = {
      EclipsePlugin.instance = this
      super.start( context )
      log.info( "Plugin start: " + pluginId )
      ensureContribution
    }

    /**
     * Eclipse plugin lifecycle.
     */
    override def stop( context : BundleContext ) = {
      log.info( "Plugin stop:  " + pluginId )
      unensureContribution
      super.stop( context )
      EclipsePlugin.instance = null
    }

  }

  /**
   * M2E project configurator contributed by this Eclipse plugin.
   */
  @Description( """
  Keep in sync with src/main/resources/plugin.xml.
  """ )
  class Configurator
    extends eclipse.Project.Configurator
    with IJavaProjectConfigurator {

    val plugin = EclipsePlugin()

    override val log = plugin.log

    /**
     * M2E configuration step #1.
     * Provide internal content of the class path container:
     * .classpath!<classpathentry kind="con" path="org.eclipse.m2e.MAVEN2_CLASSPATH_CONTAINER">
     */
    /**
     * Configures Maven project classpath,
     * i.e. content of Maven Dependencies classpath container.
     */
    override def configureClasspath(
      facade :    IMavenProjectFacade,
      classpath : IClasspathDescriptor,
      monitor :   IProgressMonitor
    ) : Unit = plugin.tryCore( "Scalor/M2E step #1." ) {
      log.info( s"Configuring dependency classpath." )
      assertCancel( monitor )
      val config = paramsConfig( log, facade, monitor )
      assertVersion( config, monitor )
      ensureOrderMaven( facade, config, classpath, monitor )
    }

    /**
     * M2E configuration step #2.
     * Provide top level class path entries, i.e:
     * .classpath!<classpathentry kind="src" output="target/classes" path="src/main/java">
     * .classpath!<classpathentry kind="con" path="org.scala-ide.sdt.launching.SCALA_CONTAINER"/>
     */
    /**
     * Configures JDT project classpath,
     * i.e. project-level entries like source folders,
     * JRE and Maven Dependencies classpath container.
     */
    override def configureRawClasspath(
      request :   ProjectConfigurationRequest,
      classpath : IClasspathDescriptor,
      monitor :   IProgressMonitor
    ) : Unit = plugin.tryCore( "Scalor/M2E step #2." ) {
      log.info( s"Configuring project classpath." )
      assertCancel( monitor )
      val config = paramsConfig( log, request.getMavenProjectFacade, monitor )
      assertVersion( config, monitor )
      ensureRoots( request, classpath, monitor )

      val javaContainerId = JavaRuntime.JRE_CONTAINER
      val scalaContainerId = SdtConstants.ScalaLibContId

      // FIXME id to config
      ensureContainer( request, config, classpath, scalaContainerId, monitor )
      ensureOrderTopLevel( request, config, classpath, monitor )
    }

    /**
     * M2E configuration step #3.
     * Provide content for:
     * .project!<nature>
     * .project!<comment>
     * .project!<buildSpec>
     * .settings/org.scala-ide.sdt.core.prefs
     */
    /**
     * Configures Eclipse project passed in ProjectConfigurationRequest,
     * using information from Maven project and other configuration request parameters.
     */
    override def configure(
      request : ProjectConfigurationRequest,
      monitor : IProgressMonitor
    ) : Unit = plugin.tryCore( "Scalor/M2E step #3." ) {
      log.info( s"Configuring project settings." )
      assertCancel( monitor )
      val config = paramsConfig( log, request.getMavenProjectFacade, monitor )
      assertVersion( config, monitor )
      ensureNature( request, config, monitor )
      ensureComment( request, config, monitor )
      reorderBuilder( request, config, monitor )
      configureScalaIDE( request, config, monitor )
      // TODO project refresh
    }

  }

  /**
   * Provide Eclipse UI decorations.
   */
  @Description( """
  Keep in sync with src/main/resources/plugin.xml.
  """ )
  class Decorator extends LabelProvider
    with ILightweightLabelDecorator
    with eclipse.Decor {

    import org.eclipse.jdt.internal.ui.packageview.ClassPathContainer

    type Process[ T ] = ( T, IDecoration ) => Unit

    val processMap = Map[ Class[ _ ], Process[ _ ] ](
      classOf[ ClassPathContainer ] -> processContainer _
    )

    def hasContext = false // TODO

    override def decorate( element : Object, decoration : IDecoration ) : Unit = {
      if ( hasContext ) {
        processMap.get( element.getClass ).map { process =>
          process.asInstanceOf[ Process[ Object ] ]( element, decoration )
        }
      }
    }

  }

}
