package demo

import scala.scalajs.js
import scala.scalajs.js.annotation._

trait Module extends meta.Macro {

  /**
   * Extract method name.
   */
  def extractedName = nameOf( Module.propertyName )

}

object Module {

  def propertyName() = {
    println( "hello-kitty" )
  }

}

@JSGlobal
@js.native
class Vue extends js.Object {

}

@JSExportAll
@JSExportTopLevel( "Arkon" )
object Arkon {

  def main() = {
    println( "hello-kitty" )
  }

}
