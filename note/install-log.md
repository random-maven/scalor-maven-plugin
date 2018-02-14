
### Sample `Maven Console` log of Eclipse companion plugin installation 

```
2/13/18, 9:40:55 PM CST: [INFO] Update started
2/13/18, 9:40:55 PM CST: [INFO] [scalor] [eclipse-config] Using Eclipse platform plugins:
2/13/18, 9:40:55 PM CST: [INFO] [scalor] [eclipse-config]    org.eclipse.core.resources_3.12.0.v20170417-1558 [124]
2/13/18, 9:40:55 PM CST: [INFO] [scalor] [eclipse-config]    org.eclipse.m2e.core_1.9.0.20171019-0117 [1033]
2/13/18, 9:40:55 PM CST: [INFO] [scalor] [eclipse-config]    org.eclipse.m2e.core.ui_1.9.0.20171019-0117 [1034]
2/13/18, 9:40:55 PM CST: [INFO] [scalor] [eclipse-config] Configuring companion Eclipse plugin:
2/13/18, 9:40:55 PM CST: [INFO] [scalor] [eclipse-config]    pluginId: com.carrotgarden.maven.scalor-maven-plugin_2.12_1.2.2.20180214032451
2/13/18, 9:40:55 PM CST: [INFO] [scalor] [eclipse-config]    location: file:/home/user0/.m2/repository/com/carrotgarden/maven/scalor-maven-plugin_2.12/1.2.2.20180214032451/scalor-maven-plugin_2.12-1.2.2.20180214032451.jar
2/13/18, 9:40:56 PM CST: [INFO] [scaler] Plugin start: com.carrotgarden.maven.scalor-maven-plugin_2.12_1.2.2.20180214032451
2/13/18, 9:40:56 PM CST: [INFO] [scalor] [eclipse-config] Companion plugin installed in Eclipse: com.carrotgarden.maven.scalor-maven-plugin_2.12_1.2.2.20180214032451_0.0.0 [1836]
2/13/18, 9:40:56 PM CST: [INFO] [scalor] [eclipse-config] Scheduling project update in Eclipse to invoke M2E project configurator.
2/13/18, 9:40:56 PM CST: [INFO] Using 'UTF-8' encoding to copy filtered resources.
2/13/18, 9:40:56 PM CST: [INFO] Copying 1 resource
2/13/18, 9:40:56 PM CST: [INFO] Using 'UTF-8' encoding to copy filtered resources.
2/13/18, 9:40:56 PM CST: [INFO] Copying 2 resources
2/13/18, 9:40:57 PM CST: [INFO] Update started
2/13/18, 9:40:57 PM CST: [INFO] Using org.eclipse.m2e.jdt.JarLifecycleMapping lifecycle mapping for MavenProject: com.carrotgarden.maven:scalor-maven-plugin-demo:0-SNAPSHOT @ /home/work/source/git/scalor-maven-plugin/demo/pom.xml.
2/13/18, 9:40:57 PM CST: [INFO] [scaler] [config-step#1] Configuring container org.eclipse.m2e.MAVEN2_CLASSPATH_CONTAINER.
2/13/18, 9:40:57 PM CST: [INFO] [scaler] [config-step#1] Verifying Maven M2E version.
2/13/18, 9:40:57 PM CST: [INFO] [scaler] [config-step#1]    version 1.9.0.20171019-0117 is in range [1.8.0,1.10.0)
2/13/18, 9:40:57 PM CST: [INFO] [scaler] [config-step#1] Verifying Scala IDE version.
2/13/18, 9:40:57 PM CST: [INFO] [scaler] [config-step#1]    version 4.7.1.local-2_12-201802082343-1eb9709 is in range [4.7.0,4.7.2)
2/13/18, 9:40:57 PM CST: [INFO] [scaler] [config-step#1] Ordering entries inside the Maven container.
2/13/18, 9:40:59 PM CST: [INFO] [scalor] [register-macro] Registering root:   /home/work/source/git/scalor-maven-plugin/demo/src/macro/java
2/13/18, 9:40:59 PM CST: [INFO] [scalor] [register-macro] Registering root:   /home/work/source/git/scalor-maven-plugin/demo/src/macro/scala
2/13/18, 9:40:59 PM CST: [INFO] [scalor] [register-macro] Already registered: /home/work/source/git/scalor-maven-plugin/demo/target/classes
2/13/18, 9:40:59 PM CST: [INFO] [scalor] [register-macro] Ensuring build folders.
2/13/18, 9:40:59 PM CST: [INFO] [scalor] [register-main] Already registered: /home/work/source/git/scalor-maven-plugin/demo/src/main/resources
2/13/18, 9:40:59 PM CST: [INFO] [scalor] [register-main] Already registered: /home/work/source/git/scalor-maven-plugin/demo/src/main/java
2/13/18, 9:40:59 PM CST: [INFO] [scalor] [register-main] Registering root:   /home/work/source/git/scalor-maven-plugin/demo/src/main/scala
2/13/18, 9:40:59 PM CST: [INFO] [scalor] [register-main] Already registered: /home/work/source/git/scalor-maven-plugin/demo/target/classes
2/13/18, 9:40:59 PM CST: [INFO] [scalor] [register-main] Ensuring build folders.
2/13/18, 9:40:59 PM CST: [INFO] [scalor] [register-test] Already registered: /home/work/source/git/scalor-maven-plugin/demo/src/test/resources
2/13/18, 9:40:59 PM CST: [INFO] [scalor] [register-test] Already registered: /home/work/source/git/scalor-maven-plugin/demo/src/test/java
2/13/18, 9:40:59 PM CST: [INFO] [scalor] [register-test] Registering root:   /home/work/source/git/scalor-maven-plugin/demo/src/test/scala
2/13/18, 9:40:59 PM CST: [INFO] [scalor] [register-test] Already registered: /home/work/source/git/scalor-maven-plugin/demo/target/test-classes
2/13/18, 9:40:59 PM CST: [INFO] [scalor] [register-test] Ensuring build folders.
2/13/18, 9:40:59 PM CST: [INFO] Adding source folder /scalor-maven-plugin-demo/src/main/java
2/13/18, 9:40:59 PM CST: [INFO] Adding source folder /scalor-maven-plugin-demo/src/macro/java
2/13/18, 9:40:59 PM CST: [INFO] Adding source folder /scalor-maven-plugin-demo/src/macro/scala
2/13/18, 9:40:59 PM CST: [INFO] Adding source folder /scalor-maven-plugin-demo/src/main/scala
2/13/18, 9:40:59 PM CST: [INFO] Adding resource folder /scalor-maven-plugin-demo/src/main/resources
2/13/18, 9:40:59 PM CST: [INFO] Adding source folder /scalor-maven-plugin-demo/src/test/java
2/13/18, 9:40:59 PM CST: [INFO] Adding source folder /scalor-maven-plugin-demo/src/test/scala
2/13/18, 9:40:59 PM CST: [INFO] Adding resource folder /scalor-maven-plugin-demo/src/test/resources
2/13/18, 9:40:59 PM CST: [INFO] [scaler] [config-step#2] Configuring project classpath.
2/13/18, 9:40:59 PM CST: [INFO] [scaler] [config-step#2]    /scalor-maven-plugin-demo/src/macro/java -> /scalor-maven-plugin-demo/target/classes
2/13/18, 9:40:59 PM CST: [INFO] [scaler] [config-step#2]    /scalor-maven-plugin-demo/src/macro/scala -> /scalor-maven-plugin-demo/target/classes
2/13/18, 9:40:59 PM CST: [INFO] [scaler] [config-step#2]    /scalor-maven-plugin-demo/src/main/resources -> /scalor-maven-plugin-demo/target/classes
2/13/18, 9:40:59 PM CST: [INFO] [scaler] [config-step#2]    /scalor-maven-plugin-demo/src/main/java -> /scalor-maven-plugin-demo/target/classes
2/13/18, 9:40:59 PM CST: [INFO] [scaler] [config-step#2]    /scalor-maven-plugin-demo/src/main/scala -> /scalor-maven-plugin-demo/target/classes
2/13/18, 9:40:59 PM CST: [INFO] [scaler] [config-step#2]    /scalor-maven-plugin-demo/src/test/resources -> /scalor-maven-plugin-demo/target/test-classes
2/13/18, 9:40:59 PM CST: [INFO] [scaler] [config-step#2]    /scalor-maven-plugin-demo/src/test/java -> /scalor-maven-plugin-demo/target/test-classes
2/13/18, 9:40:59 PM CST: [INFO] [scaler] [config-step#2]    /scalor-maven-plugin-demo/src/test/scala -> /scalor-maven-plugin-demo/target/test-classes
2/13/18, 9:40:59 PM CST: [INFO] [scaler] [config-step#2] Deleting container org.scala-ide.sdt.launching.SCALA_CONTAINER.
2/13/18, 9:40:59 PM CST: [INFO] [scaler] [config-step#2] Ordering top level entries inside the .classpath.
2/13/18, 9:40:59 PM CST: [INFO] [scaler] [config-step#1] Configuring container org.eclipse.m2e.MAVEN2_CLASSPATH_CONTAINER.
2/13/18, 9:40:59 PM CST: [INFO] [scaler] [config-step#1] Ordering entries inside the Maven container.
2/13/18, 9:40:59 PM CST: [INFO] [scaler] [config-step#3] Configuring project settings.
2/13/18, 9:40:59 PM CST: [INFO] [scaler] [config-step#3] Hacking .project symbolic links.
2/13/18, 9:40:59 PM CST: [INFO] [scaler] [config-step#3] Applying Eclipse .project comment.
2/13/18, 9:40:59 PM CST: [INFO] [scaler] [config-step#3] Ensuring required .project natures.
2/13/18, 9:40:59 PM CST: [INFO] [scaler] [config-step#3] Ordering Eclipse .project builder entries.
2/13/18, 9:40:59 PM CST: [INFO] [scaler] [config-step#3] Ordering Eclipse .project nature entries.
2/13/18, 9:40:59 PM CST: [INFO] [scaler] [config-step#3] Configuring Scala IDE.
2/13/18, 9:40:59 PM CST: [INFO] [scaler] [config-step#3] Resolving custom Scala installation.
2/13/18, 9:41:00 PM CST: [INFO] Update completed: 3 sec
2/13/18, 9:41:00 PM CST: [INFO] [scaler] [config-step#3] Configuring Scala IDE (scheduled job).
2/13/18, 9:41:00 PM CST: [INFO] [scaler] [config-step#3] Persisting custom Scala installation.
2/13/18, 9:41:00 PM CST: [INFO] [scaler] [config-step#3] Updating project Scala settings.
2/13/18, 9:41:00 PM CST: [INFO] [scaler] [config-step#3] Providing configured settings.
2/13/18, 9:41:00 PM CST: [INFO] [scaler] [config-step#3] Resetting preferences to default.
2/13/18, 9:41:00 PM CST: [INFO] [scaler] [config-step#3] Persisting compiler selection.
2/13/18, 9:41:01 PM CST: [INFO] [scaler] [config-step#3] Persisting configured settings.
2/13/18, 9:41:11 PM CST: [INFO] Using 'UTF-8' encoding to copy filtered resources.
2/13/18, 9:41:11 PM CST: [INFO] Copying 1 resource
2/13/18, 9:41:11 PM CST: [INFO] [scalor] [scala-js-link-main] Full linker build request.
2/13/18, 9:41:11 PM CST: [INFO] [scalor] [scala-js-link-main] Scala.js library present: org.scala-js:scalajs-library_2.12:jar:0.6.22:provided.
2/13/18, 9:41:11 PM CST: [INFO] [scalor] [scala-js-link-main] Linker runtime: /home/work/source/git/scalor-maven-plugin/demo/target/classes/META-INF/resources/script/runtime.js
2/13/18, 9:41:11 PM CST: [INFO] [scalor] [scala-js-link-main] Linker update result: None
2/13/18, 9:41:11 PM CST: [INFO] [scalor] [scala-js-link-main] [time]   0 s  18 ms @ Cacher: Process dirs
2/13/18, 9:41:12 PM CST: [INFO] [scalor] [scala-js-link-main] [time]   0 s 325 ms @ Cacher: Process jars
2/13/18, 9:41:12 PM CST: [INFO] [scalor] [scala-js-link-main] [time]   0 s 188 ms @ Linker: Compute reachability
2/13/18, 9:41:12 PM CST: [INFO] [scalor] [scala-js-link-main] [time]   0 s 262 ms @ Linker: Assemble LinkedClasses
2/13/18, 9:41:12 PM CST: [INFO] [scalor] [scala-js-link-main] [time]   0 s 594 ms @ Basic Linking
2/13/18, 9:41:13 PM CST: [INFO] [scalor] [scala-js-link-main] [time]   0 s 352 ms @ Emitter (write output)
2/13/18, 9:41:13 PM CST: [INFO] [scalor] [scala-js-link-main] [time]   1 s 365 ms @ Total invocation time
2/13/18, 9:41:13 PM CST: [INFO] [scalor] [scala-js-link-main] Cacher stats: dirs/files=1/9 jars/files=5/3911
2/13/18, 9:41:13 PM CST: [INFO] Using 'UTF-8' encoding to copy filtered resources.
2/13/18, 9:41:13 PM CST: [INFO] Copying 2 resources
2/13/18, 9:41:13 PM CST: [INFO] [scalor] [scala-js-link-test] Full linker build request.
2/13/18, 9:41:13 PM CST: [INFO] [scalor] [scala-js-link-test] Scala.js library present: org.scala-js:scalajs-library_2.12:jar:0.6.22:provided.
2/13/18, 9:41:13 PM CST: [INFO] [scalor] [scala-js-link-test] Linker runtime: /home/work/source/git/scalor-maven-plugin/demo/target/test-classes/META-INF/resources/script-test/runtime-test.js
2/13/18, 9:41:13 PM CST: [INFO] [scalor] [scala-js-link-test] Linker update result: None
2/13/18, 9:41:13 PM CST: [INFO] [scalor] [scala-js-link-test] [time]   0 s   2 ms @ Cacher: Process dirs
2/13/18, 9:41:13 PM CST: [INFO] [scalor] [scala-js-link-test] [time]   0 s 356 ms @ Cacher: Process jars
2/13/18, 9:41:14 PM CST: [INFO] [scalor] [scala-js-link-test] [time]   0 s 353 ms @ Linker: Compute reachability
2/13/18, 9:41:15 PM CST: [INFO] [scalor] [scala-js-link-test] [time]   1 s 201 ms @ Linker: Assemble LinkedClasses
2/13/18, 9:41:15 PM CST: [INFO] [scalor] [scala-js-link-test] [time]   1 s 662 ms @ Basic Linking
2/13/18, 9:41:15 PM CST: [INFO] [scalor] [scala-js-link-test] [time]   0 s 643 ms @ Emitter (write output)
2/13/18, 9:41:15 PM CST: [INFO] [scalor] [scala-js-link-test] [time]   2 s 679 ms @ Total invocation time
2/13/18, 9:41:15 PM CST: [INFO] [scalor] [scala-js-link-test] Cacher stats: dirs/files=2/26 jars/files=39/4175
2/13/18, 9:41:16 PM CST: [INFO] [scaler] [restart-manager] Manager create @ /home/work/source/git/scalor-maven-plugin/demo
2/13/18, 9:41:16 PM CST: [INFO] [scaler] [restart-manager] Periodic job scheduled: Scalor: application restart manager @ scalor-maven-plugin-demo(489)
2/13/18, 9:41:16 PM CST: [INFO] [scaler] [build] Manager running @ Scalor: application restart manager @ scalor-maven-plugin-demo(489)
2/13/18, 9:41:16 PM CST: [INFO] [scaler] [prescomp-manager] Periodic job scheduled: Scalor: presenation compiler manager @ scalor-maven-plugin-demo(491)
2/13/18, 9:41:16 PM CST: [INFO] [scaler] [build] Manager running @ Scalor: presenation compiler manager @ scalor-maven-plugin-demo(491)
2/13/18, 9:41:17 PM CST: [INFO] [scaler] [restart-manager] Manager setup @ Scalor: application restart manager @ scalor-maven-plugin-demo(489)
2/13/18, 9:41:17 PM CST: [INFO] [scaler] [restart-manager] Process create @ Scalor: application restart manager @ scalor-maven-plugin-demo(489)
2/13/18, 9:41:19 PM CST: [INFO] [scaler] [prescomp-manager] Manager setup @ Scalor: presenation compiler manager @ scalor-maven-plugin-demo(491)
2/13/18, 9:41:19 PM CST: [INFO] Using 'UTF-8' encoding to copy filtered resources.
2/13/18, 9:41:19 PM CST: [INFO] Copying 0 resource
2/13/18, 9:41:19 PM CST: [INFO] [scalor] [scala-js-link-main] Incremental build request.
2/13/18, 9:41:19 PM CST: [INFO] Using 'UTF-8' encoding to copy filtered resources.
2/13/18, 9:41:19 PM CST: [INFO] Copying 2 resources
2/13/18, 9:41:19 PM CST: [INFO] [scalor] [scala-js-link-test] Incremental build request.
2/13/18, 9:41:23 PM CST: [INFO] [scaler] [restart-manager] Process detect (full list) @ Scalor: application restart manager @ scalor-maven-plugin-demo(489):
   /home/work/source/git/scalor-maven-plugin/demo/target/test-classes/META-INF/resources/script-test/runtime-test.js
2/13/18, 9:41:23 PM CST: [INFO] [scaler] [restart-manager] Process update @ Scalor: application restart manager @ scalor-maven-plugin-demo(489)
2/13/18, 9:41:23 PM CST: [INFO] [scaler] [restart-manager] Process delete @ Scalor: application restart manager @ scalor-maven-plugin-demo(489)
2/13/18, 9:41:23 PM CST: [INFO] [scaler] [restart-manager] Process create @ Scalor: application restart manager @ scalor-maven-plugin-demo(489)
2/13/18, 9:41:23 PM CST: [INFO] [scaler] [restart-process] ### Test application @ 2018-02-14_03-41-23 ###
2/13/18, 9:41:28 PM CST: [INFO] [scaler] [restart-process] ### Test application @ 2018-02-14_03-41-23 ###
2/13/18, 9:41:33 PM CST: [INFO] [scaler] [restart-process] ### Test application @ 2018-02-14_03-41-23 ###
```
