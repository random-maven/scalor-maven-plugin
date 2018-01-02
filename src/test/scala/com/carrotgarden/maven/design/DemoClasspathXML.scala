package com.carrotgarden.maven.design

import xml._
import xml.transform._
import java.io.File
import java.io.PrintWriter
import scala.xml.NodeSeq.seqToNodeSeq

object DemoClasspathXML extends App {

  def x( node : Node ) = {

    object rule2 extends RewriteRule {
      override def transform( node : Node ) : Seq[ Node ] = node match {
        case Elem( prefix, "level2", attribs, scope, _* ) =>
          Elem( prefix, "level2", attribs, scope, false, Text( "value2" ) )
        case other => other
      }
    }

    object xform2 extends RuleTransformer( rule2 )

    object rule1 extends RewriteRule {
      override def transform( node : Node ) : Seq[ Node ] = node match {
        case level1 @ Elem( _, "level1", _, _, _* ) => xform2( level1 )
        case other                                  => other
      }
    }

    object xform1 extends RuleTransformer( rule1 )

    xform1( node )

  }

  trait name {
    private lazy val A = this.getClass.getName.split( """\$""" ).last
    def apply() = A
  }

  object schema {
    object classpath extends name {
      object classpathentry extends name {
        object kind extends name
        object path extends name
        object output extends name
        object attributes extends name {
          object attribute extends name {
            object name extends name
            object value extends name
          }
        }
      }
    }
  }

  def pathMap( path : String ) = {
    val m1 = "src/main/java".r
    val m2 = "src/main/scala".r
    val m3 = "src/main/resources".r
    val t1 = "src/test/java".r
    val t2 = "src/test/scala".r
    val t3 = "src/test/resources".r
    val x1 = "org.eclipse.jdt.+".r
    val x2 = "org.scala-ide.sdt.+".r
    val x3 = "org.eclipse.m2e.+".r
    path match {
      case m1() => "1"
      case m2() => "2"
      case m3() => "3"
      case t1() => "4"
      case t2() => "5"
      case t3() => "6"
      case x1() => "7"
      case x2() => "8"
      case x3() => "9"
      case any  => any
    }
  }

  object byPath extends Ordering[ Node ] {
    val key = "path"
    override def compare( e1 : Node, e2 : Node ) : Int = {
      val a1 = e1.attribute( key ); val a2 = e2.attribute( key )
      ( a1, a2 ) match {
        case ( Some( a1 ), Some( a2 ) ) => pathMap( a1.text ).compareTo( pathMap( a2.text ) )
        case ( Some( _ ), None )        => -1
        case ( None, Some( _ ) )        => +1
        case _                          => 0
      }
    }
  }

  object sortClassPathEntry extends RewriteRule {
    val `label` = "classpath"
    override def transform( n : Node ) : Seq[ Node ] = n match {
      case e @ Elem( prefix, `label`, attribs, scope, _* ) =>
        Elem( prefix, `label`, attribs, scope, false, e.child.sorted( byPath ) : _* )
      case _ =>
        n
    }
  }

  val file = new File( ".classpath" )

  val source = XML.loadFile( file )

  object transform extends RuleTransformer(
    sortClassPathEntry
  )

  val target = transform( source )

  val printer = new PrettyPrinter( 200, 4 )
  val pretty = printer.format( target )
  val writer : PrintWriter = new PrintWriter( System.out )
  XML.write( writer, XML.loadString( pretty ), "utf-8", true, null )
  writer.flush()

}
