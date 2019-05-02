package com.carrotgarden.maven.scalor

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.apache.maven.plugin.testing.AbstractMojoTestCase
import org.apache.maven.plugin.Mojo
import org.apache.maven.plugin.logging.Log
import org.codehaus.plexus.logging.LoggerManager
import org.apache.maven.monitor.logging.DefaultLog
import org.apache.maven.plugin.logging.Log

import org.junit.Assert._
import org.junit.Rule
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import org.codehaus.plexus.PlexusTestCase
import org.apache.maven.plugin.testing.MojoRule
import org.apache.maven.plugin.testing.resources.TestResources
import scala.collection.JavaConverters._
import org.apache.maven.project.MavenProject
import org.apache.maven.execution.DefaultMavenExecutionRequest
import org.eclipse.aether.DefaultRepositorySystemSession
import org.junit.Assert
import org.apache.maven.project.ProjectBuilder
import org.eclipse.aether.repository.LocalRepositoryManager
import org.eclipse.aether.repository.LocalRepository
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.impl.DefaultServiceLocator
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory
//import org.eclipse.aether.transport.wagon.WagonProvider
import org.eclipse.aether.impl.VersionResolver
import org.eclipse.aether.impl.ArtifactDescriptorReader

import org.eclipse.aether.RepositorySystemSession;

import org.eclipse.aether.impl.ArtifactDescriptorReader;
import org.eclipse.aether.impl.VersionRangeResolver;
import org.eclipse.aether.impl.VersionResolver;

import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.LocalRepositoryManager;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
//import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory
//import org.eclipse.aether.internal.transport.wagon.PlexusWagonProvider
import org.apache.maven.repository.internal.DefaultVersionResolver
import org.apache.maven.repository.internal.DefaultVersionRangeResolver
import org.apache.maven.repository.internal.DefaultArtifactDescriptorReader
import scala.reflect.ClassTag
import org.apache.maven.execution.DefaultMavenExecutionResult
import org.apache.maven.execution.MavenSession
import org.apache.maven.repository.internal.MavenRepositorySystemUtils
import java.util.Arrays

/**
 * Shared testing features.
 *
 * Note: ensure plugin descriptor is generated:
 * ${project.basedir}/target/classes/META-INF/maven/plugin.xml
 */
class BaseCase extends AbstractMojoTestCase {

  import BaseCase._

  lazy val log : Log = {
    val manager = getContainer()
      .lookup( classOf[ LoggerManager ].getName )
      .asInstanceOf[ LoggerManager ]
    new DefaultLog( manager.getLoggerForComponent( Mojo.ROLE ) );
  }

  lazy val MojoRule = MojoExtra( TestResources )

  lazy val TestResources = new TestResources(
    "src/test/resources/projects", "target/test-projects"
  )

  @Rule
  def mojoRule = MojoRule

  @Rule
  def resourcesRule = TestResources

  def basedir( project : String ) = resourcesRule.getBasedir( project )

}

object BaseCase {

  case class MojoExtra( resources : TestResources ) extends MojoRule {

    def newLocalRepository() = {
      val userHome = System.getProperty( "user.home" );
      val localRepositoryPath = new File( userHome, ".m2/repository" );
      new LocalRepository( localRepositoryPath );
    }

    def registerTransportServices( locator : DefaultServiceLocator ) = {
//      locator.addService( classOf[ RepositoryConnectorFactory ], classOf[ BasicRepositoryConnectorFactory ] )
//      locator.setServices( classOf[ WagonProvider ], new PlexusWagonProvider() )
    }

    def newRepositorySystem() = {
      val locator = MavenRepositorySystemUtils.newServiceLocator()
      registerTransportServices( locator );
      locator.getService( classOf[ RepositorySystem ] );
    }

    def newRepositorySession() = {
      new DefaultRepositorySystemSession
    }

    def newLocalRepositoryManager( session : RepositorySystemSession, system : RepositorySystem ) = {
      system.newLocalRepositoryManager( session, newLocalRepository() );
    }

    override def newMavenSession( project : MavenProject ) : MavenSession =
      {
        val request = new DefaultMavenExecutionRequest();
        val result = new DefaultMavenExecutionResult();

        val repoSystem = newRepositorySystem
        val repoSession = MavenRepositorySystemUtils.newSession()
        val localRepositoryManager = newLocalRepositoryManager( repoSession, repoSystem )
        repoSession.setLocalRepositoryManager( localRepositoryManager )

        val session = new MavenSession( getContainer, repoSession, request, result );
        session.setCurrentProject( project );
        session.setProjects( Arrays.asList( project ) );
        session;
      }

    override def lookupConfiguredMojo( basedir : File, goal : String ) : Mojo = {
      val project = readMavenProject( basedir );
      val session = newMavenSession( project );
      val execution = newMojoExecution( goal );
      lookupConfiguredMojo( session, execution );
    }

    /**
     * Provide configured mojo from test project.
     */
    def mojoFrom[ T <: base.Mojo ](
      projectName : String
    )( implicit tag : ClassTag[ T ] ) : T = {
      val basedir = resources.getBasedir( projectName )
      val mojoName = tag.runtimeClass.newInstance.asInstanceOf[ T ].mojoName
      lookupConfiguredMojo( basedir, mojoName ).asInstanceOf[ T ]
    }

  }

}
