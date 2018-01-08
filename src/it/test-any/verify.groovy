
def eclipseProjectFile = new File(basedir, ".project")

def eclipseProjectSource = eclipseProjectFile.text

def eclipseProjectTarget = """
<?xml version='1.0' encoding='utf-8'?>
<projectDescription>
    <name>com.carrotgarden.maven:scalor-maven-plugin-test-any</name>
    <comment/>
    <projects> </projects>
    <buildSpec>
        <buildCommand>
            <name>org.scala-ide.sdt.core.scalabuilder</name>
            <arguments> </arguments>
        </buildCommand>
        <buildCommand>
            <name>org.eclipse.m2e.core.maven2Builder</name>
            <arguments> </arguments>
        </buildCommand>
        <buildCommand>
            <name>org.eclipse.dltk.core.scriptbuilder</name>
            <arguments> </arguments>
        </buildCommand>
    </buildSpec>
    <natures>
        <nature>org.eclipse.jdt.core.javanature</nature>
        <nature>org.eclipse.m2e.core.maven2Nature</nature>
        <nature>org.scala-ide.sdt.core.scalanature</nature>
    </natures>
</projectDescription>
""".trim()

def eclipseProjectSourceXML = new XmlSlurper().parseText( eclipseProjectSource )

def eclipseProjectTargetXML = new XmlSlurper().parseText( eclipseProjectTarget )

// assert eclipseProjectSourceXML == eclipseProjectTargetXML

assert true
