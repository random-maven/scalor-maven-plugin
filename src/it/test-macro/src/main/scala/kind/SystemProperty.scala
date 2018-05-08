package kind

import meta.Macro

/**
 * https://github.com/non/kind-projector
 */

object SystemProperty {

  def `kp:genAsciiNames` = Macro.systemProperty( "kp:genAsciiNames" )

}
