package com.carrotgarden.maven.scalor

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.CoreException
import org.eclipse.core.runtime.IStatus
import org.eclipse.core.runtime.Status
import org.eclipse.core.runtime.Platform
import org.eclipse.core.runtime.ContributorFactoryOSGi
import org.eclipse.core.internal.registry.ExtensionRegistry

import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest
import org.eclipse.m2e.core.project.IMavenProjectFacade

import org.eclipse.m2e.jdt.AbstractSourcesGenerationProjectConfigurator
import org.eclipse.m2e.jdt.IClasspathDescriptor

import org.slf4j.LoggerFactory

import java.util.Properties
import java.net.URL

import scala.util.Success
import scala.util.Failure
import scala.collection.JavaConverters._

import util.Error._
import util.Props._
import util.OSGI._

import Eclipse._
import EclipsePlugin._
import EclipseConfigurator._
import org.apache.maven.plugin.MojoExecution
import java.io.File
import org.apache.maven.project.MavenProject
import scala.reflect.ClassTag
import org.eclipse.jdt.core.IClasspathAttribute
import org.eclipse.core.runtime.OperationCanceledException
import org.eclipse.m2e.core.project.MavenProjectUtils
import java.util.Comparator
import org.eclipse.m2e.jdt.IClasspathEntryDescriptor
import org.eclipse.jdt.core.IClasspathEntry

import org.scalaide.core.internal.project.{ Nature => ScalaNature }
import org.eclipse.core.resources.IProject
import scala.collection.mutable.ArrayBuffer
import org.scalaide.core.SdtConstants
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.launching.JavaRuntime

/**
 * Shared maven/eclipse features.
 * Ensure not loading osgi dependences here.
 */
object Eclipse extends util.JarRes {

  /**
   * Eclipse plugin self-descriptor files names.
   */
  object plugin {
    val xml = "plugin.xml"
    val properties = "plugin.properties"
  }

  /**
   * Eclipse plugin.properties keys names.
   */
  object key {
    val pluginId = "pluginId"
    val mavenGroupId = "mavenGroupId"
    val mavenArtifactId = "mavenArtifactId"
    val mavenVersion = "mavenVersion"
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
   * Configuration plugin.properties resource in the jar file.
   */
  def pluginPropertiesUrl = resourceURL( plugin.properties )

  /**
   * Configuration plugin.xml resource in the jar file.
   */
  def pluginXmlUrl = resourceURL( plugin.xml )

}

/**
 * Plugin singleton holder.
 */
object EclipsePlugin {

  @volatile private var instance : EclipsePlugin = _

  /**
   * Expose plugin instance.
   */
  def apply() : EclipsePlugin = instance

  /**
   * Simple logging facade.
   */
  trait AnyLog {
    def info( line : String ) = ()
    def warn( line : String ) = ()
    def fail( line : String ) = ()
    def fail( line : String, error : Throwable ) = ()
  }

}

/**
 * Eclipse companion plugin installed by this Maven plugin.
 */
class EclipsePlugin extends Plugin {

  /**
   * Eclipse plugin self-descriptor.
   */
  lazy val properties : Properties = {
    propertiesFrom( getBundle.getEntry( Eclipse.plugin.properties ) )
  }

  /**
   * Eclipse plugin self-descriptor.
   */
  def property( key : String ) = properties.getProperty( key )

  lazy val pluginId = property( key.pluginId )
  lazy val mavenGroupId = property( key.mavenGroupId )
  lazy val mavenArtifactId = property( key.mavenArtifactId )
  lazy val mavenVersion = property( key.mavenVersion )
  lazy val projectConfigurator = property( key.projectConfigurator )

  lazy val logId = mavenArtifactId + "-" + mavenVersion

  /**
   * Log to M2E "Maven Console".
   */
  object slf4jLog extends AnyLog {
    /**  Work around lack of logging source in M2E "Maven Console". */
    private lazy val prefix = "[" + A.eclipse.name + ":" + A.mojo.`eclipse-config` + "] "
    private lazy val logger = LoggerFactory.getLogger( logId );
    override def info( line : String ) = logger.info( prefix + line )
    override def warn( line : String ) = logger.warn( prefix + line )
    override def fail( line : String ) = logger.error( prefix + line )
    override def fail( line : String, error : Throwable ) = logger.error( prefix + line, error )
  }

  /**
   * Log to Eclipse "Error Log".
   */
  object eclipseLog extends AnyLog {
    import IStatus._
    override def info( line : String ) = getLog.log( new Status( INFO, logId, line ) )
    override def warn( line : String ) = getLog.log( new Status( WARNING, logId, line ) )
    override def fail( line : String ) = getLog.log( new Status( ERROR, logId, line ) )
    override def fail( line : String, error : Throwable ) = getLog.log( new Status( ERROR, logId, line, error ) )
  }

  /**
   * Detect optional M2E logging plugins.
   */
  def hasLogback = {
    val hasAppender = discoverBundle( getBundle, m2e.logback.appender ).isDefined
    val hasConfiguration = discoverBundle( getBundle, m2e.logback.configuration ).isDefined
    hasAppender && hasConfiguration
  }

  /**
   * Bind plugin logger to detected destination.
   */
  lazy val log : AnyLog = if ( hasLogback ) {
    eclipseLog.info( "Plugin logger is using M2E Maven Console." )
    slf4jLog
  } else {
    eclipseLog.info( "Plugin logger is using Eclipse Error Log." )
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
   * Common execution wrapper.
   */
  def tryCore[ T ]( name : String )( block : => T ) : T = try block catch {
    case error : Throwable => throw new CoreException( new Status( IStatus.ERROR, logId, name, error ) )
  }

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

object EclipseConfigurator {

  val classpathComparator = new Comparator[ IClasspathEntryDescriptor ]() {
    def compare( o1 : IClasspathEntryDescriptor, o2 : IClasspathEntryDescriptor ) : Int = {
      val w1 = getWeight( o1 );
      val w2 = getWeight( o2 );
      if ( w1 > 0 || w2 > 0 ) {
        if ( w1.equals( w2 ) )
          o1.getPath().toString().compareTo( o2.getPath().toString() );
        else
          w1.compareTo( w2 ) * -1;
      }
      return 0;
    }

    def getWeight( ce : IClasspathEntryDescriptor ) : Int = {
      var value = 0;
      if ( ce.getEntryKind() == IClasspathEntry.CPE_SOURCE ) {

        //          for(Entry<String, Integer> e : mapSourceTypeWeight.entrySet()) {
        //            if(ce.getPath().toString().contains(e.getKey())) {
        //              value += e.getValue();
        //            }
        //          }

        //          for(Entry<String, Integer> e : mapResourceWeight.entrySet()) {
        //            if(ce.getPath().toString().endsWith(e.getKey())) {
        //              value += e.getValue();
        //            }
        //          }

      }
      return value;
    }
  }

  val javaNatureId = JavaCore.NATURE_ID
  val javaContainerId = JavaRuntime.JRE_CONTAINER

  val scalaNatureId = SdtConstants.NatureId
  val scalaContainerId = SdtConstants.ScalaLibContId

}

class EclipseConfigurator extends AbstractSourcesGenerationProjectConfigurator {

  import MavenProjectUtils._
  import base._
  import A._

  val plugin = EclipsePlugin(); import plugin._

  /**
   * Be nice, exit job on request.
   */
  def assertCancel( monitor : IProgressMonitor ) = {
    if ( monitor != null && monitor.isCanceled ) throw new OperationCanceledException()
  }

  def reportWorked( monitor : IProgressMonitor, work : Int = 1 ) {
    if ( monitor != null ) monitor.worked( work )
  }

  /**
   * Configured value of maven plugin configuration parameter.
   */
  def parameterValue[ T ](
    parameter : String,
    project :   MavenProject,
    execution : MojoExecution,
    monitor :   IProgressMonitor
  )( implicit tag : ClassTag[ T ] ) : T = getParameterValue(
    project, parameter, tag.runtimeClass, execution, monitor
  ).asInstanceOf[ T ]

  /**
   * Resolve external file against the maven project.
   */
  def relativePath(
    request : ProjectConfigurationRequest,
    file :    File
  ) = {
    val project = request.getMavenProjectFacade.getProject
    getProjectRelativePath( project, file.getCanonicalPath );
  }

  /**
   * Generate .classpath source[path] -> target[output] entry.
   */
  def confitureEntry(
    request :    ProjectConfigurationRequest,
    classpath :  IClasspathDescriptor,
    sourceFile : File,
    targetFile : File,
    monitor :    IProgressMonitor,
    generated :  Boolean                     = false
  ) = {
    log.info( s"Configure entry: ${relativePath( request, sourceFile )} -> ${relativePath( request, targetFile )}" )
    assertCancel( monitor )
    val facade = request.getMavenProjectFacade
    val sourcePath = getFullPath( facade, sourceFile )
    val targetPath = getFullPath( facade, targetFile )
    val entry = classpath.addSourceEntry( sourcePath, targetPath, generated );
    entry.setClasspathAttribute( IClasspathAttribute.IGNORE_OPTIONAL_PROBLEMS, "true" )
  }

  /**
   * Generate .classpath source[path] -> target[output] entry.
   */
  def configureGoal(
    request :   ProjectConfigurationRequest,
    classpath : IClasspathDescriptor,
    execution : MojoExecution,
    param :     Build.BuildParam,
    monitor :   IProgressMonitor
  ) = {
    log.info( "Configure execution: " + execution )
    assertCancel( monitor )
    import param._
    val project = request.getMavenProject
    val sourceFilesJava = parameterValue[ Array[ File ] ]( javaSourceFolders, project, execution, monitor )
    val sourceFilesScala = parameterValue[ Array[ File ] ]( scalaSourceFolders, project, execution, monitor )
    val targetFile = parameterValue[ File ]( buildTargetFolder, project, execution, monitor )
    ( sourceFilesJava ++ sourceFilesScala ).foreach { sourceFile =>
      confitureEntry( request, classpath, sourceFile, targetFile, monitor )
    }
  }

  /**
   * Generate .classpath source[path] -> target[output] entry.
   */
  def configureExecution(
    request :   ProjectConfigurationRequest,
    classpath : IClasspathDescriptor,
    execution : MojoExecution,
    monitor :   IProgressMonitor
  ) : Unit = {
    assertCancel( monitor )
    Build.descriptorMap.foreach {
      case ( goal, param ) => if ( goal == execution.getGoal ) {
        configureGoal( request, classpath, execution, param, monitor )
      }
    }
  }

  /**
   * Provide Java and Scala nature, with optional sort.
   */
  def ensureNature(
    project : IProject,
    monitor : IProgressMonitor
  ) : Unit = {
    assertCancel( monitor )
    val description = project.getDescription
    val natureList = ArrayBuffer( description.getNatureIds : _* )
    var hasChange = false
    if ( !project.hasNature( javaNatureId ) ) {
      natureList += javaNatureId
      hasChange = true
    }
    if ( !project.hasNature( scalaNatureId ) ) {
      natureList += scalaNatureId
      hasChange = true
    }
    if ( !hasChange && natureList != natureList.sorted ) {
      hasChange = true
    }
    if ( hasChange ) {
      description.setNatureIds( natureList.sorted.toArray )
      project.setDescription( description, monitor )
    } else {
      reportWorked( monitor )
    }
  }

  def orderClassPath() = {

  }

  /**
   * M2E configuration step #1. Provide internal content of the class path container:
   * .classpath/<classpathentry kind="con" path="org.eclipse.m2e.MAVEN2_CLASSPATH_CONTAINER">
   */
  /**
   * Configures Maven project classpath,
   * i.e. content of Maven Dependencies classpath container.
   */
  override def configureClasspath(
    facade :    IMavenProjectFacade,
    classpath : IClasspathDescriptor,
    monitor :   IProgressMonitor
  ) = tryCore( "M2E step #1" ) {
    log.info( s"Configuring dependency classpath." )
    assertCancel( monitor )
    classpath.getEntries.foreach { entry =>
      log.info( s"   entry: ${entry}" )
    }

    classpath.getEntries

    val list = classpath.getEntryDescriptors
    // list.sort( null ) // XXX sort for maven deps

  }

  /**
   * M2E configuration step #2. Provide top level class path entries:
   * .classpath/<classpathentry kind="src" output="target/classes" path="src/main/java">
   * .classpath/<classpathentry kind="con" path="org.scala-ide.sdt.launching.SCALA_CONTAINER"/>
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
  ) : Unit = tryCore( "M2E step #2" ) {
    log.info( s"Configuring source/target folders." )
    assertCancel( monitor )

    getMojoExecutions( request, monitor ).asScala.foreach { execution =>
      configureExecution( request, classpath, execution, monitor )
    }

    val project = request.getProject

    // inject scala
    ScalaNature.addScalaLibAndSave( project ) // XXX

    // make total sort
    val list = classpath.getEntryDescriptors // XXX
    // list.sort( null ) // XXX sort for path entries

  }

  /**
   * M2E configuration step #3. Provide content for:
   * .project/<...>
   * .settings/[...].prefs
   */
  /**
   * Configures Eclipse project passed in ProjectConfigurationRequest,
   * using information from Maven project and other configuration request parameters.
   */
  override def configure(
    request : ProjectConfigurationRequest,
    monitor : IProgressMonitor
  ) = tryCore( "M2E step #3" ) {
    log.info( s"Configuring project settings." )
    assertCancel( monitor )

    val project = request.getProject

    ensureNature( project, monitor )

  }

}
