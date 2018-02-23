package com.carrotgarden.maven.scalor.eclipse

import org.eclipse.jdt.internal.ui.packageview.ClassPathContainer
import org.eclipse.jface.viewers.IDecoration

/**
 * Provide Eclipse UI decorations.
 *
 * TODO
 */
trait Decor {

  //    import org.eclipse.swt.graphics.Image;

  def processContainer( container : ClassPathContainer, decoration : IDecoration ) : Unit = {
    val javaProject = container.getJavaProject
    val hasAccessible = javaProject.getProject.isAccessible
    val containerPath = container.getClasspathEntry.getPath
    if ( containerPath.equals( "CLASSPATH_CONTAINER_ID" ) ) {
      if ( hasAccessible ) {
        decoration.addSuffix( " (yes)" );
      }
    }
  }

}
