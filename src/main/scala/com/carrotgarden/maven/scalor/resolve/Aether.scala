package com.carrotgarden.maven.scalor.resolve

import java.util.Collections
import java.util.LinkedList
import java.util.List
import java.util.Set

import scala.collection.JavaConverters.asScalaBufferConverter
import scala.collection.JavaConverters.bufferAsJavaListConverter
import scala.collection.JavaConverters.seqAsJavaListConverter

import org.apache.maven.RepositoryUtils
import org.eclipse.aether.RepositorySystem
import org.eclipse.aether.RepositorySystemSession
import org.eclipse.aether.artifact.Artifact
import org.eclipse.aether.artifact.ArtifactTypeRegistry
import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.collection.CollectRequest
import org.eclipse.aether.graph.Dependency
import org.eclipse.aether.graph.DependencyFilter
import org.eclipse.aether.graph.DependencyNode
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.resolution.DependencyRequest
import org.eclipse.aether.util.artifact.JavaScopes
import org.eclipse.aether.util.filter.DependencyFilterUtils
import org.eclipse.aether.graph.Exclusion

import scala.collection.immutable
import org.eclipse.aether.resolution.ArtifactRequest

/**
 * Shared Maven/Eclipse artifact resolver.
 */
trait Aether {

  import Aether._

  val repoSystem : RepositorySystem
  val repoSession : RepositorySystemSession
  val remoteRepoList : List[ RemoteRepository ]

  /**
   * Convert artifact into dependency with options.
   */
  def dependencyFrom(
    artifact :         Artifact,
    scope :            String           = JavaScopes.COMPILE,
    optional :         Boolean          = false,
    exclusions :       Set[ Exclusion ] = Collections.emptySet(),
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
    new Dependency( clone, scope, optional, exclusions )
  }

  /**
   * Download optional artifact.
   */
  def downloadOptional(
    request : ArtifactRequest
  ) : Option[ Artifact ] = {
    try {
      val result = repoSystem.resolveArtifact( repoSession, request )
      Some( result.getArtifact )
    } catch {
      case error : Throwable => None
    }
  }

  /**
   * Download required artifacts.
   */
  def downloadRequired(
    request : DependencyRequest
  ) : List[ Artifact ] = {
    val resolveResult = repoSystem.resolveDependencies( repoSession, request )
    val resultList = resolveResult.getArtifactResults
    resultList.asScala.map( _.getArtifact ).asJava
  }

  /**
   * Create dependency request.
   */
  def requestDepends(
    collect : CollectRequest,
    filter :  DependencyFilter
  ) : DependencyRequest = {
    val request = new DependencyRequest()
    request.setCollectRequest( collect )
    request.setFilter( filter )
    request
  }

  /**
   * Create collect request.
   */
  def requestCollect(
    dependencies : List[ Dependency ]
  ) : CollectRequest = {
    val request = new CollectRequest()
    request.setDependencies( dependencies )
    request.setRepositories( remoteRepoList )
    request
  }

  /**
   * Resolve both binary jars and source jars.
   */
  def resolveBothJars(
    binaryDeps : List[ Dependency ],
    scope :      String
  ) : List[ Artifact ] = {
    // all binary dependencies are required
    val binaryList = resolveRequired( binaryDeps, scope ).asScala
    // source dependency derived from binary
    val sourceDeps = binaryList.map { binary =>
      dependencyFrom( binary, classifierOption = Some( "sources" ) )
    }
    // all source dependencies are optional
    val sourceList = sourceDeps.flatMap { source =>
      resolveOptional( source )
    }
    val resultList = binaryList ++ sourceList
    resultList.distinct.sortBy( _.getFile.getCanonicalPath ).asJava
  }

  /**
   * Resolve single direct dependency.
   */
  def resolveOptional(
    dependency : Dependency
  ) : Option[ Artifact ] = {
    val source = dependency.getArtifact
    val request = new ArtifactRequest()
    request.setArtifact( source )
    request.setRepositories( remoteRepoList )
    downloadOptional( request )
  }

  /**
   * Resolve all transitive dependencies.
   */
  def resolveRequired(
    list :   List[ Dependency ],
    scope :  String             = JavaScopes.COMPILE,
    filter : DependencyFilter   = filterCompile
  ) : List[ Artifact ] = {
    val collectRequest = requestCollect( list )
    val dependsRequest = requestDepends( collectRequest, filter )
    downloadRequired( dependsRequest )
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

}

object Aether {

  val filterCompile = DependencyFilterUtils.classpathFilter( JavaScopes.COMPILE )

  val filterOptional = new DependencyFilter {
    override def accept(
      node :    DependencyNode,
      parents : List[ DependencyNode ]
    ) : Boolean = {
      !node.getDependency.isOptional
    }
  }

}

/**
 * Single resolution invocation unit.
 */
case class AetherUnit(
  val repoSystem :     RepositorySystem,
  val repoSession :    RepositorySystemSession,
  val remoteRepoList : List[ RemoteRepository ]
) extends Aether
