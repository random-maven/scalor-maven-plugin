package com.carrotgarden.maven.scalor.util

import org.junit.jupiter.api._
import org.junit.jupiter.api.Assertions._
import org.junit.runner.RunWith
import org.junit.platform.runner.JUnitPlatform
import java.io.File
import com.carrotgarden.maven.scalor.base.BuildAnyRegex

@RunWith( classOf[ JUnitPlatform ] )
class FolderTest extends AnyRef {

  import Folder._
  import FolderTest._

  val fileCaseList = Seq(
    FileCase( "Any.java", true, "^.+[.]java$", null ),
    FileCase( "Any.scala", true, "^.+[.]scala$", null ),
    FileCase( "some/path/Any.java", true, ".+[.]java", null ),
    FileCase( "some/path/Any.scala", true, ".+[.]scala", null ),
    FileCase( "some/path/Any.java", false, ".+[.]Java", null ),
    FileCase( "some/path/Any.scala", false, ".+[.]Scala", null ),
    FileCase( "some/include/Any.scala", true, "^.+[.]scala", "include" ),
    FileCase( "some/exclude/Any.scala", false, "^.+[.]scala", ".+exclude.+" ),
    FileCase( "some/include/Any.any", true, null, "include" ),
    FileCase( "some/exclude/Any.any", false, null, ".+exclude.+" ),
    FileCase( "", true, null, null )
  )

  @Test
  def verifyFileHasMatch : Unit = {
    fileCaseList.foreach { entry =>
      import entry._
      assertTrue( result == fileHasMatch( path, includeOption, excludeOption ), entry.toString )

    }
  }

  @Test
  def verifyProjectFiles : Unit = {
    val rootList = Array( new File( "./src/main/scala" ) )
    val fileList = fileListByRegex( rootList, Some( ".+EclipsePlugin[.]scala" ), None )
    assertEquals( fileList.length, 1, "Missing EclipsePlugin" )
    fileList.foreach { file =>
      // println( s"XXX ${file}" )
    }
  }

  @Test
  def verifyBuildRegex : Unit = {
    val rootList = Array(
      new File( "./src/main/java" ),
      new File( "./src/main/scala" )
    )
    val fileList = RegexModule(
      buildRegexJavaInclude  = ".+[.]java",
      buildRegexJavaExclude  = ".+StaticLoggerBinder.+",
      buildRegexScalaInclude = ".+[.]scala",
      buildRegexScalaExclude = ".+EclipsePlugin.+"
    ).buildSourceList( rootList )
    fileList.foreach { file =>
      //      println( s"XXX ${file}" )
    }
    def hasFile( regex : String ) = {
      fileList.find { case file => file.getAbsolutePath.matches( regex ) }.isDefined
    }
    assertTrue( hasFile( ".+ManageOS.java" ) )
    assertFalse( hasFile( ".+StaticLoggerBinder.java" ) )
    assertTrue( hasFile( ".+ScalaNativeLinkMojo.scala" ) )
    assertFalse( hasFile( ".+EclipsePlugin.scala" ) )
  }

}

object FolderTest {

  case class FileCase( path : String, result : Boolean, include : String, exclude : String ) {

    lazy val includeOption = Option( include ).map( _.r.pattern.matcher( "" ) )

    lazy val excludeOption = Option( exclude ).map( _.r.pattern.matcher( "" ) )

  }

  case class RegexModule(
    buildRegexJavaInclude :  String = null,
    buildRegexJavaExclude :  String = null,
    buildRegexScalaInclude : String = null,
    buildRegexScalaExclude : String = null
  ) extends BuildAnyRegex

}
