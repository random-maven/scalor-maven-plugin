package com.carrotgarden.maven.scalor

import better.files._
import java.io.{ File => JFile }

object DemoBetterFiles {

  def main( args : Array[ String ] ) : Unit = {

    val file = File( "target" )

    println( s"file=${file}" )
    
  }

}
