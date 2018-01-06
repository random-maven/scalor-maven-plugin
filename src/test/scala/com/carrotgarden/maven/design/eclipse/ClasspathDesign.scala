package com.carrotgarden.maven.design.eclipse

import xml._
import xml.transform._
import org.apache.maven.plugins.annotations._
import java.io.File
import com.carrotgarden.maven.scalor._
import com.carrotgarden.maven.tools.Description
import com.carrotgarden.maven.scalor.base

trait ClasspathDesign extends base.ParamsAny {

  import ClasspathDesign._
  import util.Folder._
  import util.Params._

  @Description( """
  Location of Eclipse .classpath configuration file.
  """ )
  @Parameter(
    property     = "scalor.eclipseClasspathFile",
    defaultValue = "${project.basedir}/.classpath"
  )
  var eclipseClasspathFile : File = _

  @Description( """
  Re-order Eclipse .classpath file by 'classpath/classpathentry/@path' .
  """ )
  @Parameter(
    property     = "scalor.eclipseClasspathReorder",
    defaultValue = "true"
  )
  var eclipseClasspathReorder : Boolean = _

  @Description( """
  Order Eclipse .classpath file entries according to these rules.
  Rule format: item = path,
  where:
  'item' - relative sort order,
  'path' - regular expression to match against 'classpath/classpathentry/@path', 
  """ )
  @Parameter(
    property     = "scalor.eclipseClasspathOrder",
    defaultValue = """
    ;
    01 = src/macro/java ;
    02 = src/macro/scala ;
    03 = src/macro/groovy ;
    04 = src/macro/resources ;
    ;
    11 = src/main/java ;
    12 = src/main/scala ;
    13 = src/main/groovy ;
    14 = src/main/resources ;
    ;
    21 = src/test/java ;
    22 = src/test/scala ;
    23 = src/test/groovy ;
    24 = src/test/resources ;
    ;
    31 = org[.]eclipse[.]jdt[.].+ ;
    32 = org[.]scala-ide[.]sdt[.].+ ;
    33 = org[.]eclipse[.]m2e[.].+ ;
    ;
    41 = target/classes ;
    ;
    """
  )
  var eclipseClasspathOrder : String = _

  @Description( """
  Scalor plugin classpath resource providing Eclipse .classpath file template.
  Used to provide project .classpath when missing.
  """ )
  @Parameter(
    property     = "scalor.eclipseClasspathTemplate",
    defaultValue = "META-INF/template/.classpath"
  )
  var eclipseClasspathTemplate : String = _

  @Description( """
  Provide default Eclipse .classpath file when missing.
  """ )
  @Parameter(
    property     = "scalor.eclipseClasspathProvide",
    defaultValue = "true"
  )
  var eclipseClasspathProvide : Boolean = _

  @Description( """
  Enable create/delete for .classpath 'classpath/classpathentry/@kind=src' entries.
  """ )
  @Parameter(
    property     = "scalor.eclipseClasspathRootsApply",
    defaultValue = "true"
  )
  var eclipseClasspathRootsApply : Boolean = _

}

object ClasspathDesign {

  def classpathSrcEntry( output : String, path : String ) = {
    <classpathentry kind="src" output={ output } path={ path }>
      <attributes>
        <attribute name="optional" value="true"/>
        <attribute name="maven.pomderived" value="true"/>
      </attributes>
    </classpathentry>
  }

  def classpathConEntry( path : String ) = {
    <classpathentry kind="con" path={ path }>
      <attributes>
        <attribute name="optional" value="true"/>
        <attribute name="maven.pomderived" value="true"/>
      </attributes>
    </classpathentry>
  }

}
