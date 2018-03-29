package com.carrotgarden.maven.scalor.eclipse

import scala.collection.mutable.HashSet
import scala.tools.nsc
import scala.tools.nsc.settings.NoScalaVersion

import org.apache.maven.artifact.Artifact
import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.IPath
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.Path
import org.eclipse.core.runtime.Status
import org.eclipse.core.runtime.jobs.Job
import org.eclipse.jdt.core.IClasspathAttribute
import org.eclipse.jdt.core.IClasspathContainer
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jface.preference.IPersistentPreferenceStore
import org.eclipse.m2e.core.project.IMavenProjectFacade
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest
import org.scalaide.core.SdtConstants
import org.scalaide.core.internal.ScalaPlugin
import org.scalaide.core.internal.project.CompileScope
import org.scalaide.core.internal.project.CustomScalaInstallationLabel
import org.scalaide.core.internal.project.LabeledScalaInstallation
import org.scalaide.core.internal.project.ScalaInstallation
import org.scalaide.core.internal.project.ScalaInstallationChoice
import org.scalaide.core.internal.project.ScalaInstallationLabel
import org.scalaide.core.internal.project.ScalaModule
import org.scalaide.core.internal.project.ScalaProject
import org.scalaide.ui.internal.preferences.CompilerSettings
import org.scalaide.ui.internal.preferences.IDESettings
import org.scalaide.ui.internal.preferences.ScopesSettings
import org.scalaide.util.eclipse.EclipseUtils
import org.scalaide.util.internal.SettingConverterUtil.SCALA_DESIRED_INSTALLATION
import org.scalaide.util.internal.SettingConverterUtil.USE_PROJECT_SETTINGS_PREFERENCE
import org.scalaide.util.internal.SettingConverterUtil.convertNameToProperty

import com.carrotgarden.maven.scalor.util.Optioner.convert_Option_Value
import com.carrotgarden.maven.scalor.zinc

/**
 * Provide Scala IDE settings for a project.
 *
 * ${project.basedir}/.settings/org.scala-ide.sdt.core.prefs
 */
trait ScalaIDE {

  self : Maven with Props with Monitor =>

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
    config :  ParamsConfig,
    monitor : IProgressMonitor
  ) : Module.Detector = {
    import config._
    Module.Detector(
      regexCompilerBridge,
      regexScalaCompiler,
      regexScalaLibrary,
      //      regexScalaReflect,
      resourcePluginDescriptor
    )
  }

  /**
   * Report all available custom Scala installations persisted by Scala IDE.
   */
  def reportCustomInstall(
    context : Config.SetupContext,
    monitor : IProgressMonitor
  ) : Unit = {
    import context._
    import config._
    if ( eclipseLogInstallReport ) {
      import com.carrotgarden.maven.scalor.util.Folder._
      logger.info( s"Producing custom Scala installation report." )
      val reportFile = eclipseInstallReportFile.getCanonicalFile
      logger.info( s"   ${reportFile}" )
      ensureParent( reportFile )
      val reportText = ScalaInstallation.customInstallations.map( report( _ ) ).mkString
      persistString( reportFile, reportText )
    }
  }

  /**
   * Build custom Scala compiler installation from Scalor plugin definitions.
   */
  def resolveCustomInstall(
    context : Config.SetupContext,
    monitor : IProgressMonitor
  ) : ScalaInstall = {
    import com.carrotgarden.maven.scalor.base.Params
    import context._
    import config._
    logger.info( s"Resolving custom Scala installation." )
    val detector = moduleDetector( config, monitor )
    val facade = request.getMavenProjectFacade
    val project = facade.getMavenProject
    val defineRequest = Params.DefineRequest(
      defineAutoBridge( project ),
      defineAutoCompiler( project ),
      defineAutoPluginList( project )
    )
    val defineResponse = Maven.resolveDefine( request, defineRequest, "compile", monitor )
    val install = ScalaInstall( zincScalaInstallTitle, detector, defineResponse ).withTitleDigest
    //    if ( eclipseLogInstallResolve ) {
    //      val scalaInstallation = installFrom( facade, install )
    //      logger.info( s"Custom Scala installation report:\n${report( scalaInstallation )}" )
    //    }
    install
  }

  /**
   * Ensure plugin custom Scala installation is persisted to disk by Scala IDE.
   */
  def persistCustomInstall(
    context : Config.SetupContext,
    install : ScalaInstall,
    monitor : IProgressMonitor
  ) : Unit = {
    import context._
    import config._
    import org.scalaide.core.internal.project.ScalaInstallation._
    logger.info( "Persisting custom Scala installation." )
    val scalaInstallation = installFrom( request.getMavenProjectFacade, install )
    val scalaPresentOption = customInstallations.find( _.label == scalaInstallation.label )
    if ( scalaPresentOption.isDefined ) {
      if ( eclipseLogPersistInstall ) {
        logger.info( s"Custom installation is already present: ${report( scalaPresentOption )}" )
      }
    } else {
      if ( eclipseLogPersistInstall ) {
        logger.info( s"Registering new custom Scala installation: ${report( scalaInstallation )}" )
      }
      customInstallations += scalaInstallation
      installationsTracker.saveInstallationsState( availableInstallations )
    }
  }

  /**
   * Convert compiler settings from Maven plugin format to Scala compiler format.
   */
  def provideConfiguredSettings(
    context : Config.SetupContext,
    install : ScalaInstall,
    monitor : IProgressMonitor
  ) : nsc.Settings = {
    import context._
    logger.info( s"Providing configured settings." )
    import com.carrotgarden.maven.scalor.zinc.Compiler
    import config._
    val zincArgs =
      parseOptionsScala.toList
    val pluginArgs = install.pluginDefineList
      .flatMap( module => Compiler.pluginStanza( module ).toList )
    val argsList = zincArgs ++ pluginArgs
    val project = pluginProject( request.getProject )
    val settings = new Settings( logger.fail )
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
    context : Config.SetupContext,
    install : ScalaInstall,
    monitor : IProgressMonitor
  ) = {
    import context._
    import config._
    logger.info( s"Persisting compiler selection." )
    val project = pluginProject( request.getProject )
    val scalaInstallation = installFrom( facade, install )
    val installationChoice = ScalaInstallationChoice( scalaInstallation )
    val store = project.projectSpecificStorage
    store.setValue( SCALA_DESIRED_INSTALLATION, installationChoice.toString )
    project.setDesiredInstallation( installationChoice, "Persisting compiler selection" )
    if ( eclipseLogPersistCompiler ) {
      logger.info( s"   ${report( scalaInstallation.label )}" )
    }
  }

  /**
   * Reset all preferences to their default values before applying Maven configuration.
   * Use this feature to remove manual user provided Eclipse UI configuration settings.
   */
  def resetSettingsStorage(
    context :  Config.SetupContext,
    store :    IPersistentPreferenceStore,
    settings : nsc.Settings,
    monitor :  IProgressMonitor
  ) = {
    import context._
    import config._
    if ( eclipseResetPreferences ) {
      logger.info( s"Resetting preferences to default." )
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
    context :  Config.SetupContext,
    store :    IPersistentPreferenceStore,
    settings : nsc.Settings,
    monitor :  IProgressMonitor
  ) = {
    import context._
    import config._
    logger.info( s"Persisting configured settings." )
    val skipList = List(
      "d" // destination folder, present by default
    )
    val userSettings = zinc.Settings.userSettings( settings )
    val extraSettings = new Settings( logger.fail )
    for {
      basic <- userSettings.toList.sortBy( _.name )
      entry = basic.asInstanceOf[ nsc.Settings#Setting ]
      entryKey = convertNameToProperty( entry.name )
      if ( !skipList.contains( entryKey ) )
    } yield {
      if ( Settings.hasCommandSetting( entry ) ) {
        // settings in a box: "additional command line parameters"
        extraSettings.configSet += entry // collect
      } else {
        // settings supported by full preference pages in eclipse ui
        val entryValue = entry match {
          case multi : nsc.Settings#MultiStringSetting => multi.value.mkString( "," )
          case plain                                   => plain.value.toString
        }
        store.setValue( entryKey, entryValue ) // persist
        if ( eclipseLogPersistSettings ) {
          val storeValue = store.getString( entryKey )
          logger.info( s"   ${entryKey}=${storeValue}" )
        }
      }
    }
    // persist "additional command line parameters"
    val extraKey = CompilerSettings.ADDITIONAL_PARAMS
    val extraValue = zinc.Settings.unparseString( extraSettings )
    store.setValue( extraKey, extraValue )
    if ( eclipseLogPersistSettings ) {
      val storeValue = store.getString( extraKey )
      logger.info( s"   ${extraKey}=${storeValue}" )
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
   * Remove Scala Library container from project class path.
   */
  def removeScalaLibraryContainer(
    context : Config.SetupContext,
    install : ScalaInstall,
    monitor : IProgressMonitor
  ) = {
    import context._
    import config._
    if ( eclipseRemoveLibraryContainer ) {
      logger.info( "Removing Scala Library container." )
      val project = pluginProject( request.getProject )
      val javaProject = project.javaProject
      val containerPath = new Path( SdtConstants.ScalaLibContId )
      val source = javaProject.getRawClasspath.toList
      val target = source.filterNot( _.getPath == containerPath )
      javaProject.setRawClasspath( target.toArray, monitor )
    }
  }

  /**
   * Rename Scala Library container in Eclipse UI.
   */
  def renameScalaLibraryContainer(
    context : Config.SetupContext,
    install : ScalaInstall,
    monitor : IProgressMonitor
  ) : Unit = {
    import context._
    import config._
    if ( !eclipseRemoveLibraryContainer && eclipseRenameLibraryContainer ) {
      logger.info( "Renaming Scala Library container." )
      val project = pluginProject( request.getProject )
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
    context : Config.SetupContext,
    install : ScalaInstall,
    monitor : IProgressMonitor
  ) : Unit = {
    import context._
    logger.info( s"Updating project Scala settings." )
    val subMon = monitor.toSub
    val settings = provideConfiguredSettings( context, install, subMon.split( 20 ) )
    val project = pluginProject( request.getProject )
    val store = project.projectSpecificStorage
    resetSettingsStorage( context, store, settings, subMon.split( 20 ) )
    store.setValue( USE_PROJECT_SETTINGS_PREFERENCE, true )
    persistCompilerSelection( context, install, subMon.split( 20 ) )
    persistConfiguredSettings( context, store, settings, subMon.split( 20 ) )
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
    context : Config.SetupContext,
    monitor : IProgressMonitor
  ) : Unit = {
    import context._
    logger.info( s"Configuring Scala IDE." )
    val subMon = monitor.toSub
    reportCustomInstall( context, subMon.split( 20 ) )
    val install = resolveCustomInstall( context, subMon.split( 20 ) )
    val project = pluginProject( request.getProject )
    val name = "Scalor: update project settings for Scala IDE" // Keep name, used in test.
    val scalaJob = scheduleScalaJob( project, name ) {
      logger.info( s"Configuring Scala IDE (scheduled job)." )
      persistCustomInstall( context, install, subMon.split( 20 ) )
      updateProjectScalaIDE( context, install, subMon.split( 20 ) )
      //      removeScalaLibraryContainer( request, config, project, install, subMon.split( 20 ) )
      //      renameScalaLibraryContainer( request, config, project, install, subMon.split( 20 ) )
    }
    // FIXME matcher
    //    val hasComplete = JobHelpers.waitForJobs( new IJobMatcher() {
    //      override def matches( job : Job ) : Boolean = {
    //        job == scalaJob
    //      }
    //    }, eclipseUpdateJobTimeout * 1000 )
    //    if ( hasComplete ) {
    //      log.info( s"Configuraton complete." )
    //    } else {
    //      log.fail( s"Configuraton timed out." )
    //    }
  }

}

object ScalaIDE {

  import Maven._
  import com.carrotgarden.maven.scalor.zinc._

  def pluginProject(
    project : IProject
  ) : ScalaProject = {
    ScalaPlugin().getScalaProject( project )
  }

  /**
   * Eclipse resource path for Maven artifact jar file.
   */
  def pathFrom( artifact : Artifact ) = {
    new Path( artifact.getFile.getCanonicalPath )
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
  def installFrom(
    facade :  IMavenProjectFacade,
    install : ScalaInstall
  ) = {
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

  def report(
    module : ScalaModule
  ) = {
    import module._
    val text = new StringBuffer
    def spacer = text.append( "      " )
    def append( name : String, path : Option[ IPath ] ) = {
      spacer
      text.append( name )
      text.append( ": " )
      path match {
        case Some( path ) =>
          text.append( path.toFile.getCanonicalFile )
        case None =>
          text.append( "<missing>" )
      }
      text.append( "\n" )
    }
    append( "binary", Some( classJar ) )
    append( "source", sourceJar )
    text.toString
  }

  def report(
    install : LabeledScalaInstallation
  ) : String = {
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

  def report(
    label : ScalaInstallationLabel
  ) = label match {
    case CustomScalaInstallationLabel( title ) => title
    case _                                     => label.toString
  }

  type ErrorFun = String => Unit

  /**
   * Support standard Scala compiler options.
   * Support build manager options for Scala IDE.
   * Support compilation scope options for Scala IDE.
   */
  class Settings( errorFun : ErrorFun ) extends nsc.Settings( errorFun ) {

    import Settings._

    val NamePrefix = "-" // Use as command line option.
    val EmptyString = "" // Common value.

    /**
     * Path-dependent type cast to expose r/w set.
     */
    def configSet = allSettings.asInstanceOf[ HashSet[ nsc.Settings#Setting ] ]

    /**
     * Extract custom class path entry attribute describing a scope.
     */
    def scopeScalorAttrib( entry : IClasspathEntry ) : Option[ IClasspathAttribute ] = {
      import com.carrotgarden.maven.scalor.base.Build._
      entry.getExtraAttributes.find( Param.attrib.scope == _.getName )
    }

    /**
     * Convert compilation scope name from this plugin to Scala IDE format.
     */
    def scopeScalaIDE( scope : String ) : Option[ String ] = {
      import com.carrotgarden.maven.scalor.base.Build._
      import org.scalaide.core.internal.project._
      scope match {
        case Param.scope.`macro` => Some( CompileMacrosScope.name )
        case Param.scope.`main`  => Some( CompileMainScope.name )
        case Param.scope.`test`  => Some( CompileTestsScope.name )
        case invalid             => errorFun( s"invalid scope: ${invalid}" ); None
      }
    }

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
      val projectRoot = scalaProject.underlying.getLocation // absolute
      val resolvedClasspath = javaProject.getResolvedClasspath( true ).toList
      val settingsList = for {
        entry <- resolvedClasspath
        attrib <- scopeScalorAttrib( entry )
        scope <- scopeScalaIDE( attrib.getValue )
        resource <- Option( workspaceRoot.findMember( entry.getPath ) )
      } yield {
        val folderAbsolute = resource.getLocation
        val folderRelative = folderAbsolute.makeRelativeTo( projectRoot )
        val name = ScopesSettings.makeKey( folderRelative )
        val setting = settingScopeMapping( name )
        setting.value = scope
        allSettings += setting
        setting
      }
    }

    /**
     * Available Scala IDE compilation scopes.
     */
    def scopeList : List[ String ] = CompileScope.scopesInCompileOrder.map { _.name }.toList

    /**
     * Setting which provides source root scope definition.
     *
     * Persisted option example entry:
     * .settings/org.scala-ide.sdt.core.prefs!//src/macro/scala=macros
     */
    def settingScopeMapping( name : String ) : ChoiceSetting = {
      ChoiceSetting(
        name    = name, choices = scopeList, default = EmptyString,
        helpArg = EmptyString, descr = scalorMarker
      )
    }

    /**
     * Configure build manager for Scala IDE.
     *
     * Reuse settings from [[org.scalaide.ui.internal.preferences.ScalaPluginSettings]]
     */
    def makeBuildMangerSettings() : Unit = {
      settingsBuilderList.foreach { setting => configSet += setting }
    }

    /**
     * Setting which describes custom scala installation.
     */
    def settingInstallTitle = StringSetting(
      name    = NamePrefix + "scalor.install.title", default = EmptyString,
      arg     = EmptyString, descr = scalorMarker
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

  object Settings {

    /**
     * Magic marker for scalor-plugin-injected settings.
     */
    final val scalorMarker = "scalor-setting"

    /**
     * Preference pages: "Standard" "Advanced" "Presentation Compiler".
     */
    lazy val settingsShownList = {
      IDESettings.shownSettings( new nsc.Settings ).flatMap( box => box.userSettings )
    }

    /**
     * Preference pages: "Build Manager".
     */
    lazy val settingsBuilderList = {
      IDESettings.buildManagerSettings.flatMap( box => box.userSettings )
    }

    /**
     * Preference pages: ALL but "Scopes Settings".
     */
    lazy val settingsEnhancedList = {
      settingsShownList ++ settingsBuilderList
    }

    /**
     * IDE settings which has UI preference editor page.
     */
    def hasEnhancedSetting( setting : nsc.Settings#Setting ) = {
      settingsEnhancedList.find( _.name == setting.name ).isDefined
    }

    /**
     * IDE settings which are injected by Scalor plugin.
     */
    def hasScalorSetting( setting : nsc.Settings#Setting ) = {
      setting.helpDescription.contains( scalorMarker )
    }

    /**
     * IDE settings which use "additional command line parameters" box.
     */
    def hasCommandSetting( setting : nsc.Settings#Setting ) = {
      !hasEnhancedSetting( setting ) && !hasScalorSetting( setting )
    }

  }

}
