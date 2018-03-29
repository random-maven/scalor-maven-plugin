
### Cross Scala version build

Integration test project for cross-scala-version build.

Project layout:
* using symlinks for `src`

Master provides build definition
* master [pom.xml](pom.xml) 

Cross-version modules provide version overrides
* module [cross/2.11/pom.xml](cross/2.11/pom.xml)
* module [cross/2.12/pom.xml](cross/2.12/pom.xml)
* module [cross/2.13/pom.xml](cross/2.13/pom.xml)  
