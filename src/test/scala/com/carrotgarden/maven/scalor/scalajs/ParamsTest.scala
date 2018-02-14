package com.carrotgarden.maven.scalor.scalajs

import org.junit.jupiter.api._
import org.junit.jupiter.api.Assertions._
import org.junit.runner.RunWith
import org.junit.platform.runner.JUnitPlatform

@RunWith( classOf[ JUnitPlatform ] )
class ParamsTest {

  @Test
  def initializerRegex() = {
    val regex = ParamsInitListAny.initializerRegex.r
    val entry = "main1.main2.main3.SomeMain.someMain(build=${project.artifactId},stamp=${maven.build.timestamp})"
    val regex( klaz, meth, args ) = entry
    println( s"klaz=${klaz} meth=${meth} args=${args}" )
    assertEquals( klaz, "main1.main2.main3.SomeMain" )
    assertEquals( meth, "someMain" )
    assertEquals( args, "build=${project.artifactId},stamp=${maven.build.timestamp}" )
  }

}
