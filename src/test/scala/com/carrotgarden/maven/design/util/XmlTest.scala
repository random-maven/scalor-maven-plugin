package com.carrotgarden.maven.design.util

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest._

@RunWith( classOf[ JUnitRunner ] )
class XmlTest extends FunSuite {

  import Xml._
  import xml._
  import xml.Utility._
  import xml.transform.RuleTransformer

  val source =
    <projectDescription>
      <name>scalor-maven-plugin</name>
      <comment></comment>
      <projects>
      </projects>
      <buildSpec>
        <buildCommand>
          <name>org.eclipse.dltk.core.scriptbuilder</name>
          <arguments>
          </arguments>
        </buildCommand>
        <buildCommand>
          <name>org.scala-ide.sdt.core.scalabuilder</name>
          <arguments>
          </arguments>
        </buildCommand>
        <buildCommand>
          <name>org.eclipse.m2e.core.maven2Builder</name>
          <arguments>
          </arguments>
        </buildCommand>
      </buildSpec>
      <natures>
        <nature>org.eclipse.jdt.core.javanature</nature>
        <nature>org.scala-ide.sdt.core.scalanature</nature>
        <nature>org.eclipse.m2e.core.maven2Nature</nature>
        <nature>org.eclipse.dltk.sh.core.nature</nature>
      </natures>
    </projectDescription>

  val target1 =
    <projectDescription>
      <name>hello-kitty</name>
      <comment></comment>
      <projects>
      </projects>
      <buildSpec>
        <buildCommand>
          <name>org.eclipse.dltk.core.scriptbuilder</name>
          <arguments>
          </arguments>
        </buildCommand>
        <buildCommand>
          <name>org.scala-ide.sdt.core.scalabuilder</name>
          <arguments>
          </arguments>
        </buildCommand>
        <buildCommand>
          <name>org.eclipse.m2e.core.maven2Builder</name>
          <arguments>
          </arguments>
        </buildCommand>
      </buildSpec>
      <natures>
        <nature>org.eclipse.jdt.core.javanature</nature>
        <nature>org.scala-ide.sdt.core.scalanature</nature>
        <nature>org.eclipse.m2e.core.maven2Nature</nature>
        <nature>org.eclipse.dltk.sh.core.nature</nature>
      </natures>
    </projectDescription>

  val target2 =
    <projectDescription>
      <name>scalor-maven-plugin</name>
      <comment></comment>
      <projects>
      </projects>
      <buildSpec>
        <buildCommand>
          <name>org.eclipse.dltk.core.scriptbuilder</name>
          <arguments>
          </arguments>
        </buildCommand>
        <buildCommand>
          <name>org.scala-ide.sdt.core.scalabuilder</name>
          <arguments>
          </arguments>
        </buildCommand>
        <buildCommand>
          <name>org.eclipse.m2e.core.maven2Builder</name>
          <arguments>
          </arguments>
        </buildCommand>
      </buildSpec>
      <natures>
        <nature>org.scala-ide.sdt.core.scalanature</nature>
        <nature>org.eclipse.m2e.core.maven2Nature</nature>
        <nature>org.eclipse.dltk.sh.core.NATURE</nature>
      </natures>
    </projectDescription>

  val target3 =
    <projectDescription>
      <name>scalor-maven-plugin</name>
      <comment></comment>
      <projects>
      </projects>
      <buildSpec>
        <buildCommand>
          <name>org.scala-ide.sdt.core.scalabuilder</name>
          <arguments>
          </arguments>
        </buildCommand>
        <buildCommand>
          <name>org.eclipse.m2e.core.maven3Builder</name>
          <arguments>
          </arguments>
        </buildCommand>
      </buildSpec>
      <natures>
        <nature>org.eclipse.jdt.core.javanature</nature>
        <nature>org.scala-ide.sdt.core.scalanature</nature>
        <nature>org.eclipse.m2e.core.maven2Nature</nature>
        <nature>org.eclipse.dltk.sh.core.nature</nature>
      </natures>
    </projectDescription>

  val printer = new PrettyPrinter( 4, 200 )

  test( "nested select" ) {

    val nameOption = nestedHead(
      source,
      Seq( "buildSpec", "buildCommand", "name" )
    )
    assert( nameOption == Some( <name>org.eclipse.dltk.core.scriptbuilder</name> ) )
    val natureOption = nestedHead(
      source,
      Seq( "natures", "nature" )
    )
    assert( natureOption == Some( <nature>org.eclipse.jdt.core.javanature</nature> ) )
  }

  test( "rename stem branch" ) {
    val rule = ReplacementRule( "projectDescription", "name", "hello-kitty" );
    val xform = new RuleTransformer( rule );
    val result = xform( source );
    // println( result )
    assert( trim( target1 ) === trim( result ) )
  }

  test( "create/delete immediate" ) {
    val rule = CreateDeleteImmediateRule(
      "natures", "nature",
      Seq( "org.eclipse.dltk.sh.core.NATURE", "org.eclipse.jdt.core.javanature" ),
      Seq( "org.eclipse.dltk.sh.core.nature", "org.eclipse.jdt.core.javanature" )
    );
    val xform = new RuleTransformer( rule );
    val result = xform( source );
    // println( result )
    assert( trim( target2 ) === trim( result ) )
  }

  test( "create/delete nested" ) {
    val make = new NodeProducer {
      def apply( label : String, text : String ) : Node = {
        <buildCommand>
          <name>{ text }</name>
          <arguments></arguments>
        </buildCommand>
      }
    }
    val check = new NodeEquality {
      def apply( n1 : Node, n2 : Node ) : Boolean = {
        n1 \ "name" == n2 \ "name"
      }
    }
    val rule = CreateDeleteNestedRule(
      "buildSpec", "buildCommand",
      Seq( "org.eclipse.m2e.core.maven3Builder", "org.eclipse.m2e.core.maven2Builder" ),
      Seq( "org.eclipse.m2e.core.maven2Builder", "org.eclipse.dltk.core.scriptbuilder" ),
      make, check
    );
    val xform = new RuleTransformer( rule );
    val result = xform( source );
    // println( result )
    assert( trim( target3 ) === trim( result ) )
  }

}
