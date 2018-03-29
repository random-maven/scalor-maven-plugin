package com.carrotgarden.maven.scalor.scalanative

import java.io.File

import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.plugins.annotations.Component

import com.carrotgarden.maven.scalor.base
import com.carrotgarden.maven.tools.Description
import com.carrotgarden.maven.scalor.util.Folder
import com.carrotgarden.maven.scalor.util.Error.Throw
import org.apache.maven.archiver.MavenArchiveConfiguration
import org.apache.maven.project.MavenProjectHelper
import org.apache.maven.project.MavenProject

trait ParamsLinkAny extends AnyRef
  with Build
  with ParamsRegex
  with ParamsLibrary
  with ParamsLogging
  with ParamsOptions
  with ParamsEntryClassMain
  with ParamsGarbageCollectorAny {

  def nativeWorkdir : File

}

trait ParamsOptions extends AnyRef
  with base.ParamsAny {

  @Description( """
  Linking behaviour for placeholder methods annotated with <code>@stub</code>.
  By default stubs are not linked and are shown as linking errors.
  """ )
  @Parameter(
    property     = "scalor.nativeOptionLinkStubs",
    defaultValue = "false"
  )
  var nativeOptionLinkStubs : Boolean = _

  @Description( """
  LLVM compile options for mode=release.
  Uses optimization by default.
  These options are added to auto-discovered LLVM options. 
  Separator parameter: <a href="#commonSequenceSeparator"><b>commonSequenceSeparator</b></a>.
  """ )
  @Parameter(
    property     = "scalor.nativeOptionsCompileRelease",
    defaultValue = "-O2 ★"
  )
  var nativeOptionsCompileRelease : String = _

  @Description( """
  LLVM compile options for mode=debug. 
  Uses no optimization by default.
  These options are added to auto-discovered LLVM options. 
  Separator parameter: <a href="#commonSequenceSeparator"><b>commonSequenceSeparator</b></a>.
  """ )
  @Parameter(
    property     = "scalor.nativeOptionsCompileDebug",
    defaultValue = "-O0 ★"
  )
  var nativeOptionsCompileDebug : String = _

  @Description( """
  LLVM linking options for mode=release. 
  Strips symbols by default.
  These options are added to auto-discovered LLVM options. 
  Separator parameter: <a href="#commonSequenceSeparator"><b>commonSequenceSeparator</b></a>.
  """ )
  @Parameter(
    property     = "scalor.nativeOptionsLinkingRelease",
    defaultValue = "-lpthread ★ -Wl,-s ★"
  )
  var nativeOptionsLinkingRelease : String = _

  @Description( """
  LLVM linking options for mode=release. 
  Keeps symbols by default.
  These options are added to auto-discovered LLVM options.
  Separator parameter: <a href="#commonSequenceSeparator"><b>commonSequenceSeparator</b></a>.
  """ )
  @Parameter(
    property     = "scalor.nativeOptionsLinkingDebug",
    defaultValue = "-lpthread ★"
  )
  var nativeOptionsLinkingDebug : String = _

  @Description( """
  Options used by <code>objcopy</code> tool during C data resource embedding.
  These options are added to required hard-coded <code>objcopy</code> invocation options.
  Empty by default.
  Separator parameter: <a href="#commonSequenceSeparator"><b>commonSequenceSeparator</b></a>.
  """ )
  @Parameter(
    property     = "scalor.nativeOptionsObjcopy",
    defaultValue = " ★"
  )
  var nativeOptionsObjcopy : String = _

  @Description( """
  Mapping required for <code>objcopy</code> embedder.
  Used to convert from LLVM <code>triplet</code> into GNU objcopy <code>binary-architecture/output</code>.
  LLVM <a href="http://llvm.org/doxygen/Triple_8h_source.html">triplet reference</a>.
  GNU <a href="https://sourceware.org/binutils/docs/binutils/objcopy.html">objcopy --info reference</a>.
  Separator parameter: <a href="#commonSequenceSeparator"><b>commonSequenceSeparator</b></a>.
  Mapping parameter: <a href="#commonMappingPattern"><b>commonMappingPattern</b></a>.
  <br/>
  Mapping format, where 
  <code>triplet-regex</code> is regular expressoin mathced against LLVM triplet,
  <code>binary-architecture/output</code> is slash-separated pair describing GNU objcopy type:
<pre>
   triplet-regex = binary-architecture/output
</pre>
  """ )
  @Parameter(
    property     = "scalor.nativeObjcopyMapping",
    defaultValue = """
    x86-([^-]+)-linux-([^-]+) = i386/elf32-i386 ★
    x86_64-([^-]+)-linux-([^-]+) = i386/elf64-x86-64 ★
    """
  )
  var nativeObjcopyMapping : String = _

  @Description( """
  Options used by LLVM <code>clangpp</code> during <code>*.cpp</code> sources compilation.
  These options are added to auto-discovered LLVM options.
  Sets standard by default.
  Separator parameter: <a href="#commonSequenceSeparator"><b>commonSequenceSeparator</b></a>.
  """ )
  @Parameter(
    property     = "scalor.nativeOptionsClangPP",
    defaultValue = "-std=c++11 ★"
  )
  var nativeOptionsClangPP : String = _

  def nativeObjCopyMaps = parseCommonMapping( nativeObjcopyMapping )

  def nativeOptsObjCopy = parseCommonList( nativeOptionsObjcopy )

  def nativeOptsClangPP = parseCommonList( nativeOptionsClangPP )

  def nativeOptsCompRelease = parseCommonList( nativeOptionsCompileRelease )

  def nativeOptsCompDebug = parseCommonList( nativeOptionsCompileDebug )

  def nativeOptsLinkRelease = parseCommonList( nativeOptionsLinkingRelease )

  def nativeOptsLinkDebug = parseCommonList( nativeOptionsLinkingDebug )

}

trait ParamsLogging {

  @Description( """
  Enable logging of linker options.
  Use to review actual Scala.native linker invocation configuration.
  """ )
  @Parameter(
    property     = "scalor.nativeLogOptions",
    defaultValue = "false"
  )
  var nativeLogOptions : Boolean = _

  @Description( """
  Enable logging of Scala.native linker runtime.
  Use to review actual generated output <code>runtime</code> location.
  """ )
  @Parameter(
    property     = "scalor.nativeLogRuntime",
    defaultValue = "false"
  )
  var nativeLogRuntime : Boolean = _

  @Description( """
  Enable logging of Scala.native linker class path.
  Use to review actual resources used for <code>*.nir</code> class discovery.
  """ )
  @Parameter(
    property     = "scalor.nativeLogClassPath",
    defaultValue = "false"
  )
  var nativeLogClassPath : Boolean = _

  @Description( """
  Enable logging of Scala.native build results.
  Use to review generated resources locations and counts.
  """ )
  @Parameter(
    property     = "scalor.nativeLogBuildStats",
    defaultValue = "false"
  )
  var nativeLogBuildStats : Boolean = _

  @Description( """
  Enable logging of Scala.native build phase durations.
  Use to review compiler and linker performance profile.
  """ )
  @Parameter(
    property     = "scalor.nativeLogBuildTimes",
    defaultValue = "false"
  )
  var nativeLogBuildTimes : Boolean = _

  @Description( """
  Enable logging of Scala.native external executable invocations.
  Use to review actual shell commands used to invoke external processes such as LLMV.
  """ )
  @Parameter(
    property     = "scalor.nativeLogBuildProcs",
    defaultValue = "false"
  )
  var nativeLogBuildProcs : Boolean = _

  @Description( """
  Enable logging of Scala.native external command invocations "vertically".
  Use to ease review of typical long LLVM compile and linking shell commands.
  """ )
  @Parameter(
    property     = "scalor.nativeLogBuildVerts",
    defaultValue = "false"
  )
  var nativeLogBuildVerts : Boolean = _

  @Description( """
  Enable logging of Scala.native linker update result of M2E incremental change detection.
  Use to review actual <code>*.nir</code> classes which triggered Eclipse linker build.
  """ )
  @Parameter(
    property     = "scalor.nativeLogUpdateResult",
    defaultValue = "false"
  )
  var nativeLogUpdateResult : Boolean = _

}

trait ParamsLibrary {

  @Description( """
  Regular expression used to discover Scala.native core <code>*.nir</code> library from class path.
  This regular expression is matched against resolved project depenencies in given scope.
  Regular expression in the form: <code>${groupId}:${artifactId}</code>.
  Enablement parameter: <a href="#nativeLibraryDetect"><b>nativeLibraryDetect</b></a>.
  """ )
  @Parameter(
    property     = "scalor.nativeScalaLibRegex",
    defaultValue = "org.scala-native:scalalib_.+"
  )
  var nativeScalaLibRegex : String = _

  @Description( """
  Regular expression used to discover Scala.native interop C/CPP library from class path.
  This library normally comes as dependency to <code>scalalib</code>, 
  see <a href="nativeScalaLibRegex">nativeScalaLibRegex</a>.
  This regular expression is matched against resolved project depenencies in given scope.
  Regular expression in the form: <code>${groupId}:${artifactId}</code>.
  """ )
  @Parameter(
    property     = "scalor.nativeNativeLibRegex",
    defaultValue = "org.scala-native:nativelib_.+"
  )
  var nativeNativeLibRegex : String = _

  @Description( """
  Invoke Scala.native linker only when Scala.native library is detected
  in project dependencies with given scope.
  Detection parameter: <a href="#nativeScalaLibRegex"><b>nativeScalaLibRegex</b></a>.
  """ )
  @Parameter(
    property     = "scalor.nativeLibraryDetect",
    defaultValue = "true"
  )
  var nativeLibraryDetect : Boolean = _

}

trait ParamsRegex {

  @Description( """
  Regular expression used to discover Scala.native <code>*.nir</code> IR classes from class path.
  """ )
  @Parameter(
    property     = "scalor.nativeClassRegex",
    defaultValue = ".+[.]nir"
  )
  var nativeClassRegex : String = _

}

trait ParamsEntryClassAny {

  def nativeEntryClass : String

}

trait ParamsEntryClassMain extends ParamsEntryClassAny {

  @Description( """
  Entry point for native runtime in scope=main.
  Fully qualified class name which follows Java <code>main</code> contract.
  For example, Scala object in file <code>main/Main.scala</code>:
<pre>
package main
object Main {
  def main( args : Array[ String ] ) : Unit = {
    println( s"scala-native" )
  }
}
</pre>
  """ )
  @Parameter(
    property     = "scalor.nativeMainEntryClass",
    defaultValue = "main.Main"
  )
  var nativeMainEntryClass : String = _

  override def nativeEntryClass = nativeMainEntryClass

}

trait ParamsEntryClassTest extends ParamsEntryClassAny {

  @Description( """
  Entry point for native runtime in scope=test.
  Fully qualified class name which follows Java <code>main</code> contract.
  For example, Scala object in file <code>test/Main.scala</code>:
<pre>
package test
object Main {
  def main( args : Array[ String ] ) : Unit = {
    println( s"scala-native" )
  }
}
</pre>
  """ )
  @Parameter(
    property     = "scalor.nativeTestEntryClass",
    defaultValue = "test.Main"
  )
  var nativeTestEntryClass : String = _

  override def nativeEntryClass = nativeTestEntryClass

}

trait ParamsGarbageCollectorAny {

  def nativeGarbageCollector : String

}

trait ParamsGarbageCollectorMain extends ParamsGarbageCollectorAny {

  @Description( """
  Select garbage collector included with Scala.native runtime in scope=main.
  Garbage collector <a href="http://www.scala-native.org/en/latest/user/sbt.html#garbage-collectors">reference</a>.
  Available garbage collectors:
<pre>
  none 
  boehm 
  immix 
</pre>
  """ )
  @Parameter(
    property     = "scalor.nativeMainGarbageCollector",
    defaultValue = "boehm"
  )
  var nativeMainGarbageCollector : String = _

  override def nativeGarbageCollector = nativeMainGarbageCollector

}

trait ParamsGarbageCollectorTest extends ParamsGarbageCollectorAny {

  @Description( """
  Select garbage collector included with Scala.native runtime in scope=test.
  Garbage collector <a href="http://www.scala-native.org/en/latest/user/sbt.html#garbage-collectors">reference</a>.
  Available garbage collectors:
<pre>
  none 
  boehm 
  immix 
</pre>
  """ )
  @Parameter(
    property     = "scalor.nativeTestGarbageCollector",
    defaultValue = "boehm"
  )
  var nativeTestGarbageCollector : String = _

  override def nativeGarbageCollector = nativeTestGarbageCollector

}

trait ParamsOperatingSystem {

  import org.apache.commons.lang3.SystemUtils

  @Description( """
  Detect operating system and invoke native goals only when running on supported o/s.
  When <code>false</code>, force native goals invocation.
  """ )
  @Parameter(
    property     = "scalor.nativeSystemDetect",
    defaultValue = "true"
  )
  var nativeSystemDetect : Boolean = _

  /**
   * http://www.scala-native.org/en/latest/user/setup.html
   */
  def nativeHasOperatingSystem = {
    import SystemUtils._
    IS_OS_LINUX || IS_OS_MAC_OSX || IS_OS_FREE_BSD
  }

}

trait ParamsLinkMain extends ParamsLinkAny
  with BuildMain
  with ParamsEntryClassMain
  with ParamsGarbageCollectorMain {

  @Description( """
  Linker working directory for scope=main.
  """ )
  @Parameter(
    property     = "scalor.nativeMainWorkdir",
    defaultValue = "${project.build.directory}/scalor/native/workdir/main"
  )
  var nativeMainWorkdir : File = _

  override def nativeWorkdir = nativeMainWorkdir

}

trait ParamsLinkTest extends ParamsLinkAny
  with BuildTest
  with ParamsEntryClassTest
  with ParamsGarbageCollectorTest {

  @Description( """
  Linker working directory for scope=main.
  """ )
  @Parameter(
    property     = "scalor.nativeTestWorkdir",
    defaultValue = "${project.build.directory}/scalor/native/workdir/test"
  )
  var nativeTestWorkdir : File = _

  override def nativeWorkdir = nativeTestWorkdir

}

trait ParamsPackAny extends AnyRef
  with ParamsLibrary {

  @Description( """
  Configuration of Scala.native archive jar. 
  Normally used with provided default values.
  Component reference:
<a href="https://maven.apache.org/shared/maven-archiver/index.html">
  MavenArchiveConfiguration
</a>
  """ )
  @Parameter()
  var nativeArchiveConfig : MavenArchiveConfiguration = new MavenArchiveConfiguration()

  @Description( """
  Maven project helper.
  """ )
  @Component()
  var projectHelper : MavenProjectHelper = _

  //  @Description( """
  //  Contains the full list of projects in the build.
  //  """ )
  //  @Parameter( defaultValue = "${reactorProjects}", readonly = true )
  //  var reactorProjects : java.util.List[ MavenProject ] = _

  @Description( """
  Root name for the generated Scala.native jar file.
  Full name will include <code>classifier</code> suffix.
  """ )
  @Parameter(
    property     = "scalor.nativeFinalName",
    defaultValue = "${project.build.finalName}"
  )
  var nativeFinalName : String = _

  def nativeHasAttach : Boolean
  def nativeClassifier : String
  def nativeOutputFolder : File

  def nativeArchiveName = s"${nativeFinalName}-${nativeClassifier}.jar"

}

trait ParamsPackMain extends ParamsPackAny {

  @Description( """
  Artifact classifier for Scala.native with scope=main.
  Appended to <a href="#nativeFinalName"><b>nativeFinalName</b></a>.
  """ )
  @Parameter(
    property     = "scalor.nativeMainClassifier",
    defaultValue = "native"
  )
  var nativeMainClassifier : String = _

  @Description( """
  Enable to attach generated Scala.native jar 
  to the project as deployment artifact with scope=main.
  """ )
  @Parameter(
    property     = "scalor.nativeMainAttach",
    defaultValue = "true"
  )
  var nativeMainAttach : Boolean = _

  @Description( """
  Folder with generated Scala.native content with scope=main. 
  """ )
  @Parameter(
    property     = "scalor.nativeMainOutputFolder",
    defaultValue = "${project.build.directory}/scalor/native/output/main"
  )
  var nativeMainOutputFolder : File = _

  override def nativeHasAttach = nativeMainAttach
  override def nativeClassifier = nativeMainClassifier
  override def nativeOutputFolder = nativeMainOutputFolder

}

trait ParamsPackTest extends ParamsPackAny {

  @Description( """
  Artifact classifier for Scala.native with scope=test.
  Appended to <a href="#nativeFinalName"><b>nativeFinalName</b></a>.
  """ )
  @Parameter(
    property     = "scalor.nativeTestClassifier",
    defaultValue = "test-native"
  )
  var nativeTestClassifier : String = _

  @Description( """
  Enable to attach generated Scala.native jar
  to the project as deployment artifact with scope=test.
  """ )
  @Parameter(
    property     = "scalor.nativeTestAttach",
    defaultValue = "true"
  )
  var nativeTestAttach : Boolean = _

  @Description( """
  Folder with generated Scala.native content with scope=test. 
  """ )
  @Parameter(
    property     = "scalor.nativeTestOutputFolder",
    defaultValue = "${project.build.directory}/scalor/native/output/test"
  )
  var nativeTestOutputFolder : File = _

  override def nativeHasAttach = nativeTestAttach
  override def nativeClassifier = nativeTestClassifier
  override def nativeOutputFolder = nativeTestOutputFolder

}
