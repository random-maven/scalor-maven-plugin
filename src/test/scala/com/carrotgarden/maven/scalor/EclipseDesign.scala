package com.carrotgarden.maven.scalor


import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations._

import A.mojo._
import com.carrotgarden.maven.scalor.util.Folder._

import com.carrotgarden.maven.tools.Description
import scala.xml.transform.RuleTransformer

import xml._
import util.Xml._
import util.OSGI._
import util.Error._
import util.Params._
import util.Chiller._
import util.Classer._
import util.Props._
import com.carrotgarden.maven.scalor.eclipse.Wiring2._
import com.carrotgarden.maven.scalor.eclipse.Wiring2
import eclipse.ScalaPlugin._
import base.Params._

import scala.util.Success
import scala.util.Failure
import org.scalaide.core.internal.project.ScalaInstallation
import java.util.concurrent.Callable
import org.scalaide.core.internal.project.LabeledScalaInstallation
import org.scalaide.core.internal.project.ScalaModule
import org.scalaide.core.IScalaProject
import org.codehaus.plexus.classworlds.realm.ClassRealm
import java.net.URLClassLoader
import com.esotericsoftware.minlog.Log
import org.scalaide.core.internal.project.ScalaInstallationLabel
import scala.tools.nsc.settings.ScalaVersion
import scala.tools.nsc.settings.NoScalaVersion
import com.esotericsoftware.kryo.serializers.FieldSerializer

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer
import org.apache.maven.artifact.Artifact
import org.scalaide.core.internal.project.CustomScalaInstallationLabel
import java.net.URL

object EclipseDesign {
  
}

//@Description( """
//Provision and configure Eclipse .classpath file.
//""" )
//@Mojo(
//  name                         = `eclipse-classpath`,
//  defaultPhase                 = LifecyclePhase.INITIALIZE,
//  requiresDependencyResolution = ResolutionScope.NONE
//)
class EclipseClasspathMojo extends EclipseAnyMojo
  with eclipse.Classpath {
  //  import eclipse.Classpath._

  override def mojoName = ??? // `eclipse-classpath`

  def eclipseClasspath = eclipseClasspathFile.getCanonicalFile

  def eclipseHasClasspath = eclipseClasspath.exists

  def eclipseProvideClasspath() = {
    provideResource( eclipseClasspathTemplate, eclipseClasspath )
  }

  /**
   * Order Eclipse .classpath file entries.
   */
  def classpathSortRule = {
    if ( eclipseClasspathReorder ) {
      say.info( "Ordering eclipse .classpath." )
      val paramMap = parameterMap( eclipseClasspathOrder, eclipseOptionSeparator )
      val regexMap = regexParamMap( paramMap )
      val mapper = SettingsRegexMapper( regexMap )
      val pathOrder = AttributeOrdering( "path", mapper )
      OrderingRule( "classpath", pathOrder )
    } else {
      NoRewriteRule
    }
  }

  /**
   */
  def eclipseProcessClasspath = {
    val transform = new RuleTransformer(
      classpathSortRule
    )
    val file = eclipseClasspath
    val source = XML.loadFile( file )
    val target = transform( source )
    xmlSave( target, file )
  }

  override def performEclipse = {
    if ( eclipseClasspathProvide ) {
      if ( eclipseHasClasspath ) {
        say.info( "Project has a .classpath." )
      } else {
        say.info( "Providing template .classpath." )
        eclipseProvideClasspath
      }
    }
    eclipseProcessClasspath
  }
}

//@Description( """
//Provision and configure Eclipse .settings/ folder.
//""" )
//@Mojo(
//  name                         = `eclipse-settings`,
//  defaultPhase                 = LifecyclePhase.INITIALIZE,
//  requiresDependencyResolution = ResolutionScope.NONE
//)
class EclipseSettingsMojo extends EclipseAnyMojo
  with eclipse.Settings {

  override def mojoName = ??? // `eclipse-settings`

  override def performEclipse = {
    if ( eclipseSettingsJavaApply ) {
      say.info( "Providing Java .settings." )
      settingsProvide( eclipseSettingsJavaFile, eclipseSettingsJavaProps )
    }
    if ( eclipseSettingsScalaApply ) {
      say.info( "Providing Scala .settings." )
      settingsProvide( eclipseSettingsScalaFile, eclipseSettingsScalaProps )
    }
    if ( eclipseSettingsResourcesApply ) {
      say.info( "Providing Resourses .settings." )
      settingsProvide( eclipseSettingsResourcesFile, eclipseSettingsResourcesProps )
    }
  }
}

//@Description( """
//Provision and configure Eclipse .project file:
//- ensure project name;
//- create/delete natur;
//- create/delete buildCommand;
//- re-order buildCommand;
//""" )
//@Mojo(
//  name                         = `eclipse-project`,
//  defaultPhase                 = LifecyclePhase.INITIALIZE,
//  requiresDependencyResolution = ResolutionScope.NONE
//)
class EclipseProjectMojo extends EclipseAnyMojo
  with eclipse.Project {

  override def mojoName = ??? // `eclipse-project`

  /**
   * Replace projectDescription/name.
   */
  def nameApplyRule = {
    if ( eclipseProjectNameApply ) {
      say.info( "Applying .project name." )
      ReplacementRule(
        "projectDescription", "name", eclipseProjectNamePattern
      )
    } else {
      NoRewriteRule
    }
  }

  /**
   * Create/Delete natures/nature.
   */
  def natureApplyRule = {
    if ( eclipseProjectNatureApply ) {
      say.info( "Applying .project nature." )
      CreateDeleteImmediateRule(
        "natures", "nature",
        eclipseProjectNatureCreate, eclipseProjectNatureDelete
      );
    } else {
      NoRewriteRule
    }
  }

  /**
   * Re-order natures by natures/nature.
   */
  def natureSortRule = {
    if ( eclipseProjectNatureReorder ) {
      say.info( "Ordering .project nature." )
      val natureOrder = ElementOrdering( Seq() )
      OrderingRule( "natures", natureOrder )
    } else {
      NoRewriteRule
    }
  }

  /**
   * Create/Delete buildSpec/buildCommand.
   */
  def buildApplyRule = {
    if ( eclipseProjectBuildApply ) {
      say.info( "Applying .project build." )
      val make = new NodeProducer {
        def apply( label : String, text : String ) : Node = {
          <buildCommand>
            <name>{ text }</name>
            <arguments>
            </arguments>
          </buildCommand>
        }
      }
      val check = new NodeEquality {
        def apply( n1 : Node, n2 : Node ) : Boolean = {
          n1 \ "name" == n2 \ "name"
        }
      }
      CreateDeleteNestedRule(
        "buildSpec", "buildCommand",
        eclipseProjectBuildCreate, eclipseProjectBuildDelete,
        make, check
      );
    } else {
      NoRewriteRule
    }
  }

  /**
   * Re-order buildSpec.
   */
  def buildSortRule = {
    if ( eclipseProjectBuildReorder ) {
      say.info( "Ordering .project build." )
      val buildParamMap = parameterMap(
        eclipseProjectBuildOrdering, eclipseOptionSeparator
      )
      val buildRegexMap = regexParamMap( buildParamMap )
      val buildMapper = SettingsRegexMapper( buildRegexMap )
      val buildOrder = ElementOrdering( "name", buildMapper )
      OrderingRule( "buildSpec", buildOrder )
    } else {
      NoRewriteRule
    }
  }

  /**
   * Update Eclipse .project file entries with to these rules.
   */
  def eclipseProcessProject() = {
    val transform = new RuleTransformer(
      nameApplyRule,
      natureApplyRule,
      natureSortRule,
      buildApplyRule,
      buildSortRule
    )
    val file = eclipseProjectCanonical
    val source = xmlLoad( file )
    val target = transform( source )
    xmlSave( target, file )
  }

  override def performEclipse = {
    if ( eclipseProjectProvide ) {
      if ( eclipseHasProject ) {
        say.info( "Project has a .project." )
      } else {
        say.info( "Providing template .project." )
        eclipseProvideProject
      }
    }
    if ( eclipseProjectProcess ) {
      say.info( "Processing eclipse .project." )
      eclipseProcessProject
    }
  }

}


//@Description( """
//TODO TODO TODO
//""" )
//@Mojo(
//  name                         = `eclipse-config`,
//  defaultPhase                 = LifecyclePhase.INITIALIZE,
//  requiresDependencyResolution = ResolutionScope.NONE
//)
class EclipseConfigMojoXXX extends EclipseAnyMojo
  with base.ParamsArtifact
  with eclipse.Config {

  import EclipseConfigMojo._

  override def mojoName = `eclipse-config`

  def configMinlog = {
    if ( eclipseConfigLogKryo ) {
      say.info( "Configuring kryo logger." )
      minlogConfig(
        file  = eclipseConfigKryoLogFile,
        level = eclipseConfigKryoLogLevel
      )
    }
  }

  def resolveInstallation( handle : Wiring2.Handle2 ) : SimpleInstallation = {
    say.info( "Resolving custom scala installation." )

    val dependencyList = pluginDependencyList()

    dependencyList.foreach {
      dependency => say.info( s"   ${dependency}" )
    }

    val resolvedDependencyList = pluginResolvedDependencyList()

    resolvedDependencyList.foreach {
      dependency => say.info( s"   ${dependency}" )
    }

    val moduleList = resolvedDependencyList
      .filter( _.getType != "pom" )
      .filter( _.getClassifier == null )
      .map { artifact =>
        val identity = artifactIdentity( artifact )
        val binary = artifact.getFile.getCanonicalFile
        val source = resolveArtifact( resolvedDependencyList, identity, "sources" )
          .toOption.map( artifact => artifact.getFile.getCanonicalFile )
        SimpleModule(
          classFile  = binary,
          sourceFile = source,
          moduleType = moduleType( artifact )
        )
      }

    val compiler = moduleList
      .find( _.moduleType == ScalaCompiler )
      .getOrElse( Throw( "Plugin dependencies missing: " + artifactScalaCompiler ) )

    val library = moduleList
      .find( _.moduleType == ScalaLibrary )
      .getOrElse( Throw( "Plugin dependencies missing: " + artifactScalaLibrary ) )

    val extraJars = moduleList
      .filterNot( _.moduleType == ScalaLibrary )
      .filterNot( _.moduleType == ScalaCompiler )
      .filterNot( _.moduleType == CompilerPlugin )
      .toSeq

    val build = SimpleInstallation(
      label     = CustomScalaInstallationLabel( "Scalor: <0>" ),
      compiler  = compiler,
      library   = library,
      extraJars = extraJars
    )

    val source = build.copy(
      label = CustomScalaInstallationLabel( SimpleInstallation.titleFrom( build ) )
    )

    import handle._

    val target = withCast[ SimpleInstallation, SimpleInstallationUpdater, SimpleInstallation ](
      casterRealm, classOf[ SimpleInstallationUpdater ], classOf[ SimpleInstallation ], source
    )

    say.info( s"Produced custom scala installation: ${target.label}" )

    if ( eclipseConfigLogInstallResolve ) {
      say.info( s"\n${target.report}" )
    }

    target

  }

  def configInstall( handle : Wiring2.Handle2 ) = {
    import handle._
    if ( eclipseConfigEnsureInstall ) {
      say.info( "Configuring custom scala installations." )

      val extractedList = withCast[ List[ SimpleInstallation ], SimpleInstallationListExtractor, Option[ _ ] ](
        casterRealm, classOf[ SimpleInstallationListExtractor ], classOf[ Option[ _ ] ], None
      )
      if ( eclipseConfigLogInstallExtract ) {
        say.info( s"Extracted custom installation list: \n\n${SimpleInstallation.report( extractedList )}" )
      }

      val validatedList = if ( eclipseConfigRemoveInvalid ) {
        say.info( "Erazing custom invalid installations." )
        extractedList.filter( _.valid )
      } else {
        say.info( "Keeping custom invalid installations." )
        extractedList
      }

      val installation = resolveInstallation( handle )

      val hasInstallation = validatedList.find( _.identity == installation.identity ).isDefined

      val updatedList = if ( hasInstallation ) {
        say.info( "Installation was present: " + installation.label )
        validatedList
      } else {
        say.info( "Registering installation: " + installation.label )
        installation :: validatedList
      }

      val persistedList = withCast[ List[ SimpleInstallation ], SimpleInstallationListPersister, List[ SimpleInstallation ] ](
        casterRealm, classOf[ SimpleInstallationListPersister ], classOf[ List[ SimpleInstallation ] ], updatedList
      )
      if ( eclipseConfigLogInstallPersist ) {
        say.info( s"Persisted custom installation list: \n\n${SimpleInstallation.report( persistedList )}" )
      }

    }
  }

  def configureXXX( handle : Wiring2.Handle2 ) = {
    import handle._

    configMinlog
    configInstall( handle )

    val projectList = workspace.getRoot.getProjects
    val eclipseProject = projectWithPath( projectList, project.getBasedir )
    val scalaProject = scalaPlugin.getScalaProject( eclipseProject )
    say.info( s"Scala project: ${scalaProject}" )

    val javaProject = scalaProject.javaProject

    val installation = scalaProject.effectiveScalaInstallation
    val report = installation match {
      case custom : LabeledScalaInstallation =>
        custom.asInstanceOf[ LabeledScalaInstallation ].label.toString
      case _ =>
        installation.toString

    }
    say.info( s"Project scala installation: ${report}" )

  }

  def configure( handle : Wiring2.Handle2 ) = {
    import handle._
    say.info( "Configuring eclipse plugin:" )

    val metaUrl = Eclipse.pluginPropertiesUrl
    val metaProps = propertiesFrom( metaUrl )

    val pluginId = metaProps.getProperty( "id" )
    val pluginVersion = metaProps.getProperty( "version" )
    val pluginLocation = Eclipse.location

    say.info( "   id:       " + pluginId )
    say.info( "   version:  " + pluginVersion )
    say.info( "   location: " + pluginLocation )

    val context = bundleM2E.getBundleContext

    val pluginOption = Option( context.getBundle( pluginLocation ) )

    val ( message, pluginBundle ) = if ( pluginOption.isDefined ) {
      ( "Plugin was already installed", pluginOption.get )
    } else {
      context.installBundle( pluginLocation ).start()
      ( "Plugin is now installed in eclipse", context.getBundle( pluginLocation ) )
    }

    say.info( message + ": " + pluginBundle )

  }

  override def performEclipse = {
    say.info( "Configuring eclipse:" )
    // configure( resolveHandle )
  }

}
