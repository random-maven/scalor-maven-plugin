package com.carrotgarden.maven.scalor.extend

import org.apache.maven.AbstractMavenLifecycleParticipant
import org.codehaus.plexus.component.annotations._
import org.apache.maven.execution.MavenSession
import org.codehaus.plexus.logging.Logger
import org.apache.maven.model.PluginExecution
import scala.collection.JavaConverters._
import org.apache.maven.model.Plugin
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.LifecyclePhase._

import com.carrotgarden.maven.scalor.A

/**
 * Provide default Maven lifecycle for Scalor plugin.
 * Note: this does not work in Eclipse / M2E environment.
 * Use only for "plain" command line Maven mode.
 */
@Component(
  role = classOf[ AbstractMavenLifecycleParticipant ],
  hint = "scalor"
)
class Lifecycle extends AbstractMavenLifecycleParticipant {

  /**
   * Use default logger.
   */
  @Requirement
  var logger : Logger = _

  /**
   * Locate a plugin in the current project.
   */
  def aquirePlugin( artifactId : String )( implicit session : MavenSession ) : Option[ Plugin ] = {
    session.getCurrentProject.getBuild.getPlugins.asScala.toList
      .filter( plugin => plugin.getArtifactId.startsWith( artifactId ) ) match {
        case List( plugin ) => Some( plugin )
        case result : Any   => None
      }
  }

  /**
   * Locate scalor-maven-plugin in the current project.
   */
  def scalorPlugin( implicit session : MavenSession ) = aquirePlugin( A.maven.artifactId )

  /**
   * Locate maven-compiler-plugin in the current project.
   */
  def compilerPlugin( implicit session : MavenSession ) = aquirePlugin( A.maven.compilerId )

  /**
   * Add phase:goal as default execution for the plugin.
   */
  def registerExecution( phase : LifecyclePhase, goal : String )( implicit plugin : Plugin ) = {
    val execution = new PluginExecution()
    execution.setPhase( phase.id );
    execution.getGoals().add( goal )
    execution.setConfiguration( plugin.getConfiguration() )
    plugin.getExecutions.add( execution )
  }

  /**
   * Provide scalor plugin lifecycle.
   */
  override def afterProjectsRead( session : MavenSession ) = {
    logger.info( "Scalor: extenson lifecycle..." )
    implicit val MS = session

    // Disable default compiler.
    compilerPlugin match {
      case Some( compiler ) => compiler.getExecutions.clear()
      case None             =>
    }

    // Enable alternative compiler.
    scalorPlugin match {
      case Some( scalor ) =>
        implicit val SP = scalor
        import A.mojo._
        // Provide default goal executions.

        registerExecution( CLEAN, `clean-macro` )
        registerExecution( CLEAN, `clean-main` )
        registerExecution( CLEAN, `clean-test` )

        registerExecution( INITIALIZE, `eclipse-config` )

        registerExecution( INITIALIZE, `register-macro` )
        registerExecution( INITIALIZE, `register-main` )
        registerExecution( INITIALIZE, `register-test` )

        registerExecution( COMPILE, `compile-macro` )
        registerExecution( COMPILE, `prepack-macro` )

        registerExecution( COMPILE, `compile-main` )
        registerExecution( COMPILE, `prepack-main` )

        registerExecution( PROCESS_CLASSES, `scaladoc-macro` )

        registerExecution( PROCESS_CLASSES, `scaladoc-main` )

        registerExecution( PROCESS_CLASSES, `link-scala-js-main` )
        registerExecution( PROCESS_CLASSES, `prepack-linker-main` )

        registerExecution( TEST_COMPILE, `compile-test` )
        registerExecution( TEST_COMPILE, `prepack-test` )

        registerExecution( PROCESS_TEST_CLASSES, `prepack-linker-test` )
        registerExecution( PROCESS_TEST_CLASSES, `scaladoc-test` )

        registerExecution( PACKAGE, `sources-macro` )
        registerExecution( PACKAGE, `sources-main` )
        registerExecution( PACKAGE, `sources-test` )

      case None =>
        throw new RuntimeException( "Missing scalor plugin." )
    }

  }

}
