package com.carrotgarden.maven.scalor.format

import org.apache.maven.plugins.annotations._
import com.carrotgarden.maven.tools.Description

import com.carrotgarden.maven.scalor.base
import com.carrotgarden.maven.scalor.zinc
import java.io.File

trait ParamsAny extends AnyRef
  with base.BuildAnySources
  with ParamsRegex
  with ParamsLogging
  with ParamsSettings {

}

trait ParamsRegex extends base.BuildAnyRegex {

  @Description( """
  Regular expression for Java source file discovery via inclusion by match against absolute path.
  File match is defined as: <code>include.hasMatch && ! exclude.hasMatch</code>.
  Matches files with <code>java</code> extension by default.
<pre>
  """ )
  @Parameter(
    property     = "scalor.formatRegexJavaInclude",
    defaultValue = """.+[.]java"""
  )
  var formatRegexJavaInclude : String = _

  @Description( """
  Regular expression for Java source file discovery via exclusion by match against absolute path.
  File match is defined as: <code>include.hasMatch && ! exclude.hasMatch</code>.
  Matches no files when empty by default.
  """ )
  @Parameter(
    property = "scalor.formatRegexJavaExclude"
  )
  var formatRegexJavaExclude : String = _

  @Description( """
  Regular expression for Scala source file discovery via inclusion by match against absolute path.
  File match is defined as: <code>include.hasMatch && ! exclude.hasMatch</code>.
  Matches files with <code>scala</code> extension by default.
  """ )
  @Parameter(
    property     = "scalor.formatRegexScalaInclude",
    defaultValue = """.+[.]scala"""
  )
  var formatRegexScalaInclude : String = _

  @Description( """
  Regular expression for Scala source file discovery via exclusion by match against absolute path.
  File match is defined as: <code>include.hasMatch && ! exclude.hasMatch</code>.
  Matches no files when empty by default.
  """ )
  @Parameter(
    property = "scalor.formatRegexScalaExclude"
  )
  var formatRegexScalaExclude : String = _

  override def buildRegexJavaInclude = formatRegexJavaInclude
  override def buildRegexJavaExclude = formatRegexJavaExclude
  override def buildRegexScalaInclude = formatRegexScalaInclude
  override def buildRegexScalaExclude = formatRegexScalaExclude

}

trait ParamsLogging {

  @Description( """
  Enable logging of formatted changed files.
  """ )
  @Parameter(
    property     = "scalor.formatLogChanged",
    defaultValue = "true"
  )
  var formatLogChanged : Boolean = _

  @Description( """
  Enable logging of successful (both changed and not changed) files.
  """ )
  @Parameter(
    property     = "scalor.formatLogSuccess",
    defaultValue = "false"
  )
  var formatLogSuccess : Boolean = _

  @Description( """
  Enable logging of files with formatting failures.
  """ )
  @Parameter(
    property     = "scalor.formatLogFailure",
    defaultValue = "true"
  )
  var formatLogFailure : Boolean = _

  @Description( """
  Enable logging of formatting failures stack traces.
  """ )
  @Parameter(
    property     = "scalor.formatLogTraces",
    defaultValue = "false"
  )
  var formatLogTraces : Boolean = _

  @Description( """
  Enable logging of format invocation statistics:
  number of files in each category:
  <code>total, changed, success, failure</code>.
  """ )
  @Parameter(
    property     = "scalor.formatLogTotal",
    defaultValue = "true"
  )
  var formatLogTotal : Boolean = _

}

trait ParamsSettings {

  @Description( """
  Enable format configuration file lookup in parents of multi-module projects.
  In each project canditate, plugin will check for existence of relevant files: 
  <a href="#formatJavaSettings"><b>formatJavaSettings</b></a>,  
  <a href="#formatScalaSettings"><b>formatScalaSettings</b></a>.  
  """ )
  @Parameter(
    property     = "scalor.formatParentLookup",
    defaultValue = "true"
  )
  var formatParentLookup : Boolean = _

  @Description( """
  Java formatter enablement parameter.
  Enable formatter invocation in Maven.
  Enable formatter settings transfer in Eclipse.
  """ )
  @Parameter(
    property     = "scalor.formatJavaEnable",
    defaultValue = "true"
  )
  var formatJavaEnable : Boolean = _

  @Description( """
  Java formatter (org.eclipse.jdt.core) configuration file. 
  Relative path to <code>${project.basedir}</code>.
  Settings <a href="https://help.eclipse.org/oxygen/index.jsp?topic=%2Forg.eclipse.jdt.doc.user%2Freference%2Fpreferences%2Fjava%2Fcodestyle%2Fref-preferences-formatter.htm">reference</a>.
  Normally located in Maven project configuration folder <code>.mvn</code>.
  Applied to Eclipse JDT with goal=eclipse-format.
  """ )
  @Parameter(
    property     = "scalor.formatJavaSettings",
    defaultValue = ".mvn/format-java.xml"
  )
  var formatJavaSettings : String = _

  @Description( """
  Scala formatter enablement parameter. 
  Enable formatter invocation in Maven.
  Enable formatter settings transfer in Eclipse.
  """ )
  @Parameter(
    property     = "scalor.formatScalaEnable",
    defaultValue = "true"
  )
  var formatScalaEnable : Boolean = _

  @Description( """
  Scala formatter (scala-ide/scalariform) configuration file. 
  Relative path to <code>${project.basedir}</code>.
  Settings <a href="https://github.com/scala-ide/scalariform">reference</a>.
  Normally located in Maven project configuration folder <code>.mvn</code>.
  Applied to Scala IDE with goal=eclipse-format.
  """ )
  @Parameter(
    property     = "scalor.formatScalaSettings",
    defaultValue = ".mvn/format-scala.props"
  )
  var formatScalaSettings : String = _

  @Description( """
  Charset for source file reading and writing.
  """ )
  @Parameter(
    property     = "scalor.formatSourceEncoding",
    defaultValue = "UTF-8"
  )
  var formatSourceEncoding : String = _

}

trait ParamsBuildMacro extends ParamsAny
  with base.BuildMacroSources {

}

trait ParamsBuildMain extends ParamsAny
  with base.BuildMainSources {

}

trait ParamsBuildTest extends ParamsAny
  with base.BuildTestSources {

}
