package com.carrotgarden.maven.scalor.util

import java.net.URL
import java.util.Properties

/**
 * Resolve resource only from current jar file.
 */
trait JarRes {

  /**
   * Discover class location URL as string.
   */
  def location = getClass.getProtectionDomain.getCodeSource.getLocation.toExternalForm

  /**
   * Discover resource location URL only from current jar file.
   */
  def resourceURL( resource : String ) = new URL( s"jar:${location}!/${resource}" )

}
