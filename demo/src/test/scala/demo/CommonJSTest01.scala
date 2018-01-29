package demo

import meta.Macro

import org.junit.Test
import org.junit.Ignore
import org.junit.Assert._

/**
 * This test is platform-neutral.
 *
 * It can run both in JS-VM and JVM.
 */
class CommonJSTest01 {

  @Test // This is JUnit 4.
  def verifyPrint() : Unit = {
    println( s"### Message from VM: ${System.getProperty( "java.vm.name" )} @ ${getClass.getName} ###" )
  }

  /**
   * Provide some method.
   */
  def propertyName() = {
    println( "hello-scalor" )
  }

  /**
   * Extract method name at compile time.
   */
  def extractedName = Macro.nameOf( propertyName )

  @Test // This is JUnit 4.
  def verifyMacro() : Unit = {
    assertEquals( extractedName, "propertyName" )
  }

}
