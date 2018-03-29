package com.carrotgarden.maven.scalor.base

/**
 * Build mode for multi-result scenario.
 */
object Mode {

  sealed trait Type {
    def name : String
  }

  case object BuildAlways extends Type {
    override val name = "always"
  }

  case object BuildNever extends Type {
    override val name = "never"
  }

  case object BuildFull extends Type {
    override val name = "full"
  }

  case object BuildIncr extends Type {
    override val name = "incr"
  }

  def buildMode( name : String ) : Type = {
    name match {
      case BuildAlways.name => BuildAlways
      case BuildNever.name  => BuildNever
      case BuildFull.name   => BuildFull
      case BuildIncr.name   => BuildIncr
      case _                => sys.error( s"Invalid mode: ${name}" )
    }
  }

  def hasBuildEnabled( mode : Type, incremental : Boolean ) : Boolean = {
    mode match {
      case BuildAlways => true
      case BuildNever  => false
      case BuildFull   => !incremental
      case BuildIncr   => incremental
    }
  }

}
