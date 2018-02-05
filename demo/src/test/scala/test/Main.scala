package test

import java.util.Date

/**
 * Managed test application.
 *
 * Restarted in Eclipse after full or incremental build.
 */
object Main {

  //////////////////

  def main( args : Array[ String ] ) : Unit = {

    val date = new Date

    while ( true ) {
      println( s"XXX Test application @ ${date} XXX" )
      Thread.sleep( 5000 )
    }
  }

}
