package com.carrotgarden.maven.scalor.base

import com.carrotgarden.maven.tools.Description
import org.apache.maven.plugins.annotations.Parameter

trait AnyPar {

  @Description( """
  Common separator for list values provided in pom.xml.
  Note: <![CDATA[ ... ]]> brackets can help preserve text entries in pom.xml.
  Separator is used as follows:   
    options.split( separator ).map( _.trim ).filterNot( _.isEmpty )
  """ )
  @Parameter(
    property     = "scalor.commonSequenceSeparator",
    defaultValue = """[;\n]"""
  )
  var commonSequenceSeparator : String = _

  /**
   * Produce clean options sequence.
   */
  def parseCommonSequence( options : String, separator : String ) : Array[ String ] = {
    options.split( separator ).map( _.trim ).filterNot( _.isEmpty )
  }

}
