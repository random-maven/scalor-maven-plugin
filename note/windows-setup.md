
### Windows build setup

Download and install Windows OS in VM
* https://www.virtualbox.org/wiki/Downloads
* https://developer.microsoft.com/en-us/windows/downloads/virtual-machines

Download and install tools in Windows OS
* Git for windows https://git-scm.com/downloads
* JDK-9 for windows http://www.oracle.com/technetwork/java/javase/downloads/index.html

Change default Windows symlink policy
* https://blogs.windows.com/buildingapps/2016/12/02/symlinks-windows-10
* https://superuser.com/questions/104845/permission-to-make-symbolic-links-in-windows-7

Verify build with `cmd.exe`
```cmd

git config --global core.symlinks true
set java_home=c:\Program Files\Java\jdk-9.0.4\

mkdir c:\tmp
cd c:\tmp
git clone https://github.com/random-maven/scalor-maven-plugin.git
cd scalor-maven-plugin
git checkout develop

mvnw.cmd clean install -B -P skip-test

```
