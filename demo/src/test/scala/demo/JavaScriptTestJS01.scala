package demo

import org.junit.Test
import org.junit.Ignore
import org.junit.Assert._

/**
 * This test is invoked in JS-VM.
 *
 * Convention for JavaScript testing:
 * - use JUnit 4 for JS, and JUnit 5 for JVM
 * - use custom JUnit 4 runner: @RunWith(ScalaJS_Suite)
 * - use JS test naming so they are not detected by SureFire plugin
 */
class JavaScriptTestJS01 {

  @Test // This is JUnit 4.
  def verifyPrint() : Unit = {
    println( s"### Message from JS-VM @ ${getClass.getName} ###" )
  }

  @Test // This is JUnit 4.
  def verifyVM() : Unit = {
    // https://github.com/scala-js/scala-js/blob/master/javalanglib/src/main/scala/java/lang/System.scala
    assertEquals( System.getProperty( "java.vm.name" ), "Scala.js" )
  }

  @Ignore
  @Test // This is JUnit 4.
  def verifyIgnored() : Unit = {
    throw new Exception( "Should not happen." )
  }

}
