package com.carrotgarden.maven.scalor.scalajs

import org.twdata.maven.mojoexecutor.MojoExecutor._
import com.carrotgarden.maven.scalor.base

import java.io.File

import com.carrotgarden.maven.tools.Description
import org.apache.maven.plugins.annotations.Parameter

import com.carrotgarden.maven.scalor._
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.nio.file.Paths
import org.webjars.WebJarExtractor
import org.webjars.WebJarExtractor.MemoryCache
import java.net.URLClassLoader

/**
 * Common Scala.js VM environment configuration.
 */
trait EnvConfAny extends AnyRef
  with base.ParamsAny {

  @Description( """
  Scala.js testing module script used to run tests inside JavaScript VM environment.
  Normally testing module script path should point to the result of Scala.js linker invocation. 
  """ )
  @Parameter(
    property     = "scalor.envconfModulePath",
    defaultValue = "${project.build.testOutputDirectory}/META-INF/resources/script-test/runtime-test.js"
  )
  var envconfModulePath : File = _

  @Description( """
  Folder with extracted webjars resources.
  Specific scripts should be activated in JavaScript VM via parameter:
  <a href="#envconfWebjarsScriptList"><b>envconfWebjarsScriptList</b></a>
  Normally webjars folder should point to the extraction result of the webjars provisioning invocation.
  Absolute path.
  """ )
  @Parameter(
    property     = "scalor.envconfWebjarsFolder",
    defaultValue = "${project.basedir}/test-tool/webjars"
  )
  var envconfWebjarsFolder : File = _

  @Description( """
  List of scripts which should be activated inside JavaScript VM during tests.
  These scripts must be provided inside the provisioning folder:
  <a href="#envconfWebjarsFolder"><b>envconfWebjarsFolder</b></a>.
  Separator parameter: <a href="#commonSequenceSeparator"><b>commonSequenceSeparator</b></a>.
  Relative path.
  """ )
  @Parameter(
    property     = "scalor.envconfWebjarsScriptList",
    defaultValue = """
    """
  )
  var envconfWebjarsScriptList : String = _

  import com.carrotgarden.sjs.junit._

  def paramExec : File
  def paramArgs : String
  def paramVars : String
  def paramType : String

  def configurationLocation : File = {
    Context.configLocation
  }

  def renderConfig : String = {
    val config = Context.configExtract()
    util.Classer.prettyPrint( config )
  }

  /**
   * Provide Scala.js JavaScript VM execution environment configuration.
   */
  def configureEnvironment() : Unit = {

    import Config._

    // JS-VM settings.
    val envExec = paramExec.getCanonicalPath
    val envArgs = parseCommonList( paramArgs ).toList
    val envVars = parseCommonMapping( paramVars )
    val envType = paramType
    val envConf = EnvConf(
      envExec = envExec,
      envArgs = envArgs,
      envVars = envVars,
      envType = envType
    )

    // Webjars resources.
    val webjarsDir = envconfWebjarsFolder.getCanonicalPath
    val scriptList = parseCommonList( envconfWebjarsScriptList ).toList
    val webConf = WebConf(
      webjarsDir = webjarsDir,
      scriptList = scriptList
    )

    // Generated runtime.js
    val path = envconfModulePath.getCanonicalPath
    val module = Config.Module(
      path = path
    )

    // Final configuration.
    val config = Config(
      envConf = envConf,
      webConf = webConf,
      module  = module
    )

    // Publish to default location.
    Context.configPersist( config )
  }

}

/**
 * Node.js Scala.js VM environment configuration.
 */
trait EnvConfNodejs extends EnvConfAny {

  @Description( """
  Absolute path to the Node.js JavaScript VM executable.
  Normally should point to the provisioning extraction result.
  """ )
  @Parameter(
    property     = "scalor.envconfNodejsExec",
    defaultValue = "${project.basedir}/test-tool/node/node"
  )
  var envconfNodejsExec : File = _

  @Description( """
  Arguments for the Node.js JavaScript VM executable.
  Separator parameter: <a href="#commonSequenceSeparator"><b>commonSequenceSeparator</b></a>.
  """ )
  @Parameter(
    property     = "scalor.envconfNodejsArgs",
    defaultValue = """
    """
  )
  var envconfNodejsArgs : String = _

  @Description( """
  Environment variables for the Node.js JavaScript VM executable.
  Normally should define <code>NODE_PATH</code> to point to the Node.js modules provisioning result.
  Separator parameter: <a href="#commonSequenceSeparator"><b>commonSequenceSeparator</b></a>.
  Mapping parameter: <a href="#commonMappingPattern"><b>commonMappingPattern</b></a>.
  Some Node.js environment variables:
<pre>
NODE_MODULE_CONTEXTS - set to 0 to disable inter-module isolation via sandboxing
NODE_DEBUG - set to comma-separated list of module names to enable logging
NODE_PATH - set to absolute path to the provisioned node_modules folder
</pre>
  """ )
  @Parameter(
    property     = "scalor.envconfNodejsVars",
    defaultValue = """
    NODE_MODULE_CONTEXTS=0 ★
    NODE_PATH=${project.basedir}/test-tool/node/node_modules ★
    """
  )
  var envconfNodejsVars : String = _

  @Description( """
  Select JS-VM type.
  Available Node.js JavaScript VM environment types:
<pre>
nodejs-basic - basic Node.js, for non-UI testing, does not expect any node modules
nodejs-jsdom - Node.js with JSdom.js, for broswer-UI testing, must provision 'jsdom' module
</pre>
  """ )
  @Parameter(
    property     = "scalor.envconfNodejsType",
    defaultValue = "nodejs-jsdom"
  )
  var envconfNodejsType : String = _

  override def paramExec = envconfNodejsExec
  override def paramArgs = envconfNodejsArgs
  override def paramVars = envconfNodejsVars
  override def paramType = envconfNodejsType

}

/**
 * Phantom.js Scala.js VM environment configuration.
 */
trait EnvConfPhantomjs extends EnvConfAny {

  @Description( """
  Absolute path to the Phantom.js JavaScript VM executable.
  Normally should point to the provisioning extraction result.
  """ )
  @Parameter(
    property     = "scalor.envconfPhantomjsExec",
    defaultValue = "${project.basedir}/test-tool/phantomjs/phantomjs"
  )
  var envconfPhantomjsExec : File = _

  @Description( """
  Arguments for the Phantom.js JavaScript VM executable.
  Separator parameter: <a href="#commonSequenceSeparator"><b>commonSequenceSeparator</b></a>.
  """ )
  @Parameter(
    property     = "scalor.envconfPhantomjsArgs",
    defaultValue = """
    """
  )
  var envconfPhantomjsArgs : String = _

  @Description( """
  Environment variables for the Phantom.js JavaScript VM executable.
  Separator parameter: <a href="#commonSequenceSeparator"><b>commonSequenceSeparator</b></a>.
  """ )
  @Parameter(
    property     = "scalor.envconfPhantomjsVars",
    defaultValue = """
    """
  )
  var envconfPhantomjsVars : String = _

  @Description( """
  Select JS-VM type.
  Available Phantom.js JavaScript VM environment types:
<pre>
phantomjs-basic - basic Phantom.js, for broswer-UI testing
</pre>
  """ )
  @Parameter(
    property     = "scalor.envconfPhantomjsType",
    defaultValue = "phantomjs-basic"
  )
  var envconfPhantomjsType : String = _

  override def paramExec = envconfPhantomjsExec
  override def paramArgs = envconfPhantomjsArgs
  override def paramVars = envconfPhantomjsVars
  override def paramType = envconfPhantomjsType

}

/**
 * Shared external mojo execution parameters.
 */
trait EnvProvAny extends AnyRef
  with base.ParamsAny {

  @Description( """
  Scala.js test-tool installaton folder used for testing.
  Contains provisioned Node.js, Phantom.js, Webjars, etc.
  Normally this folder should be excluded from source control
  and should not be a part of any <code>clean</code> execution.
  Absolute path.
  When changing, make sure to replace <code>test-tool<code> references elsewhere.
  """ )
  @Parameter(
    property     = "scalor.envprovTestToolFolder",
    defaultValue = "${project.basedir}/test-tool"
  )
  var envprovTestToolFolder : File = _

  def folderTestTool : String = {
    val folder = envprovTestToolFolder.getCanonicalFile
    folder.mkdirs
    folder.getCanonicalPath
  }

  def hasDetectExecutable( file : File ) = {
    file.exists() && file.isFile() && file.canRead() && file.canExecute()
  }

}

/**
 * Execute external plugin Mojo.
 *
 * https://github.com/eirslett/frontend-maven-plugin
 */
trait EnvProvNodejs extends EnvProvAny {

  self : base.Params =>

  @Description( """
  A folder inside
  <a href="#envprovTestToolFolder"><b>envprovTestToolFolder</b></a> 
  which contains extracted Node.js.
  Relative path.
  """ )
  @Parameter(
    property     = "scalor.envprovNodejsFolder",
    defaultValue = "node"
  )
  var envprovNodejsFolder : String = _

  @Description( """
  Node.js version used to provision node installation.
  Select from <a href="https://nodejs.org/en/">availabe versions</a>. 
  """ )
  @Parameter(
    property     = "scalor.envprovNodejsVersion",
    defaultValue = "9.5.0"
  )
  var envprovNodejsVersion : String = _

  @Description( """
  List of Node.js NPM modules configured for provisioning.
  Uses format: <code>module@version<code>.
  Separator parameter: <a href="#commonSequenceSeparator"><b>commonSequenceSeparator</b></a>.
  Used by <code>npm install<code>.
  """ )
  @Parameter(
    property     = "scalor.envprovRnpmjsModules",
    defaultValue = """
    npm@5.6.0 ★ 
    jsdom@11.6.0 ★ 
    source-map-support@0.5.2 ★
    """
  )
  var envprovRnpmjsModules : String = _

  @Description( """
  List of additional NPM invocation options.
  Separator parameter: <a href="#commonSequenceSeparator"><b>commonSequenceSeparator</b></a>.
  Used by <code>npm install<code>.
  """ )
  @Parameter(
    property     = "scalor.envprovRnpmjsOptions",
    defaultValue = """
    --save ★
    --silent ★
    """
  )
  var envprovRnpmjsOptions : String = _

  @Description( """
  URL of the Node.js distribution download.
  """ )
  @Parameter(
    property     = "scalor.envprovNodejsURL",
    defaultValue = "http://nodejs.org/dist"
  )
  var envprovNodejsURL : String = _

  @Description( """
  URL of the Node.js NPM module registry.
  Used by <code>npm install<code>.
  """ )
  @Parameter(
    property     = "scalor.envprovRnpmjsURL",
    defaultValue = "http://registry.npmjs.org"
  )
  var envprovRnpmjsURL : String = _

  def npmModuleList = parseCommonList( envprovRnpmjsModules )

  def npmInstallOptions = parseCommonList( envprovRnpmjsOptions )

  def folderTestToolNodejs : String = {
    val folder = new File( envprovTestToolFolder, envprovNodejsFolder )
    folder.mkdirs()
    folder.getCanonicalPath
  }

  def folderTestToolModules : String = {
    val folder = new File( folderTestToolNodejs, "node_modules" )
    folder.mkdirs()
    folder.getCanonicalPath
  }

  def folderWorkDir : String = {
    val folder = project.getBasedir
    folder.mkdirs()
    folder.getCanonicalPath
  }

  def provideNodeVersion = {
    if ( envprovNodejsVersion.startsWith( "v" ) ) {
      envprovNodejsVersion
    } else {
      "v" + envprovNodejsVersion
    }
  }

  val nodeModuleRegex = """([^@]+)(@[.\d]+)?""".r

  def parseModuleName( entry : String ) = {
    entry match {
      case nodeModuleRegex( name, _ ) => name
    }
  }

  def provisionedNodejs : File = {
    val folder = folderTestToolNodejs
    val exec = "node" // Node.js convention.
    val file = new File( folder, exec )
    file.getCanonicalFile
  }

  def provisionedModuleList : List[ File ] = {
    val folder = folderTestToolModules
    npmModuleList
      .map( entry => parseModuleName( entry ) )
      .map( name => new File( folder, name ) )
      .toList
  }

  /**
   * Verify if configured Node.js binary is present.
   */
  def hasDetectNodejs = {
    hasDetectExecutable( provisionedNodejs )
  }

  /**
   * Verify if configured NPM modules are present.
   */
  def hasDetectModules = {
    provisionedModuleList.forall( file => file.exists )
  }

  /**
   * Install Node.js binary.
   */
  def provisionNodejs() : Unit = {
    val nodeVersion = provideNodeVersion
    //
    executeMojo( //
      plugin( //
        groupId( "com.github.eirslett" ), //
        artifactId( "frontend-maven-plugin" ), //
        version( "1.6" ) //
      ), //
      goal( "install-node-and-npm" ), //
      configuration( //
        element( "nodeVersion", nodeVersion ), //
        element( "nodeDownloadRoot", envprovNodejsURL ), //
        element( "workingDirectory", folderWorkDir ), //
        element( "installDirectory", folderTestTool ) //
      ), //
      executionEnvironment( project, session, buildManager ) //
    )
    //
    val target = provisionedNodejs
    target.setReadable( true )
    target.setExecutable( true )
  }

  /**
   * Invoke "npm install" to provision node_modules.
   */
  def provisionModules() : Unit = {
    val modules = npmModuleList.mkString( " " )
    val options = npmInstallOptions.mkString( " " )
    val prefix = s"--prefix ${folderTestToolNodejs}"
    val arguments = s"install ${modules} ${prefix} ${options}"
    //
    executeMojo( //
      plugin( //
        groupId( "com.github.eirslett" ), //
        artifactId( "frontend-maven-plugin" ), //
        version( "1.6" ) //
      ), //
      goal( "npm" ), //
      configuration( //
        element( "arguments", arguments ), //
        element( "npmRegistryURL", envprovRnpmjsURL ), //
        element( "workingDirectory", folderWorkDir ), //
        element( "installDirectory", folderTestTool ) //
      ), //
      executionEnvironment( project, session, buildManager ) //
    )
  }

}

/**
 * Execute external plugin Mojo.
 *
 * https://github.com/klieber/phantomjs-maven-plugin
 */
trait EnvProvPhantomjs extends EnvProvAny {

  self : base.Params =>

  @Description( """
  Phantom.js distribution version.
  Must be present on Maven Central.
  """ )
  @Parameter(
    property     = "scalor.envprovPhantomjsVersion",
    defaultValue = "2.1.1"
  )
  var envprovPhantomjsVersion : String = _

  @Description( """
  A folder inside
  <a href="#envprovTestToolFolder"><b>envprovTestToolFolder</b></a> 
  which contains extracted Phantom.js.
  Relative path.
  """ )
  @Parameter(
    property     = "scalor.envprovPhantomjsFolder",
    defaultValue = "phantomjs"
  )
  var envprovPhantomjsFolder : String = _

  def folderTestToolPhantomjs : String = {
    val folder = new File( envprovTestToolFolder, envprovPhantomjsFolder )
    folder.mkdirs()
    folder.getCanonicalPath
  }

  def provisionedPhantomjs : File = {
    val folder = folderTestToolPhantomjs
    val exec = "phantomjs" // Phantom.js convention.
    val file = new File( folder, exec )
    file.getCanonicalFile
  }

  /**
   * Verify if configured Phantom.js binary is present.
   */
  def hasDetectPhantomjs = {
    hasDetectExecutable( provisionedPhantomjs )
  }

  /**
   * Phantom.js executable extraction path.
   */
  val phantomExtractKey = "phantomjs.binary"

  def extractedPhantomjs = {
    new File( project.getProperties.getProperty( phantomExtractKey ) )
  }

  /**
   * Install Phantom.js binary.
   */
  def provisionPhantomjs() : Unit = {
    //
    executeMojo( //
      plugin( //
        groupId( "com.github.klieber" ), //
        artifactId( "phantomjs-maven-plugin" ), //
        version( "0.7" ) //
      ), //
      goal( "install" ), //
      configuration( //
        element( "version", envprovPhantomjsVersion ), //
        element( "propertyName", phantomExtractKey ), //
        element( "outputDirectory", folderTestToolPhantomjs ) //
      ), //
      executionEnvironment( project, session, buildManager ) //
    )
    //
    val source = extractedPhantomjs
    val target = provisionedPhantomjs
    Files.copy( source.toPath, target.toPath, StandardCopyOption.REPLACE_EXISTING )
    target.setReadable( true )
    target.setExecutable( true )
  }

}

/**
 * Provision discovered webjars resources.
 */
trait EnvProvWebjars extends EnvProvAny {

  self : base.Params =>

  @Description( """
  A folder inside
  <a href="#envprovTestToolFolder"><b>envprovTestToolFolder</b></a> 
  which contains extracted Webjars.
  Relative path.
  """ )
  @Parameter(
    property     = "scalor.envprovWebjarsFolder",
    defaultValue = "webjars"
  )
  var envprovWebjarsFolder : String = _

  /**
   * Webjars extraction folder.
   */
  def provisionedWebjars : File = {
    val folder = new File( folderTestTool, envprovWebjarsFolder )
    folder.mkdirs()
    folder.getCanonicalFile
  }

  /**
   * Webjars discovery class path.
   */
  def webjarsClassLoader : URLClassLoader = {
    val entryList = projectClassPath().map( _.toURI.toURL )
    new URLClassLoader( entryList )
  }

  /**
   * Provision discovered webjars resources.
   */
  def provisionWebjarsResources() : Unit = {
    val cache = new MemoryCache()
    val loader = webjarsClassLoader
    val extractor = new WebJarExtractor( cache, loader )
    extractor.extractAllWebJarsTo( provisionedWebjars )
  }

}
