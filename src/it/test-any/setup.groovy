
def eclipseProjectFile = new File(basedir, ".project")

def eclipseProjectSource = """
<?xml version="1.0" encoding="UTF-8"?>
<projectDescription>
    <name>invalid</name>
    <comment></comment>
    <projects>
    </projects>
    <buildSpec>
        <buildCommand>
            <name>org.eclipse.dltk.core.scriptbuilder</name>
            <arguments>
            </arguments>
        </buildCommand>
        <buildCommand>
            <name>org.eclipse.jdt.core.javabuilder</name>
            <arguments>
            </arguments>
        </buildCommand>
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
        <nature>org.eclipse.m2e.core.maven2Nature</nature>
        <nature>org.scala-ide.sdt.core.scalanature</nature>
        <nature>org.eclipse.jdt.core.javanature</nature>
    </natures>
</projectDescription>
""".trim()

eclipseProjectFile.text = eclipseProjectSource

assert true
