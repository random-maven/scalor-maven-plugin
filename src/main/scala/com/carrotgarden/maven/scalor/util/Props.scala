package com.carrotgarden.maven.scalor.util

import java.net.URL
import java.util.Properties
import java.util.Collections
import java.util.Enumeration
import java.util.Vector
import java.util.Map

object Props {

  case class SortedProperties() extends Properties {
    override def keys : Enumeration[ Object ] = {
      val keysEnum = super.keys
      val keysList = new Vector[ String ]()
      while ( keysEnum.hasMoreElements ) {
        keysList.add( keysEnum.nextElement.asInstanceOf[ String ] )
      }
      Collections.sort( keysList )
      keysList.elements().asInstanceOf[ Enumeration[ Object ] ]
    }
  }

  def propertiesFrom( url : URL ) : Properties = {
    val config = new Properties
    val input = url.openStream()
    config.load( input )
    config
  }

}
