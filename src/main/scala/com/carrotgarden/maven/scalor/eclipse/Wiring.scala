package com.carrotgarden.maven.scalor.eclipse

import org.codehaus.plexus.classworlds.realm.ClassRealm
import org.sonatype.plexus.build.incremental.BuildContext

import com.carrotgarden.maven.scalor.base
import com.carrotgarden.maven.scalor.util
import com.carrotgarden.maven.tools.Description

import org.osgi.framework.BundleReference
import org.eclipse.core.resources.ResourcesPlugin
import org.scalaide.core.IScalaPlugin
import org.eclipse.core.resources.IWorkspace
import org.eclipse.m2e.core.embedder.IMaven
import org.eclipse.m2e.core.MavenPlugin
import org.eclipse.jdt.core.JavaCore
import org.eclipse.core.resources.IProject

import java.io.File
import org.codehaus.plexus.classworlds.ClassWorld
import org.osgi.framework.Bundle

import Wiring._
import org.eclipse.m2e.core.ui.internal.M2EUIPluginActivator
import org.eclipse.m2e.core.internal.MavenPluginActivator
import java.nio.file.Files

/**
 * Connect this Maven plugin with host Eclipse platform
 * in order to install its own companion Eclipse plugin.
 */
@Description( """
Note: controlled OSGI environment.
""" )
case class Wiring(
  buildContext :    BuildContext,
  metaRuntime :     Meta         = Meta( "org.eclipse.equinox.common", "org.eclipse.core.runtime" ),
  metaRuntimeJobs : Meta         = Meta( "org.eclipse.core.jobs", "org.eclipse.core.runtime.jobs" ),
  metaResources :   Meta         = Meta( "org.eclipse.core.resources", "org.eclipse.core.resources" ),
  metaMavenCore :   Meta         = Meta( "org.eclipse.m2e.core", "org.eclipse.m2e.core" ),
  metaMavenCoreUI : Meta         = Meta( "org.eclipse.m2e.core.ui", "org.eclipse.m2e.core.ui" ),
  metaOsgiCore :    Meta         = Meta( "osgi.core", "org.osgi" )
) {

  /**
   * Connect this Maven plugin to host Eclipse plugins.
   */
  def setup: Handle = {

    /**
     * Keep order:
     * each given step provides class loaders for following steps.
     */

    // eclipse m2e bundle loader
    val loaderM2E = buildContext.getClass.getClassLoader

    // maven-scalor-plugin loader
    val localRealm = getClass.getClassLoader.asInstanceOf[ ClassRealm ]

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
    importPackage( loaderM2E, metaOsgiCore )
    val reference = loaderM2E.asInstanceOf[ BundleReference ]
    val bundleM2E = reference.getBundle

    // discover eclipse bundle and import select packages into local realm
    def importBundle( meta : Meta ) : Bundle = {
      import util.OSGI._
      val bundle = discoverBundle( bundleM2E, meta.id ).get
      importPackage( bundleClassLoader( bundle ), meta )
      bundle
    }

    // provide eclipse runtime plugin
    val bundleRuntime = importBundle( metaRuntime )
    val bundleRuntimeJobs = importBundle( metaRuntimeJobs )

    // provide eclipse resources plugin
    val bundleResources = importBundle( metaResources )
    val workspace = ResourcesPlugin.getWorkspace
    val resourcesPlugin = ResourcesPlugin.getPlugin

    // provide eclipse maven (m2e.core) plugin
    val bundleMavenCore = importBundle( metaMavenCore )
    val mavenPlugin = MavenPluginActivator.getDefault

    // provide eclipse maven (m2e.core.ui) plugin
    val bundleMavenCoreUI = importBundle( metaMavenCoreUI )
    val mavenPluginUI = M2EUIPluginActivator.getDefault

    Handle(
      bundleM2E,
      localRealm,
      workspace,
      resourcesPlugin,
      mavenPlugin,
      mavenPluginUI
    )
  }

}

object Wiring {

  /**
   * Bundle import descriptor.
   */
  case class Meta(
    /** Bundle symbolic name. */
    id : String,
    /** Bundle package import list. */
    pkg : String*
  )

  /**
   * Maven/Eclipse wiring handle.
   * Stores available Eclipse platform plugins.
   */
  case class Handle(
    bundleM2E :       Bundle,
    localRealm :      ClassRealm,
    workspace :       IWorkspace,
    resourcesPlugin : ResourcesPlugin,
    mavenPlugin :     MavenPluginActivator,
    mavenPluginUI :   M2EUIPluginActivator
  )

  /**
   * Find Eclipse project with matching base directory.
   */
  def projectWithBase( projectList : Array[ IProject ], baseDir : File ) : IProject = {
    import util.Folder._
    import util.Error._
    val sourcePath = baseDir.toPath
    val length = projectList.length
    var index = 0
    while ( index < length ) {
      val project = projectList( index ); index += 1
      val hasFile = true &&
        project != null &&
        project.getLocation != null &&
        project.getLocation.toFile != null &&
        true
      if ( hasFile ) {
        val targetPath = project.getLocation.toFile.toPath
        val hasBoth = Files.exists( sourcePath ) && Files.exists( targetPath )
        if ( hasBoth && Files.isSameFile( sourcePath, targetPath ) ) {
          return project
        }
      }
    }
    Throw( s"Missing required project: ${sourcePath}" )
  }

}
