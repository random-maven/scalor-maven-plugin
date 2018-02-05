package com.carrotgarden.maven.scalor.eclipse

import java.io.File

import scala.collection.JavaConverters._
import scala.sys.process.Process
import scala.sys.process.ProcessLogger

import org.apache.maven.plugin.MojoExecution
import org.apache.maven.project.MavenProject
import org.eclipse.core.runtime.IProgressMonitor

import com.carrotgarden.maven.scalor.EclipseRestartMojo
import com.carrotgarden.maven.scalor.eclipse.Watcher.Detector
import com.carrotgarden.maven.scalor.util.Error
import com.carrotgarden.maven.scalor.util.Logging.AnyLog
import com.carrotgarden.maven.scalor.util.Logging.NoopLogger
import com.carrotgarden.maven.scalor.util.Logging.SwitchLogger

import org.eclipse.m2e.core.project.IMavenProjectFacade
import com.carrotgarden.maven.scalor.util.Folder

/**
 * Manage test application restart.
 */
trait Restart {

  import Restart._

  /**
   * Build participant invocation.
   *
   * Setup or update test process management job.
   */
  def restartEnsure(
    kind :      Int,
    monitor :   IProgressMonitor,
    logger :    AnyLog,
    restart :   ParamsRestart,
    facade :    IMavenProjectFacade,
    execution : MojoExecution
  ) : Unit = {
    import restart._
    if ( eclipseRestartEnable ) {
      val manager = managerEnsure(
        facade, restart, SwitchLogger( logger, "restart-manager" )
      )
      logger.info( s"Manager running @ ${manager}" )
    } else {
      Tasker.stopTask( eclipseRestartTaskName )
    }
  }

}

object Restart {

  /**
   * Process execution context.
   */
  case class Context(
    // job name
    name : String,
    // project base directory
    baseDir : File,
    // application work directory
    workDir : File,
    // monitored build folders
    buildList : Array[ String ],
    // monitored resource patterns
    matchList : Array[ String ],
    // application environment variables
    execVars : Array[ ( String, String ) ],
    // application java launch command
    command : Array[ String ],
    //
    periodInvoke :  Long,
    periodPrevent : Long,
    periodSettle :  Long,
    //
    hasLogChanged :  Boolean,
    hasLogCommand :  Boolean,
    hasLogDetected : Boolean
  ) {

    override def equals( some : Any ) : Boolean = {
      if ( !some.isInstanceOf[ Context ] ) { return false }
      val that = some.asInstanceOf[ Context ]
      if ( this.name != that.name ) { return false }
      if ( this.workDir != that.workDir ) { return false }
      if ( !this.buildList.sameElements( that.buildList ) ) { return false }
      if ( !this.matchList.sameElements( that.matchList ) ) { return false }
      if ( !this.execVars.sameElements( that.execVars ) ) { return false }
      if ( !this.command.sameElements( that.command ) ) { return false }
      if ( this.periodInvoke != that.periodInvoke ) { return false }
      if ( this.periodPrevent != that.periodPrevent ) { return false }
      if ( this.periodSettle != that.periodSettle ) { return false }
      if ( this.hasLogChanged != that.hasLogChanged ) { return false }
      if ( this.hasLogCommand != that.hasLogCommand ) { return false }
      if ( this.hasLogDetected != that.hasLogDetected ) { return false }
      return true
    }

    def reportCommand : String = {
      val regex = s"[ \t\f\n\r ${File.pathSeparator}]+"
      command.flatMap( _.split( regex ) ).mkString( "\n" )
    }

  }

  object Context {

    def apply( facade : IMavenProjectFacade, restart : ParamsRestart ) : Context = {
      import restart._
      val project = facade.getMavenProject
      val baseDir = project.getBasedir.getAbsoluteFile
      val workDir = eclipseRestartWorkDir.getAbsoluteFile
      val classPath = ( buildTargetFolder +: buildDependencyFolders ) ++ projectClassPath( project )
      val buildList = classPath.filter( path => path.isDirectory ).map( folder => folder.getAbsolutePath )
      val matchList = parseCommonList( eclipseRestartMatchList, commonSequenceSeparator )
      val execArgs = parseCommonList( eclipseRestartJavaArgs, commonSequenceSeparator )
      val execVars = parseCommonMapping( eclipseRestartJavaVars, commonSequenceSeparator ).toArray
      val argsList = execArgs ++ Array( "-classpath", javaPath( classPath ) )
      val command = javaExec +: argsList :+ eclipseRestartMainClass
      new Context(
        name           = eclipseRestartTaskName,
        baseDir        = baseDir,
        workDir        = workDir,
        buildList      = buildList,
        matchList      = matchList,
        execVars       = execVars,
        command        = command,
        periodInvoke   = eclipseRestartPeriodInvoke,
        periodPrevent  = eclipseRestartPeriodPrevent,
        periodSettle   = eclipseRestartPeriodSettle,
        hasLogChanged  = eclipseRestartLogChanged,
        hasLogCommand  = eclipseRestartLogCommand,
        hasLogDetected = eclipseRestartLogDetected
      )
    }

  }

  /**
   * Provision process management job.
   */
  case class Manager(
    context : Context,
    logger :  AnyLog
  ) extends Tasker.Periodic(
    name   = context.name,
    logger = logger,
    period = context.periodInvoke
  ) {
    import context._

    val detector = {
      val loggerOption = if ( hasLogChanged )
        Some( SwitchLogger( logger, "restart-detector" ) ) else None
      Detector( buildList, matchList, loggerOption )
    }

    @volatile var process : Process = null

    def reportCommand() : Unit = if ( hasLogCommand ) {
      logger.info( s"Process command @ ${this}:\n${context.reportCommand}" )
    }

    def processLogger = {
      ProcessLogger( logger.info, logger.fail )
    }

    def processCreate() : Unit = this.synchronized {
      logger.info( s"Process create @ ${this}" )
      try {
        Folder.ensureFolder( workDir )
        val builder = Process( command, workDir, execVars : _* )
        val process = builder.run( processLogger )
        this.process = process
      } catch {
        case error : Throwable =>
          val report = Error.reportStackTrace( error )
          logger.fail( s"Process failure: ${error.getMessage} @ ${this}\n${report}" )
      }
    }

    def processDelete() : Unit = this.synchronized {
      logger.info( s"Process delete @ ${this}" )
      try {
        processOption.foreach( process => process.destroy )
      } catch {
        case error : Throwable => // ignore
      }
    }

    def processOption : Option[ Process ] = {
      Option( this.process )
    }

    def processRestart() = {
      processDelete()
      processCreate()
    }

    def processPrevent() = {
      try {
        Thread.sleep( periodPrevent )
      } catch {
        case error : Throwable => // ignore
      }
    }

    override def init() = {
      super.init()
      reportCommand()
    }

    override def onSeriesSetup() = {
      logger.info( s"Manager setup @ ${this}" )
      Watcher.register( detector )
      processCreate()
    }

    override def onSeriesShutdown() = {
      logger.info( s"Manager shutdown @ ${this}" )
      Watcher.unregister( detector )
      processDelete()
    }

    override def runTask( monitor : IProgressMonitor ) : Unit = {
      if ( monitor.isCanceled ) {
        logger.info( s"Manager cancel: ${this}" )
        return
      }
      val option = processOption
      if ( option.isDefined ) {
        val process = option.get
        if ( process.isAlive ) {
          val hasResult = detector.hasResult( periodSettle )
          if ( hasResult ) {
            if ( hasLogDetected ) {
              logger.info( s"Process detect @ ${this}\n${detector.resultReport}" )
            }
            detector.resultClear()
            logger.info( s"Process update @ ${this}" )
            processRestart()
          }
        } else {
          logger.warn( s"Process zombie @ ${this}" )
          processPrevent() // prevent restart flood
          processRestart()
        }
      } else {
        onSeriesSetup()
      }
    }

  }

  // FIXME move to maven
  def projectClassPath( project : MavenProject ) : Array[ File ] = {
    project.getArtifacts.asScala
      .filter( artifact => artifact.getFile != null )
      .map( artifact => artifact.getFile.getAbsoluteFile )
      .toArray
  }

  def javaPath( classPath : Array[ File ] ) : String = {
    classPath.map( _.getAbsolutePath ).mkString( File.pathSeparator )
  }

  // FIXME detect o.s.
  def javaExec : String = {
    import com.carrotgarden.maven.scalor.util.Folder._
    val javaHome = System.getProperty( "java.home" )
    val javaExecNix = new File( javaHome, "/bin/java" )
    val execExecWin = new File( javaHome, "/bin/java.exe" )
    if ( hasExec( javaExecNix ) ) {
      javaExecNix.getAbsolutePath
    } else if ( hasExec( execExecWin ) ) {
      execExecWin.getAbsolutePath
    } else {
      "java"
    }
  }

  /**
   * Provision process management job.
   */
  def managerEnsure(
    facade : IMavenProjectFacade, restart : ParamsRestart, logger : AnyLog
  ) : Manager = {
    val context = Context( facade, restart )
    //
    def managerCreate( context : Context ) : Manager = {
      logger.info( s"Manager create @ ${context.baseDir}" )
      val manager = Manager( context, logger )
      manager.init()
      manager
    }
    //
    def managerDelete( context : Context ) : Unit = {
      logger.info( s"Manager delete @ ${context.baseDir}" )
      Tasker.stopTask( context.name )
    }
    //
    val option = Tasker.findTask( context.name )
    if ( option.isDefined ) {
      val current = option.get
      if ( current.isInstanceOf[ Manager ] ) {
        // reuse class loader
        val manager = current.asInstanceOf[ Manager ]
        if ( manager.context == context ) {
          // reuse existing manager
          manager
        } else {
          // manager context update
          managerDelete( manager.context )
          managerCreate( context )
        }
      } else {
        // on class loader update
        managerDelete( context )
        managerCreate( context )
      }
    } else {
      // provide new manager
      managerCreate( context )
    }
  }

}
