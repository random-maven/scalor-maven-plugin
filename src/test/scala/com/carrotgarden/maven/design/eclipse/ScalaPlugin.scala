package com.carrotgarden.maven.design.eclipse

import org.scalaide.core.IScalaProject
import java.util.concurrent.Callable
import org.scalaide.core.internal.project.ScalaModule
import org.scalaide.core.internal.project.ScalaInstallationLabel
import scala.tools.nsc.settings.ScalaVersion
import org.scalaide.core.internal.project.LabeledScalaInstallation
import scala.tools.nsc.settings.NoScalaVersion
import com.carrotgarden.maven.scalor.util.Error._
import org.scalaide.core.internal.project.ScalaInstallation
import org.eclipse.core.runtime.IPath
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.core.resources.IProject
import java.io.File
import org.eclipse.core.filesystem.EFS
import org.eclipse.core.runtime.Path
import org.scalaide.core.internal.project.CustomScalaInstallationLabel
import org.scalaide.core.internal.project.MultiBundleScalaInstallation
import java.security.MessageDigest
import java.nio.charset.StandardCharsets
import org.apache.commons.codec.binary.Hex

import com.carrotgarden.maven.scalor.base.Params._
import com.carrotgarden.maven.scalor.zinc.Module._

/**
 * Map Scala IDE values into local class loader.
 *
 * Call is executed inside the scala-ide osgi bundle.
 */
object ScalaPlugin {

  /**
   * Produce ScalaModule.
   */
  class ScalaModuleMaker( scalaProject : IScalaProject )
    extends Callable[ ScalaModule ] {
    override def call() : ScalaModule = {
      val classJar = scalaProject.underlying.getRawLocation
      val sourceJar = None
      val module = ScalaModule( classJar, sourceJar )
      module
    }
  }

  /**
   * Local form of scala module.
   */
  case class SimpleModule(
    classJar :       IPath,
    sourceJar :      Option[ IPath ],
    libraryEntries : IClasspathEntry = null,
    hashString :     String          = "",
    moduleType :     Type      = Unknown
  ) {
    def report = {
      val text = new StringBuffer
      def spacer = text.append( "      " )
      def append( name : String, path : Option[ IPath ] ) = {
        spacer
        text.append( name )
        text.append( ": " )
        path match {
          case Some( path ) =>
            text.append( path.toFile.getCanonicalPath )
          case None =>
            text.append( "<missing>" )
        }
        text.append( "\n" )
      }
      append( "binary", Some( classJar ) )
      append( "source", sourceJar )
      text.toString
    }
  }

  /**
   */
  object SimpleModule {

    def entiresFrom( module : SimpleModule ) : Seq[ String ] = {
      import module._
      Seq( classJar.toPortableString, sourceJar.map( _.toPortableString ).getOrElse( "" ) )
    }

    def apply( classFile : File, sourceFile : Option[ File ], moduleType : Type ) : SimpleModule = {
      SimpleModule(
        classJar   = new Path( classFile.getCanonicalPath ),
        sourceJar  = sourceFile.map( file => new Path( file.getCanonicalPath ) ),
        moduleType = moduleType
      )
    }

    def apply( module : ScalaModule ) : SimpleModule = {
      import module._
      SimpleModule(
        classJar,
        sourceJar,
        libraryEntries,
        hashString
      )
    }

    def unapply( module : SimpleModule ) : ScalaModule = {
      import module._
      ScalaModule(
        classJar,
        sourceJar
      )
    }

  }

  /**
   * Local form of scala installation.
   */
  case class SimpleInstallation(
    label :     ScalaInstallationLabel,
    compiler :  SimpleModule,
    library :   SimpleModule,
    extraJars : Seq[ SimpleModule ],
    version :   ScalaVersion           = NoScalaVersion,
    valid :     Boolean                = false,
    identity :  Int                    = 0
  ) {

    def digest = SimpleInstallation.digestFrom( this )

    def report = {
      val text = new StringBuffer
      def spacer = text.append( "   " )
      def append( name : String, module : SimpleModule ) = {
        spacer
        text.append( name )
        text.append( ": " )
        text.append( "\n" )
        text.append( module.report )
      }
      text.append( "identity: " )
      text.append( identity )
      text.append( "; " )
      text.append( "label: " )
      text.append( label )
      text.append( "; " )
      text.append( "valid: " )
      text.append( valid )
      text.append( "; " )
      text.append( "version: " )
      text.append( version )
      text.append( "; " )
      text.append( "\n" )
      append( "compiler", compiler )
      append( "library", library )
      extraJars.foreach { entry =>
        append( "extra: ", entry )
      }
      text.toString
    }
  }

  /**
   */
  object SimpleInstallation {

    def formatTitle( digest : String ) = s"Scalor <${digest}>"

    def extractDigest( title : String ) : Option[ String ] = {
      val rx = "[^<>]*<([a-z0-9]+)>[^<>]*".r
      title match {
        case rx( digest ) => Some( digest )
        case _            => None
      }
    }

    def titleFrom( installation : SimpleInstallation ) = {
      val digest = digestFrom( installation )
      formatTitle( digest )
    }

    def digestFrom( installation : SimpleInstallation ) : String = {
      val total = entriesFrom( installation ).mkString( ";" )
      val array = MessageDigest.getInstance( "MD5" ).digest( total.getBytes( StandardCharsets.UTF_8 ) )
      val result = new String( Hex.encodeHex( array ) );
      result
    }

    def entriesFrom( installation : SimpleInstallation ) : Seq[ String ] = {
      import installation._
      val moduleList : Seq[ SimpleModule ] = library +: compiler +: extraJars
      moduleList.flatMap( module => SimpleModule.entiresFrom( module ) )
    }

    def extractTitle( label : ScalaInstallationLabel ) : Option[ String ] = {
      label match {
        case CustomScalaInstallationLabel( title ) => Some( title )
        case _                                     => None
      }
    }

    def apply( installation : LabeledScalaInstallation ) : SimpleInstallation = {
      import installation._
      SimpleInstallation(
        label,
        SimpleModule( compiler ),
        SimpleModule( library ),
        extraJars.map( SimpleModule( _ ) ),
        version  = TryHard(
          ScalaInstallation.extractVersion( library.classJar ).getOrElse( NoScalaVersion )
        ).getOrElse( NoScalaVersion ),
        valid    = isValid,
        identity = hashString.hashCode
      )
    }

    def unapply( installation : SimpleInstallation ) : LabeledScalaInstallation = {
      import installation._
      new LabeledScalaInstallation {
        override val label = installation.label
        override val compiler = SimpleModule.unapply( installation.compiler )
        override val library = SimpleModule.unapply( installation.library )
        override val extraJars = // Note: need toList to serialize.
          installation.extraJars.map( SimpleModule.unapply( _ ) ).toList
        override val version = TryHard {
          ScalaInstallation.extractVersion( library.classJar ).getOrElse( NoScalaVersion )
        }.getOrElse( NoScalaVersion )
      }
    }

    def report( list : Seq[ SimpleInstallation ] ) = {
      val text = new StringBuffer
      list.foreach { inst => text.append( inst.report ) }
      text.toString
    }
  }

  /**
   * Re-build installation to reflect Scala IDE attributes.
   */
  class SimpleInstallationUpdater( install : SimpleInstallation )
    extends Callable[ SimpleInstallation ] {
    override def call() : SimpleInstallation = {
      SimpleInstallation.apply( SimpleInstallation.unapply( install ) )
    }
  }

  /**
   * Extract custom ScalaInstallation list from Scala IDE.
   */
  class SimpleInstallationListExtractor( unused : Option[ _ ] )
    extends Callable[ List[ SimpleInstallation ] ] {
    import ScalaInstallation._
    override def call() : List[ SimpleInstallation ] = {
      // Extract only custom installations.
      customInstallations.map( SimpleInstallation.apply( _ ) ).toList
    }
  }

  /**
   * Persist custom ScalaInstallation list into Scala IDE.
   */
  class SimpleInstallationListPersister( customList : List[ SimpleInstallation ] )
    extends Callable[ List[ SimpleInstallation ] ] {
    import ScalaInstallation._
    override def call() : List[ SimpleInstallation ] = {
      // Inject custom installations.
      customInstallations.clear()
      customList.map( SimpleInstallation.unapply( _ ) ).map( customInstallations.add( _ ) )
      // Persist all available installations.
      installationsTracker.saveInstallationsState( availableInstallations )
      // Extract custom installations, again.
      customInstallations.map( SimpleInstallation.apply( _ ) ).toList
    }
  }

}
