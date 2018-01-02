package com.carrotgarden.maven.design.util

import org.codehaus.plexus.classworlds.realm.ClassRealm
import java.util.concurrent.Callable
import Chiller._
import ChillerUtil._
import java.io.File
import java.net.URLClassLoader
import ChillerRepo._
import Chiller._

object ChillerDemo {

  class ListMaker( unused : Runnable )
    extends Callable[ List[ Option[ String ] ] ] {
    def call() : List[ Option[ String ] ] = {
      val list = "1" :: "2" :: "3" :: "4" :: "5" :: Nil
      list.map( Option( _ ) )
    }
  }

  def main( args : Array[ String ] ) : Unit = {

    val folder = new File( "." )

    minlogConfig( folder )

    val proxyRealm = this.getClass.getClassLoader

    val testList = withCast[ List[ Option[ String ] ], ListMaker, Runnable ](
      proxyRealm, classOf[ ListMaker ], classOf[ Runnable ], null
    )
    println( s"testList : ${testList}" )

  }

}

object ChillerUtil {

  def register( realm : ClassRealm, list : Array[ File ] ) = {
    list.foreach( file => realm.addURL( file.toURI.toURL ) )
  }

  def loaderOf( inst : Object ) = inst.getClass.getClassLoader.asInstanceOf[ URLClassLoader ]

}

object ChillerRepo {

  import com.carrotgarden.maven.scalor.util.Folder._
  
  case class ProxyOption()
    extends Callable[ Option[ String ] ] {
    def call() : Option[ String ] = {

      val proxyLoader = loaderOf( this )
      println( "proxy loader " + proxyLoader )
      // println( dumpLoader( proxyLoader ) )

      val project = findJarByResource( proxyLoader, "com/carrotgarden/maven/scalor" )
      //println( "proxy project \n" + project.mkString( "\n" ) )

      Some( "hello-kitty" )

    }
  }

}
