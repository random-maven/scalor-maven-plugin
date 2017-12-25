package com.carrotgarden.maven.scalor.util

import java.util.concurrent.Callable
import scala.reflect.ClassTag

import com.esotericsoftware.minlog.Log
import java.io.ByteArrayOutputStream
import com.esotericsoftware.kryo.io.Output
import com.esotericsoftware.kryo.io.Input
import java.io.ByteArrayInputStream

import Chiller._

/**
 * Class path support.
 */
object Classer {

  /**
   * Obtain companion object for a type.
   */
  def classerCompanion[ T ]( klaz : Class[ T ] )(
    implicit
    MF : Manifest[ T ], loader : ClassLoader
  ) : T =
    classerCompanion( klaz.getName )

  /**
   * Obtain companion object for a type.
   */
  def classerCompanion[ T ]( name : String )(
    implicit
    MF : Manifest[ T ], loader : ClassLoader
  ) : T =
    loader.loadClass( name + "$" ).getField( "MODULE$" ).get( MF.runtimeClass ).asInstanceOf[ T ]

  trait Companion[ T ] {
    type Module
    def apply() : Module
  }

  object Companion {
    implicit def companion[ T ]( implicit module : Companion[ T ] ) = module.apply()
  }

  /**
   * Obtain companion object for a type.
   */
  def fakeCompanion[ T ]( klaz : Class[ T ] ) : AnyRef = fakeCompanion( klaz.getName )

  /**
   * Obtain companion object for a type.
   */
  def fakeCompanion[ T ]( name : String ) : AnyRef =
    this.getClass.getClassLoader.loadClass( name + "$" ).getField( "MODULE$" ).get( null )

}
