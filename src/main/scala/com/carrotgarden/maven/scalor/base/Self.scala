package com.carrotgarden.maven.scalor.base

import com.carrotgarden.maven.scalor.util
import org.apache.maven.model.Model
import org.apache.maven.model.io.xpp3.MavenXpp3Reader
import org.apache.maven.model.Dependency
import scala.collection.JavaConverters._
import java.util.Properties
import com.carrotgarden.maven.scalor.util.Props

/**
 * Plugin self-descriptor.
 */
object Self extends util.JarRes {

  /**
   * Eclipse 'plugin.properties' keys names.
   */
  object Key {
    val pluginId = "pluginId"
    val mavenGroupId = "mavenGroupId"
    val mavenArtifactId = "mavenArtifactId"
    val mavenVersion = "mavenVersion"
    val eclipseDecorator = "eclipseDecorator"
    val projectConfigurator = "projectConfigurator"
  }

  /**
   * Maven plugin self-descriptor.
   */
  object Maven {

    /**
     * Maven "pom.xml" resource in the plugin jar file.
     */
    def pomXmlURL( groupId : String, artifactId : String ) = {
      resourceURL( s"META-INF/maven/${groupId}/${artifactId}/pom.xml" )
    }

  }

  /**
   * Eclipse plugin self-descriptor.
   */
  object Eclipse {

    val pluginXml = "plugin.xml"

    val pluginProps = "plugin.properties"

    /**
     * Eclipse "plugin.properties" resource in the jar file.
     */
    def pluginPropsURL = resourceURL( Eclipse.pluginProps )

    /**
     * Eclipse "plugin.xml" resource in the plugin jar file.
     */
    def pluginXmlURL = resourceURL( Eclipse.pluginXml )

  }

  def pluginId = pluginProps.getProperty( Key.pluginId )

  def mavenGroupId = pluginProps.getProperty( Key.mavenGroupId )

  def mavenArtifactId = pluginProps.getProperty( Key.mavenArtifactId )

  def mavenVersion = pluginProps.getProperty( Key.mavenVersion )

  /**
   * Plugin self-descriptor.
   */
  lazy val pluginProps : Properties = {
    Props.propertiesFrom( Eclipse.pluginPropsURL )
  }

  /**
   * Plugin self-descriptor.
   */
  lazy val pluginModel : Model = {
    val reader = new MavenXpp3Reader()
    val input = Maven.pomXmlURL( mavenGroupId, mavenArtifactId ).openStream
    val model = reader.read( input )
    input.close()
    model
  }

  /**
   * Find matching declared plugin dependency.
   */
  def pluginDependency( regex : String ) : Option[ Dependency ] = {
    util.Maven.locateDependency( pluginModel, regex )
  }

  /**
   * Known M2E plugin id.
   */
  object M2E {
    object logback {
      val appender = "org.eclipse.m2e.logback.appender"
      val configuration = "org.eclipse.m2e.logback.configuration"
    }
  }

}
