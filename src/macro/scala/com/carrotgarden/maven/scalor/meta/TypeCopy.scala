package com.carrotgarden.maven.scalor.meta

import scala.annotation.StaticAnnotation
import scala.reflect.macros.whitebox.Context
import scala.language.experimental.macros

/**
 * TODO
 */
class TypeCopy[ T ]() extends StaticAnnotation {
  def macroTransform( annottees : Any* ) : AnyRef = macro TypeCopy.provider
}

object TypeCopy {

  def provider( c : Context )( annottees : c.Expr[ Any ]* ) : c.Expr[ AnyRef ] = {
    import c.universe._

    def prefix( message : String ) = s"Scalor macro: $message"
    def info( message : String ) = c.info( c.enclosingPosition, prefix( message ), true )
    def warn( message : String ) = c.warning( c.enclosingPosition, prefix( message ) )
    def fail( message : String ) = c.abort( c.enclosingPosition, prefix( message ) )

    val klazParam : Tree = c.prefix.tree match {
      case q"new TypeCopy[$param]()" => param
      case _                         => fail( "Expected: new TypeCopy[$param]()" )
    }

    val klazType = c.typecheck( klazParam.duplicate ).tpe
    //    warn( s"XXX 1 type ${klazType}" )
    //    klazType.decls.foreach { member =>
    //      warn( s"XXX 2  member ${member}" )
    //    }

    val memberList = klazType.decls.collect {
      case member if member.isClass =>
        member
      case member if member.isMethod && !member.isConstructor =>
        member
    }

    val result = annottees.map( _.tree ) match {

      //          ..${memberList}
      case List( q"object $name extends $parent { ..$body }" ) if body.isEmpty =>
        q"""
        object $name extends $parent {
        }
        """

      case _ => fail(
        "Use object definition with an empty body."
      )

    }

    warn( s"XXX 3 ${showCode( result )}" )

    c.Expr[ AnyRef ]( result )

  }

}
