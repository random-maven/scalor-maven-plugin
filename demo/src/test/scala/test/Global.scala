package test

import scala.scalajs.js
import scala.scalajs.js.annotation._

/**
 * Provide global JavaScript context for testing.
 *
 * https://www.scala-js.org/doc/interoperability/global-scope.html
 */
@js.native
@JSGlobalScope
object Global extends js.Object {

  /**
   * A value set by module initializer
   * and verified by JUnit test in JS-VM.
   */
  var initializerValue : String = js.native

}
