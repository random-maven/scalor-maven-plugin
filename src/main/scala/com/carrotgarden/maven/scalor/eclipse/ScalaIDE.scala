package com.carrotgarden.maven.scalor.eclipse

import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest
import org.eclipse.core.runtime.IProgressMonitor
import com.carrotgarden.maven.scalor.A
import org.apache.maven.model.Dependency
import org.scalaide.core.internal.project.ScalaInstallation
import org.scalaide.core.internal.project.ScalaModule
import org.scalaide.core.IScalaPlugin
import org.scalaide.core.internal.project.ScalaProject
import org.scalaide.core.internal.project.ScalaInstallationChoice
import org.scalaide.util.eclipse.EclipseUtils
import org.scalaide.util.internal.SettingConverterUtil
import org.scalaide.core.ScalaInstallationChange
import org.eclipse.core.runtime.Status
import org.eclipse.core.runtime.jobs.Job
import org.scalaide.core.internal.project.ScalaModule

import scala.collection.JavaConverters._

import com.carrotgarden.maven.scalor.base
import com.carrotgarden.maven.scalor.util
import com.carrotgarden.maven.scalor.zinc

import org.apache.maven.artifact.Artifact
import org.eclipse.m2e.core.project.IMavenProjectFacade
import scala.tools.nsc.settings.NoScalaVersion
import org.scalaide.core.internal.project.LabeledScalaInstallation
import org.scalaide.core.internal.project.CustomScalaInstallationLabel

import org.eclipse.core.runtime.IPath
import org.scalaide.core.internal.project.ScalaInstallationLabel
import org.eclipse.core.runtime.Path
import java.io.File
import org.scalaide.ui.internal.preferences.ScalaPluginSettings
import org.scalaide.core.internal.compiler.ScalaPresentationCompiler
import scala.collection.mutable.ArrayBuffer
import org.scalaide.ui.internal.preferences.IDESettings
import org.eclipse.jface.preference.IPersistentPreferenceStore

import scala.tools.nsc.{ Settings => CompilerSettings }

import SettingConverterUtil._
import org.scalaide.core.internal.project.CompileScope
import org.eclipse.core.resources.IProject
import com.carrotgarden.maven.scalor.base.Build.BuildParam
import org.eclipse.jdt.core.IClasspathEntry
import scala.tools.nsc.settings.MutableSettings
import scala.collection.mutable.HashSet

import com.carrotgarden.maven.scalor.eclipse.Logging.AnyLog
import org.eclipse.jdt.core.JavaCore
import org.scalaide.core.SdtConstants
import org.eclipse.jdt.core.IClasspathContainer

/**
 * Provide Scala IDE settings for a project.
 *
 * ${project.basedir}/.settings/org.scala-ide.sdt.core.prefs
 */
trait ScalaIDE {

  self : Logging with Maven with Props =>

  import ScalaIDE._
  import zinc.ScalaInstall
  import zinc.Module

  /**
   * Schedule Scala IDE Eclipse background job.
   */
  def scheduleScalaJob(
    project : ScalaProject,
    message : String
  )( block : => Unit ) : Unit = {
    import project._
    EclipseUtils.scheduleJob(
      message, underlying, Job.BUILD
    ) { monitor =>
      block
      Status.OK_STATUS
    }
  }

  /**
   * Build artifact module type detector from maven plugin parameters.
   */
  def moduleDetector(
    request : ProjectConfigurationRequest,
    config :  ParamsConfig,
    monitor : IProgressMonitor
  ) : Module.Detector = {
    import config._
    Module.Detector(
      artifactCompilerBridge,
      artifactScalaCompiler,
      artifactScalaLibrary,
      artifactScalaReflect,
      artifactPluginDescriptor
    )
  }

  /**
   * Report all available custom Scala installations persisted by Scala IDE.
   */
  def reportCustomInstall(
    request : ProjectConfigurationRequest,
    config :  ParamsConfig,
    monitor : IProgressMonitor
  ) : Unit = {
    import config._
    if ( eclipseLogCustomInstall ) {
      import util.Folder._
      log.info( s"Producing custom Scala installation report." )
      val reportFile = eclipseCustomInstallReport
      log.info( s"   ${reportFile.getCanonicalPath}" )
      ensureParent( reportFile )
      val reportText = ScalaInstallation.customInstallations.map( report( _ ) ).mkString
      persistString( reportFile, reportText )
    }
  }

  /**
   * Build custom scala compiler installation from scalor plugin dependencies.
   */
  def resolveCustomInstall(
    request : ProjectConfigurationRequest,
    config :  ParamsConfig,
    monitor : IProgressMonitor
  ) : ScalaInstall = {
    import config._
    import Maven._
    import base.Params._
    log.info( s"Resolving custom Scala installation." )
    val detector = moduleDetector( request, config, monitor )
    val facade = request.getMavenProjectFacade
    val defineRequest = base.Params.DefineRequest(
      convert( defineBridge ), convert( defineCompiler ), convert( definePluginList )
    )
    val defineResponse = resolveDefine( request, defineRequest, "compile", monitor )
    val install = ScalaInstall( zincScalaInstallTitle, detector, defineResponse ).withTitleDigest
    if ( eclipseLogInstallResolve ) {
      val scalaInstallation = installFrom( facade, install )
      log.info( "Resolved plugin custom Scala installation:\n" + report( scalaInstallation ) )
    }
    install
  }

  /**
   * Ensure plugin custom Scala installation is persisted to disk by Scala IDE.
   */
  def persistCustomInstall(
    request : ProjectConfigurationRequest,
    project : ScalaProject,
    install : ScalaInstall,
    monitor : IProgressMonitor
  ) : Unit = {
    import ScalaInstallation._
    log.info( "Persisting custom Scala installation." )
    val scalaInstallation = installFrom( request.getMavenProjectFacade, install )
    val current = customInstallations.find( _.label == scalaInstallation.label )
    if ( current.isDefined ) {
      log.info( "   custom installation is already present: " + report( current.get.label ) )
    } else {
      log.info( "   registering custom Scala installation: " + report( scalaInstallation.label ) )
      customInstallations += scalaInstallation
      installationsTracker.saveInstallationsState( availableInstallations )
      // project.publish( ScalaInstallationChange() )
    }
  }

  /**
   * Convert compiler settings from Maven plugin format to Scala compiler format.
   */
  def provideConfiguredSettings(
    request : ProjectConfigurationRequest,
    config :  ParamsConfig,
    project : ScalaProject,
    install : ScalaInstall,
    monitor : IProgressMonitor
  ) : CompilerSettings = {
    log.info( s"Providing configured settings." )
    import config._
    import zinc.Compiler._
    val ideArgs =
      parseCommonSequence( eclipseOptsScalaIDE, commonSequenceSeparator ).toList
    val zincArgs =
      parseCommonSequence( zincOptionsScalaC, commonSequenceSeparator ).toList
    val pluginArgs = install.pluginDefineList
      .flatMap( module => pluginStanza( module.binaryArtifact.getFile ).toList )
    val argsList = ideArgs ++ zincArgs ++ pluginArgs
    val settings = CustomSettings( log.fail )
    settings.makeBuildMangerSettings()
    settings.makeCompilationScopeSettings( project )
    settings.makeScalorPluginSettings( install.title )
    settings.processArguments( argsList, true )
    settings
  }

  /**
   * Change project compiler selection to the custom Scala installation.
   */
  def persistCompilerSelection(
    request : ProjectConfigurationRequest,
    config :  ParamsConfig,
    project : ScalaProject,
    install : ScalaInstall,
    monitor : IProgressMonitor
  ) = {
    import config._
    log.info( s"Persisting compiler selection." )
    val scalaInstallation = installFrom( request.getMavenProjectFacade, install )
    val installationChoice = ScalaInstallationChoice( scalaInstallation )
    val store = project.projectSpecificStorage
    store.setValue( SCALA_DESIRED_INSTALLATION, installationChoice.toString )
    project.setDesiredInstallation( installationChoice, "Persisting compiler selection" )
    if ( eclipseLogPersistCompiler ) {
      log.info( s"   ${report( scalaInstallation.label )}" )
    }
  }

  /**
   * Reset all preferences to their default values before applying Maven configuration.
   * Use this to remove manual user provided Eclipse UI configuration settings.
   */
  def resetSettingsStorage(
    request :  ProjectConfigurationRequest,
    config :   ParamsConfig,
    store :    IPersistentPreferenceStore,
    settings : CompilerSettings,
    monitor :  IProgressMonitor
  ) = {
    import config._
    if ( eclipseResetPreferences ) {
      log.info( s"Resetting preferce store to default." )
      for {
        basic <- settings.visibleSettings.toList.sortBy( _.name )
        entry = basic.asInstanceOf[ CompilerSettings#Setting ]
        key = convertNameToProperty( entry.name )
      } yield {
        store.setToDefault( key )
      }
    }
  }

  /**
   * Update project persisted settings from configured Scala compiler settings.
   */
  def persistConfiguredSettings(
    request :  ProjectConfigurationRequest,
    config :   ParamsConfig,
    store :    IPersistentPreferenceStore,
    settings : CompilerSettings,
    monitor :  IProgressMonitor
  ) = {
    import config._
    log.info( s"Persisting configured settings." )
    val skipList = List(
      "d" // destination folder, injected by default
    )
    for {
      basic <- settings.userSetSettings.toList.sortBy( _.name )
      entry = basic.asInstanceOf[ CompilerSettings#Setting ]
      key = convertNameToProperty( entry.name )
      if ( !skipList.contains( key ) )
    } yield {
      val value = entry match {
        case multi : CompilerSettings#MultiStringSetting => multi.value.mkString( "," )
        case plain                                       => plain.value.toString
      }
      if ( eclipseLogPersistSettings ) {
        log.info( s"   ${key}=${value}" )
      }
      store.setValue( key, value )
    }
  }

  /**
   * Apply comment in Scala IDE settings file.
   * ${project.basedir}/.settings/org.scala-ide.sdt.core.prefs
   */
  def persistSettingsComment(
    request : ProjectConfigurationRequest,
    config :  ParamsConfig,
    store :   IPersistentPreferenceStore
  ) : Unit = {
    import config._
    if ( eclipseScalaSettingsCommentApply ) {
      log.info( "Applying Scala IDE .settings/ comment." )
      persistComment( store, eclipseScalaSettingsCommentString )
    }
  }

  /**
   * Rename Scala Library container in Eclipse UI.
   */
  def renameScalaLibrary(
    request : ProjectConfigurationRequest,
    config :  ParamsConfig,
    project : ScalaProject,
    install : ScalaInstall,
    monitor : IProgressMonitor
  ) : Unit = {
    import config._
    if ( eclipseRenameLibraryContainer ) {
      log.info( "Renaming Scala Library container." )
      val description = s"Scala Library @ ${install.title}"
      val javaProject = project.javaProject
      val containerPath = new Path( SdtConstants.ScalaLibContId )
      val originalContainer = JavaCore.getClasspathContainer( containerPath, javaProject )
      val customizedContainer = new IClasspathContainer {
        override val getKind = originalContainer.getKind
        override val getPath = originalContainer.getPath
        override val getClasspathEntries = originalContainer.getClasspathEntries
        override val getDescription = description
      }
      JavaCore.setClasspathContainer(
        containerPath, Array( javaProject ), Array( customizedContainer ), monitor
      )
    }
  }

  /**
   * Update Scala IDE Scala compiler settings for the current project.
   */
  def updateProjectScalaIDE(
    request : ProjectConfigurationRequest,
    config :  ParamsConfig,
    project : ScalaProject,
    install : ScalaInstall,
    monitor : IProgressMonitor
  ) : Unit = {
    log.info( s"Updating project Scala settings." )
    val settings = provideConfiguredSettings( request, config, project, install, monitor )
    val store = project.projectSpecificStorage
    resetSettingsStorage( request, config, store, settings, monitor )
    store.setValue( USE_PROJECT_SETTINGS_PREFERENCE, true )
    persistCompilerSelection( request, config, project, install, monitor )
    persistConfiguredSettings( request, config, store, settings, monitor )
    store.save()
    // persistSettingsComment( request, config, store ) // XXX
    project.initializeCompilerSettings( settings, _ => true )
    // project.publish( ScalaInstallationChange() )
  }

  /**
   * Apply project settings for Scala IDE.
   *
   * Global persistence location:
   * ${eclipse.worksapce}/.metadata/.plugins/org.scala-ide.sdt.core/ScalaInstallations.back
   *
   * Project persistence location:
   * ${project.basedir}/.settings/org.scala-ide.sdt.core.prefs
   */
  def configureScalaIDE(
    request : ProjectConfigurationRequest,
    config :  ParamsConfig,
    monitor : IProgressMonitor
  ) : Unit = {
    log.info( s"Configuring Scala IDE." )
    reportCustomInstall( request, config, monitor )
    val project = ScalaProject( request.getProject )
    val install = resolveCustomInstall( request, config, monitor )
    scheduleScalaJob( project, "Scalor: update project settings for Scala IDE" ) {
      persistCustomInstall( request, project, install, monitor )
      updateProjectScalaIDE( request, config, project, install, monitor )
      renameScalaLibrary( request, config, project, install, monitor )
    }

  }

}

object ScalaIDE {

  import zinc._
  import Maven._

  /**
   * Eclipse absolute resource path for Maven artifact jar file.
   */
  def pathFrom( artifact : Artifact ) = new Path( artifact.getFile.getCanonicalPath )

  /**
   * Convert module from this plugin format to Scala IDE format.
   */
  def moduleFrom( facade : IMavenProjectFacade, module : Module ) = {
    import module._
    val classJar = pathFrom( binaryArtifact )
    val sourceJar = sourceArtifact.map( pathFrom( _ ) )
    ScalaModule( classJar, sourceJar )
  }

  /**
   * Convert installation from this plugin format to Scala IDE format.
   */
  def installFrom( facade : IMavenProjectFacade, install : ScalaInstall ) = {
    import util.Error._
    new LabeledScalaInstallation {
      override val label = CustomScalaInstallationLabel( install.title )
      override val compiler = moduleFrom( facade, install.compiler )
      override val library = moduleFrom( facade, install.library )
      override val extraJars = install.extraJars.map( module => moduleFrom( facade, module ) )
      override val version = TryHard {
        ScalaInstallation.extractVersion( library.classJar ).getOrElse( NoScalaVersion )
      }.getOrElse( NoScalaVersion )
    }

  }

  def report( module : ScalaModule ) = {
    import module._
    val text = new StringBuffer
    def spacer = text.append( "      " )
    def append( name : String, path : Option[ IPath ] ) = {
      spacer
      text.append( name )
      text.append( ": " )
      path match {
        case Some( path ) =>
          text.append( path.toFile.getCanonicalPath )
        case None =>
          text.append( "<missing>" )
      }
      text.append( "\n" )
    }
    append( "binary", Some( classJar ) )
    append( "source", sourceJar )
    text.toString
  }

  def report( install : LabeledScalaInstallation ) : String = {
    import install._
    val text = new StringBuffer
    def spacer = text.append( "   " )
    def append( name : String, module : ScalaModule ) = {
      spacer
      text.append( name )
      text.append( ": " )
      text.append( "\n" )
      text.append( report( module ) )
    }
    //
    text.append( "\n" )
    spacer
    text.append( "identity: " )
    text.append( "\n" )
    //
    spacer
    spacer
    text.append( "hash:  " )
    text.append( install.hashCode )
    text.append( "\n" )
    //
    spacer
    spacer
    text.append( "label: " )
    text.append( report( install.label ) )
    text.append( "\n" )
    //
    append( "compiler", compiler )
    append( "library ", library )
    extraJars.foreach { entry =>
      append( "extra   ", entry )
    }
    text.toString
  }

  def report( label : ScalaInstallationLabel ) = label match {
    case CustomScalaInstallationLabel( title ) => title
    case _                                     => label.toString
  }

  type ErrorFun = String => Unit

  /**
   * Support standard Scala compiler options.
   * Support build manager options for Scala IDE.
   * Support compilation scope options for Scala IDE.
   */
  case class CustomSettings( errorFun : ErrorFun ) extends CompilerSettings( errorFun ) {
    val NamePrefix = "-" // Use as command line option.
    val EmptyString = ""

    /**
     * Path-dependent type cast.
     */
    def configSet = allSettings.asInstanceOf[ HashSet[ CompilerSettings#Setting ] ]

    /**
     * Available Scala IDE compilation scopes.
     */
    def choices = CompileScope.scopesInCompileOrder.map { _.name }.toList

    /**
     * Extract custom class path entry attribute describing a scope.
     */
    def scopeAttrib( entry : IClasspathEntry ) = {
      import BuildParam._
      entry.getExtraAttributes.find( attrib.scope == _.getName )
    }

    /**
     * Convert compilation scope name from this plugin to Scala IDE format.
     */
    def scopeIDE( scope : String ) : Option[ String ] = {
      import base.Build._
      import org.scalaide.core.internal.project._
      scope match {
        case paramMacro.scope => Some( CompileMacrosScope.name )
        case paramMain.scope  => Some( CompileMainScope.name )
        case paramTest.scope  => Some( CompileTestsScope.name )
        case invalid          => errorFun( "invalid scope: " + invalid ); None
      }
    }

    /**
     * Use Eclipse resources compatible format.
     *
     * Persisted option example entry:
     * .settings/org.scala-ide.sdt.core.prefs!//src/macro/scala=macros
     */
    def makeName( path : IPath ) : String = NamePrefix + path.segments.mkString( "/" )

    /**
     * Configure compilation scope for Scala IDE.
     *
     * Generate custom Scala compiler settings describing source folder mapping by scope.
     * Underlying Java project class path entries must be set before application.
     * See [[org.scalaide.ui.internal.preferences.ScopesSettings]]
     * See [[Entry#configureBuildParam]]
     */
    def makeCompilationScopeSettings( scalaProject : ScalaProject ) : Unit = {
      import EclipseUtils._
      val javaProject = scalaProject.javaProject
      val projectRoot = scalaProject.underlying.getLocation
      val resolvedClasspath = javaProject.getResolvedClasspath( true ).toList
      val settingsList = for {
        entry <- resolvedClasspath
        attrib <- scopeAttrib( entry )
        scope <- scopeIDE( attrib.getValue )
        resource <- Option( workspaceRoot.findMember( entry.getPath ) )
      } yield {
        val srcFolder = resource.getLocation
        val srcFolderRelativeToProject = srcFolder.makeRelativeTo( projectRoot )
        val name = makeName( srcFolderRelativeToProject )
        val setting = ChoiceSetting(
          name    = name,
          helpArg = EmptyString, descr = EmptyString,
          choices = choices, default = EmptyString
        )
        setting.value = scope
        allSettings.+=( setting )
        setting
      }
    }

    /**
     * Configure build manager for Scala IDE.
     *
     * Reuse settings from [[org.scalaide.ui.internal.preferences.ScalaPluginSettings]]
     */
    def makeBuildMangerSettings() : Unit = {
      IDESettings.buildManagerSettings
        .flatMap( _.userSettings )
        .foreach { setting => configSet += ( setting ) }
    }

    def settingInstallTitle = StringSetting(
      name    = NamePrefix + "scalor.install.title",
      arg     = EmptyString, descr = EmptyString, default = EmptyString
    )

    /**
     * Define custom settings contributed by this plugin.
     */
    def makeScalorPluginSettings( title : String ) : Unit = {
      val installTitle = settingInstallTitle
      installTitle.value = title
      allSettings.+=( installTitle )
    }

  }

}
