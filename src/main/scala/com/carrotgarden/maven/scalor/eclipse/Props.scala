package com.carrotgarden.maven.scalor.eclipse

import java.io.File
import java.io.FileOutputStream

import org.eclipse.jface.preference.IPreferenceStore
import org.eclipse.jface.preference.PreferenceStore

/**
 * Properties support.
 */
trait Props {

  import com.carrotgarden.maven.scalor.util.Error._
  import com.carrotgarden.maven.scalor.util.Folder._

  /**
   * Optionally provide header comment in preference store.
   */
  def persistComment(
    storage : IPreferenceStore,
    comment : String
  ) : Unit = TryHard {
    val klaz = classOf[ PreferenceStore ]
    val store = storage.asInstanceOf[ PreferenceStore ]
    val field = klaz.getDeclaredField( "filename" )
    field.setAccessible( true )
    val filename = field.get( store ).asInstanceOf[ String ]
    val file = new File( filename )
    ensureParent( file )
    val stream = new FileOutputStream( file )
    store.save( stream, comment )
    stream.close
  }

}
