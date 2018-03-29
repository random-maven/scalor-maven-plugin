package kind

import org.junit.jupiter.api._
import org.junit.jupiter.api.Assertions._
import org.junit.runner.RunWith
import org.junit.platform.runner.JUnitPlatform

@RunWith( classOf[ JUnitPlatform ] )
class SystemPropertyTest {

  @Test
  def propertyTest = {
    val property = SystemProperty.`kp:genAsciiNames`
    println( s"property=${property}" ) // FIXME null
  }

}
