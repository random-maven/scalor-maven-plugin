package com.carrotgarden.maven.scalor.selfregs;

import org.eclipse.core.runtime.IExtensionRegistry;


public interface ContributionFragment {

	public abstract void uninstall(IExtensionRegistry registry, Object token);
	

}