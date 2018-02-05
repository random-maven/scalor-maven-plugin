package com.carrotgarden.maven.scalor

import java.io.File

/**
 * Shared name space.
 */
object A {

  /**
   * Plugin identity.
   */
  object maven {

    /**
     * Short self name for Maven.
     */
    final val name = "scalor"

    // FIMXE remove
    final val artifactId = "scalor-maven-plugin"
    /**
     * Plugin user property naming pattern: ${plugin.property...}
     */
    final val propertyRegex = ( "^[$][{](" + name + "[.].+)[}]$" ).r

    /**
     * Well known maven plugins.
     */
    final val compilerId = "maven-compiler-plugin"

  }

  /**
   * Plugin identity.
   */
  object eclipse {

    /**
     * Short self name for Eclipse.
     */
    final val name = "scaler"

  }

  /**
   * Mojo names.
   */
  object mojo {

    final val `eclipse-config` = "eclipse-config"

    final val `eclipse-restart` = "eclipse-restart"

    final val `clean-macro` = "clean-macro"
    final val `clean-main` = "clean-main"
    final val `clean-test` = "clean-test"

    final val `register-macro` = "register-macro"
    final val `register-main` = "register-main"
    final val `register-test` = "register-test"

    final val `compile-macro` = "compile-macro"
    final val `compile-main` = "compile-main"
    final val `compile-test` = "compile-test"

    final val `scala-js-link-main` = "scala-js-link-main"
    final val `scala-js-link-test` = "scala-js-link-test"

    final val `scala-js-env-prov-nodejs` = "scala-js-env-prov-nodejs"
    final val `scala-js-env-prov-phantomjs` = "scala-js-env-prov-phantomjs"
    final val `scala-js-env-prov-webjars` = "scala-js-env-prov-webjars"
    
    final val `scala-js-env-conf-nodejs` = "scala-js-env-conf-nodejs"
    final val `scala-js-env-conf-phantomjs` = "scala-js-env-conf-phantomjs"

    final val `scaladoc-macro` = "scaladoc-macro"
    final val `scaladoc-main` = "scaladoc-main"
    final val `scaladoc-test` = "scaladoc-test"

    final val `report-main` = "report-main"
    final val `report-test` = "report-test"
  }

  /**
   * Parameter names.
   */
  object param {
    def of( name : String ) = A.maven.name + "." + name
  }

}
