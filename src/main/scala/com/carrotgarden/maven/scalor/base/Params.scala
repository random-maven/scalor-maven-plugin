package com.carrotgarden.maven.scalor.base

import org.apache.maven.execution.MavenSession
import org.apache.maven.project.MavenProject
import org.apache.maven.settings.Settings
import org.apache.maven.plugins.annotations._
import org.apache.maven.plugin.BuildPluginManager
import org.apache.maven.model.Plugin

import scala.collection.JavaConverters._

import com.carrotgarden.maven.scalor.A
import org.apache.maven.artifact.Artifact
import java.io.File
import com.carrotgarden.maven.scalor.util.Folder

import com.carrotgarden.maven.tools.Description
import org.apache.maven.plugin.descriptor.PluginDescriptor
import org.apache.maven.plugin.MojoExecution
import java.util.function.Consumer
import java.util.ArrayList
import org.apache.maven.artifact.repository.ArtifactRepository
import org.apache.maven.artifact.resolver.ArtifactResolver
import org.apache.maven.model.Repository
import org.apache.maven.repository.RepositorySystem
import org.apache.maven.project.ProjectBuilder
import org.apache.maven.project.DefaultProjectBuildingRequest
import org.apache.maven.artifact.resolver.filter.AndArtifactFilter
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter
import org.apache.maven.artifact.resolver.filter.ArtifactFilter
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest
import org.apache.maven.model.Dependency
import org.apache.maven.artifact.resolver.DefaultArtifactResolver
import org.apache.maven.project.artifact.MavenMetadataSource
import java.util.HashSet
import java.net.URLClassLoader
import java.net.URL

import Params._

/**
 * Shared mojo execution configuration parameters.
 */
trait Params {

  import Params._
  import Folder._

  @Description( """
  Skip all plugin executions.
  """ )
  @Parameter( property     = "scalor.skip", defaultValue = "false" )
  var skip : Boolean = _

  @Description( """
  Manager for plugin execution delegation.
  """ )
  @Component()
  var manager : BuildPluginManager = _

  @Description( """
  This plugin descriptor.
  """ )
  @Parameter( defaultValue = "${plugin}", required = true, readonly = true )
  var pluginDescriptor : PluginDescriptor = _

  @Description( """
  This mojo execution.
  """ )
  @Parameter( defaultValue = "${mojoExecution}", required = true, readonly = true )
  var mojoExecution : MojoExecution = _

  @Description( """
  Current maven project.
  """ )
  @Parameter( defaultValue = "${project}", required = true, readonly = true )
  var project : MavenProject = _

  @Description( """
  Maven build session.
  """ )
  @Parameter( defaultValue = "${session}", required = true, readonly = true )
  var session : MavenSession = _

  @Description( """
  User settings from settings.xml.
  """ )
  @Parameter( defaultValue = "${settings}", required = true, readonly = true )
  var settings : Settings = _

  @Description( """
  Scalor plugin dependencies.
  """ )
  @Parameter( defaultValue = "${plugin.artifacts}", required = true, readonly = true )
  var pluginArtifactList : java.util.List[ Artifact ] = _

  @Description( """
  Resolve dependency artifacts.
  """ )
  @Component()
  var resolver : ArtifactResolver = _

  @Description( """
  Resolve dependency artifacts.
  """ )
  @Component()
  var repoSystem : RepositorySystem = _

  @Description( """
  Local maven repository.
  """ )
  @Parameter( defaultValue = "${localRepository}", required = true, readonly = true )
  var localRepo : ArtifactRepository = _

  @Description( """
  Current maven project.
  """ )
  @Parameter( defaultValue = "${project.remoteArtifactRepositories}", required = true, readonly = true )
  var remoteRepoList : java.util.List[ ArtifactRepository ] = _

  @Description( """
  Resolve dependency artifacts.
  """ )
  @Component()
  var projectBuilder : ProjectBuilder = _

  /**
   */
  def extractProperty( key : String, value : String = "" ) : String = {
    project.getProperties.getProperty( key )
  }

  def extractPropertyList( key : String, value : String = "" ) = {
    extractProperty( key, value ).split( "\0" ).toList
  }

  /**
   */
  def persistProperty( key : String, value : String ) : Unit = {
    project.getProperties.setProperty( key, value )
  }

  def persistPropertyList( key : String, sourceValue : String ) : Unit = {
    val sourceList = extractPropertyList( key )
    val targetList = sourceValue :: sourceList
    val targetValue = targetList.mkString( "\0" )
    persistProperty( key, targetValue )
  }

  /**
   */
  def resolve(
    artifact : Artifact, filter : ArtifactFilter, binary : Boolean = true
  ) : java.util.Set[ Artifact ] = {
    import artifact._
    val pomFact = repoSystem.createProjectArtifact(
      getGroupId, getArtifactId, getVersion
    )
    val dependencies = new HashSet[ Artifact ]()
    if ( binary ) {
      val binaryFact = repoSystem.createArtifact(
        getGroupId, getArtifactId, getVersion, getScope, getType
      )
      dependencies.add( binaryFact )
    } else {
      val sourcesFact = repoSystem.createArtifactWithClassifier(
        getGroupId, getArtifactId, getVersion, "jar", "sources"
      )
      sourcesFact.setScope( getScope )
      dependencies.add( sourcesFact )
    }
    val request = new ArtifactResolutionRequest
    request.setArtifact( pomFact )
    request.setArtifactDependencies( dependencies )
    request.setResolveTransitively( true )
    request.setCollectionFilter( filter )
    request.setLocalRepository( localRepo )
    request.setRemoteRepositories( remoteRepoList )
    val result = resolver.resolve( request )
    result.getArtifacts
  }

  def resolve(
    dependency : Dependency, filter : ArtifactFilter
  ) : java.util.Set[ Artifact ] = {
    import dependency._
    val artifact = repoSystem.createArtifact(
      getGroupId, getArtifactId, getVersion, getScope, getType
    )
    resolve( artifact, filter )
  }

  /**
   * Resolve declared scalor plugin dependencies with sources.
   */
  def pluginResolvedDependencyList(
    filter : ArtifactFilter = filterIncludeCompile
  ) = {
    val binaries = new java.util.HashSet[ Artifact ]
    pluginDependencyList.foreach { dependency =>
      binaries.addAll( resolve( dependency, filter ) )
    }
    val sources = new java.util.HashSet[ Artifact ]
    binaries.asScala.foreach { artifact =>
      sources.addAll( resolve( artifact, filter, false ) )
    }
    val result = new java.util.TreeSet[ Artifact ]
    result.addAll( binaries )
    result.addAll( sources )
    result.asScala.toSet
  }

  /**
   * Scalor plugin dependencies.
   */
  def pluginClassPath( bucket : Classifier.Bucket = Classifier.Select.Binary ) : Array[ File ] = {
    val list = new ArrayList[ File ]()
    val iter = pluginArtifactList.iterator
    while ( iter.hasNext ) {
      val artifact = iter.next
      val hasBucket = Classifier.hasMatch( artifact, bucket )
      if ( hasBucket ) {
        val file = artifact.getFile
        if ( file != null ) {
          list.add( file.getCanonicalFile )
        }
      }
    }
    list.toArray( Array[ File ]() )
  }

  /**
   * Configured scalor plugin dependencies.
   */
  def pluginDependencyList() = {
    pluginDescriptor.getPlugin.getDependencies.asScala.toSet
  }

  /**
   * Resolved project dependencies with matching scopes.
   */
  def projectDepenencyList( bucket : Scope.Bucket = Scope.Select.Test ) = {
    val list = new ArrayList[ Artifact ]()
    val iter = project.getArtifacts.iterator
    while ( iter.hasNext ) {
      val artifact = iter.next
      if ( artifact != null && artifact.getArtifactHandler != null ) {
        // Filter by @Mojo "requiresDependencyResolution = ResolutionScope.Value".
        val hasResolve = artifact.getArtifactHandler.isAddedToClasspath
        // Filter by provided scope configuration.
        val hasBucket = Scope.hasMatch( artifact, bucket )
        // Ensure is downloaded.
        val hasFile = artifact.getFile != null
        if ( hasResolve && hasBucket && hasFile ) {
          list.add( artifact )
        }
      }
    }
    list
  }

  /**
   * Resolved project dependencies with matching scopes.
   */
  def projectClassPath( bucket : Scope.Bucket = Scope.Select.Test ) : Array[ File ] = {
    val list = new ArrayList[ File ]()
    val iter = project.getArtifacts.iterator
    while ( iter.hasNext ) {
      val artifact = iter.next
      if ( artifact != null && artifact.getArtifactHandler != null ) {
        // Filter by @Mojo "requiresDependencyResolution = ResolutionScope.Value"
        val hasResolve = artifact.getArtifactHandler.isAddedToClasspath
        // Filter by provided scope configuration
        val hasBucket = Scope.hasMatch( artifact, bucket )
        if ( hasResolve && hasBucket ) {
          val file = artifact.getFile
          if ( file != null ) {
            list.add( file.getCanonicalFile )
          }
        }
      }
    }
    list.toArray( Array[ File ]() )
  }

}

trait ParamsArtifact extends ParamsPluginList {

  @Description( """
  """ )
  @Parameter(
    property     = "scalor.artifactScalaLibrary",
    defaultValue = "org.scala-lang:scala-library"
  )
  var artifactScalaLibrary : String = _

  @Description( """
  """ )
  @Parameter(
    property     = "scalor.artifactScalaCompiler",
    defaultValue = "org.scala-lang:scala-compiler"
  )
  var artifactScalaCompiler : String = _

  @Description( """
  """ )
  @Parameter(
    property     = "scalor.artifactScalaReflect",
    defaultValue = "org.scala-lang:scala-reflect"
  )
  var artifactScalaReflect : String = _

  @Description( """
  """ )
  @Parameter(
    property     = "scalor.artifactCompilerBridge",
    defaultValue = "org.scala-sbt:compiler-bridge_(.+)"
  )
  var artifactCompilerBridge : String = _

  //  def resolveScalaLibrary(
  //    artifactList : Set[ Artifact ], classifier : String = null
  //  ) = resolveArtifact( artifactList, artifactScalaLibrary, classifier )
  //
  //  def resolveScalaCompiler(
  //    artifactList : Set[ Artifact ], classifier : String = null
  //  ) = resolveArtifact( artifactList, artifactScalaCompiler, classifier )
  //
  //  def resolveScalaReflect(
  //    artifactList : Set[ Artifact ], classifier : String = null
  //  ) = resolveArtifact( artifactList, artifactScalaReflect, classifier )
  //
  //  def resolveCompilerBridge(
  //    artifactList : Set[ Artifact ], classifier : String = null
  //  ) = resolveArtifact( artifactList, artifactCompilerBridge, classifier )

  def moduleType( artifact : Artifact ) : ModuleType = {
    artifactIdentity( artifact ) match {
      case artifactCompilerBridge.r() => CompilerBridge
      case artifactScalaCompiler.r() => ScalaCompiler
      case artifactScalaLibrary.r() => ScalaLibrary
      case artifactScalaReflect.r() => ScalaReflect
      case _ if hasResourceMatch( `artifact`, artifactPluginDescriptor ) => CompilerPlugin
      case _ => Unknown
    }
  }

}

trait ParamsPluginList {

  @Description( """
  Scala compiler plugin descriptor file name,
  stored inside compiler plugin jar.
  Used for auto discovery of compiler plugins
  form scalor maven plugin dependnency class path.
  """ )
  @Parameter(
    property     = "scalor.artifactPluginDescriptor",
    defaultValue = "scalac-plugin.xml"
  )
  var artifactPluginDescriptor : String = _

}

object Params {

  sealed trait ModuleType
  case object Unknown extends ModuleType
  case object ScalaLibrary extends ModuleType
  case object ScalaReflect extends ModuleType
  case object ScalaCompiler extends ModuleType
  case object CompilerBridge extends ModuleType
  case object CompilerPlugin extends ModuleType

  def artifactIdentity( artifact : Artifact ) = {
    import artifact._
    getGroupId + ":" + getArtifactId
  }

  def hasArtifactMatch( artifact : Artifact, regex : String, classifier : String ) : Boolean = {
    val hasIdentity = artifactIdentity( artifact ) match {
      case regex.r() => true
      case _         => false
    }
    val hasClassifier = artifact.getClassifier == classifier
    hasIdentity && hasClassifier
  }

  def hasResourceMatch( artifact : Artifact, resource : String ) : Boolean = {
    val url = artifact.getFile.getCanonicalFile.toURI.toURL
    val loader = new URLClassLoader( Array[ URL ]( url ) );
    val result = loader.getResources( resource ).hasMoreElements
    loader.close
    result
  }

  def resolveArtifact(
    artifactList : Set[ Artifact ], regex : String, classifier : String
  ) : Either[ Throwable, Artifact ] = {
    val collectList = artifactList.collect {
      case artifact if hasArtifactMatch( artifact, regex, classifier ) => artifact
    }
    collectList.toList match {
      case head :: Nil  => Right( head )
      case head :: tail => Left( new RuntimeException( "Duplicate artifact: " + regex ) )
      case Nil          => Left( new RuntimeException( "Missing artifact: " + regex ) )
    }
  }

  val filterIncludeCompile = {
    val filterAnd = new AndArtifactFilter()
    val scopeFilter = new ScopeArtifactFilter( Artifact.SCOPE_COMPILE )
    val artifactFilter = new ArtifactFilter() {
      override def include( artifact : Artifact ) = {
        !artifact.isOptional
      }
    }
    filterAnd.add( scopeFilter )
    filterAnd.add( artifactFilter )
    filterAnd
  }

  /**
   * Discover scala compiler plugins from the class loader class path.
   */
  def pluginDiscoveryList(
    artifactList : Set[ Artifact ], regex : String
  ) : Set[ Artifact ] = {
    ???
  }

}
