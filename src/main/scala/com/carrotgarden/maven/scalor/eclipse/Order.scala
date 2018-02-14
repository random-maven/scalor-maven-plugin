package com.carrotgarden.maven.scalor.eclipse

import java.util.Arrays
import java.util.Collections
import java.util.Comparator

import scala.collection.JavaConverters.asScalaBufferConverter
import scala.util.Sorting

import org.eclipse.core.resources.ICommand
import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.m2e.jdt.IClasspathDescriptor
import org.eclipse.m2e.jdt.IClasspathEntryDescriptor

import com.carrotgarden.maven.scalor.util.Optioner.convert_Option_Value
import com.carrotgarden.maven.scalor.util.Params.RegexMapper
import com.carrotgarden.maven.scalor.util.Params.SettingsRegexMapper
import com.carrotgarden.maven.scalor.util.Params.parameterMap

/**
 * Re-order eclipse .project file and .classpath file entries.
 */
trait Order {

  self : Maven =>

  import Order._
  import org.eclipse.core.resources.IResource._

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
    context : Config.SetupContext,
    monitor : IProgressMonitor
  ) : Unit = {
    import context._
    import config._
    val facade = request.getMavenProjectFacade
    if ( eclipseBuilderReorder ) {
      logger.info( "Ordering Eclipse .project builder entries." )
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
    /** Order class path entries. */
    Collections.sort( descriptorList, comparator )
    /** Notify class path changes. */
    descriptorList.asScala.foreach { entry => classpath.touchEntry( entry.getPath ) }
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
    val mapper = new SettingsRegexMapper( ordering, separator )
    val comparator = ComparatorClasspathRule( mapper )
    reorderClasspath( classpath, comparator, monitor )
  }

  /**
   * Order class path entries inside the .classpath Maven container.
   */
  def ensureOrderMaven(
    context : Config.SetupContext,
    monitor : IProgressMonitor
  ) = {
    import context._
    import config._
    if ( eclipseMavenReorder ) {
      logger.info( "Ordering entries inside the Maven container." )
      val configMap = parameterMap( eclipseMavenOrdering, commonSequenceSeparator )
      val ( field, order ) = if ( configMap.isEmpty ) {
        ( "artifactId", "ascending" )
      } else {
        configMap.head
      }
      val comparator = ComparatorArtifact( field, order )
      reorderClasspath( classpath, comparator, monitor )
    }
  }

  /**
   * Order natures inside Eclipse .project.
   */
  def ensureOrderNature(
    context : Config.SetupContext,
    monitor : IProgressMonitor
  ) = {
    import context._
    import config._
    if ( eclipseNatureReorder ) {
      logger.info( "Ordering Eclipse .project nature entries." )
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
    context : Config.SetupContext,
    monitor : IProgressMonitor
  ) = {
    import context._
    import config._
    if ( eclipseClasspathReorder ) {
      logger.info( "Ordering top level entries inside the .classpath." )
      ensureOrderAny( config, eclipseClasspathOrdering, commonSequenceSeparator, classpath, monitor )
    }
  }

}

object Order {

  case class EntryFilterAll() extends IClasspathDescriptor.EntryFilter {
    override def accept( descriptor : IClasspathEntryDescriptor ) : Boolean = true
  }

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
      entry.getArtifactId
    }
    val sort = if ( order.startsWith( "asc" ) ) true else false
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
