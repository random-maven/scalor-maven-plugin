package com.carrotgarden.maven.scalor.eclipse

import com.carrotgarden.maven.tools.Description

import org.apache.maven.plugins.annotations._
import java.io.File

trait AnyPar {

  @Description( """
  Eclipse settings option list separator.
  """ )
  @Parameter(
    property     = "scalor.eclipseOptionSeparator",
    defaultValue = """[;\n]"""
  )
  var eclipseOptionSeparator : String = _

}
