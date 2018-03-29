package com.carrotgarden.maven.scalor.scalanative

import java.io.File

import org.apache.maven.plugins.annotations.Parameter

import com.carrotgarden.maven.scalor.base
import com.carrotgarden.maven.tools.Description
import com.carrotgarden.maven.scalor.util.Folder
import com.carrotgarden.maven.scalor.util.Error.Throw

trait Build extends AnyRef
  with base.BuildAnyTarget
  with base.BuildAnyDependency
  with BuildAnyMode
  with BuildAnyCdata
  with BuildAnyClang
  with BuildAnyRuntime {

}

trait BuildAnyCdata {

  @Description( """
  Enable to create C data resource root folders when missing.
  """ )
  @Parameter(
    property     = "scalor.nativeEnsureCdataFolders",
    defaultValue = "true"
  )
  var nativeEnsureCdataFolders : Boolean = _

  @Description( """
  Enable to use a single zip archive for all C data resources.
  Insead of embedding individual C data resouces files, put them in a zip archive and then apply objcopy.
  """ )
  @Parameter(
    property     = "scalor.nativeCdataZipEnable",
    defaultValue = "true"
  )
  var nativeCdataZipEnable : Boolean = _

  @Description( """
  Name of the C data resource file used for embedding by objcopy.
  <br/>
  Archive zip file is created with these steps:
<ol>
  <li>iterate all <code>src/main/cdata</code> folders</li>
  <li>copy content into a temp directory</li>
  <li>overwrite conflicting files when present</li>
  <li>create final cdata.zip archive</li>
  <li>convert cdata.zip into cdata.zip.o</li>
</ol>
  Resulting objcopy <code>cdata.zip.o</code> used for Scala.native linking can be accessed as follows:
  <br/>
  for example, in file <code>src/main/clang/main.c</code>
<pre>
  // objcopy naming convention 
  extern char _binary_cdata_zip_start 
  extern char _binary_cdata_zip_end
  // provide forwarders for scala @extern
  char* binary_cdata_zip_start() { return & _binary_cdata_zip_start; }
  char* binary_cdata_zip_end()   { return & _binary_cdata_zip_end; }
</pre> 
  and in file <code>src/main/scala/Main.scala</code>
<pre>
  @extern
  object cdata { // access compressed zip archive
    def binary_cdata_zip_start : Ptr[ CChar ] = extern
    def binary_cdata_zip_end   : Ptr[ CChar ] = extern
  }
</pre> 
  Enablement parameter: <a href="#nativeCdataZipEnable"><b>nativeCdataZipEnable</b></a>.
  """ )
  @Parameter(
    property     = "scalor.nativeCdataZipFileName",
    defaultValue = "cdata.zip"
  )
  var nativeCdataZipFileName : String = _

  def nativeCdataEnable : Boolean

  def nativeCdataFolders : Array[ File ]

}

trait BuildAnyClang {

  @Description( """
  Enable to create C/CPP source root folders when missing.
  """ )
  @Parameter(
    property     = "scalor.nativeEnsureClangFolders",
    defaultValue = "true"
  )
  var nativeEnsureClangFolders : Boolean = _

  def nativeClangEnable : Boolean

  def nativeClangFolders : Array[ File ]

}

trait BuildAnyMode {

  /**
   * Build mode for runtime release binary.
   */
  def nativeModeBuildRelease : String

  /**
   * Build mode for runtime debug binary.
   */
  def nativeModeBuildDebug : String

  def nativeBuildModeRelease : base.Mode.Type = {
    base.Mode.buildMode( nativeModeBuildRelease )
  }

  def nativeBuildModeDebug : base.Mode.Type = {
    base.Mode.buildMode( nativeModeBuildDebug )
  }

  def nativeHasBuildRelease( incremental : Boolean ) : Boolean = {
    base.Mode.hasBuildEnabled( nativeBuildModeRelease, incremental )
  }

  def nativeHasBuildDebug( incremental : Boolean ) : Boolean = {
    base.Mode.hasBuildEnabled( nativeBuildModeDebug, incremental )
  }

}

trait BuildAnyRuntime extends base.BuildAnyTarget {

  /**
   * Relative path of the generated runtime binary.
   */
  def nativeRuntimeDebug : String

  /**
   * Relative path of the generated runtime binary.
   */
  def nativeRuntimeRelease : String

  /**
   * Absolute path of the generated runtime binary.
   */
  def nativeRuntimeDebugFile : File = {
    val file = new File( buildTargetFolder, nativeRuntimeDebug ).getCanonicalFile
    Folder.ensureParent( file )
    file
  }

  /**
   * Absolute path of the generated runtime binary.
   */
  def nativeRuntimeReleaseFile : File = {
    val file = new File( buildTargetFolder, nativeRuntimeRelease ).getCanonicalFile
    Folder.ensureParent( file )
    file
  }

}

/**
 * Scala.native linker build parameters for scope=main.
 */
trait BuildMain extends Build
  with BuildMainCdata
  with BuildMainClang
  with BuildMainDependency
  with BuildMainTarget
  with BuildMainMode
  with BuildMainRuntime {

}

trait BuildMainCdata extends AnyRef
  with BuildAnyCdata {

  @Description( """
  Enable to convert project-provided C data resources in scope=main.
  Provides a way to embed <code>objcopy</code> resources in binary runtime. 
  Basic <a href="http://www.linuxjournal.com/content/embedding-file-executable-aka-hello-world-version-5967">explanation</a>.
  """ )
  @Parameter(
    property     = "scalor.nativeMainCdataEnable",
    defaultValue = "true"
  )
  var nativeMainCdataEnable : Boolean = _

  @Description( """
  C data resource root folders to be included in compilation scope=main.
  Normally uses <code>[src/main/cdata]</code>.
  Absolute path.
  Conversion parameter <a href="#nativeCdataZipEnable"><b>nativeCdataZipEnable</b></a>
  Enablement parameter <a href="#nativeMainCdataEnable"><b>nativeMainCdataEnable</b></a>
  """ )
  @Parameter(
    property     = "scalor.nativeMainCdataFolders",
    defaultValue = "${project.build.sourceDirectory}/../cdata"
  )
  var nativeMainCdataFolders : Array[ File ] = Array.empty

  override def nativeCdataEnable = nativeMainCdataEnable
  override def nativeCdataFolders = nativeMainCdataFolders

}

trait BuildMainClang extends AnyRef
  with BuildAnyClang {

  @Description( """
  Enable to compile project-provided C/CPP sources in scope=main.
  Provides a way to inject <code>#define</code> constant extractors, etc.
  """ )
  @Parameter(
    property     = "scalor.nativeMainClangEnable",
    defaultValue = "true"
  )
  var nativeMainClangEnable : Boolean = _

  @Description( """
  C/CPP source root folders to be included in compilation scope=main.
  Normally uses <code>[src/main/clang]</code>.
  Absolute path.
  Enablement parameter <a href="#nativeMainClangEnable"><b>nativeMainClangEnable</b></a>
  """ )
  @Parameter(
    property     = "scalor.nativeMainClangFolders",
    defaultValue = "${project.build.sourceDirectory}/../clang"
  )
  var nativeMainClangFolders : Array[ File ] = Array.empty

  override def nativeClangEnable = nativeMainClangEnable
  override def nativeClangFolders = nativeMainClangFolders

}

trait BuildMainDependency extends base.BuildAnyDependency {

  @Description( """
  Folders with classes generated by current project and included in linker class path.
  Normally includes build output from scope=[macro,main]
  (<code>target/classes</code>).
  """ )
  @Parameter(
    property     = "scalor.nativeMainDependencyFolders",
    defaultValue = "${project.build.outputDirectory}"
  )
  var nativeMainDependencyFolders : Array[ File ] = Array.empty

  @Description( """
  Provide linker class path from project dependency artifacts based on these scopes.
  Scopes <a href="https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html">reference</a>.
  """ )
  @Parameter(
    property     = "scalor.nativeMainDependencyScopes",
    defaultValue = "provided"
  )
  var nativeMainDependencyScopes : Array[ String ] = Array.empty

  override def buildDependencyFolders = nativeMainDependencyFolders
  override def buildDependencyScopes = nativeMainDependencyScopes

}

trait BuildMainTarget extends base.BuildAnyTarget {

  @Description( """
  Build target directory for the generated runtime binary file with scope=main.
  """ )
  @Parameter(
    property     = "scalor.nativeMainTargetFolder",
    defaultValue = "${project.build.directory}/scalor/native/output/main"
  )
  var nativeMainTargetFolder : File = _

  override def buildTargetFolder = nativeMainTargetFolder

}

trait BuildMainMode extends BuildAnyMode {

  @Description( """
  Build mode for optimized/stripped <a href="#nativeMainRuntimeRelease"><b>nativeMainRuntimeRelease</b></a>.
  Normally uses <code>full</code>, to link only during Maven full build and skip Eclipse incremental build.
  Available build modes:
<pre>
  always - link during both full and incremental build
  never  - do not produce runtime at all
  full   - link only during full build
  incr   - link only during incremental build
</pre>
  """ )
  @Parameter(
    property     = "scalor.nativeMainBuildModeRelease",
    defaultValue = "full"
  )
  var nativeMainBuildModeRelease : String = _

  @Description( """
  Build mode for non-optimized <a href="#nativeMainRuntimeDebug"><b>nativeMainRuntimeDebug</b></a>.
  Normally uses <code>always</code>, to link during both Maven full build and Eclipse incremental build.
  Available build modes:
<pre>
  always - link during both full and incremental build
  never  - do not produce runtime at all
  full   - link only during full build
  incr   - link only during incremental build
</pre>
  """ )
  @Parameter(
    property     = "scalor.nativeMainBuildModeDebug",
    defaultValue = "always"
  )
  var nativeMainBuildModeDebug : String = _

  override def nativeModeBuildRelease = nativeMainBuildModeRelease
  override def nativeModeBuildDebug = nativeMainBuildModeDebug

}

trait BuildMainRuntime extends BuildAnyRuntime {

  @Description( """
  Relative path of the generated runtime binary file for scope=main, mode=release.
  File is packaged inside <a href="#nativeMainTargetFolder"><b>nativeMainTargetFolder</b></a>
  """ )
  @Parameter(
    property     = "scalor.nativeMainRuntimeRelease",
    defaultValue = "release"
  )
  var nativeMainRuntimeRelease : String = _

  @Description( """
  Relative path of the generated runtime binary file for scope=main, mode=debug.
  File is packaged inside <a href="#nativeMainTargetFolder"><b>nativeMainTargetFolder</b></a>
  """ )
  @Parameter(
    property     = "scalor.nativeMainRuntimeDebug",
    defaultValue = "debug"
  )
  var nativeMainRuntimeDebug : String = _

  override def nativeRuntimeRelease = nativeMainRuntimeRelease
  override def nativeRuntimeDebug = nativeMainRuntimeDebug

}

/**
 * Scala.native linker build parameters for scope=test.
 */
trait BuildTest extends Build
  with BuildTestCdata
  with BuildTestClang
  with BuildTestDependency
  with BuildTestTarget
  with BuildTestMode
  with BuildTestRuntime {

}

trait BuildTestCdata extends AnyRef
  with BuildAnyCdata {

  @Description( """
  Enable to convert project-provided C data resources in scope=test.
  Provides a way to embed <code>objcopy</code> resources in binary runtime. 
  Basic <a href="http://www.linuxjournal.com/content/embedding-file-executable-aka-hello-world-version-5967">explanation</a>.
  """ )
  @Parameter(
    property     = "scalor.nativeTestCdataEnable",
    defaultValue = "true"
  )
  var nativeTestCdataEnable : Boolean = _

  @Description( """
  C data resource root folders to be included in compilation scope=test.
  Provides a way to embed <code>objcopy</code> resources in binary runtime.
  Normally uses <code>[src/main/cdata,src/test/cdata]</code>.
  Absolute path.
  Conversion parameter <a href="#nativeCdataZipEnable"><b>nativeCdataZipEnable</b></a>
  Enablement parameter <a href="#nativeTestCdataEnable"><b>nativeTestCdataEnable</b></a>
  """ )
  @Parameter(
    property     = "scalor.nativeTestCdataFolders",
    defaultValue = "${project.build.sourceDirectory}/../cdata,${project.build.testSourceDirectory}/../cdata"
  )
  var nativeTestCdataFolders : Array[ File ] = Array.empty

  override def nativeCdataEnable = nativeTestCdataEnable
  override def nativeCdataFolders = nativeTestCdataFolders

}

trait BuildTestClang extends AnyRef
  with BuildAnyClang {

  @Description( """
  Enable to compile project-provided C/CPP sources in scope=test.
  Provides a way to inject <code>#define</code> constant extractors, etc.
  """ )
  @Parameter(
    property     = "scalor.nativeTestClangEnable",
    defaultValue = "true"
  )
  var nativeTestClangEnable : Boolean = _

  @Description( """
  C/CPP source root folders to be included in compilation scope=test.
  Normally uses <code>[src/main/clang,src/test/clang]</code>.
  Absolute path.
  Enablement parameter <a href="#nativeTestClangEnable"><b>nativeTestClangEnable</b></a>
  """ )
  @Parameter(
    property     = "scalor.nativeTestClangFolders",
    defaultValue = "${project.build.sourceDirectory}/../clang,${project.build.testSourceDirectory}/../clang"
  )
  var nativeTestClangFolders : Array[ File ] = Array.empty

  override def nativeClangEnable = nativeTestClangEnable
  override def nativeClangFolders = nativeTestClangFolders

}

trait BuildTestMode extends BuildAnyMode {

  @Description( """
  Build mode for optimized/stripped <a href="#nativeTestRuntimeRelease"><b>nativeTestRuntimeRelease</b></a>.
  Normally uses <code>full</code>, to link only during Maven full build and skip Eclipse incremental build.
  Available build modes:
<pre>
  always - link during both full and incremental build
  never  - do not produce runtime at all
  full   - link only during full build
  incr   - link only during incremental build
</pre>
  """ )
  @Parameter(
    property     = "scalor.nativeTestBuildModeRelease",
    defaultValue = "full"
  )
  var nativeTestBuildModeRelease : String = _

  @Description( """
  Build mode for non-optimized <a href="#nativeTestRuntimeDebug"><b>nativeTestRuntimeDebug</b></a>.
  Normally uses <code>always</code>, to link during both Maven full build and Eclipse incremental build.
  Available build modes:
<pre>
  always - link during both full and incremental build
  never  - do not produce runtime at all
  full   - link only during full build
  incr   - link only during incremental build
</pre>
  """ )
  @Parameter(
    property     = "scalor.nativeTestBuildModeDebug",
    defaultValue = "always"
  )
  var nativeTestBuildModeDebug : String = _

  override def nativeModeBuildRelease = nativeTestBuildModeRelease
  override def nativeModeBuildDebug = nativeTestBuildModeDebug

}

trait BuildTestDependency extends base.BuildAnyDependency {

  @Description( """
  Folders with classes generated by current project and included in linker class path.
  Normally includes build output from scope=[macro,main,test] 
  (<code>target/test-classes</code>, <code>target/classes</code>).
  """ )
  @Parameter(
    property     = "scalor.nativeTestDependencyFolders",
    defaultValue = "${project.build.testOutputDirectory},${project.build.outputDirectory}"
  )
  var nativeTestDependencyFolders : Array[ File ] = Array.empty

  @Description( """
  Provide linker class path from project dependencies selected by these scopes.
  Scopes <a href="https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html">reference</a>.
  """ )
  @Parameter(
    property     = "scalor.nativeTestDependencyScopes",
    defaultValue = "provided,test"
  )
  var nativeTestDependencyScopes : Array[ String ] = Array.empty

  override def buildDependencyFolders = nativeTestDependencyFolders
  override def buildDependencyScopes = nativeTestDependencyScopes

}

trait BuildTestTarget extends base.BuildAnyTarget {

  @Description( """
  Build target directory for the generated runtime binary file with scope=test.
  """ )
  @Parameter(
    property     = "scalor.nativeTestTargetFolder",
    defaultValue = "${project.build.directory}/scalor/native/output/test"
  )
  var nativeTestTargetFolder : File = _

  override def buildTargetFolder = nativeTestTargetFolder

}

trait BuildTestRuntime extends BuildAnyRuntime {

  @Description( """
  Relative path of the generated runtime binary file for scope=test, mode=release.
  File is packaged inside <a href="#nativeTestTargetFolder"><b>nativeTestTargetFolder</b></a>
  """ )
  @Parameter(
    property     = "scalor.nativeTestRuntimeRelease",
    defaultValue = "release"
  )
  var nativeTestRuntimeRelease : String = _

  @Description( """
  Relative path of the generated runtime binary file for scope=test, mode=debug.
  File is packaged inside <a href="#nativeTestTargetFolder"><b>nativeTestTargetFolder</b></a>
  """ )
  @Parameter(
    property     = "scalor.nativeTestRuntimeDebug",
    defaultValue = "debug"
  )
  var nativeTestRuntimeDebug : String = _

  override def nativeRuntimeRelease = nativeTestRuntimeRelease
  override def nativeRuntimeDebug = nativeTestRuntimeDebug

}
