package meta

import org.junit.jupiter.api._
import org.junit.jupiter.api.Assertions._
import org.junit.runner.RunWith
import org.junit.platform.runner.JUnitPlatform

/**
 * Verify macro operation.
 *
 * Note:
 * - this is JUnit 5 test.
 * - it can only run on JVM, not JS-VM.
 */
@RunWith( classOf[ JUnitPlatform ] )
class MacroTest extends meta.Macro {

  /**
   * Provide some method.
   */
  def propertyName() = {
    println( "hello-scalor" )
  }

  /**
   * Extract method name at compile time.
   */
  def extractedName = nameOf( propertyName )

  /**
   * Verify macro operation.
   */
  @Test // This is JUnit 5.
  def propertyTest = {
    assertEquals( extractedName, "propertyName" )
  }

}
