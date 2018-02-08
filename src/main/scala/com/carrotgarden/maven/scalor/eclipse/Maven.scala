package com.carrotgarden.maven.scalor.eclipse

import java.io.File

import scala.collection.JavaConverters.asScalaBufferConverter
import scala.reflect.ClassTag

import org.apache.maven.RepositoryUtils
import org.apache.maven.artifact.Artifact
import org.apache.maven.model.Dependency
import org.apache.maven.plugin.MojoExecution
import org.apache.maven.project.MavenProject
import org.codehaus.plexus.util.xml.Xpp3Dom
import org.eclipse.aether.util.artifact.JavaScopes
import org.eclipse.core.runtime.IPath
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.m2e.core.MavenPlugin
import org.eclipse.m2e.core.internal.MavenPluginActivator
import org.eclipse.m2e.core.internal.project.registry.MavenProjectFacade
import org.eclipse.m2e.core.internal.project.registry.ProjectRegistryManager
import org.eclipse.m2e.core.project.IMavenProjectFacade
import org.eclipse.m2e.core.project.MavenProjectUtils
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest

import org.codehaus.plexus.component.annotations.Requirement

import com.carrotgarden.maven.scalor.A
import com.carrotgarden.maven.scalor.resolve
import com.carrotgarden.maven.scalor.util
import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.Path
import org.eclipse.core.resources.IFolder
import org.apache.maven.plugin.descriptor.PluginDescriptor
import com.carrotgarden.maven.scalor.util.Error._
import scala.util.Success
import org.eclipse.aether.impl.VersionRangeResolver
import org.eclipse.aether.RepositorySystem
import org.eclipse.aether.spi.log.LoggerFactory
import org.apache.maven.repository.internal.DefaultVersionRangeResolver
import scala.util.Failure
import com.carrotgarden.maven.scalor.util.Logging
import org.apache.maven.repository.internal.DefaultArtifactDescriptorReader
import org.eclipse.aether.impl.ArtifactDescriptorReader
import org.eclipse.aether.impl.VersionResolver
import org.eclipse.aether.impl.DependencyCollector
import org.eclipse.aether.impl.ArtifactResolver
import org.eclipse.aether.impl.MetadataResolver
import org.eclipse.aether.impl.Installer
import org.eclipse.aether.impl.Deployer
import org.eclipse.aether.impl.LocalRepositoryProvider
import org.eclipse.aether.impl.SyncContextFactory
import org.eclipse.aether.impl.RemoteRepositoryManager
import org.eclipse.aether.internal.impl.DefaultRepositorySystem
import org.apache.maven.plugin.Mojo
import com.carrotgarden.maven.scalor.util.Classer

/**
 * Provide M2E infrastructure functions.
 */
trait Maven {

  self : Base.Conf with Logging =>

  import Maven._

  /**
   * Configured value of Maven plugin configuration parameter.
   */
  def paramExecValue[ T ](
    project :   MavenProject,
    execution : MojoExecution,
    name :      String,
    monitor :   IProgressMonitor
  )( implicit tag : ClassTag[ T ] ) : T = baseParamValue(
    project, name, tag.runtimeClass, execution, monitor
  ).asInstanceOf[ T ]

  /**
   * Provide Maven plugin configuration parmeter for a goal.
   */
  def paramGoalValue[ T ](
    facade :  IMavenProjectFacade,
    goal :    String,
    name :    String,
    monitor : IProgressMonitor
  )( implicit tag : ClassTag[ T ] ) : T = {
    val project = facade.getMavenProject
    executionDefinition( facade, goal, monitor ).map {
      execution => paramExecValue( project, execution, name, monitor )( tag )
    }.getOrElse( Throw( s"Trying to extract parameter for missing goal: ${goal}" ) )
  }

  /**
   * Provide configuration value for execution goal.
   */
  def extractGoalValue[ T ](
    facade :  IMavenProjectFacade,
    goal :    String,
    name :    String,
    monitor : IProgressMonitor
  )( implicit tag : ClassTag[ T ] ) : T = {
    paramGoalValue( facade, goal, name, monitor )( tag )
  }

  /**
   * Extract plugin configuration parameters for executon goal.
   */
  def extractGoalValue(
    facade :  IMavenProjectFacade,
    monitor : IProgressMonitor,
    goal :    String
  )(
    name : String, klaz : Class[ _ ]
  ) : Object = {
    val javaType = Classer.primitiveWrap( klaz )
    extractGoalValue( facade, goal, name, monitor )( ClassTag( javaType ) )
  }

}

object Maven {

  /**
   * Relative path of project file. File must be inside the project.
   */
  def relativePath( project : IProject, file : File ) = {
    import util.Folder._
    MavenProjectUtils.getProjectRelativePath( project, file.getCanonicalPath );
  }

  /**
   * Absolute path of project file. File must be inside the project.
   */
  def absolutePath( project : IProject, file : File ) : IPath = {
    project.getFullPath.append( relativePath( project, file ) )
  }

  def projectFolder( project : IProject, absolutePath : String ) : IFolder = {
    val location = project.getLocation
    if ( location.equals( Path.fromOSString( absolutePath ) ) ) {
      project.getFolder( location )
    } else {
      project.getFolder( projectRelative( project, absolutePath ) )
    }
  }

  def projectRelative( project : IProject, absolutePath : String ) : IPath = {
    val basedir = project.getLocation.toFile // absolute
    val resolve =
      if ( absolutePath.equals( basedir.getCanonicalPath ) ) {
        "."
      } else if ( absolutePath.startsWith( basedir.getCanonicalPath ) ) {
        absolutePath.substring( basedir.getCanonicalPath().length() + 1 )
      } else { // outside the project
        absolutePath
      }
    new Path( resolve.replace( '\\', '/' ) )
  }

  /**
   * Locate default Maven execution for a given Maven goal.
   */
  def executionDefinition(
    facade :  IMavenProjectFacade,
    goal :    String,
    monitor : IProgressMonitor
  ) : Option[ MojoExecution ] = {
    facade.asInstanceOf[ MavenProjectFacade ]
      .getExecutionPlan( ProjectRegistryManager.LIFECYCLE_DEFAULT, monitor )
      .asScala.find { execution =>
        execution.getGoal == goal
      }.map { execution =>
        // work around https://bugs.eclipse.org/bugs/show_bug.cgi?id=529319
        val mojoConfig : Xpp3Dom = execution.getConfiguration
        val pluginConfig : Xpp3Dom = execution.getPlugin.getConfiguration.asInstanceOf[ Xpp3Dom ]
        val configuration = ( mojoConfig, pluginConfig ) match {
          case ( null, null )               => null
          case ( null, pluginConfig )       => pluginConfig
          case ( mojoConfig, null )         => mojoConfig
          case ( mojoConfig, pluginConfig ) => Xpp3Dom.mergeXpp3Dom( mojoConfig, pluginConfig )
        }
        execution.setConfiguration( configuration )
        execution
      }
  }

  /**
   * Provide descriptor of this plugin.
   */
  def pluginDescriptor(
    facade :  IMavenProjectFacade,
    monitor : IProgressMonitor
  ) : PluginDescriptor = {
    val execution = executionDefinition( facade, A.mojo.`eclipse-config`, monitor ).get
    execution.getMojoDescriptor.getPluginDescriptor
  }

  /**
   * Access context of this (plugin,project).
   */
  def pluginContext(
    facade :  IMavenProjectFacade,
    monitor : IProgressMonitor
  ) : java.util.Map[ String, Object ] = {
    val maven = MavenPlugin.getMaven
    val context = maven.getExecutionContext
    val plugin = pluginDescriptor( facade, monitor )
    val project = facade.getMavenProject
    context.getSession.getPluginContext( plugin, project )
  }

  /**
   * Declared dependencies for this plugin.
   */
  def pluginDependencyList(
    request : ProjectConfigurationRequest,
    monitor : IProgressMonitor
  ) : List[ Dependency ] = {
    val facade = request.getMavenProjectFacade
    val execution = executionDefinition( facade, A.mojo.`eclipse-config`, monitor ).get
    execution.getPlugin.getDependencies.asScala.toList
  }

  /**
   * Resolve declared scalor plugin dependencies with sources.
   */
  def pluginResolvedDependencyList(
    request : ProjectConfigurationRequest,
    scope :   String                      = JavaScopes.COMPILE,
    monitor : IProgressMonitor
  ) : List[ Artifact ] = {

    val maven = MavenPlugin.getMaven
    val context = maven.getExecutionContext
    val activator = MavenPluginActivator.getDefault

    val repoSystem = activator.getRepositorySystem
    val repoSession = context.getRepositorySession
    val remoteRepoList = RepositoryUtils.toRepos( maven.getArtifactRepositories )

    val aether = new resolve.AetherUnit(
      repoSystem,
      repoSession,
      remoteRepoList
    )

    val stereotypes = repoSession.getArtifactTypeRegistry

    val dependencyList = pluginDependencyList( request, monitor )

    val artifactList = aether.resolveRoundTrip( dependencyList, stereotypes, scope )

    artifactList
  }

  import com.carrotgarden.maven.scalor.base.Params._
  import com.carrotgarden.maven.scalor.util.Classer

  import org.osgi.framework.Version

  /**
   * Needed for M2E 1.8 / Maven 3.3.9
   * Not needed for M2E 1.9 / Maven 3.5.2
   *
   * Work around M2E invalid component injector:
   * https://issues.apache.org/jira/browse/MNG-6233
   * https://github.com/apache/maven/commit/66fc74d6296ea0a33f8a9712dc5ed5eb3affd529
   */
  def hackRepoSystem(
    request : ProjectConfigurationRequest
  ) : RepositorySystem = {
    val project = request.getMavenProject
    val activator = MavenPluginActivator.getDefault
    val mavenFace = MavenPlugin.getMaven
    val mavenImpl = activator.getMaven

    val minimalVersion = new Version( "1.9.0" )
    val currentVersion = activator.getBundle.getVersion
    val requireHack = currentVersion.compareTo( minimalVersion ) < 0

    if ( requireHack ) {
      val loader = mavenImpl.getProjectRealm( project )
      Classer.withContextLoader[ RepositorySystem ]( loader ) {
        val system = new DefaultRepositorySystem
        import system._
        import mavenImpl._
        setLoggerFactory( lookupComponent( classOf[ LoggerFactory ] ) )
        setVersionResolver( lookupComponent( classOf[ VersionResolver ] ) )
        setVersionRangeResolver( lookupComponent( classOf[ VersionRangeResolver ] ) )
        setArtifactResolver( lookupComponent( classOf[ ArtifactResolver ] ) )
        setMetadataResolver( lookupComponent( classOf[ MetadataResolver ] ) )
        setArtifactDescriptorReader( lookupComponent( classOf[ ArtifactDescriptorReader ] ) )
        setDependencyCollector( lookupComponent( classOf[ DependencyCollector ] ) )
        setInstaller( lookupComponent( classOf[ Installer ] ) )
        setDeployer( lookupComponent( classOf[ Deployer ] ) )
        setLocalRepositoryProvider( lookupComponent( classOf[ LocalRepositoryProvider ] ) )
        setSyncContextFactory( lookupComponent( classOf[ SyncContextFactory ] ) )
        setRemoteRepositoryManager( lookupComponent( classOf[ RemoteRepositoryManager ] ) )
        system
      }
    } else {
      mavenImpl.lookupComponent( classOf[ RepositorySystem ] )
    }
  }

  def resolveDefine(
    request : ProjectConfigurationRequest,
    define :  DefineRequest,
    scope :   String,
    monitor : IProgressMonitor
  ) : DefineResponse = {
    val mavenFace = MavenPlugin.getMaven
    val context = mavenFace.getExecutionContext

    val repoSystem = hackRepoSystem( request )
    val repoSession = context.getRepositorySession
    val remoteRepoList = RepositoryUtils.toRepos( mavenFace.getArtifactRepositories )
    val stereotypes = repoSession.getArtifactTypeRegistry

    val aether = resolve.AetherUnit(
      repoSystem,
      repoSession,
      remoteRepoList
    )

    import define._
    val bridgeList = aether.resolveRoundTrip( defineBridge, stereotypes, scope )
    val compilerList = aether.resolveRoundTrip( defineCompiler, stereotypes, scope )
    val pluginDefineList = aether.resolveRoundTrip( definePluginList, stereotypes, scope )

    DefineResponse(
      bridgeList,
      compilerList,
      pluginDefineList
    )

  }

  type MojoFunction[ T <: Mojo, U ] = T => U

  /**
   * Invoke function with resolved/configured mojo .
   */
  def withProjectMojo[ T <: Mojo, U ](
    project :   MavenProject,
    execution : MojoExecution
  )(
    function : MojoFunction[ T, U ]
  )(
    implicit
    tag : ClassTag[ T ]
  ) : U = {
    val activator = MavenPluginActivator.getDefault
    val mavenFace = MavenPlugin.getMaven
    val mavenImpl = activator.getMaven
    val session = mavenFace.getExecutionContext.getSession
    // val projectLoader = mavenImpl.getProjectRealm( project )
    val pluginLoader = execution.getMojoDescriptor.getPluginDescriptor.getClassRealm
    Classer.withContextLoader( pluginLoader ) {
      val klaz = pluginLoader.loadClass( tag.runtimeClass.getName )
      val mojo = mavenFace.getConfiguredMojo( session, execution, klaz ).asInstanceOf[ T ]
      try {
        function( mojo )
      } finally {
        mavenFace.releaseMojo( mojo, execution )
      }
    }
  }

}
