package any

import scala.scalajs.js
import scala.scalajs.js.annotation._

@JSGlobal
@js.native
class JavaScript extends js.Object {

  def method( name : String ) : Unit = js.native

}

trait Main {

}

object Main {
  
}

// produce expected compile error:
// "object creation impossible, since"

//import scalatags.Text
//object CustomBundle extends Text.Cap with Text.Tags with Text.Tags2 with Text.Aggregate {
//  object st extends Text.Cap with Text.Styles with Text.Styles2
//  object at extends Text.Cap with Text.Attrs
//}
