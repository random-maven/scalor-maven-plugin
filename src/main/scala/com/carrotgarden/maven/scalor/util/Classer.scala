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

  /**
   * Execute block with context class loader switch.
   */
  def withContextLoader[ T ]( target : ClassLoader )( block : => T ) : T = {
    val thread = Thread.currentThread
    val source = thread.getContextClassLoader
    try {
      thread.setContextClassLoader( target )
      block
    } finally {
      thread.setContextClassLoader( source )
    }
  }

  import java.lang.reflect.Field

  // https://gist.github.com/carymrobbins/7b8ed52cd6ea186dbdf8

  def prettyPrint(
    instance :        Any,
    indentSize :      Int = 2,
    maxElementWidth : Int = 40,
    depth :           Int = 0
  ) : String = {

    val indent = " " * depth * indentSize
    val fieldIndent = indent + ( " " * indentSize )
    val thisDepth = prettyPrint( _ : Any, indentSize, maxElementWidth, depth )
    val nextDepth = prettyPrint( _ : Any, indentSize, maxElementWidth, depth + 1 )

    val replaceMap = Seq(
      "\n" -> "\\n",
      "\r" -> "\\r",
      "\t" -> "\\t",
      "\"" -> "\\\""
    )

    instance match {

      // Strings.
      case s : String =>
        '"' + replaceMap.foldLeft( s ) { case ( acc, ( c, r ) ) => acc.replace( c, r ) } + '"'

      // Lists
      case xs : Seq[ _ ] if xs.isEmpty => xs.toString()

      // Lists.
      case xs : Seq[ _ ] =>
        // If the Seq is not too long, pretty print on one line.
        val resultOneLine = xs.map( nextDepth ).toString()
        if ( resultOneLine.length <= maxElementWidth ) return resultOneLine
        // Otherwise, build it with newlines and proper field indents.
        val result = xs.map( x => s"\n$fieldIndent${nextDepth( x )}" ).toString()
        result.substring( 0, result.length - 1 ) + "\n" + indent + ")"

      // Case classes
      case kase : Product =>
        val prefix = kase.productPrefix
        // We'll use reflection to get the constructor arg names and values.
        val cls = kase.getClass
        val fields = cls.getDeclaredFields.filterNot( _.isSynthetic ).map( _.getName )
        val values = kase.productIterator.toSeq
        // If we weren't able to match up fields/values, fall back to toString.
        if ( fields.length != values.length ) return kase.toString
        fields.zip( values ).toList match {
          // If there are no fields, just use the normal String representation.
          case Nil                 => kase.toString
          // If there is just one field, let's just print it as a wrapper.
          case ( _, value ) :: Nil => s"$prefix(${thisDepth( value )})"
          // If there is more than one field, build up the field names and values.
          case kvps =>
            val prettyFields = kvps.map { case ( k, v ) => s"$fieldIndent$k = ${nextDepth( v )}" }
            // If the result is not too long, pretty print on one line.
            val resultOneLine = s"$prefix(${prettyFields.mkString( ", " )})"
            if ( resultOneLine.length <= maxElementWidth ) return resultOneLine
            // Otherwise, build it with newlines and proper field indents.
            s"$prefix(\n${prettyFields.mkString( ",\n" )}\n$indent)"
        }

      // Any other type.
      case _ => instance.toString
    }

  }

}
