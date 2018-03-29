package com.carrotgarden.maven.scalor.setup

import java.io.File

import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.plugins.annotations.Component

import com.carrotgarden.maven.scalor.base
import com.carrotgarden.maven.tools.Description
import com.carrotgarden.maven.scalor.util.Folder
import com.carrotgarden.maven.scalor.util.Error.Throw

trait ParamsCross extends AnyRef
  with base.ParamsAny {

  @Description( """
  List of folders for cross-version modules.
  Placed inside parent project <code>${project.basedir}</code>.
  Normally represents several current Scala versions.
  Module names must correspond to module names configured in parent <code>pom.xml</code>.
  Module names present in this list but missing from <code>pom.xml</code> are ignored.
  Relative path.
  Separator parameter: <a href="#commonSequenceSeparator"><b>commonSequenceSeparator</b></a>.
  Example matching <code>pom.xml</code> configuration entry:
<pre>
    &lt;modules&gt;
        &lt;module&gt;cross/2.11&lt;/module&gt;
        &lt;module&gt;cross/2.12&lt;/module&gt;
        &lt;module&gt;cross/2.13&lt;/module&gt;
    &lt;/modules&gt;
</pre>
  """ )
  @Parameter(
    property     = "scalor.setupCrossModuleList",
    defaultValue = """
    cross/2.11 ★
    cross/2.12 ★
    cross/2.13 ★
    """
  )
  var setupCrossModuleList : String = _

  @Description( """
  List of resources for parent/module linking.
  Resources are linked from each cross-version module into parent.
  Normally includes project source root folder <code>src</code>.
  Relative path.
  Separator parameter: <a href="#commonSequenceSeparator"><b>commonSequenceSeparator</b></a>.
  Example linking result:
<pre>
  # symlink: 'source' -> 'target'
  ${parent}/cross/2.11/src -> ${parent}/src
  ${parent}/cross/2.12/src -> ${parent}/src
  ${parent}/cross/2.13/src -> ${parent}/src
</pre>
  """ )
  @Parameter(
    property     = "scalor.setupCrossResourceList",
    defaultValue = """
    src ★
    """
  )
  var setupCrossResourceList : String = _

  def crossModuleList = parseCommonList( setupCrossModuleList )

  def crossResourceList = parseCommonList( setupCrossResourceList )

}
