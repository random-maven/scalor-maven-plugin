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
    final val `eclipse-prescomp` = "eclipse-prescomp"
    final val `eclipse-format` = "eclipse-format"

    final val `clean` = "clean"
    final val `clean-macro` = "clean-macro"
    final val `clean-main` = "clean-main"
    final val `clean-test` = "clean-test"

    final val `format` = "format"
    final val `format-macro` = "format-macro"
    final val `format-main` = "format-main"
    final val `format-test` = "format-test"

    final val `register` = "register"
    final val `register-macro` = "register-macro"
    final val `register-main` = "register-main"
    final val `register-test` = "register-test"

    final val `compile` = "compile"
    final val `compile-macro` = "compile-macro"
    final val `compile-main` = "compile-main"
    final val `compile-test` = "compile-test"

    final val `scala-js-link` = "scala-js-link"
    final val `scala-js-link-main` = "scala-js-link-main"
    final val `scala-js-link-test` = "scala-js-link-test"

    final val `scala-js-env-prov-nodejs` = "scala-js-env-prov-nodejs"
    final val `scala-js-env-prov-phantomjs` = "scala-js-env-prov-phantomjs"
    final val `scala-js-env-prov-webjars` = "scala-js-env-prov-webjars"

    final val `scala-js-env-conf-nodejs` = "scala-js-env-conf-nodejs"
    final val `scala-js-env-conf-phantomjs` = "scala-js-env-conf-phantomjs"

    final val `scala-native-link` = "scala-native-link"
    final val `scala-native-link-main` = "scala-native-link-main"
    final val `scala-native-link-test` = "scala-native-link-test"

    final val `scala-native-pack` = "scala-native-pack"
    final val `scala-native-pack-main` = "scala-native-pack-main"
    final val `scala-native-pack-test` = "scala-native-pack-test"

    final val `scaladoc` = "scaladoc"
    final val `scaladoc-macro` = "scaladoc-macro"
    final val `scaladoc-main` = "scaladoc-main"
    final val `scaladoc-test` = "scaladoc-test"

    final val `report` = "report"
    final val `report-main` = "report-main"
    final val `report-test` = "report-test"

    final val `setup-cross` = "setup-cross"

  }

  /**
   * Parameter names.
   */
  object param {
    def of( name : String ) = s"${A.maven.name}.${name}"
  }

}
