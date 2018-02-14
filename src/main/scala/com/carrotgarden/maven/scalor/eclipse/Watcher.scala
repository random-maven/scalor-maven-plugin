package com.carrotgarden.maven.scalor.eclipse

import scala.collection.concurrent.TrieMap

import org.eclipse.core.resources.IResourceChangeEvent
import org.eclipse.core.resources.IResourceChangeListener
import org.eclipse.core.resources.IResourceDelta
import org.eclipse.core.resources.IResourceDeltaVisitor
import org.eclipse.core.resources.ResourcesPlugin

import com.carrotgarden.maven.scalor.util.Logging.AnyLog
import com.carrotgarden.maven.scalor.util.Text

/**
 * Eclipse resource change listener.
 */
object Watcher {

  /**
   * Eclipse resource change detector.
   */
  case class Detector(
    buildList : Array[ String ],
    regexList : Array[ String ],
    logger :    Option[ AnyLog ]
  ) extends IResourceChangeListener with IResourceDeltaVisitor {

    val matchList = regexList.map( _.r )

    val resultMap = TrieMap[ String, String ]()

    @volatile
    var resultStart : Long = _

    @volatile
    var resultFinish : Long = _

    {
      resultClear()
    }

    def hasResult : Boolean = {
      !resultMap.isEmpty
    }

    def hasDelay( delay : Long ) : Boolean = {
      System.currentTimeMillis > delay + resultFinish
    }

    def hasResult( delay : Long ) : Boolean = {
      hasResult && hasDelay( delay )
    }

    def resultClear() : Unit = {
      resultStart = System.currentTimeMillis
      resultFinish = resultStart
      resultMap.clear()
    }

    def resultRegister( path : String ) : Unit = {
      resultMap.put( path, path )
      resultFinish = System.currentTimeMillis
    }

    def resultReport( limit : Int ) : String = {
      val array = resultMap.values.toList.sorted.take( limit ).toArray
      Text.reportArray( array )
    }

    import org.eclipse.core.resources.IResourceChangeEvent._
    import org.eclipse.core.resources.IResourceDelta._

    def hasChange( delta : IResourceDelta ) : Boolean = {
      ( delta.getKind & ALL_WITH_PHANTOMS ) != 0
    }

    def hasMatch( path : String ) : Boolean = {
      var index = 0; val count = matchList.length
      while ( index < count ) {
        val regex = matchList( index ); index += 1
        if ( regex.pattern.matcher( path ).matches ) {
          return true
        }
      }
      return false
    }

    def hasBuild( projectPath : String ) : Boolean = {
      var index = 0; val count = buildList.length
      while ( index < count ) {
        val buildPath = buildList( index ); index += 1
        // log( _.info( s"projectPath=${projectPath} buildPath=${buildPath}" ) )
        if ( buildPath.startsWith( projectPath ) ) {
          return true
        }
      }
      return false
    }

    def log = logger.foreach _

    override def visit( delta : IResourceDelta ) : Boolean = {
      log( _.info( s"visitor delta=${delta}" ) )
      if ( delta == null ) {
        return false
      }
      val resource = delta.getResource
      if ( resource == null ) {
        return false
      }
      val location = resource.getLocation
      if ( location == null ) {
        return false
      }
      val textPath = location.toFile.getCanonicalPath
      if ( hasMatch( textPath ) ) {
        log( _.info( s"register result=${textPath}" ) )
        resultRegister( textPath )
        false // terminate recursion
      } else {
        true // continue recursion
      }
    }

    override def resourceChanged( event : IResourceChangeEvent ) : Unit = {
      if ( event.getType != POST_CHANGE ) {
        return
      }
      val rootDelta = event.getDelta
      if ( rootDelta == null ) {
        return
      }
      log( _.info( s"root delta=${rootDelta}" ) )
      val projectDeltaList = rootDelta.getAffectedChildren
      projectDeltaList.foreach { projectDelta =>
        log( _.info( s"project delta=${projectDelta}" ) )
        val projectPath = projectDelta.getResource.getLocation.toFile.getCanonicalPath
        // FIXME spurious matches to parent
        if ( hasBuild( projectPath ) ) {
          projectDelta.accept( this )
        }
      }
    }

  }

  def workspace = ResourcesPlugin.getWorkspace

  def workspacePath = workspace.getRoot.getLocation

  /**
   *  Activate resource change listener.
   */
  def register( detector : Detector ) = {
    workspace.addResourceChangeListener( detector )
  }

  /**
   *  Deactivate resource change listener.
   */
  def unregister( detector : Detector ) = {
    workspace.removeResourceChangeListener( detector )
  }

}
