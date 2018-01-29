//package demo.client
//
//import org.scalatest._
//import org.scalajs.jquery.jQuery
//import org.junit.runner.RunWith
//import org.scalatest.junit.JUnitRunner
//import org.scalatest.FunSuite
//
//@RunWith( classOf[ JUnitRunner ] )
//class ModuleTest extends FunSpec {
//
//  Module.main( Array() )
//
//  describe( "client module" ) {
//
//    it( "should contain 'Hello World' text in its body" ) {
//      assert( jQuery( "p:contains('Hello World')" ).length == 1 )
//    }
//
//    it( "should append 'You clicked the button!' text when the user clicks on the 'Click me!' button" ) {
//      def messageCount =
//        jQuery( "p:contains('You clicked the button!')" ).length
//
//      val button = jQuery( "button:contains('Click me!')" )
//      assert( button.length == 1 )
//      assert( messageCount == 0 )
//
//      for ( c <- 1 to 5 ) {
//        button.click()
//        assert( messageCount == c )
//      }
//    }
//
//  }
//
//}
