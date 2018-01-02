package com.carrotgarden.maven.scalor.zinc

import com.carrotgarden.maven.tools.Description
import org.apache.maven.plugins.annotations._
import scala.collection.JavaConverters._

import com.carrotgarden.maven.scalor._

/**
 * Resolve Scala installation from plugin dependencies.
 */
trait Resolve {

  self : ParamScalaInstall // 
  with resolve.Maven with base.ParamsArtifact =>

  def moduleDetector() : Module.Detector = {
    Module.Detector(
      artifactCompilerBridge,
      artifactScalaCompiler,
      artifactScalaLibrary,
      artifactScalaReflect,
      artifactPluginDescriptor
    )
  }

  def resolveCustomInstall() : ScalaInstall = {
    val detector = moduleDetector()

    import base.Params._

    val defineRequest = base.Params.DefineRequest(
      convert( defineBridge ), convert( defineCompiler ), convert( definePluginList )
    )

    val defineResponse = resolveDefine( defineRequest, "compile" )

    val install = ScalaInstall( zincScalaInstallTitle, detector, defineResponse ).withTitleDigest

    install
  }

}
