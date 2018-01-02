package com.carrotgarden.maven.scalor.zinc

import com.carrotgarden.maven.scalor.util.Maven

import org.apache.maven.artifact.Artifact

import Module._
import java.io.File

case class Module(
  moduleType :     Type,
  binaryArtifact : Artifact,
  sourceArtifact : Option[ Artifact ]
)

object Module {

  sealed trait Type
  case object Unknown extends Type
  case object ScalaLibrary extends Type
  case object ScalaReflect extends Type
  case object ScalaCompiler extends Type
  case object CompilerBridge extends Type
  case object CompilerPlugin extends Type

  def fileFrom( module : Module ) : File = module.binaryArtifact.getFile.getCanonicalFile

  def versionFrom( module : Module ) : String = module.binaryArtifact.getVersion

  def entiresFrom( module : Module ) : Seq[ String ] = {
    import module._
    Seq(
      binaryArtifact.getFile.getCanonicalPath,
      sourceArtifact.map( _.getFile.getCanonicalPath ).getOrElse( "" )
    )
  }

  /**
   * Provide artifact module type detector.
   */
  case class Detector(
    artifactCompilerBridge :   String,
    artifactScalaCompiler :    String,
    artifactScalaLibrary :     String,
    artifactScalaReflect :     String,
    artifactPluginDescriptor : String
  ) {
    val rexexCompilerBridge = artifactCompilerBridge.r
    val regexScalaCompiler = artifactScalaCompiler.r
    val regexScalaLibrary = artifactScalaLibrary.r
    val regexCompilerBridge = artifactCompilerBridge.r
    val regexScalaReflect = artifactScalaReflect.r
    /**
     * Provide artifact module type detector.
     */
    def moduleType( artifact : Artifact ) : Type = {
      Maven.artifactIdentity( artifact ) match {
        case rexexCompilerBridge() => CompilerBridge
        case regexScalaCompiler() => ScalaCompiler
        case regexScalaLibrary() => ScalaLibrary
        case regexScalaReflect() => ScalaReflect
        case _ if Maven.hasResourceMatch( `artifact`, artifactPluginDescriptor ) => CompilerPlugin
        case _ => Unknown
      }
    }
  }

}
