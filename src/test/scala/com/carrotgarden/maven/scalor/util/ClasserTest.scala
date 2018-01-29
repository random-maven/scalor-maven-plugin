package com.carrotgarden.maven.scalor.util

import org.junit.jupiter.api._
import org.junit.runner.RunWith
import org.junit.platform.runner.JUnitPlatform

@RunWith( classOf[ JUnitPlatform ] )
class ClasserTest extends AnyRef {

  import Classer._

  @Test
  def companion: Unit = {
    implicit val loader = this.getClass.getClassLoader

    val module = trueCompanion( classOf[ ClasserModule ] )

    println( "module=" + module )

  }

}

class ClasserModule {

}

object ClasserModule extends ClasserModule {

}
