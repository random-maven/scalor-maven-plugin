package com.carrotgarden.maven.scalor.resolve

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.ArrayList
import java.util.Collection
import java.util.LinkedList
import java.util.List

import org.eclipse.aether.RepositorySystem
import org.eclipse.aether.RepositorySystemSession
import org.eclipse.aether.artifact.Artifact
import org.eclipse.aether.collection.CollectRequest
import org.eclipse.aether.graph.Dependency
import org.eclipse.aether.graph.DependencyFilter
import org.eclipse.aether.repository.Authentication
import org.eclipse.aether.repository.LocalRepository
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.resolution.ArtifactResult
import org.eclipse.aether.resolution.DependencyRequest
import org.eclipse.aether.resolution.DependencyResolutionException
import org.eclipse.aether.resolution.DependencyResult
import org.eclipse.aether.util.filter.DependencyFilterUtils
import org.eclipse.aether.util.repository.DefaultMirrorSelector
import org.eclipse.aether.util.artifact.JavaScopes
import org.eclipse.aether.artifact.DefaultArtifact

import scala.collection.JavaConverters._
import java.util.Collections

import DependencyFilterUtils._
import org.eclipse.aether.artifact.ArtifactTypeRegistry
import org.apache.maven.RepositoryUtils

/**
 * Shared Maven/Eclipse artifact resolver.
 */
trait Aether {

  import Aether._

  val repoSystem : RepositorySystem
  val repoSession : RepositorySystemSession
  val remoteRepoList : List[ RemoteRepository ]

  def localRepoFolder = repoSession.getLocalRepository.getBasedir

  /**
   * Resolve all transitive dependencies for all artifacts, filtered by scope.
   */
  def resolveArtifact(
    list :  List[ Artifact ],
    scope : String
  ) : List[ Artifact ] = {
    list.asScala.toList match {
      case Nil =>
        list
      case head :: tail =>
        val list = tail.map { artifact => dependencyFrom( artifact, scope ) }
        resolveFiltered( head, list.asJava, scope )
    }
  }

  def resolveDependency(
    list :  List[ Dependency ],
    scope : String
  ) : List[ Artifact ] = {
    list.asScala.toList match {
      case Nil =>
        Collections.emptyList()
      case head :: tail =>
        resolveFiltered( artifactFrom( head ), list, scope )
    }
  }

  import scala.collection.immutable

  /**
   * Resolve both binary jars and source jars.
   */
  def resolveBothJars(
    binaryDeps : List[ Dependency ],
    scope :      String
  ) : List[ Artifact ] = {
    val binaryList = resolveDependency( binaryDeps, scope ).asScala
    val sourceDeps = binaryList.map { binary =>
      dependencyFrom( binary, classifierOption = Some( "sources" ) )
    }.asJava
    val sourceList = resolveDependency( sourceDeps, scope ).asScala
    val resultList = binaryList ++ sourceList
    resultList.distinct.sortBy( _.toString ).asJava
  }

  /**
   * Format round-trip: Maven -> Aether -> Maven
   */
  def resolveRoundTrip(
    binaryDeps :  immutable.List[ org.apache.maven.model.Dependency ],
    stereotypes : ArtifactTypeRegistry,
    scope :       String
  ) : immutable.List[ org.apache.maven.artifact.Artifact ] = {
    val dependencyList = binaryDeps
      .map( RepositoryUtils.toDependency( _, stereotypes ) )
      .asJava
    val artifactList = resolveBothJars( dependencyList, scope )
      .asScala
      .map( artifact => RepositoryUtils.toArtifact( artifact ) )
      .toList
    artifactList
  }

  /**
   * List of transitive dependencies of the artifact.
   */
  def resolveScoped(
    root :  Artifact,
    list :  List[ Dependency ] = Collections.emptyList(),
    scope : String             = JavaScopes.COMPILE
  ) : List[ Artifact ] = {
    val filter = classpathFilter( scope )
    resolveFiltered( root, list, scope, filter )
  }

  /**
   * List of transitive dependencies of the artifact.
   */
  def resolveFiltered(
    root :   Artifact,
    list :   List[ Dependency ] = Collections.emptyList(),
    scope :  String             = JavaScopes.COMPILE,
    filter : DependencyFilter   = filterCompile
  ) : List[ Artifact ] = {
    val dependency = dependencyFrom( root, scope )
    val collectRequest = requestCollect( dependency )
    collectRequest.setDependencies( list )
    val dependsRequest = requestDepends( collectRequest, filter )
    fetchDepends( dependsRequest )
  }

  /**
   * Convert artifact to dependency in scope.
   */
  def dependencyFrom(
    artifact :         Artifact,
    scope :            String           = JavaScopes.COMPILE,
    classifierOption : Option[ String ] = None,
    extensionOption :  Option[ String ] = None,
    versionOption :    Option[ String ] = None
  ) = {
    import artifact._
    val classifier = if ( classifierOption.isDefined ) classifierOption.get else getClassifier
    val extension = if ( extensionOption.isDefined ) extensionOption.get else getExtension
    val version = if ( versionOption.isDefined ) versionOption.get else getVersion
    // DefaultArtifact( String groupId, String artifactId, String classifier, String extension, String version )
    val clone = new DefaultArtifact( getGroupId, getArtifactId, classifier, extension, version )
    new Dependency( clone, scope )
  }

  def artifactFrom(
    dependency : Dependency
  ) = dependency.getArtifact

  /**
   * Fetch dependencies.
   */
  def fetchDepends(
    request : DependencyRequest
  ) : List[ Artifact ] = {
    val artifactList = new LinkedList[ Artifact ]()
    val resolveResult = repoSystem.resolveDependencies( repoSession, request )
    val resultList = resolveResult.getArtifactResults
    for ( result <- resultList.asScala ) {
      artifactList.add( result.getArtifact )
    }
    artifactList
  }

  /**
   * Create dependency request.
   */
  def requestDepends(
    collect : CollectRequest,
    filter :  DependencyFilter
  ) : DependencyRequest = {
    new DependencyRequest( collect, filter )
  }

  /**
   * Create collect request.
   */
  def requestCollect( root : Dependency ) : CollectRequest = {
    val request = new CollectRequest()
    request.setRoot( root )
    for ( repo <- remoteRepoList.asScala ) {
      request.addRepository( repo )
    }
    request
  }

}

object Aether {

  val filterCompile = classpathFilter( JavaScopes.COMPILE )

}

/**
 * Single resolution invocation unit.
 */
case class AetherUnit(
  val repoSystem :     RepositorySystem,
  val repoSession :    RepositorySystemSession,
  val remoteRepoList : List[ RemoteRepository ]
) extends Aether
