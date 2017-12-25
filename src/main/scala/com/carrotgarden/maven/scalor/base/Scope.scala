package com.carrotgarden.maven.scalor.base

import org.apache.maven.artifact.Artifact
import org.apache.maven.project.MavenProject

import scala.language.implicitConversions

// http://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html#Dependency_Scope

trait Scope {

}

object Scope {

  type Bucket = Array[ Value ]

  implicit class Value( val name : String ) extends AnyVal

  implicit def convert( source : Array[ String ] ) : Bucket = source.map( Value( _ ) )

  val compile = Value( Artifact.SCOPE_COMPILE )
  val test = Value( Artifact.SCOPE_TEST )
  val provided = Value( Artifact.SCOPE_PROVIDED )
  val runtime = Value( Artifact.SCOPE_RUNTIME )
  val system = Value( Artifact.SCOPE_SYSTEM )

  object Select {
    val Macro : Bucket = Array[ Value ]( compile, provided, system )
    val Main : Bucket = Array[ Value ]( compile, provided, system )
    val Test : Bucket = Array[ Value ]( compile, provided, system, test, runtime )
  }

  def hasMatch( artifact : Artifact, bucket : Bucket ) : Boolean = {
    bucket.find( scope => artifact.getScope == scope.name ).isDefined
  }

  // See org.apache.maven.project.MavenProject
  def verify( project : MavenProject ) = {
    project.getCompileClasspathElements
    project.getTestClasspathElements
  }

}
