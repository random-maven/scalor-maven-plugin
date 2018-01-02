package com.carrotgarden.maven.design.eclipse

import com.carrotgarden.maven.scalor._
import com.carrotgarden.maven.scalor.eclipse.ParamsConfig
import com.carrotgarden.maven.scalor.base.Params
import com.carrotgarden.maven.scalor.eclipse.Logging.AnyLog
import org.eclipse.m2e.core.project.IMavenProjectFacade
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest
import org.eclipse.core.runtime.IProgressMonitor
import scala.reflect.ClassTag
import org.eclipse.m2e.core.project.IMavenProjectFacade
import java.lang.reflect.Modifier

import com.carrotgarden.maven.scalor._
import scala.util.DynamicVariable
import org.eclipse.jface.preference.IPreferenceStore
import org.eclipse.jface.preference.PreferenceStore

import java.io.File
import java.io.FileOutputStream
import com.carrotgarden.maven.scalor.eclipse.Logging.AnyLog
import org.eclipse.m2e.core.internal.MavenPluginActivator
import org.codehaus.plexus.component.configurator.converters.lookup.ConverterLookup
import org.codehaus.plexus.component.configurator.expression.DefaultExpressionEvaluator
import org.codehaus.plexus.configuration.PlexusConfiguration
import org.codehaus.plexus.util.xml.Xpp3Dom
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration
import org.apache.maven.model.Dependency

trait ProjectConfigurator {

  import util.Error._
  import util.Folder._
  import scala.reflect.runtime.{ universe => ru }

  //  def mavenImpl = MavenPluginActivator.getDefault().getMaven()
  //
  //  def converterLookup : ConverterLookup = {
  //    val klaz = mavenImpl.getClass
  //    val field = klaz.getDeclaredField( "converterLookup" )
  //    field.setAccessible( true )
  //    val value = field.get( mavenImpl )
  //    value.asInstanceOf[ ConverterLookup ]
  //  }

  /**
   * Obtain plugin configuration values via reflection.
   */
  // TODO make macro instead
  def paramsConfigXXX(
    log :     AnyLog,
    facade :  IMavenProjectFacade,
    monitor : IProgressMonitor
  ) : ParamsConfig = {

    //    val execution = Maven.executionDefinition( facade, A.mojo.`eclipse-config`, monitor ).get
    //    log.info( s"Z1 ${execution.getConfiguration}" )
    //
    //    val lookup = converterLookup
    //    val converterArray = lookup.lookupConverterForType( classOf[ Array[ Dependency ] ] )
    //    val converterDefine = lookup.lookupConverterForType( classOf[ Dependency ] )
    //    log.info( s"Z2 ${converterArray}" )
    //    log.info( s"Z3 ${converterDefine}" )
    //
    //    val dom = execution.getPlugin.getConfiguration.asInstanceOf[ Xpp3Dom ]
    //      .getChild( "defineBridge" ) // .getChild("dependency")
    //    log.info( s"Z4 ${dom}" )
    //
    //    val evaluator = new DefaultExpressionEvaluator
    //    val enclosingType = null
    //    val loader = getClass.getClassLoader
    //
    //    val stanza = new XmlPlexusConfiguration( dom )
    //    val result = converterArray
    //      .fromConfiguration( lookup, stanza, classOf[ Array[ Dependency ] ], enclosingType, loader, evaluator )
    //    log.info( s"Z5 ${result}" )

    val config = ParamsConfig()
    val face = classOf[ Params ]
    val klaz = classOf[ ParamsConfig ]
    face.getMethods.foreach { method =>
      val mod = method.getModifiers
      val isDefault = method.isDefault
      val isAbstract = Modifier.isAbstract( mod )
      val isStatic = Modifier.isStatic( mod )
      val isVoid = method.getReturnType == Void.TYPE
      val hasGetter = isAbstract && !isDefault && !isStatic && !isVoid
      if ( hasGetter ) {
        val name = method.getName
        val javaField = klaz.getDeclaredField( name )
        val javaType = util.Classer.primitiveWrap( javaField.getType ) // wrap
        val scalaTag = ClassTag( javaType )
        //
        val value = ??? // configValue( facade, name, monitor )( scalaTag ).asInstanceOf[ Object ] // cast
        //
        javaField.setAccessible( true )
        javaField.set( config, value )
      }
    }
    config
  }

}
