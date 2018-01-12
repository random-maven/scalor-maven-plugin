package com.carrotgarden.maven.scalor.util

import org.osgi.framework.Bundle
import org.osgi.framework.BundleContext
import org.osgi.framework.wiring.BundleWiring
import org.osgi.framework.Version

/**
 * Eclipse platform support.
 */
object OSGI {

  /**
   * Obtain class loader servicing given bundle.
   */
  def bundleClassLoader( bundle : Bundle ) : ClassLoader = {
    bundle.adapt( classOf[ BundleWiring ] ).getClassLoader
  }

  /**
   * Find bundle by symbolic name and version.
   */
  def discoverBundle(
    root :          Bundle,
    name :          String,
    versionOption : Option[ String ] = None
  ) : Option[ Bundle ] = {
    val list = root.getBundleContext.getBundles
    val version = Version.parseVersion( versionOption.getOrElse( "0.0.0.invalid" ) )
    var index = 0
    val length = list.length
    while ( index < length ) {
      val bundle = list( index ); index += 1
      val hasName = bundle.getSymbolicName.equals( name )
      val hasVersion = !versionOption.isDefined || version.equals( bundle.getVersion )
      if ( hasName && hasVersion ) {
        return Some( bundle )
      }
    }
    None
  }

}
