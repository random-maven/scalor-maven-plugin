
### Cross Scala version build

Integration test project for cross-scala-version build.

Project layout:

Master provides build definition
* master [pom.xml](pom.xml) 

Cross-version modules provide version overrides
* module [pom-2.11.xml](pom-2.11.xml)
* module [pom-2.12.xml](pom-2.12.xml)
* module [pom-2.13.xml](pom-2.13.xml)  
