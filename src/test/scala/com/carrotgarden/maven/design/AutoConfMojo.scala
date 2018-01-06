package com.carrotgarden.maven.design

import com.carrotgarden.maven.scalor
import com.carrotgarden.maven.scalor._

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations._
import com.carrotgarden.maven.tools.Description
import A.mojo._
import java.util.Date
import java.text.SimpleDateFormat
import org.apache.maven.plugin.PluginParameterExpressionEvaluator
import java.util.function.Consumer
import org.apache.maven.plugin.descriptor
import org.codehaus.plexus.util.xml.Xpp3Dom
import java.io.File
import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer
import org.apache.maven.model.Resource
import java.nio.file.Path

@Description( """
Discover and inject maven project properties for use by other executions.
Generated properties look like: [scalor.autoconfSomeName].
Interpolated parameters look like: [scalor.othermoduleSomeName].
Interpolated parameters are collected from all scalor plugin executions.
""" )
@Mojo(
  name         = `auto-conf`,
  defaultPhase = LifecyclePhase.INITIALIZE,
  /** Use COMPILE to discover scala-library version. */
  requiresDependencyResolution = ResolutionScope.COMPILE
)
class AutoConfMojo extends AbstractMojo
  with base.Mojo
  with base.Params
  with base.Logging
  // with base.BuildEncoding
  with base.BuildMacroSources
  with base.BuildMainSources
  with base.BuildTestSources
  with com.carrotgarden.maven.design.conf.Params
  //  with zinc.ParamsPluginList
  //  with zinc.ParamsRegexJar
  with scalor.eclipse.Build {

  override def mojoName = `auto-conf`

  @Description( """
  Flag to skip goal execution: auto-conf.
  """ )
  @Parameter(
    property     = "scalor.skipAutoConf", //
    defaultValue = "false"
  )
  var skipAutoConf : Boolean = _

  def userProps = session.getUserProperties

  def projectProps = project.getProperties

  def pluginConfig : Xpp3Dom = ??? // pluginDescriptor.getPlugin.getConfiguration.asInstanceOf[ Xpp3Dom ]

  def hasProperty( name : String ) =
    userProps.getProperty( name ) != null || projectProps.getProperty( name ) != null

  //  def persistProperty( key : String, value : String ) = {
  //    projectProps.put( key, value )
  //    reportProperty( key )
  //  }

  def reportMojo( mojo : descriptor.MojoDescriptor ) = {
    if ( autoconfLogProperties ) {
      say.info( s"${mojo.getFullGoalName}" )
    }
  }

  def reportProperty( name : String ) = {
    if ( autoconfLogProperties ) {
      say.info( s"   ${name} = ${project.getProperties.getProperty( name )}" )
    }
  }

  type EclipseFormat = ( Path => String )

  /**
   * Eclipse .settings resource encoding entry:
   * encoding//src/main/java=UTF-8
   */
  def eclipseEncodingFormat( encoding : String )( path : Path ) : String = {
    s"encoding//${path.toString}=${encoding}"
  }

  /**
   * Eclipse .settings source root entry for a compilation scope:
   * //src/main/java=main
   */
  def eclipseRootFormat( scope : String )( path : Path ) : String = {
    s"//${path.toString}=${scope}"
  }

  /**
   * Build a list of Eclipse source root entries.
   */
  def eclipseAppend(
    format : EclipseFormat,
    base :   File, pathList : Seq[ File ], list : ArrayBuffer[ String ]
  ) : Unit = {
    pathList.foreach { file =>
      val path = base.getCanonicalFile.toPath.relativize( file.getCanonicalFile.toPath )
      list += format( path )
    }
  }

  def convert( source : String ) = new File( source ).getCanonicalFile

  def convert( resource : Resource ) = new File( resource.getDirectory ).getCanonicalFile

  def convert( list : ArrayBuffer[ String ] ) : String = list.mkString( "\n" )

  def merge(
    javaList : Array[ File ], scalaList : Array[ File ],
    rootList : java.util.List[ String ]
  ) : List[ File ] = {
    ( javaList.toList ++ scalaList.toList ++ rootList.asScala.map( convert ) ).distinct.sorted
  }

  def rootsMain = {
    val sources = merge(
      buildMainSourceJavaFolders, buildMainSourceScalaFolders,
      project.getCompileSourceRoots
    )
    val resources = project.getBuild.getResources.asScala.map( convert )
    ( sources, resources )
  }

  def rootsTest = {
    val sources = merge(
      buildTestSourceJavaFolders, buildTestSourceScalaFolders,
      project.getTestCompileSourceRoots
    )
    val resources = project.getBuild.getTestResources.asScala.map( convert )
    ( sources, resources )
  }

  /**
   * Provide Eclipse resource encoding configuration.
   */
  def generateEclipseEncodings : Unit = {
    val base = project.getBasedir
     val encoding = "UTF-8" // buildSourceEncoding
     val format = eclipseEncodingFormat( encoding ) _;
    {
      val list = ArrayBuffer[ String ]()
      eclipseAppend( format, base, buildMacroSourceJavaFolders, list )
      eclipseAppend( format, base, buildMacroSourceScalaFolders, list )
      persistProperty( autoconfEclipseEncodinsMacro, convert( list ) )
    };
    {
      val list = ArrayBuffer[ String ]()
      val ( sources, resources ) = rootsMain
      eclipseAppend( format, base, sources, list )
      eclipseAppend( format, base, resources, list )
      persistProperty( autoconfEclipseEncodinsMain, convert( list ) )
    };
    {
      val list = ArrayBuffer[ String ]()
      val ( sources, resources ) = rootsTest
      eclipseAppend( format, base, sources, list )
      eclipseAppend( format, base, resources, list )
      persistProperty( autoconfEclipseEncodinsTest, convert( list ) )
    };
  }

  /**
   * Provide Eclipse build roots configuration.
   */
  def generateEclipseRoots : Unit = {
    val base = project.getBasedir;
    {
      val scope = "macros" // hard coded in eclipse-ide
      val format = eclipseRootFormat( scope ) _;
      val list = ArrayBuffer[ String ]()
      eclipseAppend( format, base, buildMacroSourceJavaFolders, list )
      eclipseAppend( format, base, buildMacroSourceScalaFolders, list )
      persistProperty( autoconfEclipseRootsMacro, convert( list ) )
    };
    {
      val scope = "main" // hard coded in eclipse-ide
      val format = eclipseRootFormat( scope ) _;
      val list = ArrayBuffer[ String ]()
      val ( sources, resources ) = rootsMain
      eclipseAppend( format, base, sources, list )
      eclipseAppend( format, base, resources, list )
      persistProperty( autoconfEclipseRootsMain, convert( list ) )
    };
    {
      val scope = "tests" // hard coded in eclipse-ide
      val format = eclipseRootFormat( scope ) _;
      val list = ArrayBuffer[ String ]()
      val ( sources, resources ) = rootsTest
      eclipseAppend( format, base, sources, list )
      eclipseAppend( format, base, resources, list )
      persistProperty( autoconfEclipseRootsTest, convert( list ) )
    };
  }

  /**
   * Provide formatted build stamp.
   */
  def generateBuildStamp : Unit = {
    val format = new SimpleDateFormat( autoconfStampFormat )
    persistProperty( autoconfBuildStamp, format.format( new Date() ) )
  }

  /**
   * Provide scala version from project build path.
   */
  def generateScalaVersion : Unit = {
    //    val classPath : Array[ File ] = projectClassPath()
    //    util.Folder.resolveJar( classPath, zincRegexScalaLibrary ) match {
    //      case Right( library ) =>
    //        val ( epoch, release ) = util.Params.versionPair( library.version )
    //        persistProperty( autoconfScalaVersionEpoch, epoch )
    //        persistProperty( autoconfScalaVersionRelease, release )
    //      case Left( error ) =>
    //        say.error( error )
    //    }
  }

  /**
   * Provide scala compiler plugins from scalor maven plugin class path.
   */
  def generatePluginList : Unit = {
    //    val loader = this.getClass.getClassLoader
    //    val separator = "," // hard coded in scala-ide
    //    val pluginList = zincPluginDiscoveryList( loader ).mkString( separator )
    //    persistProperty( autoconfCompilerPluginList, pluginList )
  }

  /**
   * Inject generated properties.
   */
  def performGenerate() = {
    say.info( "Providing generated properties." )
    generateBuildStamp
    generateScalaVersion
    generatePluginList
    generateEclipseEncodings
    generateEclipseRoots
  }

  /**
   * Resolve and inject scalor plugin parameters.
   */
  def performSubstitute() {
    say.info( "Providing substituted parameters." )
    val evaluator = new PluginParameterExpressionEvaluator( session, mojoExecution )
    val paramAction = new Consumer[ descriptor.Parameter ] {
      override def accept( param : descriptor.Parameter ) = {
        val parameterName = param.getName // plugin config parameter
        val propertyExpression = param.getExpression // parameter user property reference
        propertyExpression match {
          case null =>
            say.debug( s"Parameter w/o property: ${param}" )
          case A.maven.propertyRegex( propertyName ) => // has configurable property
            if ( !hasProperty( propertyName ) ) { // property is not yet configured
              val entry = pluginConfig.getChild( parameterName ) // optional configuration entry
              val expression = if ( entry != null ) {
                entry.getValue // use plugin/configuration entry
              } else {
                param.getDefaultValue // use plugin/parameter/default entry
              }
              val propertyValue = evaluator.evaluate( expression ) // interpolate for final result
              persistProperty( propertyName, propertyValue.toString ) // inject changes
            }
          case _ => // no inject
        }
      }
    }
    val mojoAction = new Consumer[ descriptor.MojoDescriptor ] {
      override def accept( mojo : descriptor.MojoDescriptor ) = {
        reportMojo( mojo )
        mojo.getParameters.forEach( paramAction )
      }
    }

    // pluginDescriptor.getMojos.forEach( mojoAction )

  }

  def performAutoConf : Unit = {
    performGenerate
    performSubstitute
  }

  def perform() : Unit = {
    if ( skipAutoConf ) {
      say.info( "Skipping disabled goal execution." )
      return
    }
    if ( hasIncremental ) {
      say.info( "Skipping incremental build invocation." )
      return
    }
    performAutoConf
  }

}
