package com.carrotgarden.maven.design.extend

import org.codehaus.plexus.component.annotations._
import org.codehaus.plexus.component.configurator.BasicComponentConfigurator
import org.codehaus.plexus.component.configurator.ComponentConfigurator
import org.codehaus.plexus.logging.Logger
import org.codehaus.plexus.configuration.PlexusConfiguration
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator
import org.codehaus.plexus.component.configurator.ConfigurationListener
import org.codehaus.plexus.classworlds.realm.ClassRealm
import com.carrotgarden.maven.scalor.A._
import org.apache.maven.project.MavenProject
import org.apache.maven.execution.MavenSession

//
class Configurator extends BasicComponentConfigurator {

  /**
   * Use default logger.
   */
  @Requirement
  var logger : Logger = _

  /**
   * Current project.
   */
  @Requirement
  var project : MavenProject = _

  /**
   * Current session.
   */
  @Requirement
  var session : MavenSession = _

  //  /**
  //   * Scalor plugin descriptor.
  //   */
  //  @Requirement
  //  var pluginDescriptor : PluginDescriptor = _

  def userProps = session.getUserProperties

  def projectProps = project.getProperties

  def hasProperty( name : String ) =
    userProps.getProperty( name ) != null || projectProps.getProperty( name ) != null

  override def configureComponent(
    component :     Object,
    configuration : PlexusConfiguration,
    evaluator :     ExpressionEvaluator,
    realm :         ClassRealm,
    listener :      ConfigurationListener
  ) : Unit = {
    
    logger.info( s"ZZZ project ${project} session ${session} " )
    //    logger.info( s"ZZZ project ${project} session ${session} pluginDescriptor ${pluginDescriptor}" )

    logger.info( s"XXX component ${component} configuration ${configuration}" )

    super.configureComponent( component, configuration, evaluator, realm, listener )

  }

}
