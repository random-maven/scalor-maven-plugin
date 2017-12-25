package com.carrotgarden.maven.scalor.selfregs;

import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;

public class ExtPointContributionFragment implements ContributionFragment {
	
	private final String namespace;
	private final String id;

	public ExtPointContributionFragment(String namespace, String id) {
		this.namespace = namespace;
		this.id = id;
	}

	public void uninstall(IExtensionRegistry registry, Object token) {
		IExtensionPoint extPoint = registry.getExtensionPoint(namespace, id);
		if (extPoint != null) {
			registry.removeExtensionPoint(extPoint, token);
		}
	}

}