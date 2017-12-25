package com.carrotgarden.maven.scalor.selfregs;

import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IExecutableExtensionFactory;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * <p>
 * DO NOT USE.
 * </p>
 * 
 * <p>
 * This extension factory is used internally by the dynamic extension injector,
 * and it must be part of the public API so that it can be loaded by the
 * extension registry, however it does not need to be used directly by clients.
 * </p>
 * 
 * @author Neil Bartlett
 * 
 */
public class DelegatingExtensionFactory implements IExecutableExtension, IExecutableExtensionFactory {
	
	private static final Map<String, IExecutableExtensionFactory> factoryMapping = new HashMap<String, IExecutableExtensionFactory>();
	
	private String factoryId = null;
	private IConfigurationElement config;
	private String propertyName;

	/**
	 * DO NOT USE.
	 * @deprecated
	 */
	@Deprecated
	public DelegatingExtensionFactory() {
	}

	public Object create() throws CoreException {
		IExecutableExtensionFactory delegate = findDelegate();
		if (delegate == null)
			throw new CoreException(new Status(IStatus.ERROR, Constants.BUNDLE_ID, 0, "Could not find extension factory delegate for factory ID:" + factoryId, null));

		// Pass through configuration data -- needed by many extension types e.g. Views.
		if (delegate instanceof IExecutableExtension)
			((IExecutableExtension) delegate).setInitializationData(config, propertyName, null);
		
		return delegate.create();
	}

	private IExecutableExtensionFactory findDelegate() {
		synchronized (factoryMapping) {
			return factoryMapping.get(factoryId);
		}
	}

	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		this.config = config;
		this.propertyName = propertyName;
		
		if (data instanceof String) {
			factoryId = (String) data;
		} else if (data instanceof Dictionary) {
			Dictionary<?,?> dict = (Dictionary<?,?>) data;
			Object factoryIdObj = dict.get(Constants.PROP_FACTORY_ID);
			if (factoryIdObj != null) {
				if (factoryIdObj instanceof String)
					factoryId = (String) factoryIdObj;
				else
					throw new CoreException(new Status(IStatus.ERROR, Constants.BUNDLE_ID, 0, "Factory ID attribute has invalid type (String required)", null));
			}
		}
		
		if (factoryId == null)
			throw new CoreException(new Status(IStatus.ERROR, Constants.BUNDLE_ID, 0, "No factory ID specified", null));
	}

	static void addFactoryMapping(String id, IExecutableExtensionFactory factory) {
		synchronized (factoryMapping) {
			if (factoryMapping.containsKey(id))
				throw new IllegalArgumentException("Factory mapping already exists");
			factoryMapping.put(id, factory);
		}
	}

	static void removeFactoryMappings(Collection<? extends String> ids) {
		synchronized (factoryMapping) {
			for (String id : ids) {
				factoryMapping.remove(id);
			}
		}
	}
}