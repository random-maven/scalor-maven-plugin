package com.carrotgarden.maven.scalor

import com.twitter.chill._

object DemoChill {

  def main( args : Array[ String ] ) : Unit = {

    val source = Option( "hello-kitty" )

    val kryo = KryoPool.withByteArrayOutputStream( 10, KryoSerializer.registered )

    val array = kryo.toBytesWithClass( source );

    val target = kryo.fromBytes( array );

    println( "source " + source )
    println( "target " + target )

  }

}
