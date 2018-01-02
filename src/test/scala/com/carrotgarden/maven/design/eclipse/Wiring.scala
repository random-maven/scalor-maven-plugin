package com.carrotgarden.maven.design.eclipse

import org.codehaus.plexus.classworlds.realm.ClassRealm
import org.sonatype.plexus.build.incremental.BuildContext

import org.osgi.framework.BundleReference
import org.eclipse.core.resources.ResourcesPlugin
import org.scalaide.core.IScalaPlugin
import org.eclipse.core.resources.IWorkspace
import org.eclipse.m2e.core.embedder.IMaven
import org.eclipse.m2e.core.MavenPlugin
import org.eclipse.jdt.core.JavaCore
import org.eclipse.core.resources.IProject
import java.io.File
import org.osgi.framework.Bundle
import org.scalaide.core.IScalaPlugin
import Wiring._
import org.scalaide.core.IScalaPlugin

import com.carrotgarden.maven.scalor.util

import util.Folder._
import util.Classer._
import util.OSGI._
import util.Error._

/**
 * Connect this maven plugin with host eclispe plugins.
 */
case class Wiring(
  buildContext :  BuildContext,
  resourcesMeta : Meta         = Meta( "org.eclipse.core.resources", "org.eclipse.core.resources" ),
  runtimeMeta :   Meta         = Meta( "org.eclipse.equinox.common", "org.eclipse.core.runtime" ),
  scalaMeta :     Meta         = Meta( "org.scala-ide.sdt.core", "org.scalaide.core" ),
  mavenMeta :     Meta         = Meta( "org.eclipse.m2e.core", "org.eclipse.m2e.core" ),
  javaMeta :      Meta         = Meta( "org.eclipse.jdt.core", "org.eclipse.jdt.core", "org.eclipse.jdt.internal" ),
  osgiCoreMeta :  Meta         = Meta( "osgi.core", "org.osgi" )
) {

  /**
   * Connect this maven plugin to host eclispe plugins.
   */
  def setup[ T ] : Handle2 = {

    /**
     * Keep order:
     * each given step provides class loaders for following steps.
     */

    // eclipse m2e bundle loader
    val loaderM2E = buildContext.getClass.getClassLoader

    // maven scalor plugin loader
    val localRealm = this.getClass.getClassLoader.asInstanceOf[ ClassRealm ]

    // verify existing package import
    def hasImport( pkg : String ) = {
      localRealm.getImportClassLoader( pkg ) != null
    }

    // append external class loader to local realm for select packages
    def importPackage( loader : ClassLoader, meta : Meta ) : Unit = {
      meta.pkg.foreach { pkg =>
        localRealm.importFrom( loader, pkg )
      }
    }

    // provide eclipse osgi framework
    importPackage( loaderM2E, osgiCoreMeta )
    val reference = loaderM2E.asInstanceOf[ BundleReference ]
    val bundleM2E = reference.getBundle

    // discover eclipse bundle and import select packages into local realm
    def importBundle( meta : Meta ) : Bundle = {
      val bundle = discoverBundle( bundleM2E, meta.id ).get
      importPackage( bundleClassLoader( bundle ), meta )
      bundle
    }

    // provide eclipse runtime plugin
    val bundleRuntime = importBundle( runtimeMeta )

    // provide eclipse resources plugin
    val bundleResources = importBundle( resourcesMeta )
    val workspace = ResourcesPlugin.getWorkspace
    val resourcesPlugin = ResourcesPlugin.getPlugin

    // provide eclipse scala (sdt) plugin
    val bundleScala = importBundle( scalaMeta )
    val scalaModule = fakeCompanion( classOf[ IScalaPlugin ] ).asInstanceOf[ IScalaPlugin.type ]
    val scalaPlugin = scalaModule.apply()

    // provide eclipse maven (m2e) plugin
    val bundleMaven = importBundle( mavenMeta )
    val mavenPlugin = MavenPlugin.getMaven

    // provide eclipse java (jdt) plugin
    val bundleJava = importBundle( javaMeta )
    val javaPlugin = JavaCore.getJavaCore

    // setup scala-ide object caster class loader
    val casterRealm = new ClassRealm(
      localRealm.getWorld, "caster-realm", bundleClassLoader( bundleScala )
    )
    val packageList = List(
      "com.carrotgarden.maven.scalor", // self
      "com.esotericsoftware", // kryo base
      "org.objenesis", // kryo deps
      "com.twitter.chill" // kryo scala
    )
    packageList
      .map { packageName =>
        packageName.replaceAll( "[.]", "/" )
      }
      .foreach { resourceName =>
        findJarByResource( localRealm, resourceName ).foreach { file =>
          casterRealm.addURL( file.getCanonicalFile.toURI.toURL )
        }
      }

    Handle2(
      bundleM2E,
      localRealm,
      casterRealm,
      workspace,
      resourcesPlugin,
      scalaPlugin,
      mavenPlugin,
      javaPlugin
    )
  }

}

object Wiring {

  //  class TestClassRealm( world : ClassWorld, id : String, baseClassLoader : ClassLoader )
  //    extends ClassRealm( world, id, baseClassLoader ) {
  //
  //  }

  /**
   * Bundle import descriptor.
   */
  case class Meta(
    /** Bundle symbolic name. */
    id : String,
    /** Bundle package import list. */
    pkg : String*
  )

  case class Handle2(
    bundleM2E :       Bundle,
    localRealm :      ClassRealm,
    casterRealm :     ClassRealm,
    workspace :       IWorkspace,
    resourcesPlugin : ResourcesPlugin,
    scalaPlugin :     IScalaPlugin,
    mavenPlugin :     IMaven,
    javaPlugin :      JavaCore
  )

  /**
   * Find eclipse project with matching base directory.
   */
  def projectWithPath( projectList : Array[ IProject ], baseDir : File ) : IProject = {
    val canonicalPath = baseDir.getCanonicalPath
    val length = projectList.length
    var index = 0
    while ( index < length ) {
      val project = projectList( index ); index += 1
      val hasFile = true &&
        project != null &&
        project.getRawLocation != null &&
        project.getRawLocation.makeAbsolute != null &&
        project.getRawLocation.makeAbsolute.toFile != null &&
        true
      if ( hasFile ) {
        val hasMatch = project.getRawLocation.makeAbsolute.toFile.getCanonicalPath == canonicalPath
        if ( hasMatch ) {
          return project
        }
      }
    }
    throw new RuntimeException( "Missing required project: " + baseDir )
  }

}
