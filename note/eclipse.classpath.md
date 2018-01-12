
### Sample Eclipse `.classpath` descriptor

Location:
* `${project.basedir}/.classpath`

What to look for:
* expected `<classpathentry>` are present
* `<classpathentry>` are ordered as expected
* `<attribute name="scalor.scope">` are present
  
```xml
<?xml version="1.0" encoding="UTF-8"?>
<classpath>
    <classpathentry kind="src" output="target/scalor/classes/macro" path="src/macro/java">
        <attributes>
            <attribute name="maven.pomderived" value="true"/>
            <attribute name="optional" value="true"/>
            <attribute name="scalor.scope" value="macro"/>
        </attributes>
    </classpathentry>
    <classpathentry kind="src" output="target/scalor/classes/macro" path="src/macro/scala">
        <attributes>
            <attribute name="maven.pomderived" value="true"/>
            <attribute name="optional" value="true"/>
            <attribute name="scalor.scope" value="macro"/>
        </attributes>
    </classpathentry>
    <classpathentry kind="src" output="target/scalor/classes/main" path="src/main/java">
        <attributes>
            <attribute name="maven.pomderived" value="true"/>
            <attribute name="optional" value="true"/>
            <attribute name="scalor.scope" value="main"/>
        </attributes>
    </classpathentry>
    <classpathentry kind="src" output="target/scalor/classes/main" path="src/main/scala">
        <attributes>
            <attribute name="maven.pomderived" value="true"/>
            <attribute name="optional" value="true"/>
            <attribute name="scalor.scope" value="main"/>
        </attributes>
    </classpathentry>
    <classpathentry kind="src" output="target/scalor/classes/main" path="src/main/resources">
        <attributes>
            <attribute name="maven.pomderived" value="true"/>
            <attribute name="optional" value="true"/>
            <attribute name="scalor.scope" value="main"/>
        </attributes>
    </classpathentry>
    <classpathentry kind="src" output="target/scalor/classes/test" path="src/test/java">
        <attributes>
            <attribute name="maven.pomderived" value="true"/>
            <attribute name="optional" value="true"/>
            <attribute name="scalor.scope" value="test"/>
        </attributes>
    </classpathentry>
    <classpathentry kind="src" output="target/scalor/classes/test" path="src/test/scala">
        <attributes>
            <attribute name="maven.pomderived" value="true"/>
            <attribute name="optional" value="true"/>
            <attribute name="scalor.scope" value="test"/>
        </attributes>
    </classpathentry>
    <classpathentry kind="src" output="target/scalor/classes/test" path="src/test/resources">
        <attributes>
            <attribute name="maven.pomderived" value="true"/>
            <attribute name="optional" value="true"/>
            <attribute name="scalor.scope" value="test"/>
        </attributes>
    </classpathentry>
    <classpathentry kind="con" path="org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-1.8">
        <attributes>
            <attribute name="maven.pomderived" value="true"/>
        </attributes>
    </classpathentry>
    <classpathentry kind="con" path="org.eclipse.m2e.MAVEN2_CLASSPATH_CONTAINER">
        <attributes>
            <attribute name="maven.pomderived" value="true"/>
        </attributes>
    </classpathentry>
    <classpathentry kind="output" path="target/classes"/>
</classpath>
```
