package com.carrotgarden.maven.scalor.eclipse

import org.eclipse.core.runtime.preferences.InstanceScope
import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.preferences.IScopeContext
import org.eclipse.core.resources.ProjectScope

/**
 * Eclipse preferences support.
 */
trait Prefs {

  import Prefs._

  val plugin : Plugin.Activator

  def prefsPluginSettings = InstanceScope.INSTANCE.getNode( plugin.pluginId )

  def prefsProjectScope( project : IProject ) : IScopeContext = new ProjectScope( project )

  def prefsProjectSettings( project : IProject ) =
    prefsProjectScope( project ).getNode( plugin.pluginId )

  def prefsStringGet( project : IProject, key : String ) =
    Option( prefsProjectSettings( project ).get( key, null ) )

  def prefsStringPut( project : IProject, key : String, option : Option[ String ] ) = {
    val settings = prefsProjectSettings( project )
    option match {
      case Some( value ) => settings.put( key, value )
      case None          => settings.remove( key )
    }
    settings.flush()
  }

  def prefsParamsConfig( project : IProject ) : Option[ ParamsConfig ] =
    prefsStringGet( project, key.config ).map( ParamsConfig.parse( _ ) )

  def prefsParamsConfig_=( project : IProject )( option : Option[ ParamsConfig ] ) =
    prefsStringPut( project, key.config, option.map( ParamsConfig.unparse( _ ) ) )

}

object Prefs {

  object key {
    val config = "config"
  }

}
