package com.carrotgarden.maven.scalor.eclipse

import com.carrotgarden.maven.scalor.base
import com.carrotgarden.maven.scalor.util

import scala.collection.mutable.HashSet
import scala.tools.nsc
import scala.tools.nsc.settings.NoScalaVersion

import org.apache.maven.artifact.Artifact

import org.eclipse.core.runtime.IPath
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.Path
import org.eclipse.core.runtime.Status
import org.eclipse.core.runtime.jobs.Job
import org.eclipse.jdt.core.IClasspathContainer
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jface.preference.IPersistentPreferenceStore
import org.eclipse.m2e.core.project.IMavenProjectFacade
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest

import org.scalaide.core.SdtConstants
import org.scalaide.core.internal.project.CompileScope
import org.scalaide.core.internal.project.CustomScalaInstallationLabel
import org.scalaide.core.internal.project.LabeledScalaInstallation
import org.scalaide.core.internal.project.ScalaInstallation
import org.scalaide.core.internal.project.ScalaInstallationChoice
import org.scalaide.core.internal.project.ScalaInstallationLabel
import org.scalaide.core.internal.project.ScalaModule
import org.scalaide.core.internal.project.ScalaProject
import org.scalaide.ui.internal.preferences.IDESettings
import org.scalaide.util.eclipse.EclipseUtils
import org.scalaide.util.internal.SettingConverterUtil.SCALA_DESIRED_INSTALLATION
import org.scalaide.util.internal.SettingConverterUtil.USE_PROJECT_SETTINGS_PREFERENCE
import org.scalaide.util.internal.SettingConverterUtil.convertNameToProperty
import org.eclipse.jdt.core.IClasspathAttribute
import com.carrotgarden.maven.scalor.util.Logging

/**
 * Provide Scala IDE settings for a project.
 *
 * ${project.basedir}/.settings/org.scala-ide.sdt.core.prefs
 */
trait ScalaIDE {

  self : Logging with Maven with Props with Monitor =>

  import ScalaIDE._
  import com.carrotgarden.maven.scalor.zinc.Module
  import com.carrotgarden.maven.scalor.zinc.ScalaInstall

  /**
   * Schedule Scala IDE Eclipse background job.
   */
  def scheduleScalaJob(
    project : ScalaProject,
    name :    String
  )( block : => Unit ) : Job = {
    import project._
    EclipseUtils.scheduleJob(
      name, underlying, Job.BUILD
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
      regexCompilerBridge,
      regexScalaCompiler,
      regexScalaLibrary,
      regexScalaReflect,
      resourcePluginDescriptor
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
    if ( eclipseLogInstallReport ) {
      import util.Folder._
      log.info( s"Producing custom Scala installation report." )
      val reportFile = ensureCanonicalFile( eclipseInstallReportFile )
      log.info( s"   ${reportFile}" )
      ensureParent( reportFile )
      val reportText = ScalaInstallation.customInstallations.map( report( _ ) ).mkString
      persistString( reportFile, reportText )
    }
  }

  /**
   * Build custom Scala compiler installation from Scalor plugin definitions.
   */
  def resolveCustomInstall(
    request : ProjectConfigurationRequest,
    config :  ParamsConfig,
    monitor : IProgressMonitor
  ) : ScalaInstall = {
    import Maven._
    import com.carrotgarden.maven.scalor.base.Params._
    import config._
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
      log.info( "Custom Scala installation report:\n" + report( scalaInstallation ) )
    }
    install
  }

  /**
   * Ensure plugin custom Scala installation is persisted to disk by Scala IDE.
   */
  def persistCustomInstall(
    request : ProjectConfigurationRequest,
    config :  ParamsConfig,
    project : ScalaProject,
    install : ScalaInstall,
    monitor : IProgressMonitor
  ) : Unit = {
    import config._
    import org.scalaide.core.internal.project.ScalaInstallation._
    log.info( "Persisting custom Scala installation." )
    val scalaInstallation = installFrom( request.getMavenProjectFacade, install )
    val current = customInstallations.find( _.label == scalaInstallation.label )
    if ( current.isDefined ) {
      if ( eclipseLogPersistInstall ) {
        log.info( "   custom installation is already present: " + report( current.get.label ) )
      }
    } else {
      if ( eclipseLogPersistInstall ) {
        log.info( "   registering custom Scala installation: " + report( scalaInstallation.label ) )
      }
      customInstallations += scalaInstallation
      installationsTracker.saveInstallationsState( availableInstallations )
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
  ) : nsc.Settings = {
    log.info( s"Providing configured settings." )
    import com.carrotgarden.maven.scalor.zinc.Compiler._
    import config._
    val zincArgs =
      parseCompileOptions.toList
    val pluginArgs = install.pluginDefineList
      .flatMap( module => pluginStanza( module.binaryArtifact.getFile ).toList )
    val argsList = zincArgs ++ pluginArgs
    val settings = Settings( log.fail )
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
   * Use this feature to remove manual user provided Eclipse UI configuration settings.
   */
  def resetSettingsStorage(
    request :  ProjectConfigurationRequest,
    config :   ParamsConfig,
    store :    IPersistentPreferenceStore,
    settings : nsc.Settings,
    monitor :  IProgressMonitor
  ) = {
    import config._
    if ( eclipseResetPreferences ) {
      log.info( s"Resetting preferences to default." )
      for {
        basic <- settings.visibleSettings.toList.sortBy( _.name )
        entry = basic.asInstanceOf[ nsc.Settings#Setting ]
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
    settings : nsc.Settings,
    monitor :  IProgressMonitor
  ) = {
    import config._
    log.info( s"Persisting configured settings." )
    val skipList = List(
      "d" // destination folder, injected by default
    )
    for {
      basic <- settings.userSetSettings.toList.sortBy( _.name )
      entry = basic.asInstanceOf[ nsc.Settings#Setting ]
      key = convertNameToProperty( entry.name )
      if ( !skipList.contains( key ) )
    } yield {
      val value = entry match {
        case multi : nsc.Settings#MultiStringSetting => multi.value.mkString( "," )
        case plain                                   => plain.value.toString
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
    //    import config._
    //    if ( eclipseScalaSettingsCommentApply ) {
    //      log.info( "Applying Scala IDE .settings/ comment." )
    //      persistComment( store, eclipseScalaSettingsCommentString )
    //    }
  }

  /**
   * Rename Scala Library container in Eclipse UI.
   */
  def renameScalaLibraryContainer(
    request : ProjectConfigurationRequest,
    config :  ParamsConfig,
    project : ScalaProject,
    install : ScalaInstall,
    monitor : IProgressMonitor
  ) : Unit = {
    import config._
    if ( !eclipseRemoveLibraryContainer && eclipseRenameLibraryContainer ) {
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
    val subMon = monitor.toSub
    val settings = provideConfiguredSettings( request, config, project, install, subMon.split( 20 ) )
    val store = project.projectSpecificStorage
    resetSettingsStorage( request, config, store, settings, subMon.split( 20 ) )
    store.setValue( USE_PROJECT_SETTINGS_PREFERENCE, true )
    persistCompilerSelection( request, config, project, install, subMon.split( 20 ) )
    persistConfiguredSettings( request, config, store, settings, subMon.split( 20 ) )
    store.save()
    // TODO
    // persistSettingsComment( request, config, store )
    project.initializeCompilerSettings( settings, _ => true )
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
    val subMon = monitor.toSub
    reportCustomInstall( request, config, subMon.split( 20 ) )
    val project = ScalaProject( request.getProject )
    val install = resolveCustomInstall( request, config, subMon.split( 20 ) )
    val name = "Scalor: update project settings for Scala IDE" // Keep name, used in test.
    val scalaJob = scheduleScalaJob( project, name ) {
      log.context( "step#3+" )
      log.info( s"Configuring Scala IDE (scheduled job)." )
      persistCustomInstall( request, config, project, install, subMon.split( 20 ) )
      updateProjectScalaIDE( request, config, project, install, subMon.split( 20 ) )
      renameScalaLibraryContainer( request, config, project, install, subMon.split( 20 ) )
    }
    // FIXME deadlock
    //    val hasDone = scalaJob.join( 10 * 1000, subMon.split( 10 ) )
    //    if ( !hasDone ) {
    //      log.fail( "Configuring Scala IDE (scheduled job) failure." )
    //    }
  }

}

object ScalaIDE {

  import Maven._
  import com.carrotgarden.maven.scalor.zinc._

  /**
   * Eclipse resource path for Maven artifact jar file.
   */
  def pathFrom( artifact : Artifact ) = {
    import util.Folder._
    new Path( ensureCanonicalPath( artifact.getFile ) )
  }

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
    import com.carrotgarden.maven.scalor.util.Error._
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
    import util.Folder._
    import module._
    val text = new StringBuffer
    def spacer = text.append( "      " )
    def append( name : String, path : Option[ IPath ] ) = {
      spacer
      text.append( name )
      text.append( ": " )
      path match {
        case Some( path ) =>
          text.append( ensureCanonicalPath( path.toFile ) )
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
  case class Settings( errorFun : ErrorFun ) extends nsc.Settings( errorFun ) {
    val NamePrefix = "-" // Use as command line option.
    val EmptyString = "" // Common value.

    /**
     * Path-dependent type cast.
     */
    def configSet = allSettings.asInstanceOf[ HashSet[ nsc.Settings#Setting ] ]

    /**
     * Available Scala IDE compilation scopes.
     */
    def choices = CompileScope.scopesInCompileOrder.map { _.name }.toList

    /**
     * Extract custom class path entry attribute describing a scope.
     */
    def scopeAttrib( entry : IClasspathEntry ) : Option[ IClasspathAttribute ] = {
      import com.carrotgarden.maven.scalor.base.Build._
      entry.getExtraAttributes.find( Param.attrib.scope == _.getName )
    }

    /**
     * Convert compilation scope name from this plugin to Scala IDE format.
     */
    def scopeIDE( scope : String ) : Option[ String ] = {
      import com.carrotgarden.maven.scalor.base.Build._
      import org.scalaide.core.internal.project._
      scope match {
        case Param.scope.`macro` => Some( CompileMacrosScope.name )
        case Param.scope.`main`  => Some( CompileMainScope.name )
        case Param.scope.`test`  => Some( CompileTestsScope.name )
        case invalid             => errorFun( "invalid scope: " + invalid ); None
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
     * See [[Entry#ensureSourceRoots]]
     */
    def makeCompilationScopeSettings( scalaProject : ScalaProject ) : Unit = {
      import org.scalaide.util.eclipse.EclipseUtils._
      val javaProject = scalaProject.javaProject
      val projectRoot = scalaProject.underlying.getLocation
      val resolvedClasspath = javaProject.getResolvedClasspath( true ).toList
      val settingsList = for {
        entry <- resolvedClasspath
        attrib <- scopeAttrib( entry )
        scope <- scopeIDE( attrib.getValue )
        resource <- Option( workspaceRoot.findMember( entry.getPath ) )
      } yield {
        val folderAbsolute = resource.getLocation
        val folderRelative = folderAbsolute.makeRelativeTo( projectRoot )
        val name = makeName( folderRelative )
        val setting = ChoiceSetting(
          name    = name, choices = choices, default = EmptyString,
          helpArg = EmptyString, descr = EmptyString
        )
        setting.value = scope
        allSettings += setting
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
        .foreach { setting => configSet += setting }
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
      allSettings += installTitle
    }

  }

}
