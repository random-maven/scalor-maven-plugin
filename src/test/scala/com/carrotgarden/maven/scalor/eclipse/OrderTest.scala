package com.carrotgarden.maven.scalor.eclipse

import scala.collection.JavaConverters._

import com.carrotgarden.maven.scalor._

import java.util.Arrays
import java.util.ArrayList
import java.util.Collections

import org.eclipse.m2e.jdt.internal.ClasspathEntryDescriptor
import org.eclipse.core.runtime.Path
import org.eclipse.m2e.core.embedder.ArtifactKey
import org.eclipse.m2e.jdt.IClasspathEntryDescriptor

import org.junit.jupiter.api._
import org.junit.runner.RunWith
import org.junit.platform.runner.JUnitPlatform

@RunWith( classOf[ JUnitPlatform ] )
class OrderTest extends AnyRef
  with base.ParamsAny {

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

  @Test
  def arraySort : Unit = {
    val mapper = new SettingsRegexMapper( ordering, separator )
    val comparator = ComparatorString( mapper )
    val sourceList = parseCommonList( source, separator )
    val resultList = sourceList.clone
    Arrays.sort( resultList, comparator )
    val targetList = parseCommonList( target, separator )
    println( s"sourceList ${sourceList.mkString( ";" )}" )
    println( s"targetList ${resultList.mkString( ";" )}" )
    assert( targetList.toList == resultList.toList )
  }

  @Test
  def collectionSort : Unit = {
    val mapper = new SettingsRegexMapper( ordering, separator )
    val comparator = ComparatorString( mapper )
    val sourceList = Arrays.asList( parseCommonList( source, separator ) : _* )
    val resultList = new ArrayList[ String ]( sourceList )
    Collections.sort( resultList, comparator )
    val targetList = Arrays.asList( parseCommonList( target, separator ) : _* )
    println( s"sourceList ${sourceList.asScala.mkString( ";" )}" )
    println( s"targetList ${resultList.asScala.mkString( ";" )}" )
    assert( targetList == resultList )
  }

  @Test
  def artifactSort : Unit = {

    def entryFrom( artifactId : String ) : IClasspathEntryDescriptor = {
      val entry = new ClasspathEntryDescriptor( 1, Path.fromPortableString( "" ) )
      val artifactKey = new ArtifactKey( "groupId", artifactId, "version", "classifier" )
      entry.setArtifactKey( artifactKey )
      entry
    }

    val comparator = ComparatorArtifact( "artifactId", "ascending" )

    val source = """
    runtime
    commons
    xz
    aop
    """

    val target = """
    aop
    commons
    runtime
    xz
    """

    val sourceList = Arrays.asList( parseCommonList( source, separator ) : _* ).asScala.map( entryFrom( _ ) )
    val targetList = Arrays.asList( parseCommonList( target, separator ) : _* ).asScala.map( entryFrom( _ ) )
    val resultList = new ArrayList[ IClasspathEntryDescriptor ]( sourceList.asJava )
    Collections.sort( resultList, comparator )

    assert {
      targetList.corresponds( resultList.asScala ) {
        case ( one, two ) => one.getArtifactId == two.getArtifactId
      }
    }

  }

}
