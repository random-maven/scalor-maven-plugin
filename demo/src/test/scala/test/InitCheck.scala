package test

import org.junit.Test
import org.junit.Ignore
import org.junit.Assert._

/**
 * Verify Scala.js initializer is in fact invoked.
 *
 * Module initializer must provide global variable value.
 *
 * This is a worker test invoked in JS-VM by master controller in JVM.
 */
// Sample console output:
// 01:15:03.440 [Thread-1] INFO  [JS-VM/console] - Test initializer check value: abrakadabra
class InitCheck { // Using non-surefire test name.

  @Test // This is JUnit 4.
  def verifyInitializerValue() : Unit = {
    println( s"Test initializer check value: ${Global.initializerValue}" )
    assertEquals( Global.initializerValue, "abrakadabra" )
  }

}
