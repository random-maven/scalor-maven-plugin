package com.carrotgarden.maven.design

import java.io.File
import scala.collection.JavaConverters._
import com.carrotgarden.maven.scalor.eclipse.ParamsConfig
import scala.reflect.runtime.{ universe => ru }
import xsbti.compile.CompileOrder
import java.lang.reflect.Modifier
import com.carrotgarden.maven.scalor.eclipse.ParamsConfig
import java.io.File
import scala.reflect.api.materializeTypeTag
import com.carrotgarden.maven.design.conf.Params
import com.carrotgarden.maven.scalor.eclipse.ParamsConfig
import java.io.File

import com.carrotgarden.maven.scalor.util.Classer

object DemoParams {

  def typeTag[ T : ru.TypeTag ]( obj : T ) = ru.typeTag[ T ]

  def main( args : Array[ String ] ) : Unit = {

    println( s"XXX ${Classer.primitiveWrap( java.lang.Boolean.TYPE )}" )

    val face = classOf[ Params ]
    val klaz = classOf[ ParamsConfig ]

    face.getMethods.foreach { method =>

      val mod = method.getModifiers

      val isDefault = method.isDefault
      val isAbstract = Modifier.isAbstract( mod )
      val isStatic = Modifier.isStatic( mod )
      val isVoid = method.getReturnType == Void.TYPE

      val hasGetter = isAbstract && !isDefault && !isStatic && !isVoid

      if ( hasGetter ) {
        println( s"${method}" )
        val name = method.getName
        val javaField = klaz.getDeclaredField( name )
      }

    }

  }

  def main2( args : Array[ String ] ) : Unit = {

    val handle = ParamsConfig()

    println( handle )

    val tag = typeTag( handle )

    println( tag )

    val klaz = classOf[ ParamsConfig ]

    ru.typeOf[ ParamsConfig ].members.foreach { member =>
      val method = member.asMethod
      val hasGetter = method.isVar && method.isGetter
      if ( hasGetter ) {
        val name = method.name.toString
        val javaField = klaz.getDeclaredField( name )
        val javaType = javaField.getType
        javaField.setAccessible( true )

        println( s"${javaField} ${javaType}" )

        val value = javaType match {
          case k : Class[ _ ] if k == classOf[ String ]       => "hello"
          case k : Class[ _ ] if k == classOf[ File ]         => new File( "" )
          case k : Class[ _ ] if k == classOf[ Boolean ]      => true
          case k : Class[ _ ] if k == classOf[ CompileOrder ] => CompileOrder.Mixed
        }
        javaField.set( handle, value )
        println( s"${javaField.get( handle )}" )

      }
    }

    //    val mirror = ru.runtimeMirror( this.getClass.getClassLoader )
    //    val someType = mirror.typeOf[ ParamsHandle ]

  }

}
