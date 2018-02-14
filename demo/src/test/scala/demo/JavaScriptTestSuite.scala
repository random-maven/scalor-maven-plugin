package demo

import com.carrotgarden.sjs.junit.ScalaJS_Suite

import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api._

import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * This is a Master -> Worker test controller.
 * 
 * Note:
 * - master test (JavaScriptTestSuite) is invoked in JVM,
 * - while workers (JavaScriptTestJS01, JavaScriptTestJS02) are invoked in JS-VM.
 *
 * Basic setup for JavaScript testing inside JS-VM:
 * - use custom JUnit 4 runner: @RunWith(ScalaJS_Suite)
 * - declare JS-VM test classes via @Suite.SuiteClasses(...)
 * - provision JS-VM environment via "scala-js-env-prov-nodejs"
 * - configure JS-VM environment via "scala-js-env-conf-nodejs"
 * - invoke "mvn test" for Maven build
 * - invoke "Run as -> JUnit Test" for Eclipse run
 */
@RunWith( classOf[ ScalaJS_Suite ] )
@Suite.SuiteClasses( Array(
  classOf[ JavaScriptTestJS01 ], // Worker test is invoked in JS-VM.
  classOf[ JavaScriptTestJS02 ] // Worker test is invoked in JS-VM.
) )
class JavaScriptTestSuite // Master test is invoked in JVM.
