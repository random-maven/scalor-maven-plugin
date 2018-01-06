package com.carrotgarden.maven.design.eclipse

import org.apache.maven.plugins.annotations._
import java.io.File
import com.carrotgarden.maven.scalor._
import com.carrotgarden.maven.tools.Description

trait ConfigDesign extends AnyRef
  with ConfigKryo
  with ConfigInstall

trait ConfigKryo {

  @Description( """
  """ )
  @Parameter(
    property     = "scalor.eclipseConfigLogKryo",
    defaultValue = "false"
  )
  var eclipseConfigLogKryo : Boolean = _

  @Description( """
  """ )
  @Parameter(
    property     = "scalor.eclipseConfigKryoLogFolder",
    defaultValue = "${project.basedir}/chiller.log"
  )
  var eclipseConfigKryoLogFile : File = _

  @Description( """
    LEVEL_NONE = 6;
    LEVEL_ERROR = 5;
    LEVEL_WARN = 4;
    LEVEL_INFO = 3;
    LEVEL_DEBUG = 2;
    LEVEL_TRACE = 1;
  """ )
  @Parameter(
    property     = "scalor.eclipseConfigKryoLogLevel",
    defaultValue = "1"
  )
  var eclipseConfigKryoLogLevel : Int = _

}

trait ConfigInstall {

  @Description( """
  """ )
  @Parameter(
    property     = "scalor.eclipseConfigEnsureInstall",
    defaultValue = "true"
  )
  var eclipseConfigEnsureInstall : Boolean = _

  @Description( """
  """ )
  @Parameter(
    property     = "scalor.eclipseConfigLogInstallExtract",
    defaultValue = "false"
  )
  var eclipseConfigLogInstallExtract : Boolean = _

  @Description( """
  """ )
  @Parameter(
    property     = "scalor.eclipseConfigLogInstallPersist",
    defaultValue = "false"
  )
  var eclipseConfigLogInstallPersist : Boolean = _

//  @Description( """
//  """ )
//  @Parameter(
//    property     = "scalor.eclipseConfigInstallLabel",
//    defaultValue = "Scalor: version=<InstallVersion> identity=<InstallIdentity>"
//  )
//  var eclipseConfigInstallLabel : String = _

  @Description( """
  """ )
  @Parameter(
    property     = "scalor.eclipseConfigRemoveInvalid",
    defaultValue = "true"
  )
  var eclipseConfigRemoveInvalid : Boolean = _

  @Description( """
  """ )
  @Parameter(
    property     = "scalor.eclipseConfigLogInstallResolve",
    defaultValue = "false"
  )
  var eclipseConfigLogInstallResolve : Boolean = _

}
