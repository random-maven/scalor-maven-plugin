package demo

import org.junit.Test
import org.junit.Ignore
import org.junit.Assert._

/**
 * This test is platform-neutral.
 *
 * It can run both in JS-VM and JVM.
 */
class CommonJSTest02 {

  @Test // This is JUnit 4.
  def verifyPrint() : Unit = {
    println( s"### Message from VM: ${System.getProperty( "java.vm.name" )} @ ${getClass.getName} ###" )
  }

  @Ignore // FIXME https://gist.github.com/Andrei-Pozolotin/0eeeca4653b03d5f067504ed100154d2
  @Test( expected = classOf[ IllegalStateException ] ) // This is JUnit 4.
  def verifyException() : Unit = {
    throw new IllegalStateException( "hello-scalor" )
  }

}
