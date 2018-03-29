package com.carrotgarden.maven.scalor.zinc

import org.apache.maven.plugin.testing.AbstractMojoTestCase
import org.codehaus.plexus.logging.LoggerManager
import org.apache.maven.monitor.logging.DefaultLog
import org.apache.maven.plugin.Mojo
import org.apache.maven.plugin.logging.Log

import org.junit.jupiter.api._
import org.junit.jupiter.api.Assertions._
import org.junit.runner.RunWith
import org.junit.platform.runner.JUnitPlatform

import scala.tools.nsc
import scala.collection.mutable.HashSet

@RunWith( classOf[ JUnitPlatform ] )
class SettingsTest {

  @Test
  def basicUserSettings : Unit = {

    val settings = new nsc.Settings( println( _ ) )
    val sourceText = "-Xmaxerrs 123 -language:higherKinds -Dscalor.artifact=tester -Xplugin /aaa1 -Xplugin /aaa2"
    val resultText = "-Dscalor.artifact=tester -Xmaxerrs 123 -Xplugin:/aaa1 -Xplugin:/aaa2 -language:higherKinds"
    settings.processArgumentString( sourceText )
    val targetText = Settings.unparseString( settings )

    //    println( s"sourceText ${sourceText}" )
    //    println( s"targetText ${targetText}" )

    assertEquals( resultText, targetText )

  }

}
