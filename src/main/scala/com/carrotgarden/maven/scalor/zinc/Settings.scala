package com.carrotgarden.maven.scalor.zinc

import scala.tools.nsc
import scala.collection.mutable.HashSet

import com.carrotgarden.maven.scalor.meta.TypeCopy
import org.scalaide.ui.internal.preferences.ScalaPluginSettings

import Settings._
import scala.tools.nsc.settings.ScalaVersion

/**
 * Abstract away from Scala, Scala IDE, Maven Zinc
 */
object Settings {

  val version211 = ScalaVersion( "2.11.0" )
  val version212 = ScalaVersion( "2.12.0" )
  val version213 = ScalaVersion( "2.13.0" )

  type ErrorFun = String => Unit

  val NoError : ErrorFun = String => ()

  /**
   * @param standard - ScalaC compiler options
   * @param extended - options recognized by Scala IDE
   * @param compileOrder - option recognized by Maven Zinc compiler
   * @param maxErrors - option recognized by Maven Zinc compiler
   */
  case class Config(
    standard :     Array[ String ],
    extended :     Array[ String ],
    compileOrder : String,
    maxErrors :    Int,
    reportFun :    ( () => String ) // deferred rendering
  )

  /**
   * Produce compiler options as arguments list.
   */
  def unparseArray( settings : nsc.Settings ) : Array[ String ] = {
    userSettings( settings ).toList.sortBy( _.name ).flatMap( _.unparse ).toArray
  }

  /**
   * Produce compiler options as arguments list.
   */
  def unparseString( settings : nsc.Settings ) : String = {
    unparseArray( settings ).mkString( " " )
  }

  /**
   * User-set settings with work around for missing entries.
   */
  def userSettings( settings : nsc.Settings ) : Set[ nsc.Settings#Setting ] = {
    val result = settings.userSetSettings ++ settings.prefixSettings
    result.toSet.asInstanceOf[ Set[ nsc.Settings#Setting ] ] // path-dependent type cast
  }

  /**
   * Report available/effective compiler settings.
   */
  def report( settings : nsc.Settings ) : String = {
    val text = new StringBuffer
    import text._
    settings.visibleSettings.toList.sortBy( _.name ).foreach { setting =>
      append( "\n" )
      append( setting.name )
      append( "\n" )
      append( "   description: " ); append( setting.helpDescription )
      append( "\n" )
      append( "   syntax format: " ); append( setting.helpSyntax )
      append( "\n" )
      append( "   effective value: " ); append( setting.value )
      append( "\n" )
      append( "   user provided entry: " ); append( setting.unparse.mkString( " " ) )
      append( "\n" )
    }
    text.toString
  }

  /**
   * Abstract away from Scala, ScalaIDE, Maven Zinc
   */
  def extract( version : ScalaVersion, array : Array[ String ], errorFun : ErrorFun ) : Config = {

    val list = array.toList

    val standard = new SettingsProvider( version, NoError )
    standard.optionsContract()
    standard.processArguments( list, true )

    val extended = new SettingsProvider( version, errorFun )
    extended.optionsExpand()
    extended.processArguments( list, true )

    Config(
      standard     = unparseArray( standard ),
      extended     = unparseArray( extended ),
      compileOrder = extended.zincCompileOrder,
      maxErrors    = extended.zincMaximumErrors,
      reportFun    = () => report( extended )
    )

  }

}
/**
 * Compiler options with version compatibility mode.
 */
class SettingsProvider( version : ScalaVersion, errorFun : ErrorFun ) extends nsc.Settings( errorFun ) {

  /**
   * Path-dependent type cast.
   */
  def configSet = allSettings.asInstanceOf[ HashSet[ nsc.Settings#Setting ] ]

  /**
   * Extract Zinc compiler order.
   */
  def zincCompileOrder : String = {
    configSet.find( _.name == SettingsProvider.compileOrder.name ).get.value.asInstanceOf[ String ]
  }

  /**
   * Extract Zinc maximum errors.
   */
  def zincMaximumErrors : Int = {
    configSet.find( _.name == SettingsProvider.maxerrsCompat.name ).get.value.asInstanceOf[ Int ]
  }

  /**
   * Version compatibility mode: emulate options available for Scala IDE.
   */
  def optionsExpand() = {
    SettingsProvider.optionsIDE.foreach( setting => configSet += setting )
    if ( version < version212 ) {
      SettingsProvider.options211.foreach( setting => configSet += setting )
    }
  }

  /**
   * Version compatibility mode: emulate original settings @ version.
   */
  def optionsContract() = {
    SettingsProvider.optionsIDE.foreach( setting => configSet -= setting )
    if ( version < version212 ) {
      SettingsProvider.options211.foreach( setting => configSet -= setting )
    }
  }

}

// TODO macro
//@TypeCopy[ ScalaPluginSettings.type ]()
//object SettingsTesterIDE

/**
 * Configure build manager for Scala IDE.
 *
 * Re-produce settings from [[org.scalaide.ui.internal.preferences.ScalaPluginSettings]]
 */
object SettingsProvider extends nsc.Settings {

  // IDE

  val compileOrder = ChoiceSetting(
    "-compileorder", "which", "Compilation order",
    List( "Mixed", "JavaThenScala", "ScalaThenJava" ), "Mixed"
  )
  val stopBuildOnErrors = new BooleanSettingWithDefault(
    "-stopBuildOnError", "Stop build if dependent projects have errors.", true
  )
  val relationsDebug = BooleanSetting(
    "-relationsDebug", "Log very detailed information about relations, such as dependencies between source files."
  )
  val withVersionClasspathValidator = new BooleanSettingWithDefault(
    "-withVersionClasspathValidator", "Check Scala compatibility of jars in classpath", true
  )
  val apiDiff = BooleanSetting(
    "-apiDiff", "Log type diffs that trigger additional compilation (slows down builder)"
  )
  val recompileOnMacroDef = BooleanSetting(
    "-recompileOnMacroDef", "Always recompile all dependencies of a macro def"
  )
  val useScopesCompiler = new BooleanSettingWithDefault(
    "-useScopesCompiler", "Compiles every scope separately.", true
  )

  // Missing in 2.11

  val maxerrsCompat = IntSetting( "-Xmaxerrs", "Maximum errors to print", 100, None, _ => None )

  //

  val optionsIDE : List[ nsc.Settings#Setting ] = List(
    compileOrder,
    stopBuildOnErrors,
    relationsDebug,
    withVersionClasspathValidator,
    apiDiff,
    recompileOnMacroDef,
    useScopesCompiler
  ).map( _.asInstanceOf[ nsc.Settings#Setting ] )

  val options211 : List[ nsc.Settings#Setting ] = List(
    maxerrsCompat
  ).map( _.asInstanceOf[ nsc.Settings#Setting ] )

  /** A setting represented by a boolean flag, with a custom default */
  // original code from MutableSettings#BooleanSetting
  class BooleanSettingWithDefault(
    name :        String,
    descr :       String,
    val default : Boolean
  )
    extends Setting( name, descr ) {
    type T = Boolean
    protected var v : Boolean = false
    override def value : Boolean = v

    def tryToSet( args : List[ String ] ) = { value = true; Some( args ) }
    def unparse : List[ String ] = if ( value ) List( name ) else Nil
    override def tryToSetFromPropertyValue( s : String ) : Unit = { // used from ide
      value = s.equalsIgnoreCase( "true" )
    }
    override def tryToSetColon( args : List[ String ] ) = args match {
      case Nil => tryToSet( Nil )
      case List( x ) =>
        if ( x.equalsIgnoreCase( "true" ) ) {
          value = true
          Some( Nil )
        } else if ( x.equalsIgnoreCase( "false" ) ) {
          value = false
          Some( Nil )
        } else errorAndValue( "'" + x + "' is not a valid choice for '" + name + "'", None )
    }

  }

}
