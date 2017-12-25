package com.carrotgarden.maven.scalor.util

import scala.util.matching.Regex

/**
 * Common parameter functions.
 */
object Params {

  val parse = """\s*([^s]+)\s*=\s*([^\s]+)\s*""".r

  /**
   * Extract key=value map form settings.
   */
  def parameterMap(
    settings :  String,
    separator : String, parse : Regex = Params.parse
  ) = {
    settings
      .split( separator )
      .map( _.trim )
      .filterNot( _.isEmpty )
      .collect { case parse( key, value ) => ( key, value ) }
      .toMap
  }

  type RegexMap = Map[ String, Regex ]

  trait RegexMapper {
    def map( text : String ) : String = text
  }

  object NoRegexMapper extends RegexMapper

  case class SettingsRegexMapper( settings : RegexMap ) extends RegexMapper {
    override def map( text : String ) : String = {
      settings.find {
        case ( key, regex ) => regex.pattern.matcher( text ).matches
      }.map {
        case ( key, regex ) => key
      }.getOrElse( text )
    }
  }

  def regexParamMap( settings : Map[ String, String ] ) : RegexMap = {
    settings.map {
      case ( key, value ) => ( key, value.r )
    }
  }

  /**
   * Generate Scala epoch version, such as 2.12
   */
  def versionEpoch( version : String ) = {
    version.split( "[.]" ).take( 2 ).mkString( "." )
  }

  /**
   * Generate Scala release version, such as 2.12.4
   */
  def versionRelease( version : String ) = {
    version.split( "[.]" ).take( 3 ).mkString( "." )
  }

  def versionPair( version : String ) = ( versionEpoch( version ), versionRelease( version ) )

}
