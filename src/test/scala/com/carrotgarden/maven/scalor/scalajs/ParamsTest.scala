package com.carrotgarden.maven.scalor.scalajs

import org.junit.jupiter.api._
import org.junit.jupiter.api.Assertions._
import org.junit.runner.RunWith
import org.junit.platform.runner.JUnitPlatform

@RunWith( classOf[ JUnitPlatform ] )
class ParamsTest {

  import ParamsTest._

  val initializerTestList = List(
    InitTest(
      "test.Init.main()",
      "test.Init", "main", ""
    ),
    InitTest(
      "test.Init.main(build=${project.artifactId},stamp=${maven.build.timestamp})",
      "test.Init", "main", "build=${project.artifactId},stamp=${maven.build.timestamp}"
    ),
    InitTest(
      "main1.main2.main3.SomeMain.someMain()",
      "main1.main2.main3.SomeMain", "someMain", ""
    ),
    InitTest(
      "main1.main2.main3.SomeMain123.someMain456(build=${project.artifactId},stamp=${maven.build.timestamp})",
      "main1.main2.main3.SomeMain123", "someMain456", "build=${project.artifactId},stamp=${maven.build.timestamp}"
    )
  )

  @Test
  def initializerRegex() = {
    val regex = ParamsInitListAny.initializerRegex.r
    val split = ","
    initializerTestList.foreach { test =>
      // parse
      val regex( klaz, meth, args ) = test.conf
      val list = args.split( split )
      // unparse
      val conf = s"${klaz}.${meth}(${list.mkString( split )})"
      println( s"${conf}" )
      assertEquals( test.conf, conf )
      assertEquals( test.klaz, klaz )
      assertEquals( test.meth, meth )
      assertEquals( test.args, args )
    }
  }

}

object ParamsTest {

  case class InitTest(
    conf : String,
    klaz : String,
    meth : String,
    args : String
  )

}
