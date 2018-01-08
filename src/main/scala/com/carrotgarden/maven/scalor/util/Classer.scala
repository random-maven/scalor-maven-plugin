package com.carrotgarden.maven.scalor.util

import java.util.concurrent.Callable
import scala.reflect.ClassTag

import java.io.ByteArrayOutputStream
import java.io.ByteArrayInputStream
import java.io.StringWriter
import java.io.PrintWriter

/**
 * Classes support.
 */
object Classer {

  val primitiveMap = Map[ Class[ _ ], Class[ _ ] ](
    java.lang.Boolean.TYPE -> classOf[ java.lang.Boolean ],
    java.lang.Byte.TYPE -> classOf[ java.lang.Byte ],
    java.lang.Short.TYPE -> classOf[ java.lang.Short ],
    java.lang.Character.TYPE -> classOf[ java.lang.Character ],
    java.lang.Integer.TYPE -> classOf[ java.lang.Integer ],
    java.lang.Long.TYPE -> classOf[ java.lang.Long ],
    java.lang.Float.TYPE -> classOf[ java.lang.Float ],
    java.lang.Double.TYPE -> classOf[ java.lang.Double ]
  )

  def reportTrace( error : Throwable ) : String = {
    val writer = new StringWriter
    val printer = new PrintWriter( writer )
    error.printStackTrace( printer )
    writer.toString
  }

  /**
   * Convert from primitive type into wrapper type.
   */
  def primitiveWrap( klaz : Class[ _ ] ) = {
    primitiveMap.getOrElse( klaz, klaz )
  }

  /**
   * Obtain true companion object for a type.
   * True companion extends the type.
   */
  def trueCompanion[ T ]( klaz : Class[ T ] )(
    implicit
    MF : Manifest[ T ], loader : ClassLoader
  ) : T =
    trueCompanion( klaz.getName )

  /**
   * Obtain true companion object for a type.
   * True companion extends the type.
   */
  def trueCompanion[ T ]( name : String )(
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
   * Fake companion does not extend the type.
   */
  def fakeCompanion[ T ]( klaz : Class[ T ] ) : AnyRef = fakeCompanion( klaz.getName )

  /**
   * Obtain companion object for a type.
   * Fake companion does not extend the type.
   */
  def fakeCompanion[ T ]( name : String ) : AnyRef =
    this.getClass.getClassLoader.loadClass( name + "$" ).getField( "MODULE$" ).get( null )

}
