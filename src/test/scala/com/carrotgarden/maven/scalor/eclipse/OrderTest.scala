package com.carrotgarden.maven.scalor.eclipse

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest._
import scala.collection.JavaConverters._
import java.util.Arrays
import com.carrotgarden.maven.scalor._
import java.util.ArrayList
import java.util.Collections
import org.scalactic.source.Position.apply

@RunWith( classOf[ JUnitRunner ] )
class OrderTest extends FunSuite with base.AnyPar {

  import eclipse.Order._
  import util.Params._

  case class ComparatorString( mapper : RegexMapper )
    extends ComparatorAnyRule[ String ] {
    override def text( entry : String ) = {
      entry
    }
  }

  val ordering = """

    11 = .*src/macr.*/java
    12 = .*src/macr.*/scala 
    13 = .*src/macr.*/groovy 
    14 = .*src/macr.*/resources 
    
    21 = .*src/main.*/java 
    22 = .*src/main.*/scala 
    23 = .*src/main.*/groovy 
    24 = .*src/main.*/resources
    
    31 = .*src/test.*/java 
    32 = .*src/test.*/scala 
    33 = .*src/test.*/groovy 
    34 = .*src/test.*/resources 
    
    51 = .*target/gen[a-z-]*sources.* 
    52 = .*target/gen[a-z-]*test-.* 
    53 = .*target/gen[a-z-]*.*
    
    81 = org.scala-ide.sdt.*
    82 = org.eclipse.jdt.*
    83 = org.eclipse.m2e.*
    84 = GROOVY_SUPPORT
    85 = GROOVY_DSL_SUPPORT
    
    91 = .*target/macro-classes 
    92 = .*target/classes 
    93 = .*target/test-classes 

  """

  val separator = """[;\n]"""

  val source = """
    org.eclipse.m2e.MAVEN2_CLASSPATH_CONTAINER
    /scalor-maven-plugin-demo/src/test/java 
    /scalor-maven-plugin-demo/src/test/scala 
    /scalor-maven-plugin-demo/src/main/resources
    /scalor-maven-plugin-demo/src/main/scala 
    /scalor-maven-plugin-demo/src/test/groovy 
    /scalor-maven-plugin-demo/src/main/java 
    /scalor-maven-plugin-demo/src/test/resources
    /scalor-maven-plugin-demo/src/main/groovy 
    /scalor-maven-plugin-demo/src/macro/java 
    /scalor-maven-plugin-demo/src/macro/scala 
    org.scala-ide.sdt.launching.SCALA_CONTAINER
  """

  val target = """
    /scalor-maven-plugin-demo/src/macro/java 
    /scalor-maven-plugin-demo/src/macro/scala 
    /scalor-maven-plugin-demo/src/main/java 
    /scalor-maven-plugin-demo/src/main/scala 
    /scalor-maven-plugin-demo/src/main/groovy 
    /scalor-maven-plugin-demo/src/main/resources
    /scalor-maven-plugin-demo/src/test/java 
    /scalor-maven-plugin-demo/src/test/scala 
    /scalor-maven-plugin-demo/src/test/groovy 
    /scalor-maven-plugin-demo/src/test/resources
    org.scala-ide.sdt.launching.SCALA_CONTAINER
    org.eclipse.m2e.MAVEN2_CLASSPATH_CONTAINER
  """

  test( "array sort" ) {
    val mapper = new SettingsRegexMapper( ordering, separator )
    val comparator = ComparatorString( mapper )
    val sourceList = parseCommonSequence( source, separator )
    val resultList = sourceList.clone
    Arrays.sort( resultList, comparator )
    val targetList = parseCommonSequence( target, separator )
    println( s"sourceList ${sourceList.mkString( ";" )}" )
    println( s"targetList ${resultList.mkString( ";" )}" )
    assert( targetList.toList == resultList.toList )
  }

  test( "collection sort" ) {
    val mapper = new SettingsRegexMapper( ordering, separator )
    val comparator = ComparatorString( mapper )
    val sourceList = Arrays.asList( parseCommonSequence( source, separator ) : _* )
    val resultList = new ArrayList[ String ]( sourceList )
    Collections.sort( resultList, comparator )
    val targetList = Arrays.asList( parseCommonSequence( target, separator ) : _* )
    println( s"sourceList ${sourceList.asScala.mkString( ";" )}" )
    println( s"targetList ${resultList.asScala.mkString( ";" )}" )
    assert( targetList == resultList )
  }

}
