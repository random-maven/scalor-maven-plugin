
### Sample Eclipse `.project` descriptor

Location:
* `${project.basedir}/.project`

What to look for:
* expected `<comment>` is present
* `<natures>` are ordered as expected 
* `<buildCommand>` are ordered as expected
* `scalanature`, `javanature`, `maven2Nature` are present

```xml
<?xml version="1.0" encoding="UTF-8"?>
<projectDescription>
    <name>scalor-maven-plugin_2.12</name>
    <comment>scalor-maven-plugin @ 20180114043512</comment>
    <projects>
    </projects>
    <buildSpec>
        <buildCommand>
            <name>org.scala-ide.sdt.core.scalabuilder</name>
            <arguments>
            </arguments>
        </buildCommand>
        <buildCommand>
            <name>org.eclipse.m2e.core.maven2Builder</name>
            <arguments>
            </arguments>
        </buildCommand>
    </buildSpec>
    <natures>
        <nature>org.scala-ide.sdt.core.scalanature</nature>
        <nature>org.eclipse.jdt.core.javanature</nature>
        <nature>org.eclipse.pde.PluginNature</nature>
        <nature>org.eclipse.m2e.core.maven2Nature</nature>
    </natures>
    <linkedResources>
        <link>
            <name>src</name>
            <type>2</type>
            <location>/home/work/source/git/scalor-maven-plugin/src</location>
        </link>
        <link>
            <name>test-repo</name>
            <type>2</type>
            <location>/home/work/source/git/scalor-maven-plugin/cross/2.12/test-repo</location>
        </link>
    </linkedResources>
</projectDescription>
```
