package com.carrotgarden.maven.design

import javassist.ClassPool
import org.scalaide.core.IScalaPlugin
import javassist.Modifier
import org.scalaide.core.IScalaPlugin

object DemoJavassist {

  def main( args : Array[ String ] ) = {

    val cp = new ClassPool( true );

    val name = classOf[ IScalaPlugin ].getName + "$"
    
    val ctClass = cp.get( name );
    println( "ctClass" + ctClass );

    val ctField = ctClass.getDeclaredField( "MODULE$" );
    println( "ctField" + ctField );

//    val klaz1 = ctClass.toClass();
//    println( "klaz1" + klaz1 );

    val mods = ctClass.getModifiers

    ctClass.setModifiers( mods & ~Modifier.FINAL )
    
    val name2 = name + "$A" 
    
    ctClass.setName(name2)
    println( "ctClass" + ctClass );
    val klaz2 = ctClass.toClass();
    println( "klaz2" + klaz2 );

  }

}
