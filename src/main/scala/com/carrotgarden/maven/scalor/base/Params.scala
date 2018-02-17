package com.carrotgarden.maven.scalor.base

import java.io.File
import java.util.ArrayList
import java.util.Arrays
import java.util.Collections

import org.apache.maven.artifact.Artifact
import org.apache.maven.execution.MavenSession
import org.apache.maven.model.Dependency
import org.apache.maven.plugin.BuildPluginManager
import org.apache.maven.plugin.MojoExecution
import org.apache.maven.plugins.annotations.Component
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject
import org.apache.maven.toolchain.ToolchainManager

import com.carrotgarden.maven.tools.Description
import com.carrotgarden.maven.scalor.util.Maven
import com.carrotgarden.maven.scalor.util.Error.Throw
import com.carrotgarden.maven.scalor.meta.Macro
import org.apache.maven.plugin.descriptor.PluginDescriptor

/**
 * Shared mojo execution configuration parameters.
 *
 * Provides components injected by Maven runtime.
 */
trait Params extends AnyRef
  with ParamsPlugin
  with ParamsProject {

}

trait ParamsPlugin {

  @Description( """
  Maven build session.
  """ )
  @Parameter( defaultValue = "${session}", required = true, readonly = true )
  var session : MavenSession = _

  @Description( """
  This plugin descriptor.
  """ )
  @Parameter( defaultValue = "${plugin}", required = true, readonly = true )
  var pluginMeta : PluginDescriptor = _

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

  @Description( """
  Maven build plugin manager component.
  """ )
  @Component()
  var buildManager : BuildPluginManager = _

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
  // FIXME switch to aether
  def projectClassPath( bucket : Scope.Bucket = Scope.Select.Test ) : Array[ File ] = {
    import com.carrotgarden.maven.scalor.util.Folder._
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
 * Common parameters.
 */
trait ParamsAny {

  @Description( """
  Separator for plugin configuration list values provided in <code>pom.xml</code>.
  Separator regular expression is used as follows:
<pre>
  string.split( commonSequenceSeparator ).map( _.trim ).filterNot( _.isEmpty )
</pre>
  Note: <code>&lt;![CDATA[ ... ]]&gt;</code> brackets can help preserve text entries in <code>pom.xml</code>.
  Note: to insert unicode symbol in Eclipse/GTK, type CTRL+SHIFT+U, then XXXX - a 4-hex-digit unicode value.
  For example, for star ★, use hex code 2605.
  """ )
  @Parameter(
    property     = "scalor.commonSequenceSeparator",
    defaultValue = """[★\n]+"""
  )
  var commonSequenceSeparator : String = _

  @Description( """
  Regular expression for plugin configuration map values provided in <code>pom.xml</code>.
  Extractor for pattern: <code>key=value</code>.
  Must define exactly two regex capture groups.
  Mapping regular expression is used as follows:
<pre>
  case commonMappingPattern.r( key, value ) => ( key, value )
</pre>
  Note: <code>&lt;![CDATA[ ... ]]&gt;</code> brackets can help preserve text entries in <code>pom.xml</code>.
  """ )
  @Parameter(
    property     = "scalor.commonMappingPattern",
    defaultValue = """\s*([^=\s]+)\s*=\s*([^\s]+)\s*"""
  )
  var commonMappingPattern : String = _

  /**
   * Produce clean options list.
   */
  def parseCommonList( options : String ) : Array[ String ] = {
    val separator = commonSequenceSeparator
    options.split( separator ).map( _.trim ).filterNot( _.isEmpty )
  }

  /**
   * Produce clean options mapping.
   */
  def parseCommonMapping( options : String ) : Map[ String, String ] = {
    val regexKeyValue = commonMappingPattern.r
    val termList = parseCommonList( options )
    val entryList = termList.collect {
      case regexKeyValue( key, value ) => ( key, value )
    }
    entryList.toMap
  }

}

/**
 * Scala compiler installation definition.
 */
trait ParamsCompiler extends AnyRef
  with ParamsDefine
  with ParamsRegex {

  import scala.tools.nsc.settings.ScalaVersion
  import scala.tools.nsc.settings.SpecificScalaVersion
  import com.carrotgarden.maven.scalor.zinc.Version

  /**
   * Locate plugin bridge dependency.
   */
  def defineFindBridge() : Dependency = {
    Self.pluginDependency( regexCompilerBridge ).getOrElse(
      Throw( s"Missing bridge, see: ${Macro.nameOf( defineAuto )}, ${Macro.nameOf( regexCompilerBridge )}." )
    )
  }

  /**
   * Locate plugin compiler dependency.
   */
  def defineFindCompiler() : Dependency = {
    Self.pluginDependency( regexScalaCompiler ).getOrElse(
      Throw( s"Missing compiler, see: ${Macro.nameOf( defineAuto )}, ${Macro.nameOf( regexScalaCompiler )}." )
    )
  }

  /**
   * Locate project library dependency.
   */
  def defineFindLibrary( project : MavenProject ) : Artifact = {
    Maven.locateArtifact( project, regexScalaLibrary ).getOrElse(
      Throw( s"Missing library, see: ${Macro.nameOf( defineAuto )}, ${Macro.nameOf( regexScalaLibrary )}." )
    )
  }

  /**
   * Extract project library version.
   */
  def defineLibraryVersion( project : MavenProject ) : SpecificScalaVersion = {
    val library = defineFindLibrary( project )
    ScalaVersion( library.getVersion ).asInstanceOf[ SpecificScalaVersion ]
  }

  /**
   * Construct bridge dependency.
   */
  def defineMakeBridge( project : MavenProject ) : Dependency = {
    val scalaVersion = defineLibraryVersion( project )
    val scalaVersionEpoch = Version.scalaVersionEpoch( scalaVersion )
    val scalaVersionRelease = Version.scalaVersionRelease( scalaVersion )
    val bridgeSource = defineFindBridge()
    val bridgeSourceId = bridgeSource.getArtifactId
    val bridgeArtifactPast = Version.artifactVersion( bridgeSourceId ).getOrElse(
      Throw( s"Can not parse bridge: regex=${Version.atifactVersionRegex} artifact=${bridgeSourceId}." )
    )
    val bridgeArtifactNext = bridgeArtifactPast.copy( versionTail = scalaVersionEpoch )
    val bridgeTarget = new Dependency()
    bridgeTarget.setGroupId( bridgeSource.getGroupId )
    bridgeTarget.setArtifactId( bridgeArtifactNext.unparse )
    bridgeTarget.setVersion( bridgeSource.getVersion )
    bridgeTarget
  }

  /**
   * Construct compiler dependency.
   */
  def defineMakeCompiler( project : MavenProject ) : Dependency = {
    val scalaVersion = defineLibraryVersion( project )
    val scalaVersionEpoch = Version.scalaVersionEpoch( scalaVersion )
    val scalaVersionRelease = Version.scalaVersionRelease( scalaVersion )
    val compilerSource = defineFindCompiler()
    val compilerTarget = new Dependency()
    compilerTarget.setGroupId( compilerSource.getGroupId )
    compilerTarget.setArtifactId( compilerSource.getArtifactId )
    compilerTarget.setVersion( scalaVersionRelease )
    compilerTarget
  }

  /**
   * Provide bridge via auto-discovery.
   */
  def defineAutoBridge( project : MavenProject ) : Array[ Dependency ] = {
    if ( defineBridge == null ) {
      defineBridge = Array.empty
    }
    if ( defineBridge.nonEmpty ) {
      defineBridge
    } else if ( defineBridge.isEmpty && defineAuto ) {
      Array( defineMakeBridge( project ) )
    } else {
      Throw( s"Missing bridge, see: ${Macro.nameOf( defineAuto )}, ${Macro.nameOf( defineBridge )}." )
    }
  }

  /**
   * Provide compiler via auto-discovery.
   */
  def defineAutoCompiler( project : MavenProject ) : Array[ Dependency ] = {
    if ( defineCompiler == null ) {
      defineCompiler = Array.empty
    }
    if ( defineCompiler.nonEmpty ) {
      defineCompiler
    } else if ( defineCompiler.isEmpty && defineAuto ) {
      Array( defineMakeCompiler( project ) )
    } else {
      Throw( s"Missing compiler, see: ${Macro.nameOf( defineAuto )}, ${Macro.nameOf( defineCompiler )}." )
    }
  }

  /**
   * Provide compiler plugin definitions.
   */
  def defineAutoPluginList( project : MavenProject ) : Array[ Dependency ] = {
    if ( definePluginList == null ) {
      definePluginList = Array.empty
    }
    definePluginList
  }

}

/**
 * Provide compiler dependency definitions.
 */
trait ParamsDefine {

  @Description( """
  Enable auto-discovery of 
  <a href="#defineBridge"><b>defineBridge</b></a> and
  <a href="#defineCompiler"><b>defineCompiler</b></a> dependencies.
  When <code>false</code>, bridge and compiler definitions must be explicitly provided in <code>pom.xml</code>.
  When <code>true</code>, plugin will use the following bridge and compiler dependency discovery heuristic:
<ol>
  <li>check for bridge and compiler defined in <code>pom.xml</code>, if yes - use that, if not - try auto define:</li>
  <li>find <code>scala-library</code> on project build class path with help of <a href="#regexScalaLibrary"><b>regexScalaLibrary</b></a></li>
  <li>determine Scala epoch <code>X.Y</code> and release <code>X.Y.Z</code> versions of Scala Library</li>
  <li>construct <code>compiler-bridge</code> dependency from:
    <ul>
        <li>discovered project Scala epoch version</li>
        <li>bridge artifact included with the plugin</li>
        <li>with help of <a href="#regexCompilerBridge"><b>regexCompilerBridge</b></a></li>
    </ul>
  </li>
  <li>construct <code>scala-compiler</code> dependency from:
    <ul>
        <li>discovered project Scala release version</li>
        <li>compiler artifact included with the plugin</li>
        <li>with help of <a href="#regexScalaCompiler"><b>regexScalaCompiler</b></a></li>
    </ul>
  </li>
</ol>
  Use <a href="#zincLogBridgeClassPath"><b>zincLogBridgeClassPath</b></a> 
  to review actual resolved bridge artifact in Maven.
  Use <a href="#zincLogCompilerClassPath"><b>zincLogCompilerClassPath</b></a> 
  to review actual resolved compiler artifact in Maven.
  Use <a href="#eclipseLogPersistInstall"><b>eclipseLogPersistInstall</b></a> 
  to review actual resolved compiler artifact in Eclipse (bridge is managed by Scala IDE).
  """ )
  @Parameter(
    property     = "scalor.defineAuto",
    defaultValue = "true"
  )
  var defineAuto : Boolean = _

  @Description( """
  Provide Scala compiler bridge dependency.
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
  This dependency list is empty by default.
  """ )
  @Parameter(
    defaultValue = ""
  )
  var defineBridge : Array[ Dependency ] = Array.empty

  @Description( """
  Provide Scala compiler dependency.
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
  This dependency list is empty by default.
  """ )
  @Parameter(
    defaultValue = ""
  )
  var defineCompiler : Array[ Dependency ] = Array.empty

  @Description( """
  Provide Scala plugins dependency.
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
  This dependency list is empty by default.
  """ )
  @Parameter(
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

  //  @Description( """
  //  Maven identity of Scala reflect artifact.
  //  Regular expression in the form: <code>${groupId}:${artifactId}</code>.
  //  Used for auto discovery of scala-reflect from
  //    <a href="#defineCompiler"><b>defineCompiler</b></a>.
  //  """ )
  //  @Parameter(
  //    property     = "scalor.regexScalaReflect",
  //    defaultValue = "org.scala-lang:scala-reflect"
  //  )
  //  var regexScalaReflect : String = _

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
    val defineBridge : Seq[ T ]
    val defineCompiler : Seq[ T ]
    val definePluginList : Seq[ T ]
  }

  /**
   * Maven resolution request.
   */
  case class DefineRequest(
    defineBridge :     Seq[ Dependency ],
    defineCompiler :   Seq[ Dependency ],
    definePluginList : Seq[ Dependency ]
  ) extends Define[ Dependency ]

  /**
   * Maven resolution response.
   */
  case class DefineResponse(
    defineBridge :     Seq[ Artifact ],
    defineCompiler :   Seq[ Artifact ],
    definePluginList : Seq[ Artifact ]
  ) extends Define[ Artifact ]

}
