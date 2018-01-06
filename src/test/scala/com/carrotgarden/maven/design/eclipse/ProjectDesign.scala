package com.carrotgarden.maven.design.eclipse

import xml._
import xml.transform._
import org.apache.maven.plugins.annotations._
import java.io.File
import com.carrotgarden.maven.scalor._
import com.carrotgarden.maven.tools.Description
import com.carrotgarden.maven.scalor.base

trait ProjectDesign extends base.ParamsAny {

  import ProjectDesign._
  import util.Folder._
  import util.Params._

  @Description( """
  Location of Eclipse .project configuration file.
  """ )
  @Parameter(
    property     = "scalor.eclipseProjectFile",
    defaultValue = "${project.basedir}/.project"
  )
  var eclipseProjectFile : File = _

  @Description( """
  Re-order Eclipse .project file by 'projectDescription/buildSpec/buildCommand/name' .
  """ )
  @Parameter(
    property     = "scalor.eclipseProjectBuildReorder",
    defaultValue = "true"
  )
  var eclipseProjectBuildReorder : Boolean = _

  @Description( """
  Order Eclipse .project file entries according to these rules.
  Rule format: item = path,
  where:
  'item' - relative sort order,
  'path' - regular expression to match against 'projectDescription/buildSpec/buildCommand/name', 
  """ )
  @Parameter(
    property     = "scalor.eclipseProjectBuildOrdering",
    defaultValue = """
    01 = org.scala-ide.sdt.core.scalabuilder ;
    02 = org.eclipse.m2e.core.maven2Builder ;
    """
  )
  var eclipseProjectBuildOrdering : String = _

  @Description( """
  Enable enforcement for .project 'projectDescription/name'.
  """ )
  @Parameter(
    property     = "scalor.eclipseProjectNameApply",
    defaultValue = "true"
  )
  var eclipseProjectNameApply : Boolean = _

  @Description( """
  Derive Eclipse project name from maven variables.
  """ )
  @Parameter(
    property     = "scalor.eclipseProjectNamePattern",
    defaultValue = "${project.artifactId}"
  )
  var eclipseProjectNamePattern : String = _

  @Description( """
  Enable create/delete for .project 'projectDescription/buildSpec' entries.
  """ )
  @Parameter(
    property     = "scalor.eclipseProjectBuildApply",
    defaultValue = "true"
  )
  var eclipseProjectBuildApply : Boolean = _

  @Description( """
  Append these Eclipse builders into the .project 'projectDescription/buildSpec' section.
  """ )
  @Parameter(
    property     = "scalor.eclipseProjectBuildCreate",
    defaultValue = "org.scala-ide.sdt.core.scalabuilder,org.eclipse.m2e.core.maven2Builder"
  )
  var eclipseProjectBuildCreate : Array[ String ] = _

  @Description( """
  Remove these Eclipse builders from the .project 'projectDescription/buildSpec' section.
  """ )
  @Parameter(
    property     = "scalor.eclipseProjectBuildDelete",
    defaultValue = "org.eclipse.jdt.core.javabuilder"
  )
  var eclipseProjectBuildDelete : Array[ String ] = _

  @Description( """
  Enable create/delete for .project 'projectDescription/natures/nature' entries.
  """ )
  @Parameter(
    property     = "scalor.eclipseProjectNatureApply",
    defaultValue = "true"
  )
  var eclipseProjectNatureApply : Boolean = _

  @Description( """
  Append these Eclipse natures into .project 'projectDescription/natures' section.
  """ )
  @Parameter(
    property     = "scalor.eclipseProjectNatureCreate",
    defaultValue = "org.eclipse.jdt.core.javanature,org.scala-ide.sdt.core.scalanature,org.eclipse.m2e.core.maven2Nature"
  )
  var eclipseProjectNatureCreate : Array[ String ] = _

  @Description( """
  Remove these Eclipse natures from .project 'projectDescription/natures' section.
  """ )
  @Parameter(
    property     = "scalor.eclipseProjectNatureDelete",
    defaultValue = "" // empty
  )
  var eclipseProjectNatureDelete : Array[ String ] = _

  @Description( """
  Re-order Eclipse .project file by 'projectDescription/natures/nature' .
  """ )
  @Parameter(
    property     = "scalor.eclipseProjectNatureReorder",
    defaultValue = "true"
  )
  var eclipseProjectNatureReorder : Boolean = _

  @Description( """
  Provide Eclipse .project file when missing.
  """ )
  @Parameter(
    property     = "scalor.eclipseProjectProvide",
    defaultValue = "true"
  )
  var eclipseProjectProvide : Boolean = _

  @Description( """
  Enable processing of Eclipse .project file.
  """ )
  @Parameter(
    property     = "scalor.eclipseProjectProcess",
    defaultValue = "true"
  )
  var eclipseProjectProcess : Boolean = _

  @Description( """
  Scalor plugin classpath resource providing Eclipse .project file template.
  """ )
  @Parameter(
    property     = "scalor.eclipseProjectTemplate",
    defaultValue = "META-INF/template/.project"
  )
  var eclipseProjectTemplate : String = _

  /**
   *
   */
  def eclipseProjectCanonical = eclipseProjectFile.getCanonicalFile

  /**
   *
   */
  def eclipseHasProject = eclipseProjectCanonical.exists

  /**
   *
   */
  def eclipseProvideProject = {
    provideResource( eclipseProjectTemplate, eclipseProjectCanonical )
  }

}

object ProjectDesign {

}
