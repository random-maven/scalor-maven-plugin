package com.carrotgarden.maven.scalor.meta

import scala.language.experimental.macros
import scala.reflect.macros.whitebox.Context
import scala.collection.mutable.Map

/**
 * Cached value provider.
 */
case class Cached() {

  val entryMap : Map[ Any, Any ] = Map.empty

  def apply[ B ]( body : B ) : B = macro Cached.provider[ B ]

}

object Cached {

  def provider[ B : c.WeakTypeTag ]( c : Context )( body : c.Expr[ B ] ) : c.Expr[ B ] = {
    import c.universe._
    cachedContext( c ).cache( body )
  }

  def cachedContext( context : Context ) =
    new CachedContext {
      val c : context.type = context
    }

}

// FIXME hard coded "cached"
trait CachedContext extends RichContext {

  import c.universe._

  def cache[ B : c.WeakTypeTag ]( body : c.Expr[ B ] ) : c.Expr[ B ] = {
    val name = extractName( body.tree )
    val key : String = name.decodedName.toString
    //    val objTerm = c.weakTypeOf[ Cached.type ].termSymbol
    val resType = c.weakTypeOf[ B ]
    val result : Tree = q"""
      cached.entryMap.getOrElse( $key, {
        val res = $body
        cached.entryMap += ($key -> res)
        res
      } ).asInstanceOf[${resType}]
    """
    info( s"${showCode( result )}" )
    c.Expr[ B ]( result )
  }

}
