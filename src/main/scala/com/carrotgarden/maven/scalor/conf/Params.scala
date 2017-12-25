package com.carrotgarden.maven.scalor.conf

import org.apache.maven.plugins.annotations._

import com.carrotgarden.maven.tools.Description

/**
 * Plugin parameters available via mojo=auto-conf.
 */
trait Params extends AnyRef
  with ParamsStamp
  with ParamsRoots
  with ParamsEncodins
  with ParamsPlugins
  with ParamsVersion {

  @Description( """
  Report properties injected in maven project.
  """ )
  @Parameter(
    property     = "scalor.autoconfLogProperties",
    defaultValue = "false"
  )
  var autoconfLogProperties : Boolean = _

}

trait ParamsStamp {

  @Description( """
  Name of maven project property set to current time.
  """ )
  @Parameter(
    defaultValue = "scalor.autoconfBuildStamp"
  )
  var autoconfBuildStamp : String = _

  @Description( """
  java.text.SimpleDateFormat expression for [autoconfBuildStamp].
  """ )
  @Parameter(
    property     = "scalor.autoconfStampFormat",
    defaultValue = "yyyy-MM-dd_HH-mm-ss"
  )
  var autoconfStampFormat : String = _

}

trait ParamsEncodins {

  @Description( """
  Name of maven project property set to settings fragment 
  of resource encodings for Eclipse scope=macro.
  """ )
  @Parameter(
    defaultValue = "scalor.autoconfEclipseEncodinsMacro"
  )
  var autoconfEclipseEncodinsMacro : String = _

  @Description( """
  Name of maven project property set to settings fragment 
  of resource encodings for Eclipse scope=main.
  """ )
  @Parameter(
    defaultValue = "scalor.autoconfEclipseEncodinsMain"
  )
  var autoconfEclipseEncodinsMain : String = _

  @Description( """
  Name of maven project property set to settings fragment 
  of resource encodings for Eclipse scope=test.
  """ )
  @Parameter(
    defaultValue = "scalor.autoconfEclipseEncodinsTest"
  )
  var autoconfEclipseEncodinsTest : String = _

}

trait ParamsRoots {

  @Description( """
  Name of maven project property set to settings fragment 
  of compilation roots for Eclipse scope=macro.
  """ )
  @Parameter(
    defaultValue = "scalor.autoconfEclipseRootsMacro"
  )
  var autoconfEclipseRootsMacro : String = _

  @Description( """
  Name of maven project property set to settings fragment 
  of compilation roots for Eclipse scope=main.
  """ )
  @Parameter(
    defaultValue = "scalor.autoconfEclipseRootsMain"
  )
  var autoconfEclipseRootsMain : String = _

  @Description( """
  Name of maven project property set to settings fragment 
  of compilation roots for Eclipse scope=test.
  """ )
  @Parameter(
    defaultValue = "scalor.autoconfEclipseRootsTest"
  )
  var autoconfEclipseRootsTest : String = _

}

trait ParamsPlugins {

  @Description( """
  Name of maven property set to list of discovered compiler plugin absolute jar paths.
  Discovery happens by looking through scalor plugin dependencies.
  The list is comma-separated and can be empty.
  """ )
  @Parameter(
    defaultValue = "scalor.autoconfCompilerPluginList"
  )
  var autoconfCompilerPluginList : String = _

}

trait ParamsVersion {

  @Description( """
  Name of maven project property set to discovered Scala epoch version, such as 2.12.
  Discovery happens by looking through project dependencies.
  """ )
  @Parameter(
    defaultValue = "scalor.autoconfScalaVersionEpoch"
  )
  var autoconfScalaVersionEpoch : String = _

  @Description( """
  Name of maven project property set to discovered Scala release version, such as 2.12.4.
  Discovery happens by looking through project dependencies.
  """ )
  @Parameter(
    defaultValue = "scalor.autoconfScalaVersionRelease"
  )
  var autoconfScalaVersionRelease : String = _

}
