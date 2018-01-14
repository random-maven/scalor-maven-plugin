package com.carrotgarden.maven.scalor

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4;

@RunWith( classOf[ JUnit4 ] )
class DocumentMojoTest extends BaseCase {

  // FIXME test resolver

  @Test
  def scaladocMain() = {
    val mojo = mojoRule.mojoFrom[ ScaladocMainMojo ]( "scaladoc" )
    val project = mojo.project
    val repoSession = mojo.session.getRepositorySession
    log.info( s"project ${project}" )
    log.info( s"repoSession ${repoSession.getLocalRepositoryManager}" )
    //    mojo.execute()
  }

}
