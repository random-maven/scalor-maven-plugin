
// scala source folder
def sourceMainDir = new File( basedir, "src/main/scala")

// scala doc folder
def scaladocMainDir = new File( basedir, "target/site/scaladoc-main")

// expected scala source files
def scalaMain = new File(sourceMainDir, "any/Main.scala") // should be present
def scalaSkip = new File(sourceMainDir, "any/Skip.scala") // should be present

// expected scala doc files
def htmlMain = new File(scaladocMainDir, "any/Main.html") // should be present
def htmlSkip = new File(scaladocMainDir, "any/Skip.html") // should be missing

assert scalaMain.exists()
assert scalaSkip.exists()

assert htmlMain.exists()
assert !htmlSkip.exists()
