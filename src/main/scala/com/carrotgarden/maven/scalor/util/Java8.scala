package com.carrotgarden.maven.scalor.util

import scala.language.implicitConversions

import java.util.function.{
  Function ⇒ JFunction,
  Supplier ⇒ JSupplier,
  Predicate ⇒ JPredicate,
  BiPredicate
}

object Java8 {

  implicit def convert_ToJavaFunction[ A, B ]( f : Function1[ A, B ] ) = new JFunction[ A, B ] {
    override def apply( a : A ) : B = f( a )
  }

  implicit def convert_ToJavaSupplier[ A ]( a : A ) = new JSupplier[ A ] {
    override def get() : A = a
  }

}
