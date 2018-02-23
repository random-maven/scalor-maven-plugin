package com.carrotgarden.maven.scalor.util

import java.io.File
import java.net.URL
import java.net.URLClassLoader

import scala.collection.JavaConverters.asScalaBufferConverter
import scala.collection.JavaConverters.asScalaSetConverter

import org.apache.maven.artifact.Artifact
import org.apache.maven.model.Dependency
import org.apache.maven.model.Model
import org.apache.maven.plugin.descriptor.PluginDescriptor
import org.apache.maven.project.MavenProject
import org.codehaus.plexus.configuration.PlexusConfiguration
import org.codehaus.plexus.util.xml.Xpp3Dom
import org.twdata.maven.mojoexecutor.PlexusConfigurationUtils

/**
 * Maven support.
 */
object Maven {

  /**
   * Maven identity based on G.A.V.
   */
  def artifactIdentity( artifact : Artifact, useVersion : Boolean = false ) = {
    import artifact._
    s"${getGroupId}:${getArtifactId}${if ( useVersion ) s":${getVersion}" else ""}"
  }

  /**
   * Compare artifacts by their identity.
   */
  def hasIdentityMatch( a1 : Artifact, a2 : Artifact, useVersion : Boolean = false ) = {
    if ( a1 == null || a2 == null ) {
      false
    } else {
      artifactIdentity( a1, useVersion ) == artifactIdentity( a2, useVersion )
    }
  }

  /**
   * Verify artifact has regex identity and a classifier.
   */
  def hasArtifactMatch( artifact : Artifact, regex : String, classifier : String ) : Boolean = {
    val hasIdentity = artifactIdentity( artifact ) match {
      case regex.r() => true
      case _         => false
    }
    val hasClassifier = artifact.getClassifier == classifier
    hasIdentity && hasClassifier
  }

  /**
   * Verify that artifact jar contains named resource.
   */
  def hasResourceMatch( artifact : Artifact, resource : String ) : Boolean = {
    val url = artifact.getFile.getCanonicalFile.toURI.toURL
    val loader = new URLClassLoader( Array[ URL ]( url ) );
    val result = loader.getResources( resource ).hasMoreElements
    loader.close
    result
  }

  /**
   * Find matching artifact in a list by identity regex and a classifier.
   */
  def extractArtifact(
    artifactList : Seq[ Artifact ], regex : String, classifier : String
  ) : Either[ Throwable, Artifact ] = {
    val collectList = artifactList.collect {
      case artifact if hasArtifactMatch( artifact, regex, classifier ) => artifact
    }
    collectList.toList match {
      case head :: Nil =>
        Right( head )
      case head :: tail =>
        Left( new RuntimeException( s"Duplicate artifact: ${regex}" ) )
      case Nil =>
        Left( new RuntimeException( s"Missing artifact: ${regex}" ) )
    }
  }

  /**
   * Find matching artifact in a project by identity regex.
   */
  def locateArtifact( project : MavenProject, regex : String ) : Option[ Artifact ] = {
    val matcher = regex.r.pattern.matcher( "" )
    project.getArtifacts.asScala.find { artifact =>
      import artifact._
      val identity = s"${getGroupId}:${getArtifactId}"
      matcher.reset( identity ).matches
    }
  }

  /**
   * Find matching artifact in a plugin by identity regex.
   */
  def locateArtifact( pluginMeta : PluginDescriptor, regex : String ) : Option[ Artifact ] = {
    val matcher = regex.r.pattern.matcher( "" )
    pluginMeta.getArtifacts.asScala.find { artifact =>
      import artifact._
      val identity = s"${getGroupId}:${getArtifactId}"
      matcher.reset( identity ).matches
    }
  }

  /**
   * Find matching dependency in a model by identity regex.
   */
  def locateDependency( model : Model, regex : String ) : Option[ Dependency ] = {
    val matcher = regex.r.pattern.matcher( "" )
    model.getDependencies.asScala.find { dependency =>
      import dependency._
      val identity = s"${getGroupId}:${getArtifactId}"
      matcher.reset( identity ).matches
    }
  }

  /**
   * Find matching file in a project/parent hierarchy.
   */
  def locateHierarchyFile( project : MavenProject, path : String, enableParent : Boolean ) : Option[ File ] = {
    if ( project == null ) {
      None
    } else {
      val file = new File( project.getBasedir, path )
      if ( file.exists ) {
        Some( file )
      } else if ( enableParent ) {
        locateHierarchyFile( project.getParent, path, enableParent )
      } else {
        None
      }
    }
  }

  val emptyXpp3Dom = new Xpp3Dom( "configuration" )

  def convertPlexusConfig( config : PlexusConfiguration ) : Xpp3Dom = {
    if ( config == null ) {
      return emptyXpp3Dom
    }
    PlexusConfigurationUtils.toXpp3Dom( config )
  }

}
