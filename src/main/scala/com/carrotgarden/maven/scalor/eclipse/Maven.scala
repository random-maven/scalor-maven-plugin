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

import com.carrotgarden.maven.scalor.A
import com.carrotgarden.maven.scalor.resolve
import com.carrotgarden.maven.scalor.util
import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.Path
import org.eclipse.core.resources.IFolder
import org.apache.maven.plugin.descriptor.PluginDescriptor

/**
 * Provide M2E infrastructure functions.
 */
trait Maven {

  self : Base.Conf =>

  import Maven._

  /**
   * Configured value of maven plugin configuration parameter.
   */
  def configValue[ T ](
    project :   MavenProject,
    execution : MojoExecution,
    parameter : String,
    monitor :   IProgressMonitor
  )( implicit tag : ClassTag[ T ] ) : T = baseParamValue(
    project, parameter, tag.runtimeClass, execution, monitor
  ).asInstanceOf[ T ]

  /**
   * Provide Maven plugin configuration parmeter for a goal.
   */
  def configValue[ T ](
    facade :    IMavenProjectFacade,
    goal :      String,
    parameter : String,
    monitor :   IProgressMonitor
  )( implicit tag : ClassTag[ T ] ) : T = {
    val project = facade.getMavenProject
    val execution = executionDefinition( facade, goal, monitor ).get
    configValue( project, execution, parameter, monitor )( tag )
  }

  /**
   * Provide companion Eclipse plugin configuration from Maven plugin configuration.
   */
  def configValue[ T ](
    facade :    IMavenProjectFacade,
    parameter : String,
    monitor :   IProgressMonitor
  )( implicit tag : ClassTag[ T ] ) : T = {
    configValue( facade, A.mojo.`eclipse-config`, parameter, monitor )( tag )
  }

}

object Maven {

  /**
   * Relative path of project file. File must be inside the project.
   */
  def relativePath( project : IProject, file : File ) = {
    import util.Folder._
    MavenProjectUtils.getProjectRelativePath( project, ensureAbsolutePath( file ) );
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
      if ( absolutePath.equals( basedir.getAbsolutePath ) ) {
        "."
      } else if ( absolutePath.startsWith( basedir.getAbsolutePath ) ) {
        absolutePath.substring( basedir.getAbsolutePath().length() + 1 )
      } else { // outside the project
        absolutePath
      }
    new Path( resolve.replace( '\\', '/' ) )
  }

  /**
   * Locate default maven execution for a given maven goal.
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
        val mojoConfig = execution.getConfiguration
        val pluginConfig = execution.getPlugin.getConfiguration.asInstanceOf[ Xpp3Dom ]
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

  def resolveDefine(
    request : ProjectConfigurationRequest,
    define :  DefineRequest,
    scope :   String,
    monitor : IProgressMonitor
  ) : DefineResponse = {

    val maven = MavenPlugin.getMaven
    val context = maven.getExecutionContext
    val activator = MavenPluginActivator.getDefault

    val repoSystem = activator.getRepositorySystem
    val repoSession = context.getRepositorySession
    val remoteRepoList = RepositoryUtils.toRepos( maven.getArtifactRepositories )

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

}
