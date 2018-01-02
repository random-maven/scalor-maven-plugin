package com.carrotgarden.maven.scalor.util

object Hex {

  def value( array : Array[ Byte ] ) : String = array.map( "%02x" format _ ).mkString

}
