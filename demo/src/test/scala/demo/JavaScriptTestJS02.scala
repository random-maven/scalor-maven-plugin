package demo

import org.junit.Test
import org.junit.Assert._

import scala.scalajs.js.annotation.JSExportTopLevel

import org.scalajs.dom
import org.scalajs.dom.document
import org.scalajs.jquery.jQuery

/**
 * This test is invoked in JS-VM.
 *
 * Convention for JavaScript testing:
 * - use JUnit 4 for JS, and JUnit 5 for JVM
 * - use custom JUnit 4 runner: @RunWith(ScalaJS_Suite)
 * - use JS test naming so they are not detected by SureFire plugin
 */
class JavaScriptTestJS02 {

  import JavaScriptTestJS02._

  @Test // This is JUnit 4.
  def verifyPrint() : Unit = {
    println( s"### Message from JS-VM @ ${getClass.getName} ###" )
  }

  @Test // This is JUnit 4.
  def verifyDOM() : Unit = {
    val text = "Hello DOM"
    assertFalse( document.body.textContent.contains( text ) )
    appendParagraph( document.body, text )
    assertTrue( document.body.textContent.contains( text ) )
  }

  @Test // This is JUnit 4.
  def verifyJQuery() : Unit = {
    val text = "Hello JQuery"
    assertFalse( jQuery( "body" ).contents().text().contains( text ) )
    jQuery( "body" ).append( s"<p>${text}</p>" )
    assertTrue( jQuery( "body" ).contents().text().contains( text ) )
  }

}

object JavaScriptTestJS02 {

  def appendParagraph( targetNode : dom.Node, text : String ) : Unit = {
    val parNode = document.createElement( "p" )
    val textNode = document.createTextNode( text )
    parNode.appendChild( textNode )
    targetNode.appendChild( parNode )
  }

  @JSExportTopLevel( "addClickedMessage" )
  def addClickedMessage() : Unit = {
    appendParagraph( document.body, "You clicked the button!" )
  }

}
