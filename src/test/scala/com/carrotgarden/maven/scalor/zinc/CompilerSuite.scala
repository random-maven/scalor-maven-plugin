package com.carrotgarden.maven.scalor.zinc

import org.junit.runner.RunWith
import scala.collection.JavaConverters._
import java.util.Arrays
import com.carrotgarden.maven.scalor._
import java.util.ArrayList
import java.util.Collections
import org.apache.maven.plugin.testing.AbstractMojoTestCase
import org.codehaus.plexus.logging.LoggerManager
import org.apache.maven.monitor.logging.DefaultLog
import org.apache.maven.plugin.Mojo
import org.apache.maven.plugin.logging.Log

import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import org.junit._
import org.junit.runner.RunWith
import org.junit.platform.runner.JUnitPlatform

// @RunWith( classOf[ JUnitPlatform ] )
class CompilerBase extends AbstractMojoTestCase {

  lazy val log : Log = {
    val manager = getContainer()
      .lookup( classOf[ LoggerManager ].getName )
      .asInstanceOf[ LoggerManager ]
    new DefaultLog( manager.getLoggerForComponent( Mojo.ROLE ) );
  }

  // @Before
  override def setUp = {
    super.setUp
    log.info( s"setup" )
  }

  // @Test
  def testMain() = {
    log.info( s"test main" )

  }

}
