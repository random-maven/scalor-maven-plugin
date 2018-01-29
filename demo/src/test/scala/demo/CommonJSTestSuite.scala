package demo

import com.carrotgarden.sjs.junit.ScalaJS_Suite

import org.junit.jupiter.api.Assertions._;
import org.junit.jupiter.api._;

import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * This is a Master -> Worker test controller for JS-VM.
 *
 * Note:
 * - master test (CommonJSTestSuite) is invoked in JVM,
 * - while workers (CommonJSTest01, CommonJSTest02) are invoked in JS-VM.
 *
 * Basic setup for JavaScript testing inside JS-VM:
 * - use custom JUnit 4 runner: @RunWith(ScalaJS_Suite)
 * - declare JS-VM test classes via @Suite.SuiteClasses(...)
 * - provision JS-VM environment via "scala-js-env-prov-nodejs"
 * - configure JS-VM environment via "scala-js-env-conf-nodejs"
 * - invoke "mvn test" for Maven build
 * - invoke "Run as -> JUnit Test" for Eclipse run
 */

// Sample output:
//12:41:50.962 [Thread-1] INFO  [JS-VM/console] - ### Message from VM: Scala.js @ demo.CommonJSTest01 ###
//12:41:51.019 [Thread-1] INFO  [JS-VM/console] - ### Message from VM: Scala.js @ demo.CommonJSTest02 ###

@RunWith( classOf[ ScalaJS_Suite ] ) // Using custom Suite.
@Suite.SuiteClasses( Array(
  classOf[ CommonJSTest01 ], // Worker test is invoked in JS-VM.
  classOf[ CommonJSTest02 ] // Worker test is invoked in JS-VM.
) )
class CommonJSTestSuite // Master test is invoked in JVM.
