package com.carrotgarden.maven.scalor.util

import com.esotericsoftware.kryo.Kryo
import com.twitter.chill.IKryoRegistrar
import com.twitter.chill.AllScalaRegistrar
import org.objenesis.strategy.StdInstantiatorStrategy

import java.util.concurrent.Callable
import scala.reflect.ClassTag
import com.esotericsoftware.minlog.Log
import java.io.ByteArrayOutputStream
import com.esotericsoftware.kryo.io.Output
import java.io.ByteArrayInputStream
import com.esotericsoftware.kryo.io.Input

import com.carrotgarden.maven.scalor.A
import java.io.File
import java.nio.file.Paths

import Folder._
import java.io.PrintWriter
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Arrays
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import com.twitter.chill.ScalaKryoInstantiator

/**
 * Kryo/Chill serializer for scala.
 */
object Chiller {

  /**
   * Settings for kryo logger.
   */
  object log {
    object key {
      val a = A.maven.name + "." + "minlog"
      val enable = a + ".enable"
      val level = a + ".level"
      val file = a + ".file"
    }
    def enable =
      System.getProperty( key.enable, false.toString ).toBoolean
    def enable_=( state : Boolean = true ) =
      System.setProperty( key.enable, state.toString )
    def level =
      System.getProperty( key.level, Log.LEVEL_NONE.toString ).toInt
    def level_=( value : Int ) =
      System.setProperty( key.level, value.toString )
    def name =
      "chiller.log"
    def file : File = {
      val userHome = System.getProperty( "user.home", "." )
      val userFile = Paths
        .get( userHome, A.maven.name, name )
        .toFile.getCanonicalPath
      val path = System.getProperty( key.file, userFile )
      new File( path ).getCanonicalFile
    }
    def file_=( path : File ) =
      System.setProperty( key.file, path.getCanonicalPath )
  }

  /**
   * Configure kryo logger.
   */
  def minlogConfig(
    file :   File,
    enable : Boolean = true,
    level :  Int     = Log.LEVEL_TRACE
  ) = {
    log.file = file
    log.enable = enable
    log.level = level
  }

  /**
   * Activate kryo logger.
   */
  def minlogSetup() = if ( log.enable ) {
    val file = log.file
    ensureParent( file )
    if ( !file.exists() ) file.createNewFile
    val appends = new FileWriter( file, true )
    val printer = new PrintWriter( appends )
    //
    val form = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" )
    val date = new Date()
    printer.println( "=" * 80 )
    printer.println( s"Session: ${form.format( date )}" )
    printer.flush
    //
    val logger = new Log.Logger {
      override def print( line : String ) : Unit = {
        printer.println( line )
        printer.flush
      }
    }
    Log.setLogger( logger )
    Log.set( log.level )
  }

  /**
   */
  def makeKryo() = {
    val ator = new ScalaKryoInstantiator
    val kryo = ator.newKryo
    kryo
  }

  /**
   * Render readable binary array.
   */
  def asText( array : Array[ Byte ] ) : String = {
    new String( array, StandardCharsets.US_ASCII ).replaceAll( "\\p{C}", "*" )
  }

  /**
   */
  case class KryoEncoder( result : Object )
    extends Callable[ Array[ Byte ] ] {
    override def call() : Array[ Byte ] = {
      minlogSetup
      Log.info( s"ENCODE @ ${getClass.getClassLoader}" )
      val kryo = makeKryo()
      val output = new Output( new ByteArrayOutputStream() )
      kryo.writeClassAndObject( output, result )
      output.flush
      val stream = output.getOutputStream.asInstanceOf[ ByteArrayOutputStream ]
      val array = stream.toByteArray
      if ( Log.TRACE ) Log.trace( s"array : [${array.length}] ${asText( array )}" )
      array
    }
  }

  object KryoEncoder {
    val initParamType = classOf[ Object ]
  }

  /**
   */
  case class KryoDecoder( array : Array[ Byte ] )
    extends Callable[ Object ] {
    override def call() : Object = {
      minlogSetup
      Log.info( s"DECODE @ ${getClass.getClassLoader}" )
      if ( Log.TRACE ) Log.trace( s"array : [${array.length}] ${asText( array )}" )
      val kryo = makeKryo()
      val stream = new ByteArrayInputStream( array )
      val input = new Input( stream )
      val result = kryo.readClassAndObject( input )
      result
    }
  }

  object KryoDecoder {
    val initParamType = classOf[ Array[ Byte ] ]
  }

  /**
   * Make and cast object across class loaders.
   */
  def withCast[ R <: AnyRef, M <: Callable[ R ], P <: AnyRef ](
    remoteLoader : ClassLoader, maker : Class[ M ], paraFace : Class[ P ], paraValue : P
  )( implicit returnTag : ClassTag[ R ] ) : R = {
    val thread = Thread.currentThread
    val localLoader = this.getClass.getClassLoader
    val contextLoader = thread.getContextClassLoader

    val paramArray = try {
      thread.setContextClassLoader( localLoader )
      val encoder = KryoEncoder( paraValue )
      val param = encoder.call()
      param
    } finally {
      thread.setContextClassLoader( contextLoader )
    }
    val resultArray = try {
      thread.setContextClassLoader( remoteLoader )

      val decoderKlaz = remoteLoader.loadClass( classOf[ KryoDecoder ].getName )
      val decoderInit = decoderKlaz.getConstructor( classOf[ Array[ Byte ] ] )
      val decoderExec = decoderInit.newInstance( paramArray ).asInstanceOf[ Callable[ P ] ]
      val paramValue = decoderExec.call

      val makerKlaz = remoteLoader.loadClass( maker.getName )
      val paramType = remoteLoader.loadClass( paraFace.getName )
      val makerInit = makerKlaz.getConstructor( paramType )
      val makerExec = makerInit.newInstance( paramValue ).asInstanceOf[ Callable[ R ] ]
      val result = makerExec.call

      val encoderKlaz = remoteLoader.loadClass( classOf[ KryoEncoder ].getName )
      val encoderInit = encoderKlaz.getConstructor( classOf[ Object ] )
      val encoderExec = encoderInit.newInstance( result ).asInstanceOf[ Callable[ Array[ Byte ] ] ]
      val array = encoderExec.call

      array
    } finally {
      thread.setContextClassLoader( contextLoader )
    }
    val result = try {
      thread.setContextClassLoader( localLoader )
      val decoder = KryoDecoder( resultArray )
      val result = decoder.call
      result
    } finally {
      thread.setContextClassLoader( contextLoader )
    }
    result.asInstanceOf[ R ]
  }

}
