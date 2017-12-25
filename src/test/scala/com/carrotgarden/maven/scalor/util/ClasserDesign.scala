package com.carrotgarden.maven.scalor.util

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import org.powermock.classloading.SingleClassloaderExecutor
import java.util.concurrent.Callable
import javassist.util.proxy.ProxyFactory
import javassist.util.proxy.MethodFilter
import javassist.util.proxy.MethodHandler
import javassist.ClassPool
import java.net.URLClassLoader
import sun.misc.URLClassPath
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream
import java.io.ByteArrayInputStream
import java.io.ObjectInputStream
import java.util.concurrent.Callable
import java.io.FileInputStream
import java.io.ObjectStreamClass
import java.util.IdentityHashMap
import org.powermock.classloading.DeepCloner
import com.twitter.chill.KryoPool
import com.twitter.chill.KryoSerializer

object ClasserDesign {

  val noTypeList = Array[ Class[ _ ] ]()
  val noArgsList = Array[ Object ]()

  val classPool = new ClassPool( true )

  /**
   * Create recursive dynamic class proxy.
   */
  def javassistProxy[ T ]( sourceType : Class[ T ], targetValue : Object ) //
  : T = {

    val sourceName = sourceType.getName

    if ( targetValue == null ) {
      return null.asInstanceOf[ T ]
    }

    if ( sourceType.isPrimitive ) {
      return targetValue.asInstanceOf[ T ]
    }

    val targetType = targetValue.getClass

    if ( sourceType.isAssignableFrom( targetType ) ) {
      return targetValue.asInstanceOf[ T ]
    }

    val sourceLoader = sourceType.getClassLoader
    val targetLoader = targetType.getClassLoader

    /**
     * Map values from source into target class loader.
     */
    def convertSourceAny( sourceType : Class[ _ ], sourceValue : Object ) : Object = {
      if ( sourceValue == null ) {
        return null
      }
      if ( sourceType.isPrimitive() ) {
        return sourceValue
      }
      if ( sourceType.isArray() ) {
        return convertSourceArray( sourceType.getComponentType, sourceValue.asInstanceOf[ Array[ _ ] ] )
      }
      val targetType = targetLoader.loadClass( sourceType.getName ).asInstanceOf[ Class[ AnyRef ] ]
      javassistProxy( targetType, sourceValue )
    }

    /**
     * Map values from source into target class loader.
     */
    def convertSourceArray( sourceType : Class[ _ ], sourceValue : Array[ _ ] ) : Object = {
      if ( sourceType.isPrimitive ) {
        return sourceValue
      }
      val length = sourceValue.length
      val targetType = targetLoader.loadClass( sourceType.getName )
      val targetValue = java.lang.reflect.Array
        .newInstance( targetType, length ).asInstanceOf[ Array[ AnyRef ] ]
      var index = 0
      while ( index < length ) {
        val source = sourceValue( index ).asInstanceOf[ Object ]
        val target = convertSourceAny( sourceType, source )
        targetValue( index ) = source
        index += 1
      }
      targetValue
    }

    /**
     * Map values from target into source class loader.
     */
    def convertTargetAny( sourceType : Class[ _ ], targetValue : Object ) : Object = {
      if ( targetValue == null ) {
        return null
      }
      if ( sourceType.isPrimitive() ) {
        return targetValue
      }
      if ( sourceType.isArray() ) {
        return convertTargetArray( sourceType.getComponentType, targetValue.asInstanceOf[ Array[ _ ] ] )
      }
      javassistProxy( sourceType.asInstanceOf[ Class[ AnyRef ] ], targetValue )
    }

    /**
     * Map values from target into source class loader.
     */
    def convertTargetArray( sourceType : Class[ _ ], targetValue : Array[ _ ] ) : Object = {
      if ( sourceType.isPrimitive ) {
        return targetValue
      }
      val length = targetValue.length
      val sourceValue = java.lang.reflect.Array
        .newInstance( sourceType, length ).asInstanceOf[ Array[ AnyRef ] ]
      var index = 0
      while ( index < length ) {
        val target = targetValue( index ).asInstanceOf[ Object ]
        val source = convertTargetAny( sourceType, target )
        sourceValue( index ) = source
        index += 1
      }
      sourceValue
    }

    if ( sourceType.isArray ) {
      val targetArray = targetValue.asInstanceOf[ Array[ _ ] ]
      return convertTargetArray( sourceType.getComponentType, targetArray ).asInstanceOf[ T ]
    }

    if ( ( sourceType.getModifiers & java.lang.reflect.Modifier.FINAL ) != 0 ) {

    }

    val sourceFactory = new ProxyFactory {
      override def getClassLoader : ClassLoader = sourceLoader
    }

    val sourceHandler = new MethodHandler {
      def invoke(
        proxy : Object, sourceMethod : Method, proceed : Method, argsList : Array[ Object ]
      ) : Object = {

        /**
         * Map argument types from source into target class loader.
         */
        val sourceTypeList = sourceMethod.getParameterTypes
        val targetTypeList = sourceTypeList.map(
          klaz => targetLoader.loadClass( klaz.getName )
        )

        /**
         * Map argument values from source into target class loader.
         */
        val sourceArgsList = if ( argsList == null ) Array[ Object ]() else argsList
        val targetArgsList = sourceTypeList.zip( sourceArgsList ).map {
          case ( sourceType, sourceValue ) => convertSourceAny( sourceType, sourceValue )
        }

        /**
         * Map method from source into target class loader.
         */
        val targetMethod = targetValue.getClass.getMethod( sourceMethod.getName, targetTypeList : _* );

        /**
         * Produce calculation in target class loader.
         */
        val targetResult = targetMethod.invoke( targetValue, targetArgsList : _* )

        /**
         * Export results from target into source class loader.
         */
        val sourceReturnType = sourceMethod.getReturnType
        convertTargetAny( sourceReturnType, targetResult )
      }

    }

    if ( sourceType.isInterface ) {
      sourceFactory.setInterfaces( Array( sourceType ) );
    } else {
      sourceFactory.setSuperclass( sourceType );
    }

    val sourceValue = sourceFactory.create( noTypeList, noArgsList, sourceHandler )

    sourceValue.asInstanceOf[ T ]

  }

  def isObjectModule( instance : Object ) : Boolean = {
    isObjectModule( instance.getClass )
  }

  def isObjectModule( klaz : Class[ _ ] ) : Boolean = {
    try {
      klaz.getDeclaredField( "MODULE$" )
      true
    } catch {
      case e : Throwable =>
        false
    }
  }

  /**
   * Recursive dynamic class proxy handler.
   */
  case class Handler( sourceLoader : ClassLoader, targetInstance : Object )
    extends InvocationHandler {
    override def invoke(
      proxy :        Object,
      sourceMethod : Method, argsList : Array[ Object ]
    ) : Object = {

      val targetLoader = targetInstance.getClass.getClassLoader

      /**
       * Map values from source into target class loader.
       */
      def convertSourceAny( sourceType : Class[ _ ], sourceValue : Object ) : Object = {
        if ( sourceValue == null ) {
          return null
        }
        if ( sourceType.isArray() ) {
          return convertSourceArray( sourceType.getComponentType, sourceValue.asInstanceOf[ Array[ _ ] ] )
        }
        if ( sourceType.isInterface() ) {
          val targetType = targetLoader.loadClass( sourceType.getName )
          val targetFaceList = Array[ Class[ _ ] ]( targetType )
          val sourceHandler = Handler( targetLoader, sourceValue )
          return Proxy.newProxyInstance( targetLoader, targetFaceList, sourceHandler )
        }
        return sourceValue
      }

      /**
       * Map values from source into target class loader.
       */
      def convertSourceArray( sourceType : Class[ _ ], sourceValue : Array[ _ ] ) : Object = {
        if ( sourceType.isPrimitive ) {
          return sourceValue
        }
        val length = sourceValue.length
        val targetType = targetLoader.loadClass( sourceType.getName )
        val targetValue = java.lang.reflect.Array
          .newInstance( targetType, length ).asInstanceOf[ Array[ AnyRef ] ]
        var index = 0
        while ( index < length ) {
          val source = sourceValue( index ).asInstanceOf[ Object ]
          val target = convertSourceAny( sourceType, source )
          targetValue( index ) = source
          index += 1
        }
        targetValue
      }

      /**
       * Map values from target into source class loader.
       */
      def convertTargetAny( sourceType : Class[ _ ], targetValue : Object ) : Object = {
        if ( targetValue == null ) {
          return null
        }
        if ( sourceType.isArray() ) {
          return convertTargetArray( sourceType.getComponentType, targetValue.asInstanceOf[ Array[ _ ] ] )
        }
        if ( sourceType.isInterface ) {
          val sourceFaceList = Array[ Class[ _ ] ]( sourceType )
          val targetHandler = Handler( sourceLoader, targetValue )
          return Proxy.newProxyInstance( sourceLoader, sourceFaceList, targetHandler )
        }
        return targetValue
      }

      /**
       * Map values from target into source class loader.
       */
      def convertTargetArray( sourceType : Class[ _ ], targetValue : Array[ _ ] ) : Object = {
        if ( sourceType.isPrimitive ) {
          return targetValue
        }
        val length = targetValue.length
        val sourceValue = java.lang.reflect.Array
          .newInstance( sourceType, length ).asInstanceOf[ Array[ AnyRef ] ]
        var index = 0
        while ( index < length ) {
          val target = targetValue( index ).asInstanceOf[ Object ]
          val source = convertTargetAny( sourceType, target )
          sourceValue( index ) = source
          index += 1
        }
        sourceValue
      }

      /**
       * Map argument types from source into target class loader.
       */
      val sourceTypeList = sourceMethod.getParameterTypes
      val targetTypeList = sourceTypeList.map(
        klaz => targetLoader.loadClass( klaz.getName )
      )

      /**
       * Map argument values from source into target class loader.
       */
      val sourceArgsList = if ( argsList == null ) Array[ Object ]() else argsList
      val targetArgsList = sourceTypeList.zip( sourceArgsList ).map {
        case ( sourceType, sourceValue ) => convertSourceAny( sourceType, sourceValue )
      }

      /**
       * Map method from source into target class loader.
       */
      val targetMethod = targetInstance.getClass.getMethod( sourceMethod.getName, targetTypeList : _* );

      /**
       * Produce calculation in target class loader.
       */
      val targetResult = targetMethod.invoke( targetInstance, targetArgsList : _* )

      /**
       * Export results from target into source class loader.
       */
      val sourceReturnType = sourceMethod.getReturnType
      convertTargetAny( sourceReturnType, targetResult )

    }
  }

  /**
   * Create recursive dynamic class proxy.
   */
  def jdkProxy[ T <: Object ]( targetInstance : Object, sourceFaceList : Class[ T ]* ) : T = {
    val sourceLoader = sourceFaceList.getClass.getClassLoader
    val sourceHandler = Handler( sourceLoader, targetInstance )
    Proxy.newProxyInstance( sourceLoader, sourceFaceList.toArray, sourceHandler ).asInstanceOf[ T ]
  }

  /**
   * Execute code block in separate class loader via bi-directional deep clone.
   */
  def invokeIsolated[ T ]( block : => T )( implicit loader : ClassLoader ) : T = {
    invokeIsolated( new Callable[ T ] { def call = block } )( loader )
  }

  /**
   * Execute code block in separate class loader via bi-directional deep clone.
   */
  def invokeIsolated[ T ]( block : Callable[ T ] )( implicit loader : ClassLoader ) : T = {
    new SingleClassloaderExecutor( loader ).execute( block )
  }

  /**
   *
   */
  def withLoad[ T, F ](
    loader : ClassLoader, typeFace : Class[ T ], paraFace : Class[ F ], paraValue : Object
  ) : AnyRef = {
    val klaz = loader.loadClass( typeFace.getName )
    val ctor = klaz.getConstructor( paraFace )
    val inst = ctor.newInstance( paraValue ).asInstanceOf[ AnyRef ]
    inst
  }

  /**
   * Invoke source code block in target class loader.
   */
  def withRealm[ T ]( target : URLClassLoader )( block : Callable[ T ] ) : T = {
    val klaz = classOf[ ClassLoader ]
    val klazURL = classOf[ URLClassLoader ]
    val parentField = klaz.getDeclaredField( "parent" ); parentField.setAccessible( true )
    val urlListField = klazURL.getDeclaredField( "ucp" ); urlListField.setAccessible( true )

    val source = block.getClass.getClassLoader.asInstanceOf[ URLClassLoader ]

    val sourceParent = parentField.get( source )
    val sourceUrlList = urlListField.get( source )

    val targetParent = parentField.get( target )
    val targetUrlList = urlListField.get( target )

    val thread = Thread.currentThread
    val context = thread.getContextClassLoader

    try {
      thread.setContextClassLoader( target )
      parentField.set( source, targetParent )
      urlListField.set( source, targetUrlList )
      block.call
    } finally {
      thread.setContextClassLoader( context )
      parentField.set( source, sourceParent )
      urlListField.set( source, sourceUrlList )
    }
  }

  def dumpLoader( loader : ClassLoader ) : String = {
    val klaz = classOf[ ClassLoader ]
    val klazURL = classOf[ URLClassLoader ]
    val parentField = klaz.getDeclaredField( "parent" ); parentField.setAccessible( true )
    val urlListField = klazURL.getDeclaredField( "ucp" ); urlListField.setAccessible( true )

    val parent = parentField.get( loader )
    val urlList = urlListField.get( loader ).asInstanceOf[ URLClassPath ]

    val text = new StringBuffer
    text.append( "\n" )
    text.append( "--------------------------------------" )
    text.append( "\n" )

    text.append( "parent " + parent )
    text.append( "\n" )

    text.append( "loader " + loader )
    text.append( "\n" )

    urlList.getURLs
      .map( _.toExternalForm )
      .sorted
      .foreach { url =>
        text.append( "   " )
        text.append( url )
        text.append( "\n" )
      }

    text.append( "--------------------------------------" )
    text.append( "\n" )

    text.toString
  }

  def isCaseClass( instance : Object ) : Boolean = instance.isInstanceOf[ Product ]

  /**
   * Store identity of another object via serialization.
   */
  case class ObjectHandle( key : Int, klaz : String ) extends Serializable

  /**
   * Caster serlialize/deserialize object cache.
   */
  case class IdentityMap() extends IdentityHashMap[ Int, Object ]

  trait Logger {
    def info( line : String ) : Unit = ()
  }

  object NoLogger extends Logger

  /**
   * Cast an object from remote to local class loader.
   */
  case class ObjectCaster( logger : Logger = NoLogger ) {
    val cache = IdentityMap()
    def cast( remote : Object ) : Object = {
      import logger._
      info( s"remote: ${remote}" )
      val encoder = CasterEncoder( cache, logger )
      val binary = encoder.encode( remote )
      val decoder = CasterDecoder( cache, binary, logger )
      val local = decoder.decode()
      info( s"local: ${local}" )
      local
    }
  }

  def hasHandle( obj : Object ) = {
    obj.isInstanceOf[ ObjectHandle ]
  }

  def hasCloneable( obj : Object ) = {
    obj.isInstanceOf[ Cloneable ]
  }

  def hasSerializable( obj : Object ) = {
    obj.isInstanceOf[ Serializable ]
  }

  def hasSameType( local : ClassLoader, remote : ClassLoader, obj : Object ) = {
    if ( local == null && remote == null ) {
      true
    } else if ( local == null || remote == null ) {
      false
    } else {
      val typeName = obj.getClass.getName
      val localType = local.loadClass( typeName )
      val remoteType = remote.loadClass( typeName )
      identity( localType ) == identity( remoteType )
    }
  }

  /**
   * JVM unique object identity.
   */
  def identity( obj : Object ) = System.identityHashCode( obj )

  /**
   * Serialize object from remote class loader.
   */
  case class CasterEncoder(
    cache :  IdentityMap,
    logger : Logger                = NoLogger,
    local :  ClassLoader           = Classer.getClass.getClassLoader,
    output : ByteArrayOutputStream = new ByteArrayOutputStream()
  )
    extends ObjectOutputStream( output ) {
    import logger._
    lazy val cloner = new DeepCloner( local )
    enableReplaceObject( true )
    def encode( remote : Object ) : Array[ Byte ] = {
      info( s"encode:" )
      writeObject( remote )
      close
      output.toByteArray
    }
    def replaceWtihHandleSame( obj : Object ) : ObjectHandle = {
      val key = identity( obj );
      cache.put( key, obj )
      ObjectHandle( key, obj.getClass.getName )
    }
    def replaceWtihHandleClone( obj : Object ) : ObjectHandle = {
      val clone = cloner.clone( obj, false )
      replaceWtihHandleSame( clone )
    }
    override def replaceObject( obj : Object ) : Object = {
      val remote = obj.getClass.getClassLoader
      if ( obj == null ) {
        info( s"encode: null   :" )
        obj
      } else if ( hasHandle( obj ) ) {
        info( s"encode: handle : ${identity( obj )} ${obj}" )
        obj
      } else if ( hasSerializable( obj ) ) {
        info( s"encode: serial : ${identity( obj )} ${obj}" )
        obj
      } else if ( hasSameType( local, remote, obj ) ) {
        info( s"encode: same   : ${identity( obj )} ${obj}" )
        replaceWtihHandleSame( obj )
      } else {
        info( s"encode: clone  : ${identity( obj )} ${obj}" )
        replaceWtihHandleClone( obj )
      }
    }
    override def annotateClass( klaz : Class[ _ ] ) : Unit = {
      val name = klaz.getName
      info( s"encode>>> ${name}" )
    }
  }

  /**
   * De-serialize object into local class loader.
   */
  case class CasterDecoder(
    cache :  IdentityMap,
    input :  Array[ Byte ],
    logger : Logger        = NoLogger,
    local :  ClassLoader   = Classer.getClass.getClassLoader
  ) extends ObjectInputStream( new ByteArrayInputStream( input ) ) {
    import logger._
    enableResolveObject( true )
    def decode() = {
      info( s"decode:" )
      readObject()
    }
    def resolveHandle( handle : ObjectHandle ) = {
      import handle._
      if ( cache.containsKey( key ) ) {
        cache.get( key )
      } else {
        throw new RuntimeException( "Wrong handle: " + handle )
      }
    }
    override def resolveObject( obj : Object ) : Object = {
      if ( obj == null ) {
        info( s"decode: null   :" )
        obj
      } else if ( hasHandle( obj ) ) {
        info( s"decode: handle : ${identity( obj )} ${obj}" )
        resolveHandle( obj.asInstanceOf[ ObjectHandle ] )
      } else if ( hasSerializable( obj ) ) {
        info( s"decode: serial : ${identity( obj )} ${obj}" )
        obj
      } else {
        throw new RuntimeException( "Can not resolve: " + obj )
      }
    }
    override def resolveClass( meta : ObjectStreamClass ) : Class[ _ ] = {
      val name = meta.getName
      info( s"decode>>> ${name}" )
      Class.forName( name, false, local )
    }
  }

  /**
   *
   */
  def cloneCast[ T <: Serializable ]( source : AnyRef ) : T = {
    val outputArray = new ByteArrayOutputStream()
    val outputObject = new ObjectOutputStream( outputArray )
    outputObject.writeObject( source )
    val result = outputArray.toByteArray
    val inputArray = new ByteArrayInputStream( result )
    val inputObject = new ObjectInputStream( inputArray )
    val target = inputObject.readObject
    target.asInstanceOf[ T ]
  }

}
