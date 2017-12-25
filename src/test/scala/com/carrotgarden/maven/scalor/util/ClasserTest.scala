package com.carrotgarden.maven.scalor.util

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest._

@RunWith( classOf[ JUnitRunner ] )
class ClasserTest extends FunSuite {

  import Classer._

  test( "companion" ) {
    implicit val loader = this.getClass.getClassLoader

    val module = classerCompanion( classOf[ ClasserModule ] )

    println( "module=" + module )

  }

}

class ClasserModule {

}

object ClasserModule extends ClasserModule {

}
