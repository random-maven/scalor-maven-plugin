
/**
* forwarder/adapter for resources
* https://github.com/scala-native/scala-native/issues/1204
*/

// src/main/cdata content as single zip file
extern char _binary_cdata_zip_start;
extern char _binary_cdata_zip_end;
char* binary_cdata_zip_start() { return & _binary_cdata_zip_start; }
char* binary_cdata_zip_end()   { return & _binary_cdata_zip_end; }
