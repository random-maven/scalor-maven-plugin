package com.carrotgarden.maven.scalor.base

import org.apache.maven.artifact.Artifact
import org.apache.maven.project.MavenProject
import scala.util.Try

import scala.language.implicitConversions

object Classifier {

  type Bucket = Array[ Value ]

  implicit class Value( val name : String ) extends AnyVal

  object Value {
    def unapply( value : Value ) = Try( value.name ).toOption
  }

  implicit def convert( source : Array[ String ] ) : Bucket = source.map( Value( _ ) )

  val binary = Value( null )
  val sources = Value( "sources" )
  val javadoc = Value( "javadoc" )

  object Select {
    val All = Array[ Value ]( binary, sources, javadoc )
    val Binary = Array[ Value ]( binary )
    val Sources = Array[ Value ]( sources )
    val Javadoc = Array[ Value ]( javadoc )
  }

  def hasMatch( artifact : Artifact, bucket : Bucket ) : Boolean = {
    bucket.find { classifier =>
      ( classifier, artifact.getClassifier ) match {
        case ( `binary`, null )    => true
        case ( _, null )           => false
        case ( Value( one ), two ) => one == two
      }
    }.isDefined
  }

  // See org.apache.maven.project.MavenProject
  def verify( project : MavenProject ) = {
    project.getArtifact.getClassifier
  }

}
