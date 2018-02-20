package com.carrotgarden.maven.scalor

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.ResolutionScope
import com.carrotgarden.maven.tools.Description
import java.util.Arrays

/**
 * Shared Scala.js JavaScript VM environment interface.
 */
trait ScalaJsEnvAnyMojo extends AbstractMojo
  with base.Mojo
  with base.Params
  with base.Logging
  with base.SkipMojo {

}

/**
 * Shared Scala.js JavaScript VM environment provisioning interface.
 */
trait ScalaJsEnvProvAnyMojo extends ScalaJsEnvAnyMojo {

  @Description( """
  Flag to skip environment provisioning.
  """ )
  @Parameter(
    property     = "scalor.skipEnvProv",
    defaultValue = "false"
  )
  var skipEnvProv : Boolean = _

  @Description( """
  Flag to force environment provisioning.
<pre>
false - use auto-detection
true  - skip detection of installed resources
</pre>
  """ )
  @Parameter(
    property     = "scalor.envprovForce",
    defaultValue = "false"
  )
  var envprovForce : Boolean = _

}

@Description( """
Provision Scala.js JavaScript VM environment for testing: 
install Node.js binary and NPM modules.
""" )
@Mojo(
  name                         = A.mojo.`scala-js-env-prov-nodejs`,
  defaultPhase                 = LifecyclePhase.GENERATE_TEST_RESOURCES,
  requiresDependencyResolution = ResolutionScope.NONE
)
class ScalaJsEnvProvNodeJsMojo extends ScalaJsEnvProvAnyMojo
  with scalajs.EnvProvNodejs {

  override def mojoName = A.mojo.`scala-js-env-prov-nodejs`

  override def perform() : Unit = {
    if ( skipEnvProv ) {
      reportSkipReason( "Skipping disabled goal execution." )
      return
    }
    if ( envprovForce ) {
      logger.info( "Forcing enironment provision." )
      provisionNodejs()
      provisionModules()
    } else {
      if ( hasDetectNodejs ) {
        logger.info( "Skipping provisioning, detected Node.js:" )
        logger.info( s"   ${configuredNodejs}" )
      } else {
        logger.info( "Provisioning enironment: Node.js." )
        provisionNodejs()
      }
      if ( hasDetectModules ) {
        logger.info( "Skipping provisioning, detected NPM modules:" )
        provisionedModuleList.foreach( file => logger.info( s"   ${file}" ) )
      } else {
        logger.info( "Provisioning enironment: NPM modules." )
        provisionModules()
      }
    }
  }

}

@Description( """
Provision Scala.js JavaScript VM environment for testing: 
install Phantom.js binary.
""" )
@Mojo(
  name                         = A.mojo.`scala-js-env-prov-phantomjs`,
  defaultPhase                 = LifecyclePhase.GENERATE_TEST_RESOURCES,
  requiresDependencyResolution = ResolutionScope.NONE
)
class ScalaJsEnvProvPhantomJsMojo extends ScalaJsEnvProvAnyMojo
  with scalajs.EnvProvPhantomjs {

  override def mojoName = A.mojo.`scala-js-env-prov-phantomjs`

  override def perform() : Unit = {
    if ( skipEnvProv ) {
      reportSkipReason( "Skipping disabled goal execution." )
      return
    }
    if ( envprovForce ) {
      logger.info( "Forcing enironment provision." )
      provisionPhantomjs()
    } else {
      if ( hasDetectPhantomjs ) {
        logger.info( "Skipping provisioning, detected Phantom.js:" )
        logger.info( s"   ${configuredPhantomjs}" )
      } else {
        logger.info( "Provisioning enironment: Phantom.js." )
        provisionPhantomjs()
      }
    }
  }

}

@Description( """
Provision Scala.js JavaScript VM environment for testing: 
install Webjars resources discovered from build classpath.
""" )
@Mojo(
  name                         = A.mojo.`scala-js-env-prov-webjars`,
  defaultPhase                 = LifecyclePhase.GENERATE_TEST_RESOURCES,
  requiresDependencyResolution = ResolutionScope.TEST
)
class ScalaJsEnvProvWebjarsMojo extends ScalaJsEnvProvAnyMojo
  with scalajs.EnvProvWebjars {

  override def mojoName = A.mojo.`scala-js-env-prov-webjars`

  override def perform() : Unit = {
    if ( skipEnvProv ) {
      reportSkipReason( "Skipping disabled goal execution." )
      return
    }
    logger.info( "Provisioning enironment: Webjars." )
    logger.info( s"   ${configuredWebjars}" )
    provisionWebjarsResources()
  }

}

/**
 * Shared Scala.js JavaScript VM environment configuration interface.
 */
trait ScalaJsEnvConfAnyMojo extends ScalaJsEnvAnyMojo
  with scalajs.EnvConfAny {

  @Description( """
  Flag to skip environment configuration.
  """ )
  @Parameter(
    property     = "scalor.skipEnvConf",
    defaultValue = "false"
  )
  var skipEnvConf : Boolean = _

  @Description( """
  Enable to log provided JS-VM environment configuration.
  Use to review actual settings used to create tesing JS-VM instance:
<pre>
  - full path to the executable
  - process launch arguments
  - process environment variables
  - webjars scripts settings
  - runtime.js module settings 
  - etc.
</pre>
  """ )
  @Parameter(
    property     = "scalor.envconfLogConfig",
    defaultValue = "false"
  )
  var envconfLogConfig : Boolean = _

  def reportConfiguration() : Unit = {
    if ( envconfLogConfig ) {
      logger.info( s"Configuration path: ${configurationLocation}" )
      logger.info( s"Configuration data:\n${renderConfig}" )
    }
  }

}

@Description( """
Configure Scala.js JavaScript VM environment for testing: 
provide scala-js-junit-tools settings for Node.js.
""" )
@Mojo(
  name                         = A.mojo.`scala-js-env-conf-nodejs`,
  defaultPhase                 = LifecyclePhase.PROCESS_TEST_RESOURCES,
  requiresDependencyResolution = ResolutionScope.TEST
)
class ScalaJsEnvConfNodeJsMojo extends ScalaJsEnvConfAnyMojo
  with scalajs.EnvConfNodejs {

  override def mojoName = A.mojo.`scala-js-env-conf-nodejs`

  override def perform() : Unit = {
    if ( skipEnvConf ) {
      reportSkipReason( "Skipping disabled goal execution." )
      return
    }
    logger.info( "Configuring enironment: Node.js" )
    configureEnvironment()
    reportConfiguration()
  }

}

@Description( """
Configure Scala.js JavaScript VM environment for testing: 
provide scala-js-junit-tools settings for Phantom.js.
""" )
@Mojo(
  name                         = A.mojo.`scala-js-env-conf-phantomjs`,
  defaultPhase                 = LifecyclePhase.PROCESS_TEST_RESOURCES,
  requiresDependencyResolution = ResolutionScope.TEST
)
class ScalaJsEnvConfPhantomJsMojo extends ScalaJsEnvConfAnyMojo
  with scalajs.EnvConfPhantomjs {

  override def mojoName = A.mojo.`scala-js-env-conf-phantomjs`

  override def perform() : Unit = {
    if ( skipEnvConf ) {
      reportSkipReason( "Skipping disabled goal execution." )
      return
    }
    logger.info( "Configuring enironment: Phantom.js" )
    configureEnvironment()
    reportConfiguration()
  }

}
