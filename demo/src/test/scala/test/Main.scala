package test

import java.time.format.DateTimeFormatter
import java.time.ZoneOffset
import java.time.Instant

/**
 * Example of managed test application.
 *
 * Activated by Maven goal "eclipse-restart".
 *
 * Application is restarted in Eclipse after full or incremental build.
 *
 * Open Eclipse/M2E "Maven Console" to observe output from this application.
 *
 * Edit and save this file to observe application restart and launch stamp change.
 */
object Main {

  /////////////////

  val stamp = DateTimeFormatter
    .ofPattern( "yyyy-MM-dd_HH-mm-ss" )
    .withZone( ZoneOffset.UTC )
    .format( Instant.now() )

  def main( args : Array[ String ] ) : Unit = {
    while ( true ) {
      println( s"### Test application @ ${stamp} ###" )
      Thread.sleep( 5000 )
    }
  }

}
