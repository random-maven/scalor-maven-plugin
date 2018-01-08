package com.carrotgarden.maven.scalor.meta

import scala.annotation.tailrec
import scala.language.experimental.macros
import scala.reflect.macros.whitebox.Context
import scala.reflect.api.materializeWeakTypeTag

/**
 * Build support.
 */
trait Macro {

  import Macro._

  /**
   *  Generate property name.
   */
  def nameOf( member : Any ) : String = macro MacroBundle.nameOf

  /**
   * Generate variable registry.
   */
  def variableMap[ T ]() : Map[ String, Class[ _ ] ] = macro MacroBundle.variableMap[ T ]

  def variableCount[ T ]() : Int = macro MacroBundle.variableCount[ T ]

  /**
   * Generate variable assignment block.
   */
  def variableUpdateBlock[ T ]( fun : UpdateFun ) : Unit = macro MacroBundle.variableUpdateBlock[ T ]

  /**
   * Generate variable reporting block.
   */
  def variableReportBlock[ T ]( fun : ReportFun ) : Unit = macro MacroBundle.variableReportBlock[ T ]

}

object Macro extends Macro {

  /**
   * Produce value based on variable name and type.
   */
  type UpdateFun = ( String, Class[ _ ] ) => Object

  /**
   * Produce value report based on variable name.
   */
  type ReportFun = ( String, Any ) => Unit

}

// use "c" https://github.com/scala/bug/issues/10615
class MacroBundle( val c : Context ) extends RichContext {

  import c.universe._
  import Macro._

  def hasMethod( tree : Tree ) : Boolean = {
    tree.symbol.isMethod && !tree.symbol.isConstructor
  }

  def hasVariable( tree : Tree ) : Boolean = {
    tree.symbol.isMethod && tree.symbol.asMethod.isVar && tree.symbol.asMethod.isGetter
  }

  def methodList( body : List[ Tree ] ) : List[ Tree ] = {
    body.collect {
      case tree : DefDef if hasMethod( tree ) => tree
    }
  }

  def variableList( body : List[ Tree ] ) : List[ Tree ] = {
    body.collect {
      case tree : ValDef if hasVariable( tree ) => tree
    }
  }

  def renderName( tree : Tree ) : String = {
    tree.symbol.name.decodedName.toString
  }

  def nameOf( member : c.Expr[ Any ] ) : c.Expr[ String ] = {
    val name = extractName( member.tree ).decodedName.toString
    val result = q"$name"
    info( s"${showCode( result )}" )
    c.Expr[ String ]( result )
  }

  def variableMap[ T : c.WeakTypeTag ]() : c.Expr[ Map[ String, Class[ _ ] ] ] = {
    val entryList = c.weakTypeOf[ T ].decls.collect {
      case member : TermSymbol if member.isGetter =>
        val name = member.name.decodedName.toString
        val klaz = member.asMethod.returnType
        val stem : Tree = q"${name} -> classOf[${klaz}]"
        stem
    }
    val result = q"""
    Map[String, Class[_]](
    ..${entryList}
    )
    """
    info( s"${showCode( result )}" )
    c.Expr[ Map[ String, Class[ _ ] ] ]( result )
  }

  def extractGetterList( klazType : Type ) : List[ MethodSymbol ] = {
    klazType.baseClasses.reverse.flatMap { base =>
      klazType.baseType( base ).decls.collect {
        case member : TermSymbol if member.isGetter => member.asMethod
      }
    }
  }
  def variableCount[ T : c.WeakTypeTag ]() : c.Expr[ Int ] = {
    val klazType = c.weakTypeOf[ T ]
    val entryList = extractGetterList( klazType )
    val entryCount = entryList.size
    val result = q"""
    ${entryCount}
    """
    info( s"${showCode( result )}" )
    c.Expr[ Int ]( result )
  }

  def variableUpdateBlock[ T : c.WeakTypeTag ]( fun : c.Expr[ UpdateFun ] ) : c.Expr[ Unit ] = {
    val klazType = c.weakTypeOf[ T ]
    val entryList = extractGetterList( klazType ).map { member =>
      val varTerm = member.name
      val varName = member.name.decodedName.toString
      val varType = member.asMethod.returnType
      val statement : Tree = q"""
          ${varTerm} = ${fun} ( ${varName}, classOf[${varType}] ).asInstanceOf[${varType}]
        """
      statement
    }
    val result = q"""
    ..${entryList}
    """
    info( s"${showCode( result )}" )
    c.Expr[ Unit ]( result )
  }

  def variableReportBlock[ T : c.WeakTypeTag ]( fun : c.Expr[ ReportFun ] ) : c.Expr[ Unit ] = {
    val klazType = c.weakTypeOf[ T ]
    val entryList = extractGetterList( klazType ).map { member =>
      val varTerm = member.name
      val varName = member.name.decodedName.toString
      val varType = member.asMethod.returnType
      val statement : Tree = q"""
          ${fun} ( ${varName}, ${varTerm} )
        """
      statement
    }
    val result = q"""
    ..${entryList}
    """
    info( s"${showCode( result )}" )
    c.Expr[ Unit ]( result )
  }

}
