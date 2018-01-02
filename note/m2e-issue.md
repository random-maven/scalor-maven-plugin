
### Pom Editor class loading

```
org.eclipse.m2e.editor.xml.mojo.PlexusConfigHelper
  public List<Class> getCandidateClasses(ClassRealm realm, Class enclosingClass, Class paramClass) {
```

uses realms class loader which fails when encountering 
any top level classes which can not be resolved 
from plugin dependencies

these classes are present because of eclipse api, scope=provided
which are used by eclipse companion plugin

workaround: 
hide classes which load eclipse api as inner static classes
i.e. do not use them as top level classes
