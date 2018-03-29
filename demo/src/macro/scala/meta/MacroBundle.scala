package meta

import scala.language.experimental.macros
import scala.reflect.macros.whitebox.Context
import scala.reflect.api.materializeWeakTypeTag

class MacroBundle( val c : Context ) extends MacroContext {

  import c.universe._
  import meta.API;

  def nameOf( member : c.Expr[ Any ] ) : c.Expr[ String ] = {
    val name = extractName( member.tree ).decodedName.toString
    val result = q"$name"
    info( s"${showCode( result )}" )
    c.Expr[ String ]( result )
  }

  def systemProperty( name : c.Expr[ String ] ) : c.Expr[ String ] = {
    val Literal( Constant( key ) ) = name.tree
    val value = sys.props( key.toString )
    info( s"${key}=${value}" )
    val result = q"$value"
    c.Expr[ String ]( result )
  }

}
