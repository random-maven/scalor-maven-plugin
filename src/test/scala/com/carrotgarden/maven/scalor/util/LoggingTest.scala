package com.carrotgarden.maven.scalor.util

import org.junit.jupiter.api._
import org.junit.runner.RunWith
import org.junit.platform.runner.JUnitPlatform
import com.carrotgarden.maven.scalor.util.Logging.AnyLog

@RunWith( classOf[ JUnitPlatform ] )
class LoggingTest extends AnyRef {

  import LoggingTest._

  @Test
  def branchTest : Unit = {

    val root = TestLog( "root" )
    root.info( "test-root" )

    val stem = root.branch( "stem" )
    stem.warn( "test-stem" )

    val leaf = stem.branch( "leaf" )
    leaf.fail( "test-leaf" )

  }

}

object LoggingTest {

  case class TestLog( context : String ) extends AnyLog {
    override val founder = this
    override def dbug( line : String ) = println( "DBUG " + text( line ) )
    override def info( line : String ) = println( "INFO " + text( line ) )
    override def warn( line : String ) = println( "WARN " + text( line ) )
    override def fail( line : String ) = println( "FAIL " + text( line ) )
    override def fail( line : String, error : Throwable ) = println( "FAIL " + text( line ), error )
  }

}
