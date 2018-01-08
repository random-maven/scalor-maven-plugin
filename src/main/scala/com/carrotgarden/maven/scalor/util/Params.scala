package com.carrotgarden.maven.scalor.util

import scala.util.matching.Regex

/**
 * Common parameter functions.
 */
object Params {

  type RegexMap = Map[ String, Regex ]

  /**
   * Extract key=value map form settings.
   */
  val extractor : Regex = """\s*([^s]+)\s*=\s*([^\s]+)\s*""".r

  /**
   * Extract key=value map form settings.
   */
  def parameterMap(
    settings :  String,
    separator : String,
    regex :     Regex  = extractor
  ) : Map[ String, String ] = {
    // work around https://issues.scala-lang.org/browse/SI-8825
    def hasMatch( text : String ) = regex.pattern.matcher( text ).matches
    def parseRegex( text : String ) = { val regex( key, value ) = text; ( key, value ) }
    settings
      .split( separator )
      .map( _.trim )
      .filterNot( _.isEmpty )
      .collect { case text if hasMatch( text ) => parseRegex( text ) }
      .toMap
  }

  /**
   * Convert paramter map into regex paramter map.
   */
  def regexParamMap( settings : Map[ String, String ] ) : RegexMap = {
    settings.map {
      case ( key, value ) => ( key, value.r )
    }
  }

  /**
   * Transform text matching to a regex with another text.
   */
  trait RegexMapper {
    def map( text : String ) : String = text // Identity transform.
  }

  /**
   * Identity transform.
   */
  object NoRegexMapper extends RegexMapper

  /**
   * Transform text matching to a regex with another text, provided by settings.
   * Specifically, if 'text' matches 'regex'  in the rule 'key=regex', return the 'key'.
   */
  case class SettingsRegexMapper( settings : RegexMap ) extends RegexMapper {
    def this( settings : String, separator : String ) = this(
      regexParamMap( parameterMap( settings, separator ) )
    )
    override def map( text : String ) : String = {
      settings.find {
        case ( key, regex ) => regex.pattern.matcher( text ).matches
      }.map {
        case ( key, regex ) => key
      }.getOrElse( text )
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
