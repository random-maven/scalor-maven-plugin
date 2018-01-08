
### Pom Editor class loading

m2e pom.xml editor

```
org.eclipse.m2e.editor.xml.mojo.PlexusConfigHelper
  public List<Class> getCandidateClasses(ClassRealm realm, Class enclosingClass, Class paramClass) {
```

uses realms class loader which fails when encountering 
any top level classes which can not be resolved 
from runtime plugin dependencies

these classes are present because of eclipse api, scope=provided
artifacts, which are used by eclipse companion plugin

workaround: 
hide classes which load eclipse api as inner static classes,
i.e. do not use them as top level classes

### M2E Project Configuration Manager

sends spurious events which result in multiple project configurator instantiations

```
org.eclipse.m2e.core.internal.project.ProjectConfigurationManager
  /*package*/Map<String, IStatus> updateProjectConfiguration0(Collection<IFile> pomFiles, boolean updateConfiguration,
      boolean cleanProjects, boolean refreshFromLocal, IProgressMonitor monitor) {

    // refresh projects and update all dependencies
    // this will ensure that project registry is up-to-date on GAV of all projects being updated
    // TODO this sends multiple update events, rework using low-level registry update methods
    try {
      projectManager.refresh(pomsToRefresh, new SubProgressMonitor(monitor, pomFiles.size()));

```

1) work around: use lazy vals for all configurator fields

2) work around: implement IExecutableExtensionFactory#create() and return singleton
