package meta

import scala.language.experimental.macros
import scala.reflect.macros.whitebox.Context
import scala.reflect.api.materializeWeakTypeTag

/**
 * Macro demo.
 */
trait Macro extends API {

  /**
   *  Generate property name.
   */
  override def nameOf( member : Any ) : String = macro MacroBundle.nameOf

  def systemProperty( name : String ) : String = macro MacroBundle.systemProperty

}

object Macro extends Macro {

}
