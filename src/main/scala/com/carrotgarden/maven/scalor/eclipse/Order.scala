package com.carrotgarden.maven.scalor.eclipse

import com.carrotgarden.maven.scalor.util

import util.Params._
import Params._

import scala.collection.JavaConverters._

import org.eclipse.m2e.jdt.IClasspathDescriptor
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.m2e.jdt.IClasspathDescriptor
import org.eclipse.core.runtime.IProgressMonitor
import java.util.Comparator
import org.eclipse.m2e.core.project.IMavenProjectFacade
import org.eclipse.m2e.core.project.IMavenProjectFacade

import org.eclipse.core.resources.ICommand
import org.eclipse.m2e.jdt.IClasspathEntryDescriptor
import java.util.ArrayList
import java.util.Collections
import org.eclipse.core.resources.IProject
import scala.util.Sorting

import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest
import java.util.Arrays
import org.eclipse.core.resources.IResource

/**
 * Re-order eclipse .project file and .classpath file entries.
 */
trait Order {

  self : Logging with Maven =>

  import Order._
  import IResource._

  /**
   *
   */
  def reorderBuilder(
    project :    IProject,
    comparator : Comparator[ ICommand ],
    monitor :    IProgressMonitor
  ) : Unit = {
    implicit val _ = comparator
    import scala.math.Ordering.comparatorToOrdering
    val description = project.getDescription
    val buildSpec = description.getBuildSpec // clone
    val buildTest = description.getBuildSpec // clone
    Sorting.quickSort( buildSpec )
    val hasChange = !buildSpec.sameElements( buildTest )
    if ( hasChange ) {
      description.setBuildSpec( buildSpec )
      project.setDescription( description, FORCE, monitor )
    }
  }

  /**
   *
   */
  def reorderBuilder(
    project :   IProject,
    ordering :  String,
    separator : String,
    monitor :   IProgressMonitor
  ) : Unit = {
    val mapper = new SettingsRegexMapper( ordering, separator )
    val comparator = ComparatorBuilderRule( mapper )
    reorderBuilder( project, comparator, monitor )
  }

  /**
   * Order Eclipse .project builder entries
   */
  def ensureOrderBuilder(
    request : ProjectConfigurationRequest,
    config :  ParamsConfig,
    monitor : IProgressMonitor
  ) : Unit = {
    import config._
    val facade = request.getMavenProjectFacade
    if ( eclipseBuilderReorder ) {
      log.info( "Ordering Eclipse .project builder entries." )
      reorderBuilder( facade.getProject, eclipseBuilderOrdering, commonSequenceSeparator, monitor )
    }
  }

  /**
   * Order class path entries according to comparator.
   */
  def reorderClasspath(
    classpath :  IClasspathDescriptor,
    comparator : Comparator[ IClasspathEntryDescriptor ],
    monitor :    IProgressMonitor
  ) = {
    /** This is list by reference. */
    val descriptorList = classpath.getEntryDescriptors
    val descriptorTest = new ArrayList[ IClasspathEntryDescriptor ]( descriptorList )
    Collections.sort( descriptorTest, comparator )
    val hasChange = descriptorList != descriptorTest
    if ( hasChange ) {
      Collections.sort( descriptorList, comparator )
    }
  }

  /**
   * Order class path entries.
   */
  def ensureOrderAny(
    config :    ParamsConfig,
    ordering :  String,
    separator : String,
    classpath : IClasspathDescriptor,
    monitor :   IProgressMonitor
  ) = {
    import config._
    val mapper = new SettingsRegexMapper( ordering, separator )
    val comparator = ComparatorClasspathRule( mapper )
    reorderClasspath( classpath, comparator, monitor )
    if ( eclipseLogClasspathOrder ) {
      classpath.getEntryDescriptors.asScala.foreach { entry =>
        log.info( s"   ${entry.getPath.toPortableString}" )
      }
    }
  }

  /**
   * Order class path entries inside the .classpath Maven container.
   */
  def ensureOrderMaven(
    facade :    IMavenProjectFacade,
    config :    ParamsConfig,
    classpath : IClasspathDescriptor,
    monitor :   IProgressMonitor
  ) = {
    import config._
    if ( eclipseMavenReorder ) {
      log.info( "Ordering entries inside the .classpath Maven container." )
      val configMap = parameterMap( eclipseMavenOrdering, commonSequenceSeparator )
      val ( field, sort ) = if ( configMap.isEmpty ) {
        ( "artifactId", "ascending" )
      } else {
        configMap.head
      }
      val comparator = ComparatorArtifact( field, sort )
      reorderClasspath( classpath, comparator, monitor )
    }
  }

  /**
   * Order natures inside Eclipse .project.
   */
  def ensureOrderNature(
    request : ProjectConfigurationRequest,
    config :  ParamsConfig,
    monitor : IProgressMonitor
  ) = {
    import config._
    if ( eclipseNatureReorder ) {
      log.info( "Ordering Eclipse .project nature entries." )
      val mapper = new SettingsRegexMapper( eclipseNatureOrdering, commonSequenceSeparator )
      val comparator = ComparatorStringRule( mapper )
      val project = request.getProject
      val description = project.getDescription // clone
      val natureList = description.getNatureIds // clone
      Arrays.sort( natureList, comparator )
      val natureTest = description.getNatureIds // clone
      val hasChange = !natureList.sameElements( natureTest )
      if ( hasChange ) {
        Nature.persistNature( project, natureList, monitor )
      }
    }
  }

  /**
   * Order top level class path entries inside the .classpath.
   */
  def ensureOrderTopLevel(
    request :   ProjectConfigurationRequest,
    config :    ParamsConfig,
    classpath : IClasspathDescriptor,
    monitor :   IProgressMonitor
  ) = {
    import config._
    if ( eclipseClasspathReorder ) {
      log.info( "Ordering top level entries inside the .classpath." )
      ensureOrderAny( config, eclipseClasspathOrdering, commonSequenceSeparator, classpath, monitor )
    }
  }

}

object Order {

  /**
   * Compare by Maven artifact fields.
   */
  case class ComparatorArtifact(
    field : String,
    order : String
  ) extends Comparator[ IClasspathEntryDescriptor ] {
    def text( entry : IClasspathEntryDescriptor ) = {
      field match {
        case "groupId"    => entry.getGroupId
        case "artifactId" => entry.getArtifactId
        case _            => entry.getArtifactKey.toPortableString
      }
    }
    def sort = if ( order.startsWith( "asc" ) ) true else false
    override def compare( e1 : IClasspathEntryDescriptor, e2 : IClasspathEntryDescriptor ) : Int = {
      val t1 = text( e1 )
      val t2 = text( e2 )
      if ( sort ) {
        t1.compareTo( t2 )
      } else {
        t2.compareTo( t1 )
      }
    }
  }

  trait ComparatorAnyRule[ T ]
    extends Comparator[ T ] {
    val mapper : RegexMapper
    val sortTail = "zzzzzzz"
    def text( entry : T ) : String
    override def compare( e1 : T, e2 : T ) : Int = {
      val t1 = mapper.map( text( e1 ) )
      val t2 = mapper.map( text( e2 ) )
      return t1.compareTo( t2 )
    }
  }

  case class ComparatorBuilderRule( mapper : RegexMapper )
    extends ComparatorAnyRule[ ICommand ] {
    override def text( entry : ICommand ) = {
      if ( entry == null ) sortTail
      else if ( entry.getBuilderName == null ) sortTail
      else entry.getBuilderName
    }
  }

  case class ComparatorClasspathRule( mapper : RegexMapper )
    extends ComparatorAnyRule[ IClasspathEntryDescriptor ] {
    override def text( entry : IClasspathEntryDescriptor ) = {
      if ( entry == null ) sortTail
      else if ( entry.getPath == null ) sortTail
      else if ( entry.getPath.toPortableString == null ) sortTail
      else entry.getPath.toPortableString
    }
  }

  case class ComparatorStringRule( mapper : RegexMapper )
    extends ComparatorAnyRule[ String ] {
    override def text( entry : String ) = {
      entry
    }
  }

}
