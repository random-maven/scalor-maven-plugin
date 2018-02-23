package com.carrotgarden.maven.scalor.eclipse

import scala.collection.JavaConverters.mapAsScalaMapConverter

import org.eclipse.core.resources.ProjectScope
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.preferences.InstanceScope
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.ui.JavaUI
import org.eclipse.jdt.ui.PreferenceConstants

import com.carrotgarden.maven.scalor.format.Format.formatJavaOptions
import com.carrotgarden.maven.scalor.format.Format.formatJavaSettingsFile
import com.carrotgarden.maven.scalor.format.Format.formatScalaSettingsFile
import com.carrotgarden.maven.scalor.util.Optioner.convert_Option_Value

import scalariform.formatter.preferences.PreferencesImporterExporter
import com.carrotgarden.maven.scalor.util.Logging.AnyLog

/**
 * Manage Eclipse formatter settings.
 */
trait Format {

  /**
   * Build participant invocation.
   *
   * Transfer Maven formatter settings to Eclipse.
   */
  def formatEnsure(
    context : Config.BuildContext,
    monitor : IProgressMonitor
  ) : Unit = {
    import context._
    if ( format.eclipseFormatEnable ) {
      val formatLogger = logger.branch( "eclipse-format" )
      if ( format.formatJavaEnable ) {
        formatEnsureJava( context, formatLogger )
      }
      if ( format.formatScalaEnable ) {
        formatEnsureScala( context, formatLogger )
      }
    }
  }

  /**
   * Transfer Maven formatter settings to Eclipse.
   */
  def formatEnsureJava(
    context : Config.BuildContext,
    logger :  AnyLog
  ) : Unit = {
    logger.info( s"Applying Java formatter settings." )
    val project = context.facade.getProject
    val mavenProject = context.facade.getMavenProject
    val settingsFile = formatJavaSettingsFile( mavenProject, context.format ).getCanonicalFile
    logger.info( s"Java formatter settings file: ${settingsFile}" )
    val options = formatJavaOptions( settingsFile )
    val projectScope = new ProjectScope( project )
    // formatter preferences
    val projectPrefs = projectScope.getNode( JavaCore.PLUGIN_ID )
    options.asScala.foreach { case ( key, value ) => projectPrefs.put( key, value ) }
    projectPrefs.flush()
    // TODO managed formatter profile
    val projectPrefsUI = projectScope.getNode( JavaUI.ID_PLUGIN )
    val formatterProfile = PreferenceConstants.FORMATTER_PROFILE
    projectPrefsUI.flush()
  }

  /**
   * Transfer Maven formatter settings to Eclipse.
   */
  def formatEnsureScala(
    context : Config.BuildContext,
    logger :  AnyLog
  ) : Unit = {
    import org.scalaide.core.internal.formatter.FormatterPreferences._
    logger.info( s"Applying Scala formatter settings." )
    val project = context.facade.getProject
    val mavenProject = context.facade.getMavenProject
    val scalaProject = ScalaIDE.pluginProject( project )
    val preferenceStore = scalaProject.projectSpecificStorage
    val settingsFile = formatScalaSettingsFile( mavenProject, context.format ).getCanonicalPath
    logger.info( s"Scala formatter settings file: ${settingsFile}" )
    val formatterPreferences = PreferencesImporterExporter.loadPreferences( settingsFile )
    preferenceStore.importPreferences( formatterPreferences )
    preferenceStore.setValue( USE_PROJECT_SPECIFIC_SETTINGS_KEY, true )
    preferenceStore.save()
  }

}
