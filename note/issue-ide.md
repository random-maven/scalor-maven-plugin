
### Scala IDE nature decorator

scala plugin uses simple nature decorator
```xml
   <extension
         point="org.eclipse.ui.ide.projectNatureImages">
      <image
            natureId="org.scala-ide.sdt.core.scalanature"
            icon="icons/full/ovr16/scala_co.gif"
            id="org.scala-ide.sdt.core.scalaNatureImage"/>
   </extension>
```

which depends on order in
``` 
${project.basedir}/.project!<natures>/<nature>
```

work around is to ensure scala nature comes first

### Scala IDE -Xsource / -Ymacro-expand handling

scala plugin goes extra mile to remove user-provided -Xsource / -Ymacro-expand options

for example: scala plugin log entry:
```
Adding  -Xsource:2.11 -Ymacro-expand:none to compiler arguments of scalor-maven-plugin-demo because of: Persisting compiler selection
```

relevant code
```
org.scalaide.core.internal.project.InstallationManagement
```

to restore "normal" behaviour would require use of aop
