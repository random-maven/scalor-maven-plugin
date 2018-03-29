package com.carrotgarden.maven.scalor.resolve

import com.carrotgarden.maven.tools.Description
import org.apache.maven.plugins.annotations._
import scala.collection.JavaConverters._

import org.eclipse.aether.RepositorySystem
import org.eclipse.aether.RepositorySystemSession
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.repository.LocalRepository
import org.eclipse.aether.util.artifact.JavaScopes
//import org.eclipse.aether.artifact.Artifact
//import org.eclipse.aether.graph.Dependency

import org.apache.maven.artifact.Artifact
import org.apache.maven.model.Dependency
import org.apache.maven.RepositoryUtils
import org.apache.maven.plugin.descriptor.PluginDescriptor

import com.carrotgarden.maven.scalor._

/**
 * Maven-only artifact resolver.
 */
trait Maven {

  @Description( """
  Aether repository system.
  """ )
  @Component()
  var repoSystem : RepositorySystem = _

  @Description( """
  Aether system session.
  """ )
  @Parameter( defaultValue = "${repositorySystemSession}", readonly = true, required = true )
  var repoSession : RepositorySystemSession = _

  @Description( """
  Aether repository list.
  """ )
  @Parameter( defaultValue = "${project.remoteProjectRepositories}", readonly = true, required = true )
  var remoteProjectRepositories : java.util.List[ RemoteRepository ] = _

  @Description( """
  Aether plugin repository list.
  """ )
  @Parameter( defaultValue = "${project.remotePluginRepositories}", readonly = true, required = true )
  var remotePluginRepositories : java.util.List[ RemoteRepository ] = _

  def stereotypes = repoSession.getArtifactTypeRegistry

  import base.Params._

  def resolveDefine(
    define : DefineRequest,
    scope :  String
  ) : DefineResponse = {

    val remoteRepoList = {
      val projRepos = remoteProjectRepositories.asScala
      val pluginRepos = remotePluginRepositories.asScala
      val availableRepos = projRepos ++ pluginRepos
      availableRepos.asJava
    }

    val aether = AetherUnit(
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
