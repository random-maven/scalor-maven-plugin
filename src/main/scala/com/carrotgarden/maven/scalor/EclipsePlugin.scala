package com.carrotgarden.maven.scalor

import com.carrotgarden.maven.tools.Description

import org.apache.maven.plugin.MojoExecution

import org.eclipse.core.runtime.SubMonitor
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.jface.viewers.IDecoration
import org.eclipse.jface.viewers.ILightweightLabelDecorator
import org.eclipse.jface.viewers.LabelProvider
import org.eclipse.m2e.core.internal.embedder.IMavenComponentContributor
import org.eclipse.m2e.core.lifecyclemapping.model.IPluginExecutionMetadata
import org.eclipse.m2e.core.project.configurator.AbstractBuildParticipant
import org.eclipse.m2e.core.project.configurator.AbstractProjectConfigurator
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest
import org.eclipse.m2e.core.project.configurator.AbstractCustomizableLifecycleMapping
import org.eclipse.m2e.core.project.IMavenProjectFacade
import org.eclipse.m2e.jdt.IClasspathDescriptor
import org.eclipse.m2e.jdt.IJavaProjectConfigurator
import org.eclipse.m2e.jdt.IClasspathManager

import org.osgi.framework.BundleContext

/**
 * Eclipse companion plugin installed by this Maven plugin.
 */
@Description( """
Keep in sync with src/main/resources/plugin.xml.
""" )
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
      log.info( s"Plugin start: ${pluginId}" )
      ensureContribution
    }

    /**
     * Eclipse plugin lifecycle.
     */
    override def stop( context : BundleContext ) = {
      log.info( s"Plugin stop:  ${pluginId}" )
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
  Use lazy vals to work around multiple instantiation (note/issue-m2e.md).
  """ )
  class Configurator
    extends eclipse.Project.Configurator
    with IJavaProjectConfigurator {

    lazy val plugin = EclipsePlugin()

    override lazy val log = plugin.log

    /**
     * Cache values during configurator life cycle.
     */
    lazy val cached = meta.Cached()

    /**
     * M2E build participant implements Maven goal executions.
     */
    override def getBuildParticipant(
      facade :    IMavenProjectFacade,
      execution : MojoExecution,
      metadata :  IPluginExecutionMetadata
    ) : AbstractBuildParticipant = plugin.tryCore( "Scalor build." ) {
      val logger = log.branch( "build" )
      def hasGoal( goal : String ) = execution.getGoal == goal
      val subMon = SubMonitor.convert( null )
      val config = cached( paramsConfig( logger, facade, subMon ) )
      val restart = if ( hasGoal( A.mojo.`eclipse-restart` ) )
        Some( cached( paramsRestart( logger, facade, subMon ) ) ) else None
      val prescomp = if ( hasGoal( A.mojo.`eclipse-prescomp` ) )
        Some( cached( paramsPrescomp( logger, facade, subMon ) ) ) else None
      val context = eclipse.Config.BuildContext(
        logger    = logger,
        config    = config,
        restart   = restart,
        prescomp  = prescomp,
        facade    = facade,
        execution = execution
      )
      eclipse.Build.Participant( context )
    }

    /**
     * M2E configuration step #1.
     * Provide internal content of the class path container:
     * .classpath!<classpathentry kind="con" path="org.eclipse.m2e.MAVEN2_CLASSPATH_CONTAINER">
     *
     * Configures Maven project classpath,
     * i.e. content of Maven Dependencies classpath container.
     */
    override def configureClasspath(
      facade :    IMavenProjectFacade,
      classpath : IClasspathDescriptor,
      monitor :   IProgressMonitor
    ) : Unit = plugin.tryCore( "Scalor step#1." ) {
      val logger = log.branch( "config-step#1" )
      logger.info( s"Configuring container ${IClasspathManager.CONTAINER_ID}." )
      val subMon = monitor.toSub
      val config = cached( paramsConfig( logger, facade, subMon.split( 10 ) ) )
      val assert = cached( assertVersion( logger, config, subMon.split( 10 ) ) )
      val context = eclipse.Config.SetupContext(
        logger    = logger,
        config    = config,
        facade    = facade,
        classpath = Some( classpath )
      )
      ensureOrderMaven( context, subMon.split( 80 ) )
      reportClassPath( context, false, subMon.split( 10 ) )
    }

    /**
     * M2E configuration step #2.
     * Provide top level class path entries, i.e:
     * .classpath!<classpathentry kind="src" output="target/classes" path="src/main/java">
     * .classpath!<classpathentry kind="con" path="org.scala-ide.sdt.launching.SCALA_CONTAINER"/>
     *
     * Configures JDT project classpath,
     * i.e. project-level entries like source folders,
     * JRE and Maven Dependencies classpath container.
     */
    override def configureRawClasspath(
      request :   ProjectConfigurationRequest,
      classpath : IClasspathDescriptor,
      monitor :   IProgressMonitor
    ) : Unit = plugin.tryCore( "Scalor step#2." ) {
      val logger = log.branch( "config-step#2" )
      logger.info( s"Configuring project classpath." )
      val subMon = monitor.toSub
      val facade = request.getMavenProjectFacade
      val config = cached( paramsConfig( logger, facade, subMon.split( 10 ) ) )
      val assert = cached( assertVersion( logger, config, subMon.split( 10 ) ) )
      val context = eclipse.Config.SetupContext(
        logger    = logger,
        config    = config,
        facade    = facade,
        request   = Some( request ),
        classpath = Some( classpath )
      )
      ensureSourceRoots( context, subMon.split( 20 ) )
      ensureScalaLibrary( context, subMon.split( 20 ) )
      ensureOrderTopLevel( context, subMon.split( 20 ) )
      reportClassPath( context, true, subMon.split( 10 ) )
    }

    /**
     * M2E configuration step #3.
     * Provide content for:
     * .project!<nature>
     * .project!<comment>
     * .project!<buildSpec>
     * .settings/org.scala-ide.sdt.core.prefs
     *
     * Configures Eclipse project passed in ProjectConfigurationRequest,
     * using information from Maven project and other configuration request parameters.
     */
    override def configure(
      request : ProjectConfigurationRequest,
      monitor : IProgressMonitor
    ) : Unit = plugin.tryCore( "Scalor step#3." ) {
      val logger = log.branch( "config-step#3" )
      logger.info( s"Configuring project settings." )
      val subMon = monitor.toSub
      val facade = request.getMavenProjectFacade
      val project = request.getProject
      val config = cached( paramsConfig( logger, facade, subMon.split( 10 ) ) )
      val assert = cached( assertVersion( logger, config, subMon.split( 10 ) ) )
      val context = eclipse.Config.SetupContext(
        logger  = logger,
        config  = config,
        facade  = facade,
        request = Some( request )
      )
      plugin.projectRegister( project, config );
      hackSymbolicLinks( context, subMon.split( 10 ) )
      ensureProjectComment( context, subMon.split( 10 ) )
      ensureProjectNature( context, subMon.split( 10 ) )
      ensureOrderBuilder( context, subMon.split( 10 ) )
      ensureOrderNature( context, subMon.split( 10 ) )
      configureScalaIDE( context, subMon.split( 10 ) )
    }

  }

  /**
   * Eclipse UI decorator contributed by this Eclipse plugin.
   */
  @Description( """
  TODO
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

    def hasContext = false

    override def decorate( element : Object, decoration : IDecoration ) : Unit = {
      if ( hasContext ) {
        processMap.get( element.getClass ).map { process =>
          process.asInstanceOf[ Process[ Object ] ]( element, decoration )
        }
      }
    }

  }

  /**
   * Eclipse M2E lifecycle mapping contributed by this Eclipse plugin.
   */
  @Description( """
  TODO
  Keep in sync with src/main/resources/plugin.xml.
  """ )
  class LifecycleMapping extends AbstractCustomizableLifecycleMapping {

  }

  /**
   * Contribute this plugin components into M2E runtime environment.
   */
  @Description( """
  TODO
  FIXME not activated on dynamic bundle install
  Keep in sync with src/main/resources/plugin.xml.
  """ )
  class Injector extends IMavenComponentContributor {
    import IMavenComponentContributor.IMavenComponentBinder
    import extend.Lifecycle
    lazy val plugin = EclipsePlugin()
    lazy val log = plugin.log
    override def contribute( binder : IMavenComponentBinder ) : Unit = {
      // log.context( "inject" )
      log.info( s"Provide lifecycle extension ${Lifecycle.Hint}" )
      binder.bind( Lifecycle.Role, Lifecycle.Impl, Lifecycle.Hint )
    }
  }

}
