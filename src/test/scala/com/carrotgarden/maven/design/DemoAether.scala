package com.carrotgarden.maven.design

import com.jcabi.aether.Aether
import java.io.File
import java.util.Arrays
import org.sonatype.aether.repository.RemoteRepository
import org.sonatype.aether.util.artifact.DefaultArtifact
import scala.collection.JavaConverters._

object DemoAether {

  def main( args : Array[ String ] ) : Unit = {

    val local = new File( "/tmp/local-repository" );

    val remotes = Arrays.asList(
      new RemoteRepository(
        "maven-central",
        "default",
        "http://repo1.maven.org/maven2/"
      )
    )

    val deps = new Aether( remotes, local ).resolve(
      new DefaultArtifact( "junit", "junit-dep", "", "jar", "4.10" ),
      "runtime"
    )

    deps.asScala.foreach { artifact =>
      println( s"artifact ${artifact}" )
    }

  }

}
