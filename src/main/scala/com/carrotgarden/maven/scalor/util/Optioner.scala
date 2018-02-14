package com.carrotgarden.maven.scalor.util

import scala.language.implicitConversions
import scala.reflect.ClassTag

/**
 * Scala options support.
 */
object Optioner {

  implicit def convert_Option_Value[ T ](
    option : Option[ T ]
  )(
    implicit
    tag : ClassTag[ T ]
  ) : T = {
    option.getOrElse( Error.Throw( s"Missing required value ${tag.runtimeClass.getName}" ) )
  }

}
