package com.carrotgarden.maven.scalor.scalanative

import Cacher._
import java.io.File
import scala.collection.concurrent.TrieMap

case class Cacher(
  nativeCache : TrieMap[ String, AnyRef ] = TrieMap[ String, AnyRef ]()
) {

}

object Cacher {

}
