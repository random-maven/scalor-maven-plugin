package scala.scalanative

import java.nio.file.Path

/**
 * Access private [scala.scalanative] features.
 */
object Access {

  // pending
  // pending
  // pending

  def process( config : build.Config, outpath : Path ) : Path = {

    val driver = optimizer.Driver.default( config.mode )

    val result = scanatLink( config, driver )

    if ( result.unresolved.nonEmpty ) {
      result.unresolved.map( _.show ).sorted.foreach { signature =>
        config.logger.error( s"cannot link: $signature" )
      }
      sys.error( "unable to link" )
    }

    val classCount = result.defns.count {
      case _ : nir.Defn.Class | _ : nir.Defn.Module | _ : nir.Defn.Trait => true
      case _ => false
    }

    val methodCount = result.defns.count( _.isInstanceOf[ nir.Defn.Define ] )
    config.logger.info(
      s"Discovered ${classCount} classes and ${methodCount} methods"
    )

    val optimized = scanatOptimize( config, driver, result )

    val generated = {
      codegenProduce( config, optimized )
      build.IO.getAll( config.workdir, "glob:**.ll" )
    }

    val objectFiles = llvmCompileGen( config, generated )

    val unpackedLib = llvmUnpackLib( config )

    llvmCompileLib( config, result, unpackedLib )

    llvmLinkRuntime( config, result, objectFiles, unpackedLib, outpath )

  }

  def llvmUnpackLib( config : build.Config ) = {
    build.LLVM.unpackNativelib( config.nativelib, config.workdir )
  }

  def llvmCompileLib( config : build.Config, result : linker.Result, unpackedLib : Path ) = {
    val nativelibConfig = config.withCompileOptions( "-O2" +: config.compileOptions )
    build.LLVM.compileNativelib( nativelibConfig, result, unpackedLib )
  }

  def llvmCompileGen( config : build.Config, generated : Seq[ Path ] ) = {
    build.LLVM.compile( config, generated )
  }

  def llvmLinkRuntime(
    config :      build.Config,
    result :      linker.Result,
    objectFiles : Seq[ Path ],
    unpackedLib : Path, outpath : Path
  ) = {
    build.LLVM.link( config, result, objectFiles, unpackedLib, outpath )
  }

  // wokring
  // wokring
  // wokring

  def codegenProduce( config : build.Config, assembly : Seq[ nir.Defn ] ) : Seq[ Path ] = {
    scalanative.codegen.CodeGen( config, assembly )
    val produced = build.IO.getAll( config.workdir, "glob:**.ll" )
    produced
  }

  def scanatLink( config : build.Config, driver : optimizer.Driver ) : linker.Result = {
    build.ScalaNative.link( config, driver )
  }

  def scanatOptimize(
    config : build.Config,
    driver : optimizer.Driver,
    result : linker.Result
  ) : Seq[ nir.Defn ] = {
    build.ScalaNative.optimize( config, driver, result.defns, result.dyns )
  }

  def selectOption[ T ]( buildMode : build.Mode, debugOpts : T, releaseOpts : T ) : T = {
    buildMode match {
      case build.Mode.Debug   => debugOpts
      case build.Mode.Release => releaseOpts
    }
  }

  def resultReport( result : linker.Result ) : String = {
    val classCount = result.defns.count {
      case _ : nir.Defn.Class | _ : nir.Defn.Module | _ : nir.Defn.Trait => true
      case _ => false
    }
    val methodCount = result.defns.count( _.isInstanceOf[ nir.Defn.Define ] )
    s"discovered ${classCount} classes and ${methodCount} methods"
  }

  def resultAssert( result : linker.Result ) : Unit = {
    if ( result.unresolved.nonEmpty ) {
      val list = result.unresolved.map( _.show ).mkString( "\n" )
      sys.error( s"Failed to link ${result.unresolved.size} nir files: \n${list}" )
    }
  }

  def runtimeLinks( config : build.Config, result : linker.Result ) = {
    config.gc.links ++ result.links.map( _.name )
  }

}
