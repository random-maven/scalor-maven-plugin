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

import com.carrotgarden.maven.scalor.util

/**
 * Shared mojo execution configuration parameters.
 *
 * Provides components injected by Maven runtime.
 */
trait Params extends ParamsProject {

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

}

trait ParamsProject {

  @Description( """
  Current maven project.
  """ )
  @Parameter( defaultValue = "${project}", required = true, readonly = true )
  var project : MavenProject = _

  val propertySeparator = "\u0000"

  /**
   * Custom optional project property.
   */
  def extractProperty( key : String ) : Option[ String ] = {
    Option( project.getProperties.getProperty( key ) )
  }

  /**
   * Custom optional project property containing a list.
   */
  def extractPropertyList( key : String ) : Option[ java.util.List[ String ] ] = {
    extractProperty( key ).map( entry => Arrays.asList( entry.split( propertySeparator ) : _* ) )
  }

  /**
   * Custom optional project property.
   */
  def persistProperty( key : String, value : String ) : Unit = {
    project.getProperties.setProperty( key, value )
  }

  /**
   * Custom optional project property containing a list.
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
  // FIXME remove
  def projectClassPath( bucket : Scope.Bucket = Scope.Select.Test ) : Array[ File ] = {
    import util.Folder._
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
            list.add( ensureCanonicalFile( file ) )
          }
        }
      }
    }
    list.toArray( Array[ File ]() )
  }

}

case class ParamsProjectUnit( proj : MavenProject ) extends ParamsProject {
  project = proj
}

/**
 * Common parameters.
 */
trait ParamsAny {

  @Description( """
  Common separator for plugin configuration list values provided in <code>pom.xml</code>.
  Separator regular expression is used as follows:
<pre>
  options.split( separator ).map( _.trim ).filterNot( _.isEmpty )
</pre>
  Note: <code>&lt;![CDATA[ ... ]]&gt;</code> brackets 
  can help preserve text entries in <code>pom.xml</code>.
  """ )
  @Parameter(
    property     = "scalor.commonSequenceSeparator",
    defaultValue = """[;\n]+"""
  )
  var commonSequenceSeparator : String = _

  /**
   * Produce clean options sequence.
   */
  def parseCommonSequence( options : String, separator : String ) : Array[ String ] = {
    options.split( separator ).map( _.trim ).filterNot( _.isEmpty )
  }

}

/**
 * Scala compiler installation definition.
 */
trait ParamsCompiler extends AnyRef
  with ParamsDefine
  with ParamsRegex {

}

/**
 * Provide required dependency definitions.
 */
trait ParamsDefine {

  import Params._

  @Description( """
  Provide required Scala compiler bridge dependency.
  Can declare here additional dependencies for the bridge.
  Bridge artifact must match expected regular expression in 
    <a href="#regexCompilerBridge"><b>regexCompilerBridge</b></a>.
  Example entry in <code>pom.xml</code>:
<pre>
&lt;defineBridge&gt;
  &lt;dependency&gt;
      &lt;groupId&gt;org.scala-sbt&lt;/groupId&gt;
      &lt;artifactId&gt;compiler-bridge_${version.scala.epoch}&lt;/artifactId&gt;
      &lt;version&gt;${version.scala.zinc}&lt;/version&gt;
  &lt;/dependency&gt;
&lt;/defineBridge&gt;
</pre>
  """ )
  @Parameter(
    required = true
  )
  var defineBridge : Array[ Dependency ] = Array.empty

  @Description( """
  Provide required Scala compiler dependency.
  Can declare here additional dependencies for the compiler.
  Compiler artifact must match expected regular expression in 
    <a href="#regexScalaCompiler"><b>regexScalaCompiler</b></a>.
  Example entry in <code>pom.xml</code>:
<pre>
&lt;defineCompiler&gt;
  &lt;dependency&gt;
      &lt;groupId&gt;org.scala-lang&lt;/groupId&gt;
      &lt;artifactId&gt;scala-compiler&lt;/artifactId&gt;
      &lt;version&gt;${version.scala.release}&lt;/version&gt;
  &lt;/dependency&gt;
&lt;/defineCompiler&gt;
</pre>
  """ )
  @Parameter(
    required = true
  )
  var defineCompiler : Array[ Dependency ] = Array.empty

  @Description( """
  Provide optional Scala plugins dependency.
  Can declare mulitiple scala compiler plugins.
  Plugin artifact jar must contain expected descriptor resource in 
    <a href="#resourcePluginDescriptor"><b>resourcePluginDescriptor</b></a>.
  Example entry in <code>pom.xml</code>:
<pre>
&lt;definePluginList&gt;
  &lt;dependency&gt;
      &lt;groupId&gt;org.scala-js&lt;/groupId&gt;
      &lt;artifactId&gt;scalajs-compiler_${version.scala.release}&lt;/artifactId&gt;
      &lt;version&gt;${version.sjs.release}&lt;/version&gt;
  &lt;/dependency&gt;
&lt;/definePluginList&gt;
</pre>
  """ )
  @Parameter(
    required     = true,
    defaultValue = ""
  )
  var definePluginList : Array[ Dependency ] = Array.empty

}

trait ParamsRegex {

  @Description( """
  Maven identity of Scala bridge artifact.
  Regular expression in the form: <code>${groupId}:${artifactId}</code>.
  Used for auto discovery of compiler-bridge from 
    <a href="#defineBridge"><b>defineBridge</b></a>.
  """ )
  @Parameter(
    property     = "scalor.regexCompilerBridge",
    defaultValue = "org.scala-sbt:compiler-bridge_.+"
  )
  var regexCompilerBridge : String = _

  @Description( """
  Maven identity of Scala compiler artifact.
  Regular expression in the form: <code>${groupId}:${artifactId}</code>.
  Used for auto discovery of scala-compiler from 
    <a href="#defineCompiler"><b>defineCompiler</b></a>.
  """ )
  @Parameter(
    property     = "scalor.regexScalaCompiler",
    defaultValue = "org.scala-lang:scala-compiler"
  )
  var regexScalaCompiler : String = _

  @Description( """
  Maven identity of Scala library artifact.
  Regular expression in the form: <code>${groupId}:${artifactId}</code>.
  Used for auto discovery of scala-library from 
    <a href="#defineCompiler"><b>defineCompiler</b></a>.
  """ )
  @Parameter(
    property     = "scalor.regexScalaLibrary",
    defaultValue = "org.scala-lang:scala-library"
  )
  var regexScalaLibrary : String = _

  @Description( """
  Maven identity of Scala reflect artifact.
  Regular expression in the form: <code>${groupId}:${artifactId}</code>.
  Used for auto discovery of scala-reflect from 
    <a href="#defineCompiler"><b>defineCompiler</b></a>.
  """ )
  @Parameter(
    property     = "scalor.regexScalaReflect",
    defaultValue = "org.scala-lang:scala-reflect"
  )
  var regexScalaReflect : String = _

  @Description( """
  Scala compiler plugin descriptor file name, stored inside compiler plugin jar.
  Used for auto discovery of Scala compiler plugins form 
    <a href="#definePluginList"><b>definePluginList</b></a>.
  Plain file name, not a regex.
  """ )
  @Parameter(
    property     = "scalor.resourcePluginDescriptor",
    defaultValue = "scalac-plugin.xml"
  )
  var resourcePluginDescriptor : String = _

}

object Params {

  /**
   * Artifact set definition.
   */
  trait Define[ T ] {
    val defineBridge : List[ T ]
    val defineCompiler : List[ T ]
    val definePluginList : List[ T ]
  }

  /**
   * Cleanup user entries.
   */
  def convert( array : Array[ Dependency ] ) : List[ Dependency ] = {
    array.toList
  }

  /**
   * Maven resolution request.
   */
  case class DefineRequest(
    defineBridge :     List[ Dependency ],
    defineCompiler :   List[ Dependency ],
    definePluginList : List[ Dependency ]
  ) extends Define[ Dependency ]

  /**
   * Maven resolution response.
   */
  case class DefineResponse(
    defineBridge :     List[ Artifact ],
    defineCompiler :   List[ Artifact ],
    definePluginList : List[ Artifact ]
  ) extends Define[ Artifact ]

}
