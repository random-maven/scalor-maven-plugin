package demo

import com.carrotgarden.sjs.junit.ScalaJS_Suite

import org.junit.jupiter.api.Assertions._;
import org.junit.jupiter.api._;

import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * This is a Master -> Worker test controller for JVM.
 *
 * Note:
 * - master test (CommonJVMTestSuite) is invoked in JVM,
 * - workers (CommonJSTest01, CommonJSTest02) are also invoked in JVM.
 *
 */

// Sample output:
//
//### Message from VM: Java HotSpot(TM) 64-Bit Server VM @ demo.CommonJSTest01 ###
//### Message from VM: Java HotSpot(TM) 64-Bit Server VM @ demo.CommonJSTest02 ###

@RunWith( classOf[ Suite ] ) // Using default Suite.
@Suite.SuiteClasses( Array(
  classOf[ CommonJSTest01 ], // Worker test is invoked in JVM.
  classOf[ CommonJSTest02 ] // Worker test is invoked in JVM.
) )
class CommonJVMTestSuite // Master test is invoked in JVM.
