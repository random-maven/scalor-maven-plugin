package com.carrotgarden.maven.scalor.util

/**
 * Render Hex values.
 */
object Hex {

  def value( array : Array[ Byte ] ) : String = array.map( "%02x" format _ ).mkString

}
