package com.carrotgarden.maven.scalor.zinc

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest._
import scala.collection.JavaConverters._
import java.util.Arrays
import com.carrotgarden.maven.scalor._
import java.util.ArrayList
import java.util.Collections
import org.scalactic.source.Position.apply
import org.apache.maven.plugin.testing.AbstractMojoTestCase
import org.codehaus.plexus.logging.LoggerManager
import org.apache.maven.monitor.logging.DefaultLog
import org.apache.maven.plugin.Mojo
import org.apache.maven.plugin.logging.Log
import org.junit.Before
import org.junit.Test
import org.junit.runners.Suite
import org.junit.runners.Suite.SuiteClasses

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

//@RunWith( classOf[ Suite ] )
//@SuiteClasses(
//  Array( classOf[ CompilerBase ] )
//)
//class CompilerSuite

@RunWith( classOf[ JUnit4 ] )
class CompilerBase extends AbstractMojoTestCase {

  lazy val log : Log = {
    val manager = getContainer()
      .lookup( classOf[ LoggerManager ].getName )
      .asInstanceOf[ LoggerManager ]
    new DefaultLog( manager.getLoggerForComponent( Mojo.ROLE ) );
  }

  @Before
  override def setUp = {
    super.setUp
    log.info( s"setup" )
  }

  @Test
  def testMain() = {
    log.info( s"test main" )

  }

}

object CompilerSuiteTest {

}
