import java.io.File

def hasExecutable(File file) {
	file.exists() && file.isFile() && file.canRead() && file.canExecute()
}

File provisionedNodejs = new File( basedir, "test-tool/node/node" )
File provisionedPhantomjs = new File( basedir, "test-tool/phantomjs/phantomjs" )
File provisionedWebjars = new File( basedir, "test-tool/webjars" )
File provisionedWebjarsJQuery = new File( provisionedWebjars, "jquery/jquery.js" )

assert hasExecutable(provisionedNodejs)
assert hasExecutable(provisionedPhantomjs)
assert provisionedWebjars.exists()
assert provisionedWebjarsJQuery.exists()
