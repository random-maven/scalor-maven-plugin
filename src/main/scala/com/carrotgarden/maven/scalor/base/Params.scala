package com.carrotgarden.maven.scalor.base

import java.io.File
import java.util.ArrayList
import java.util.Arrays
import java.util.Collections

import org.apache.maven.artifact.Artifact
import org.apache.maven.execution.MavenSession
import org.apache.maven.model.Dependency
import org.apache.maven.plugin.MojoExecution
import org.apache.maven.plugins.annotations.Component
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject

import scala.collection.JavaConverters._

import com.carrotgarden.maven.tools.Description
import org.apache.maven.toolchain.ToolchainManager

/**
 * Shared mojo execution configuration parameters.
 *
 * Provides injected components from maven runtime.
 */
trait Params {

  //  import Params._

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
  This mojo execution.
  """ )
  @Parameter( defaultValue = "${mojoExecution}", required = true, readonly = true )
  var mojoExecution : MojoExecution = _

  @Description( """
  Maven tool chain provider.
  """ )
  @Component()
  var toolchainManager : ToolchainManager = _

  //

  val propertySeparator = "\u0000"

  /**
   * Custom optonal project property.
   */
  def extractProperty( key : String ) : Option[ String ] = {
    Option( project.getProperties.getProperty( key ) )
  }

  /**
   * Custom optonal project property containing a list.
   */
  def extractPropertyList( key : String ) : Option[ java.util.List[ String ] ] = {
    extractProperty( key ).map( entry => Arrays.asList( entry.split( propertySeparator ) : _* ) )
  }

  /**
   * Custom optonal project property.
   */
  def persistProperty( key : String, value : String ) : Unit = {
    project.getProperties.setProperty( key, value )
  }

  /**
   * Custom optonal project property containing a list.
   */
  def persistPropertyList( key : String, sourceValue : String ) : Unit = {
    val sourceList = extractPropertyList( key ).getOrElse( Collections.emptyList() )
    val targetList = sourceValue.split( propertySeparator ) ++ sourceList.toArray
    val targetValue = targetList.mkString( propertySeparator )
    persistProperty( key, targetValue )
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

/**
 * Scala installation artifact descriptors.
 */
trait ParamsArtifact extends AnyRef
  with ParamsDefine
  with ParamsPluginList {

  @Description( """
  Maven identity of Scala library artifact.
  Regular expression in the form: '${groupId}:${artifactId}'.
  """ )
  @Parameter(
    property     = "scalor.artifactScalaLibrary",
    defaultValue = "org.scala-lang:scala-library"
  )
  var artifactScalaLibrary : String = _

  @Description( """
  Maven identity of Scala compiler artifact.
  Regular expression in the form: '${groupId}:${artifactId}'.
  """ )
  @Parameter(
    property     = "scalor.artifactScalaCompiler",
    defaultValue = "org.scala-lang:scala-compiler"
  )
  var artifactScalaCompiler : String = _

  @Description( """
  Maven identity of Scala reflect artifact.
  Regular expression in the form: '${groupId}:${artifactId}'.
  """ )
  @Parameter(
    property     = "scalor.artifactScalaReflect",
    defaultValue = "org.scala-lang:scala-reflect"
  )
  var artifactScalaReflect : String = _

  @Description( """
  Maven identity of Scala bridge artifact.
  Regular expression in the form: '${groupId}:${artifactId}'.
  """ )
  @Parameter(
    property     = "scalor.artifactCompilerBridge",
    defaultValue = "org.scala-sbt:compiler-bridge_.+"
  )
  var artifactCompilerBridge : String = _

  import com.carrotgarden.maven.scalor.util.Maven._
  import com.carrotgarden.maven.scalor.zinc.Module._

  // XXX remove
  def moduleType( artifact : Artifact ) : Type = {
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

/**
 * Provide required dependency definitions.
 */
trait ParamsDefine {

  import Params._

  @Description( """
  Provide required compiler bridge dependency.
  Can declare additional dependencies for bridge.
  Bridge artifact must match expected regular expression.
  Example:
&lt;defineBridge&gt;
  &lt;dependency&gt;
      &lt;groupId&gt;org.scala-sbt&lt;/groupId&gt;
      &lt;artifactId&gt;compiler-bridge_${version.scala.epoch}&lt;/artifactId&gt;
      &lt;version&gt;${version.scala.zinc}&lt;/version&gt;
  &lt;/dependency&gt;
&lt;/defineBridge&gt;
  """ )
  @Parameter(
    required = true
  )
  var defineBridge : Array[ Dependency ] = _

  @Description( """
  Provide required scala compiler dependency.
  Can declare additional dependencies for compiler.
  Compiler artifact must match expected regular expression.
  Example:
&lt;defineCompiler&gt;
  &lt;dependency&gt;
      &lt;groupId&gt;org.scala-lang&lt;/groupId&gt;
      &lt;artifactId&gt;scala-compiler&lt;/artifactId&gt;
      &lt;version&gt;${version.scala.release}&lt;/version&gt;
  &lt;/dependency&gt;
&lt;/defineCompiler&gt;
  """ )
  @Parameter(
    required = true
  )
  var defineCompiler : Array[ Dependency ] = _

  @Description( """
  Provide optional scala plugins dependency.
  Can declare mulitiple scala compiler plugins.
  Plugin artifact jar must contain expected descriptor resource. 
  Example:
&lt;definePluginList&gt;
  &lt;dependency&gt;
      &lt;groupId&gt;org.scala-js&lt;/groupId&gt;
      &lt;artifactId&gt;scalajs-compiler_${version.scala.release}&lt;/artifactId&gt;
      &lt;version&gt;${version.sjs.release}&lt;/version&gt;
  &lt;/dependency&gt;
&lt;/definePluginList&gt;
  """ )
  @Parameter(
    required = false
  )
  var definePluginList : Array[ Dependency ] = _

}

object Params {

  trait Define[ T ] {
    val defineBridge : List[ T ]
    val defineCompiler : List[ T ]
    val definePluginList : List[ T ]
  }

  //  /**
  //   * Use "Object with Fields" to get support
  //   * from Maven DefaultConfigurationConverter in M2E.
  //   */
  //  class DefineConfig {
  //    var groupId : String = _
  //    var artifactId : String = _
  //    var version : String = _
  //    override def toString = s"${groupId}:${artifactId}:${version}"
  //    def toDependency : Dependency = {
  //      val dependency = new Dependency()
  //      dependency.setGroupId( groupId )
  //      dependency.setArtifactId( artifactId )
  //      dependency.setVersion( version )
  //      dependency
  //    }
  //  }

  //    def convert( array : Array[ DefineConfig ] ) : List[ Dependency ] = {
  //      array.map( _.toDependency ).toList
  //    }

  /**
   * Cleanup user entries.
   */
  def convert( array : Array[ Dependency ] ) : List[ Dependency ] = {
    array.toList
  }

  case class DefineRequest(
    defineBridge :     List[ Dependency ],
    defineCompiler :   List[ Dependency ],
    definePluginList : List[ Dependency ]
  ) extends Define[ Dependency ]

  case class DefineResponse(
    defineBridge :     List[ Artifact ],
    defineCompiler :   List[ Artifact ],
    definePluginList : List[ Artifact ]
  ) extends Define[ Artifact ]

}
