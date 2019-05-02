
### Eclipse / Scala IDE setup

1. install [OpenJDK LTS](https://adoptopenjdk.net/installation.html?variant=openjdk11&jvmVariant=hotspot)

2. launch [Eclipse Installer](https://www.eclipse.org/downloads/packages/installer)

3. switch installer wizard to **advanced mode** via button &#9776;

![install_picture]

4. on **Product** wizard page, add user product URI:

https://raw.githubusercontent.com/random-maven/scalor-maven-plugin/develop/setup/scalor-product.setup

![product_picture]

5. on **Projects** wizard page, add user project URI:

https://raw.githubusercontent.com/random-maven/scalor-maven-plugin/develop/setup/scalor-project.setup

![project_picture]

6. select **Show all variables** to customize installation locations

7. finish installer wizard invocation, start product, await setup tasks completion

[install_picture]: https://raw.githubusercontent.com/random-maven/scalor-maven-plugin/develop/setup/picture/installer-advanced.png
[product_picture]: https://raw.githubusercontent.com/random-maven/scalor-maven-plugin/develop/setup/picture/scalor-product.png
[project_picture]: https://raw.githubusercontent.com/random-maven/scalor-maven-plugin/develop/setup/picture/scalor-project.png
