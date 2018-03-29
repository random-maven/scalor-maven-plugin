package main

import scala.scalanative.native._
import java.nio.charset.StandardCharsets
import java.io.ByteArrayInputStream
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import java.util.zip.ZipEntry

/**
 * Scala.native main entry class.
 */
object Main {

  /**
   * Embedded resources from src/main/cdata.
   * https://github.com/scala-native/scala-native/issues/1204
   */
  @extern
  object cdata {
    // file: cdata.zip directly
    def _binary_cdata_zip_start : Ptr[ CString ] = extern // segfault
    def _binary_cdata_zip_end : Ptr[ CString ] = extern // segfault
    // file: cdata.zip via forwrder/adapter in main.c
    def binary_cdata_zip_start : Ptr[ CChar ] = extern // works as expected
    def binary_cdata_zip_end : Ptr[ CChar ] = extern // works as expected
  }

  /**
   * Project c/cpp functions from src/main/clang.
   */
  @extern
  object clang {
    // file: main-a.c
    def main_a() : CInt = extern
    // file: main-b.cpp
    def main_b() : CInt = extern
  }

  /**
   * Extract file content embedded in binary runtime.
   */
  def binary_data( start : Ptr[ CChar ], end : Ptr[ CChar ] ) : Array[ Byte ] = {
    val store = start
    val limit = ( end - start ).toInt
    val array = new Array[ Byte ]( limit )
    var index = 0
    while ( index < limit ) {
      array( index ) = store( index )
      index += 1
    }
    array
  }

  /**
   * List binary data encoded as zip file.
   */
  def printZipEnries( data : Array[ Byte ] ) : Unit = {
    val input = new ZipInputStream( new ByteArrayInputStream( data ) )
    Stream.continually( input.getNextEntry ).takeWhile( _ != null ).toList
      .foreach { entry =>
        val name = entry.getName
        val comp = entry.getCompressedSize
        val size = entry.getSize
        println( s"entry: ${name} [${comp}/${size}]" )
      }
  }

  def binary_cdata_zip : Array[ Byte ] = {
    import cdata._
    binary_data( binary_cdata_zip_start, binary_cdata_zip_end )
  }

  def main( args : Array[ String ] ) : Unit = {

    val file = sourcecode.File
    val line = implicitly[ sourcecode.Line ]
    println( s"scala-native file: ${file} line: ${line}" )

    println( s"scala-native cdata.zip:" )
    printZipEnries( binary_cdata_zip )

  }

}
