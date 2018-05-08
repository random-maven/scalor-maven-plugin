package kind

import org.junit.Test

class SystemPropertyTest {

  @Test
  def propertyTest = {
    val property = SystemProperty.`kp:genAsciiNames`
    println(s"property=${property}") // FIXME null
  }

}
