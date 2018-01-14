package meta

import scala.annotation.tailrec
import scala.language.experimental.macros
import scala.reflect.macros.whitebox.Context
import scala.reflect.api.materializeWeakTypeTag

/**
 * Macro demo.
 */
trait Macro {

  /**
   *  Generate property name.
   */
  def nameOf( member : Any ) : String = macro MacroBundle.nameOf

}

object Macro extends Macro {

}

class MacroBundle( val c : Context ) extends RichContext {

  import c.universe._

  def nameOf( member : c.Expr[ Any ] ) : c.Expr[ String ] = {
    val name = extractName( member.tree ).decodedName.toString
    val result = q"$name"
    info( s"${showCode( result )}" )
    c.Expr[ String ]( result )
  }

}
