package com.carrotgarden.maven.scalor.scalajs

import org.junit.jupiter.api._
import org.junit.jupiter.api.Assertions._
import org.junit.runner.RunWith
import org.junit.platform.runner.JUnitPlatform

@RunWith( classOf[ JUnitPlatform ] )
class LinkerTest {

  import Linker._

  import Options._

  import upickle._
  import upickle.default._

  @Test
  def optionsCodec() = {
    val source = Options()
    val result = write( source )
    val target = read[ Options ]( result )
    //    println( s"source ${source}" )
    //    println( s"result ${result}" )
    //    println( s"target ${target}" )
    assertEquals( source, target )
  }

  @Test
  def optionsParser() = {
    val result = """{"checkIR":false,"parallel":true,"optimizer":false,"batchMode":false,"sourceMap":true,"prettyPrint":false,"closureCompiler":false}"""
    val source = Options()
    val target = read[ Options ]( result )
    //    println( s"source ${source}" )
    //    println( s"result ${result}" )
    //    println( s"target ${target}" )
    assertEquals( source, target )
  }

  @Test
  def linkerIdentity() = {
    val source = Options()
    val target = Options()
    assertEquals( linkerEngineId( source ), linkerEngineId( target ) )
  }

}
