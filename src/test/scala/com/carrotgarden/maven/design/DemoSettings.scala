package com.carrotgarden.maven.design

import scala.collection.JavaConverters._
import scala.tools.nsc.settings.MutableSettings
import org.scalaide.ui.internal.preferences.ScalaPluginSettings

object DemoSettings {

  def main( args : Array[ String ] ) : Unit = {

    val settings = new MutableSettings( _ => true )

    ScalaPluginSettings.copyInto( settings )

    val argsList = List( "-Xplugin", "/some/path" )

    settings.processArguments( argsList, true )

    println( settings )

  }

}
