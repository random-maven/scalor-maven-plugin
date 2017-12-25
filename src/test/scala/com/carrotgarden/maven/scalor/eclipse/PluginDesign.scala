//package com.carrotgarden.maven.scalor.eclipse
//
//import com.carrotgarden.maven.tools.Description
//
//import org.apache.maven.plugins.annotations._
//import java.io.File
//
//import Wiring.Meta
//
//trait PluginJava {
//
//  @Description( """
//  Bundle symbolic name of Eclipse Java plugin.
//  """ )
//  @Parameter(
//    property     = "scalor.eclipsePluginJavaId",
//    defaultValue = "org.eclipse.jdt.core"
//  )
//  var eclipsePluginJavaId : String = _
//
//  @Description( """
//  Root package name of Eclipse Java plugin.
//  """ )
//  @Parameter(
//    property     = "scalor.eclipsePluginJavaPackage",
//    defaultValue = "org.eclipse.jdt.core"
//  )
//  var eclipsePluginJavaPackage : String = _
//
//  def eclipseMetaJava = Meta( eclipsePluginJavaId, eclipsePluginJavaPackage )
//
//}
//
//trait PluginScala {
//
//  @Description( """
//  Bundle symbolic name of Eclipse Scala plugin.
//  """ )
//  @Parameter(
//    property     = "scalor.eclipsePluginScalaId",
//    defaultValue = "org.scala-ide.sdt.core"
//  )
//  var eclipsePluginScalaId : String = _
//
//  @Description( """
//  Root package name of Eclipse Scala plugin.
//  """ )
//  @Parameter(
//    property     = "scalor.eclipsePluginScalaPackage",
//    defaultValue = "org.scalaide.core"
//  )
//  var eclipsePluginScalaPackage : String = _
//
//  def eclipseMetaScala = Meta( eclipsePluginScalaId, eclipsePluginScalaPackage )
//
//}
//
//trait PluginResources {
//
//  @Description( """
//  Bundle symbolic name of Eclipse Resources plugin.
//  """ )
//  @Parameter(
//    property     = "scalor.eclipsePluginResourcesId",
//    defaultValue = "org.eclipse.core.resources"
//  )
//  var eclipsePluginResourcesId : String = _
//
//  @Description( """
//  Root package name of Eclipse Resources plugin.
//  """ )
//  @Parameter(
//    property     = "scalor.eclipsePluginResourcesPackage",
//    defaultValue = "org.eclipse.core.resources"
//  )
//  var eclipsePluginResourcesPackage : String = _
//
//  def eclipseMetaResources = Meta( eclipsePluginResourcesId, eclipsePluginResourcesPackage )
//
//}
//
//trait PluginMaven {
//
//  @Description( """
//  Bundle symbolic name of Eclipse Maven plugin.
//  """ )
//  @Parameter(
//    property     = "scalor.eclipsePluginMavenId",
//    defaultValue = "org.eclipse.m2e.core"
//  )
//  var eclipsePluginMavenId : String = _
//
//  @Description( """
//  Root package name of Eclipse Maven plugin.
//  """ )
//  @Parameter(
//    property     = "scalor.eclipsePluginMavenPackage",
//    defaultValue = "org.eclipse.m2e.core"
//  )
//  var eclipsePluginMavenPackage : String = _
//
//  def eclipseMetaMaven = Meta( eclipsePluginMavenId, eclipsePluginMavenPackage )
//
//}
//
//trait PluginRuntime {
//
//  @Description( """
//  Bundle symbolic name of Eclipse runtime plugin.
//  """ )
//  @Parameter(
//    property     = "scalor.eclipsePluginRuntimeId",
//    defaultValue = "org.eclipse.equinox.common"
//  )
//  var eclipsePluginRuntimeId : String = _
//
//  @Description( """
//  Root package name of Eclipse runtime plugin.
//  """ )
//  @Parameter(
//    property     = "scalor.eclipsePluginRuntimePackage",
//    defaultValue = "org.eclipse.core.runtime"
//  )
//  var eclipsePluginRuntimePackage : String = _
//
//  def eclipseMetaRuntime = Meta( eclipsePluginRuntimeId, eclipsePluginRuntimePackage )
//
//}
//
//trait PluginOsgiCore {
//
//  @Description( """
//  Bundle symbolic name of Eclipse OSGI core plugin.
//  """ )
//  @Parameter(
//    property     = "scalor.eclipsePluginOsgiCoreId",
//    defaultValue = "osgi.core"
//  )
//  var eclipsePluginOsgiCoreId : String = _
//
//  @Description( """
//  Root package name of Eclipse OSGI core plugin.
//  """ )
//  @Parameter(
//    property     = "scalor.eclipsePluginOsgiCorePackage",
//    defaultValue = "org.osgi"
//  )
//  var eclipsePluginOsgiCorePackage : String = _
//
//  def eclipseMetaOsgiCore = Meta( eclipsePluginOsgiCoreId, eclipsePluginOsgiCorePackage )
//
//}
//
//trait PluginArkon extends AnyRef
//  with PluginJava
//  with PluginScala
//  with PluginMaven
//  with PluginResources
//  with PluginRuntime
//  with PluginOsgiCore
