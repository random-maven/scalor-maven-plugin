package com.carrotgarden.maven.scalor.util

import xml._
import xml.transform._

import java.io.File
import java.io.FileWriter

import scala.annotation.tailrec
import scala.xml.NodeBuffer
import scala.collection.mutable.ArrayBuffer

object Xml {

  import Params._

  /**
   * Parse xml file.
   */
  def xmlLoad( file : File ) = XML.loadFile( file )

  /**
   * Persist  xml file.
   */
  def xmlSave(
    root :     Node,
    file :     File,
    encoding : String = "utf-8",
    width :    Int    = 200, ident : Int = 4
  ) = {
    val printer = new PrettyPrinter( width, ident )
    val source = XML.loadString( printer.format( root ) )
    val writer = new FileWriter( file )
    XML.write( writer, source, encoding, true, null )
    writer.close
  }

  /**
   * Order XML Nodes by attribute key with optional sort order mapper.
   */
  case class AttributeOrdering( key : String, mapper : RegexMapper = NoRegexMapper )
    extends Ordering[ Node ] {
    override def compare( n1 : Node, n2 : Node ) : Int = {
      ( n1.attribute( key ), n2.attribute( key ) ) match {
        case ( Some( a1 ), Some( a2 ) ) => mapper.map( a1.text ).compareTo( mapper.map( a2.text ) )
        case ( Some( _ ), None )        => -1
        case ( None, Some( _ ) )        => +1
        case _                          => 0
      }
    }
  }

  /**
   * Select first nested node recursively from path.
   */
  @tailrec
  def nestedHead( root : NodeSeq, path : Seq[ String ] ) : Option[ Node ] =
    path match {
      case Seq()                  => root.headOption
      case Seq( head, tail @ _* ) => nestedHead( root \ head, tail )
    }

  /**
   * Sort children of an element by nested head element text.
   */
  case class ElementOrdering( path : Seq[ String ], mapper : RegexMapper = NoRegexMapper )
    extends Ordering[ Node ] {
    override def compare( n1 : Node, n2 : Node ) : Int = {
      ( nestedHead( n1, path ), nestedHead( n2, path ) ) match {
        case ( Some( e1 ), Some( e2 ) ) => mapper.map( e1.text ).compareTo( mapper.map( e2.text ) )
        case ( Some( _ ), None )        => -1
        case ( None, Some( _ ) )        => +1
        case _                          => 0
      }
    }
  }

  object ElementOrdering {
    def apply( path : String, mapper : RegexMapper ) : ElementOrdering =
      ElementOrdering( path.split( "/" ), mapper )
  }

  /**
   * Sort children of an element with an order.
   */
  case class OrderingRule( label : String, order : Ordering[ Node ] )
    extends RewriteRule {
    override def transform( node : Node ) : Seq[ Node ] = node match {
      case elem @ Elem( prefix, `label`, attribs, scope, _* ) =>
        Elem( prefix, `label`, attribs, scope, false, elem.child.sorted( order ) : _* )
      case elem =>
        elem
    }
  }

  /**
   * Replace text content for the immediate child element: stem -> branch.
   */
  case class ReplacementRule( stem : String, branch : String, text : String )
    extends RewriteRule {
    override def transform( node : Node ) : Seq[ Node ] = node match {
      case elem @ Elem( prefix, `stem`, attribs, scope, _* ) =>
        val xform = elem.child.map { child =>
          child match {
            case Elem( prefix, `branch`, attribs, scope, _* ) =>
              Elem( prefix, `branch`, attribs, scope, false, Text( text ) )
            case other => other
          }
        }
        Elem( prefix, `stem`, attribs, scope, false, xform : _* )
      case elem =>
        elem
    }
  }

  /**
   * Create and delete immediate text nodes for stem -> branch.
   */
  case class CreateDeleteImmediateRule(
    stem : String, branch : String,
    create : Seq[ String ], delete : Seq[ String ]
  )
    extends RewriteRule {
    override def transform( node : Node ) : Seq[ Node ] = node match {
      case elem @ Elem( prefix, `stem`, attribs, scope, _* ) =>
        val listCreate = create.map( term => Elem( prefix, `branch`, attribs, scope, false, Text( term ) ) )
        val listDelete = delete.map( term => Elem( prefix, `branch`, attribs, scope, false, Text( term ) ) )
        val xform = ( elem.child ++ listCreate )
          .filterNot( listDelete.contains )
          .distinct
        Elem( prefix, `stem`, attribs, scope, false, xform : _* )
      case elem =>
        elem
    }
  }

  trait NodeProducer {
    def apply( label : String, text : String ) : Node
  }

  trait NodeEquality {
    def apply( n1 : Node, n2 : Node ) : Boolean
  }

  /**
   * Create and delete nested text nodes for stem -> branch.
   */
  case class CreateDeleteNestedRule(
    stem : String, branch : String,
    create : Seq[ String ], delete : Seq[ String ],
    make : NodeProducer, check : NodeEquality
  )
    extends RewriteRule {
    override def transform( node : Node ) : Seq[ Node ] = node match {
      case elem @ Elem( prefix, `stem`, attribs, scope, _* ) =>
        val listCreate = create.map( term => make( branch, term ) )
        val listDelete = delete.map( term => make( branch, term ) )
        val buffer = ArrayBuffer[ Node ]()
        buffer ++= elem.child
        buffer ++= listCreate.filterNot( e1 => elem.child.find( e2 => check( e1, e2 ) ).isDefined )
        val result = buffer.filterNot( e1 => listDelete.find( e2 => check( e1, e2 ) ).isDefined )
        Elem( prefix, `stem`, attribs, scope, false, result : _* )
      case elem =>
        elem
    }
  }

  lazy val NoRuleTransformer = new RuleTransformer()

  lazy val NoRewriteRule = new RewriteRule {}

}
