package com.carrotgarden.maven.scalor.util

import java.net.URL
import java.util.Properties

/**
 * Resolve resource only from current jar file.
 */
trait JarRes {

  def location = getClass.getProtectionDomain.getCodeSource.getLocation.toExternalForm

  def resourceURL( path : String ) = new URL( "jar:" + location + "!/" + path )

}
