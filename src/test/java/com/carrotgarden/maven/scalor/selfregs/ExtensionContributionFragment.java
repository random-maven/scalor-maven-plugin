package com.carrotgarden.maven.scalor.selfregs;

import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionRegistry;

public class ExtensionContributionFragment implements ContributionFragment {
	
	private final String namespace;
	private final String id;

	protected ExtensionContributionFragment(String namespace, String id) {
		this.namespace = namespace;
		this.id = id;
	}

	public void uninstall(IExtensionRegistry registry, Object token) {
		String fullId = namespace + "." + id;
		IExtension extension = registry.getExtension(fullId);
		if (extension != null) {
			registry.removeExtension(extension, token);
		}
	}
}