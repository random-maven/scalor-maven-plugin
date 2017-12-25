package com.carrotgarden.maven.scalor.util

import org.osgi.framework.Bundle
import org.osgi.framework.BundleContext
import org.osgi.framework.wiring.BundleWiring
import org.osgi.framework.Version

object OSGI {

  def discoverBundle( bundle : Bundle, name : String, versionOption : Option[ String ] = None ) : Option[ Bundle ] = {
    val list = bundle.getBundleContext.getBundles
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

  def bundleClassLoader( bundle : Bundle ) : ClassLoader = {
    bundle.adapt( classOf[ BundleWiring ] ).getClassLoader
  }

  /**
   * Obtain companion object for a type.
   */
  def trueCompanion[ T ]( klaz : Class[ T ] )(
    implicit
    //    MF : Manifest[ T ], bundle : Bundle
    bundle : Bundle
  ) : T = trueCompanion( klaz.getName )

  /**
   * Obtain companion object for a type.
   */
  def trueCompanion[ T ]( name : String )(
    implicit
    //    MF : Manifest[ T ], bundle : Bundle
    bundle : Bundle
  ) : T = bundle
    .loadClass( name + "$" )
    .getField( "MODULE$" )
    .get( null )
    .asInstanceOf[ T ]

  /**
   * Obtain companion object for a type.
   */
  def fakeCompanionXXX[ T ]( klaz : Class[ T ] )(
    implicit
    //    MF : Manifest[ T ], bundle : Bundle
    bundle : Bundle
  ) : AnyRef = fakeCompanionXXX( klaz.getName )

  /**
   * Obtain companion object for a type.
   */
  def fakeCompanionXXX[ T ]( name : String )(
    implicit
    //    MF : Manifest[ T ], bundle : Bundle
    bundle : Bundle
  ) : AnyRef = bundle
    .loadClass( name + "$" )
    .getField( "MODULE$" )
    .get( null )

}
