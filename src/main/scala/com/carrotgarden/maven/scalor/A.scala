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

    final val `auto-conf` = "auto-conf"

    final val `eclipse-config` = "eclipse-config"

    final val `clean-macro` = "clean-macro"
    final val `clean-main` = "clean-main"
    final val `clean-test` = "clean-test"

    final val `register-macro` = "register-macro"
    final val `register-main` = "register-main"
    final val `register-test` = "register-test"

    final val `compile-macro` = "compile-macro"
    final val `compile-main` = "compile-main"
    final val `compile-test` = "compile-test"

    final val `link-scala-js-main` = "link-scala-js-main"
    final val `link-scala-js-test` = "link-scala-js-test"

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
