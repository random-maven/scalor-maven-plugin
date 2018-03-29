package com.carrotgarden.maven.scalor.util

import java.io.File
import java.nio.file.Files
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

/**
 * Text support.
 */
object Text {

  /**
   * Truncate text string.
   */
  def preview( text : String, size : Int ) = {
    if ( text.length <= size ) {
      text
    } else {
      text.take( text.lastIndexWhere( _.isSpaceChar, size + 1 ) ).trim
    }
  }

  /**
   * Array as list with indent.
   */
  def reportArray[ T ]( array : Array[ T ] ) = {
    array.mkString( "   ", "\n   ", "" )
  }

  /**
   * Load file into string.
   */
  def textRead(
    file :    File,
    charset : Charset = StandardCharsets.UTF_8
  ) : String = {
    new String( Files.readAllBytes( file.toPath ), charset )
  }

  /**
   * Save string into file.
   */
  def textWrite(
    text :    String,
    file :    File,
    charset : Charset = StandardCharsets.UTF_8
  ) : Unit = {
    Files.write( file.toPath, text.getBytes( charset ) )
  }

  /**
   * Render array as hex values.
   */
  def renderHex( array : Array[ Byte ] ) : String = array.map( "%02x" format _ ).mkString

}
