package com.carrotgarden.maven.scalor.eclipse

import java.io.File
import java.nio.file.Files

import org.codehaus.plexus.classworlds.realm.ClassRealm
import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IWorkspace
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.m2e.core.internal.MavenPluginActivator
import org.eclipse.m2e.core.ui.internal.M2EUIPluginActivator
import org.osgi.framework.Bundle
import org.sonatype.plexus.build.incremental.BuildContext

import com.carrotgarden.maven.tools.Description

import Wiring.Handle
import Wiring.Meta
import Wiring.bundleClassLoader
import Wiring.discoverBundleFrom

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

  import org.osgi.framework.BundleReference

  /**
   * Connect this Maven plugin to host Eclipse plugins.
   */
  def setup : Handle = {

    /**
     * Keep order: each given step provides class loaders for following steps.
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
      val bundle = discoverBundleFrom( bundleM2E, meta.id ).get
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

  import org.osgi.framework.BundleContext
  import org.osgi.framework.BundleReference
  import org.osgi.framework.Version
  import org.osgi.framework.wiring.BundleWiring

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
    import com.carrotgarden.maven.scalor.util.Error._
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

  /**
   * Obtain class loader servicing given bundle.
   */
  def bundleClassLoader( bundle : Bundle ) : ClassLoader = {
    bundle.adapt( classOf[ BundleWiring ] ).getClassLoader
  }

  /**
   * Find bundle by symbolic name and version.
   */
  def discoverBundleFrom(
    root :          Bundle,
    symbolicName :  String,
    versionOption : Option[ String ] = None
  ) : Option[ Bundle ] = {
    val list = root.getBundleContext.getBundles
    val version = Version.parseVersion( versionOption.getOrElse( "0.0.0.invalid" ) )
    var index = 0
    val length = list.length
    while ( index < length ) {
      val bundle = list( index ); index += 1
      val hasName = bundle.getSymbolicName.equals( symbolicName )
      val hasVersion = !versionOption.isDefined || version.equals( bundle.getVersion )
      if ( hasName && hasVersion ) {
        return Some( bundle )
      }
    }
    None
  }

}
