
### Sample `Maven Console` log of Eclipse companion plugin installation 

```
1/7/18, 6:14:31 PM CST: [INFO] [scalor:eclipse-config] Using Eclipse platform plugins:
1/7/18, 6:14:31 PM CST: [INFO] [scalor:eclipse-config]    org.eclipse.core.resources_3.12.0.v20170417-1558 [124]
1/7/18, 6:14:31 PM CST: [INFO] [scalor:eclipse-config]    org.eclipse.m2e.core_1.9.0.20171019-0117 [1195]
1/7/18, 6:14:31 PM CST: [INFO] [scalor:eclipse-config]    org.eclipse.m2e.core.ui_1.9.0.20171019-0117 [1196]
1/7/18, 6:14:31 PM CST: [INFO] [scalor:eclipse-config] Configuring companion Eclipse plugin:
1/7/18, 6:14:31 PM CST: [INFO] [scalor:eclipse-config]    pluginId: com.carrotgarden.maven.scalor-maven-plugin_2.12_1.0.0.20180107230449
1/7/18, 6:14:31 PM CST: [INFO] [scalor:eclipse-config]    location: file:/home/user0/.m2/repository/com/carrotgarden/maven/scalor-maven-plugin_2.12/1.0.0.20180107230449/scalor-maven-plugin_2.12-1.0.0.20180107230449.jar
1/7/18, 6:14:31 PM CST: [INFO] [scaler] Plugin start: com.carrotgarden.maven.scalor-maven-plugin_2.12_1.0.0.20180107230449
1/7/18, 6:14:31 PM CST: [INFO] [scalor:eclipse-config] Companion plugin is now installed in Eclipse: com.carrotgarden.maven.scalor-maven-plugin_2.12_1.0.0.20180107230449_0.0.0 [2143]
1/7/18, 6:14:31 PM CST: [INFO] [scalor:eclipse-config] Scheduling project update in Eclipse to invoke M2E project configurator.
1/7/18, 6:14:32 PM CST: [INFO] Using 'UTF-8' encoding to copy filtered resources.
1/7/18, 6:14:32 PM CST: [INFO] Copying 0 resource
1/7/18, 6:14:32 PM CST: [INFO] Using 'UTF-8' encoding to copy filtered resources.
1/7/18, 6:14:32 PM CST: [INFO] Copying 0 resource
1/7/18, 6:14:32 PM CST: [INFO] Resolving dependencies.
1/7/18, 6:14:33 PM CST: [INFO] Removing pom.xml model members.
1/7/18, 6:14:33 PM CST: [INFO] Switching project to flattened pom.xml.
1/7/18, 6:14:33 PM CST: [INFO] Update started
1/7/18, 6:14:34 PM CST: [INFO] Using org.eclipse.m2e.jdt.JarLifecycleMapping lifecycle mapping for MavenProject: com.carrotgarden.maven:scalor-maven-plugin-demo:0.20180108001433 @ /home/work/source/git/scalor-maven-plugin/demo/pom.xml.
1/7/18, 6:14:34 PM CST: [INFO] [scaler:step#1] Configuring dependency classpath.
1/7/18, 6:14:34 PM CST: [INFO] [scaler:step#1] Verifying Maven M2E version.
1/7/18, 6:14:34 PM CST: [INFO] [scaler:step#1]    version 1.9.0.20171019-0117 is in range [1.8.0,1.10.0)
1/7/18, 6:14:34 PM CST: [INFO] [scaler:step#1] Verifying Scala IDE version.
1/7/18, 6:14:34 PM CST: [INFO] [scaler:step#1]    version 4.7.1.local-2_12-201712302221-31d47eb is in range [4.7.0,4.7.2)
1/7/18, 6:14:34 PM CST: [INFO] [scaler:step#1] Ordering entries inside the .classpath Maven container.
1/7/18, 6:14:35 PM CST: [INFO] [scalor:register-macro] Register source root: /home/work/source/git/scalor-maven-plugin/demo/src/macro/java
1/7/18, 6:14:35 PM CST: [INFO] [scalor:register-macro] Register source root: /home/work/source/git/scalor-maven-plugin/demo/src/macro/scala
1/7/18, 6:14:35 PM CST: [INFO] [scalor:register-macro] Register target root: /home/work/source/git/scalor-maven-plugin/demo/target/macro-classes
1/7/18, 6:14:35 PM CST: [INFO] [scalor:register-macro] Ensuring build folders.
1/7/18, 6:14:35 PM CST: [INFO] [scalor:register-main] Already registered:   /home/work/source/git/scalor-maven-plugin/demo/src/main/resources
1/7/18, 6:14:35 PM CST: [INFO] [scalor:register-main] Already registered:   /home/work/source/git/scalor-maven-plugin/demo/src/main/java
1/7/18, 6:14:35 PM CST: [INFO] [scalor:register-main] Register source root: /home/work/source/git/scalor-maven-plugin/demo/src/main/scala
1/7/18, 6:14:35 PM CST: [INFO] [scalor:register-main] Already registered:   /home/work/source/git/scalor-maven-plugin/demo/target/classes
1/7/18, 6:14:35 PM CST: [INFO] [scalor:register-main] Ensuring build folders.
1/7/18, 6:14:35 PM CST: [INFO] [scalor:register-test] Already registered:   /home/work/source/git/scalor-maven-plugin/demo/src/test/resources
1/7/18, 6:14:35 PM CST: [INFO] [scalor:register-test] Already registered:   /home/work/source/git/scalor-maven-plugin/demo/src/test/java
1/7/18, 6:14:35 PM CST: [INFO] [scalor:register-test] Register source root: /home/work/source/git/scalor-maven-plugin/demo/src/test/scala
1/7/18, 6:14:35 PM CST: [INFO] [scalor:register-test] Already registered:   /home/work/source/git/scalor-maven-plugin/demo/target/test-classes
1/7/18, 6:14:35 PM CST: [INFO] [scalor:register-test] Ensuring build folders.
1/7/18, 6:14:35 PM CST: [INFO] Skipping incremental execution.
1/7/18, 6:14:35 PM CST: [INFO] Adding source folder /scalor-maven-plugin-demo/src/main/java
1/7/18, 6:14:35 PM CST: [INFO] Adding source folder /scalor-maven-plugin-demo/src/main/scala
1/7/18, 6:14:35 PM CST: [INFO] Adding resource folder /scalor-maven-plugin-demo/src/main/resources
1/7/18, 6:14:35 PM CST: [INFO] Adding source folder /scalor-maven-plugin-demo/src/test/java
1/7/18, 6:14:35 PM CST: [INFO] Adding source folder /scalor-maven-plugin-demo/src/test/scala
1/7/18, 6:14:35 PM CST: [INFO] Adding resource folder /scalor-maven-plugin-demo/src/test/resources
1/7/18, 6:14:35 PM CST: [INFO] [scaler:step#2] Configuring project classpath.
1/7/18, 6:14:35 PM CST: [INFO] [scaler:step#2]    entry: src/macro/scala -> target/macro-classes
1/7/18, 6:14:35 PM CST: [INFO] [scaler:step#2]    entry: src/macro/java -> target/macro-classes
1/7/18, 6:14:35 PM CST: [INFO] [scaler:step#2]    entry: src/main/java -> target/classes
1/7/18, 6:14:35 PM CST: [INFO] [scaler:step#2]    entry: src/main/scala -> target/classes
1/7/18, 6:14:35 PM CST: [INFO] [scaler:step#2]    entry: src/main/resources -> target/classes
1/7/18, 6:14:35 PM CST: [INFO] [scaler:step#2]    entry: src/test/java -> target/test-classes
1/7/18, 6:14:35 PM CST: [INFO] [scaler:step#2]    entry: src/test/scala -> target/test-classes
1/7/18, 6:14:35 PM CST: [INFO] [scaler:step#2]    entry: src/test/resources -> target/test-classes
1/7/18, 6:14:35 PM CST: [INFO] [scaler:step#2] Deleting container org.scala-ide.sdt.launching.SCALA_CONTAINER.
1/7/18, 6:14:35 PM CST: [INFO] [scaler:step#2] Ordering top level entries inside the .classpath.
1/7/18, 6:14:36 PM CST: [INFO] [scaler:step#1] Configuring dependency classpath.
1/7/18, 6:14:36 PM CST: [INFO] [scaler:step#1] Ordering entries inside the .classpath Maven container.
1/7/18, 6:14:36 PM CST: [INFO] [scaler:step#3] Configuring project settings.
1/7/18, 6:14:36 PM CST: [INFO] [scaler:step#3] Ensuring project natures.
1/7/18, 6:14:36 PM CST: [INFO] [scaler:step#3] Applying Eclipse .project comment.
1/7/18, 6:14:36 PM CST: [INFO] [scaler:step#3] Ordering Eclipse .project builder entries.
1/7/18, 6:14:36 PM CST: [INFO] [scaler:step#3] Ordering Eclipse .project nature entries.
1/7/18, 6:14:36 PM CST: [INFO] [scaler:step#3] Configuring Scala IDE.
1/7/18, 6:14:36 PM CST: [INFO] [scaler:step#3] Resolving custom Scala installation.
1/7/18, 6:14:37 PM CST: [INFO] Update completed: 3 sec
1/7/18, 6:14:37 PM CST: [INFO] [scaler:step#3+] Configuring Scala IDE (scheduled job).
1/7/18, 6:14:37 PM CST: [INFO] [scaler:step#3+] Persisting custom Scala installation.
1/7/18, 6:14:37 PM CST: [INFO] [scaler:step#3+] Updating project Scala settings.
1/7/18, 6:14:37 PM CST: [INFO] [scaler:step#3+] Providing configured settings.
1/7/18, 6:14:37 PM CST: [INFO] [scaler:step#3+] Resetting preferences to default.
1/7/18, 6:14:37 PM CST: [INFO] [scaler:step#3+] Persisting compiler selection.
1/7/18, 6:14:37 PM CST: [INFO] [scaler:step#3+] Persisting configured settings.
```
