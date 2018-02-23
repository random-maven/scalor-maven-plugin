package com.carrotgarden.maven.scalor.format

import java.io.File
import java.nio.charset.Charset

import com.carrotgarden.maven.scalor.base
import com.carrotgarden.maven.scalor.util

import scala.collection.JavaConverters._
import org.apache.maven.project.MavenProject
import com.carrotgarden.maven.scalor.meta.Macro
import com.carrotgarden.maven.scalor.util.Maven
import com.carrotgarden.maven.scalor.util.Error.Throw

import scalariform.formatter.ScalaFormatter
import scalariform.formatter.preferences.IFormattingPreferences
import scalariform.formatter.preferences.PreferencesImporterExporter

import org.eclipse.jdt.core.ToolFactory
import org.eclipse.jdt.core.formatter.CodeFormatter
import org.eclipse.jface.text.Document

/**
 * Format operations.
 */
trait Format extends AnyRef
  with FormatJava
  with FormatScala {

}

/**
 * Shared format invocation.
 */
trait FormatAny {

  import Format._
  import com.carrotgarden.maven.scalor.util.Text

  /**
   * Shared format invocation.
   */
  def formatProcess(
    file :      File,
    charset :   Charset,
    formatFun : FormatFun
  ) : Result = {
    try {
      val source = Text.textRead( file, charset )
      val target = formatFun( source )
      if ( source == target ) {
        Result( file, false )
      } else {
        Text.textWrite( target, file, charset )
        Result( file, true )
      }
    } catch {
      case error : Throwable =>
        Result( file, false, Some( error ) )
    }
  }

}

object Format {

  /**
   * Shared format invocation.
   */
  type FormatFun = String => String

  /**
   * Format invocation result.
   */
  case class Result(
    file :      File,
    hasChange : Boolean,
    error :     Option[ Throwable ] = None
  ) {
    def path = file.getCanonicalPath
    def hasSuccess = !hasFailure
    def hasFailure = error.isDefined
  }

  /**
   * Load Eclipse Java formatter XML file.
   */
  def formatJavaOptions( file : File ) : java.util.Map[ String, String ] = {
    import scala.xml.XML
    val xml = XML.loadFile( file.getCanonicalPath )
    val profiles = xml \\ "profiles"
    require( profiles.length == 1, s"Expecting single profile: ${file}" )
    val profile = profiles( 0 )
    val settings = profile \\ "setting"
    val options = settings.map { setting =>
      val key = setting \@ "id"
      val value = setting \@ "value"
      ( key -> value )
    }.toMap.asJava
    options
  }

  /**
   * Load Eclipse Java formatter XML file.
   */
  def formatJavaContext( file : File ) : CodeFormatter = {
    val options = formatJavaOptions( file )
    ToolFactory.createCodeFormatter( options )
  }

  /**
   * Find Eclipse Java formatter XML file.
   */
  def formatJavaSettingsFile( project : MavenProject, format : ParamsSettings ) : File = {
    import format._
    Maven.locateHierarchyFile(
      project, formatJavaSettings, formatParentLookup
    ).getOrElse(
      Throw( s"Missing configuration: ${Macro.nameOf( formatJavaSettings )}=${formatJavaSettings}" )
    )
  }

  /**
   * Load Scalariform Scala formatter properties file.
   */
  def formatScalaContext( file : File ) : IFormattingPreferences = {
    PreferencesImporterExporter.loadPreferences( file.getCanonicalPath )
  }

  /**
   * Find Scalariform Scala formatter properties file.
   */
  def formatScalaSettingsFile( project : MavenProject, format : ParamsSettings ) : File = {
    import format._
    Maven.locateHierarchyFile(
      project, formatScalaSettings, formatParentLookup
    ).getOrElse(
      Throw( s"Missing configuration: ${Macro.nameOf( formatScalaSettings )}=${formatScalaSettings}" )
    )
  }

}

/**
 * Java source format.
 */
trait FormatJava extends FormatAny {

  import Format._

  /**
   * Java source format.
   */
  def formatJavaList(
    fileList : Array[ File ],
    charset :  Charset,
    context :  CodeFormatter
  ) : Array[ Result ] = {
    fileList.map { file =>
      formatJavaFile( file, charset, context )
    }
  }

  /**
   * Java source format.
   */
  def formatJavaFile(
    file :    File,
    charset : Charset,
    context : CodeFormatter
  ) : Result = {
    formatProcess( file, charset, formatJavaText( context ) )
  }

  /**
   * Java source format.
   */
  def formatJavaText( context : CodeFormatter )( source : String ) : String = {
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=528085
    val kind = CodeFormatter.K_COMPILATION_UNIT | CodeFormatter.F_INCLUDE_COMMENTS
    val offset = 0
    val length = source.length
    val indentationLevel = 0
    val lineSeparator = null
    val textEdit = context.format( kind, source, offset, length, indentationLevel, lineSeparator )
    val document = new Document( source )
    textEdit.apply( document )
    document.get
  }

}

/**
 * Scala source format.
 */
trait FormatScala extends FormatAny {

  import Format._

  /**
   * Scala source format.
   */
  def formatScalaList(
    fileList : Array[ File ],
    charset :  Charset,
    context :  IFormattingPreferences
  ) : Array[ Result ] = {
    fileList.map { file =>
      formatScalaFile( file, charset, context )
    }
  }

  /**
   * Scala source format.
   */
  def formatScalaFile(
    file :    File,
    charset : Charset,
    context : IFormattingPreferences
  ) : Result = {
    formatProcess( file, charset, formatScalaText( context ) )
  }

  /**
   * Scala source format.
   */
  def formatScalaText( context : IFormattingPreferences )( source : String ) : String = {
    ScalaFormatter.format( source, context )
  }

}
