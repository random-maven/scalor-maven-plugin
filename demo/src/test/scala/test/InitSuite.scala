package test

import com.carrotgarden.sjs.junit.ScalaJS_Suite

import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * This is a Master -> Worker test controller for JS-VM.
 *
 * Verify that Scala.js initializer is in fact invoked early.
 */
@RunWith( classOf[ ScalaJS_Suite ] ) // Using custom runner.
@Suite.SuiteClasses( Array(
  classOf[ InitCheck ] // Worker test is invoked in JS-VM.
) )
class InitSuite // Master test is invoked in JVM.
