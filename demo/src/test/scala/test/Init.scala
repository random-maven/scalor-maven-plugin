package test

import scala.scalajs.js.annotation._

/**
 * Example of Scala.js module initializer.
 *
 * Module initializers are JavaScript equivalent to Java <code>main</code> method convention.
 *
 * They are invoked when linker-generated <code>runtime.js</code> script is loaded in Node.js or Browser JS-VM.
 */
// Sample console output:
// 08:04:19.061 [Thread-0] INFO  [JS-VM/console] - Test initializer module: build=scalor-maven-plugin-demo stamp=2018-02-14T02:03:57Z
@JSExportTopLevel( "test.Init" )
object Init {

  // Arguments contain "build" and "stamp" entries from pom.xml.
  @JSExport
  def main( args : Array[ String ] ) : Unit = {

    // This module output is printed on JS-VM console (Node.js or Browser).
    println( s"Test initializer module: ${args( 0 )} ${args( 1 )}" )
    
    // Provide some global value for verification by JUnit test in JS-VM.
    Global.initializerValue = "abrakadabra"
  }

}
