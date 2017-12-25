package com.carrotgarden.maven.scalor.selfregs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.ContributorFactoryOSGi;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExecutableExtensionFactory;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.osgi.framework.Bundle;

public class Contribution {

	private final Document document;
	private final List<ContributionFragment> fragments;
	private final List<String> mappedFactoryIds = new LinkedList<String>();
	private IContributor contributor;

	public static Contribution loadContribution(InputStream stream, Bundle bundle)
			throws ContributionProcessingException, IOException {

		// Setup the output to a stream buffer
		try {

			// Parse the input
			SAXBuilder builder = new SAXBuilder();
			Document document = builder.build(stream);

			// Inject IDs and generate contribution fragments
			IdInjectionDocumentModifier idInjector = new IdInjectionDocumentModifier(getBundleNamespace(bundle));
			idInjector.modify(document);

			return new Contribution(document, idInjector.getFragments(), bundle);

		} catch (JDOMException e) {
			throw new ContributionProcessingException("Error parsing contribution XML.", e);
		}
	}

	private Contribution(Document document, List<ContributionFragment> fragments, Bundle bundle) {
		this.document = document;
		this.fragments = fragments;
		contributor = ContributorFactoryOSGi.createContributor(bundle);
	}

	public boolean install(IExtensionRegistry registry, String name, Object token) {
		XMLOutputter outputter = new XMLOutputter();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			outputter.output(document, outputStream);

			return registry.addContribution( //
					new ByteArrayInputStream(outputStream.toByteArray()), //
					contributor, false, name, null, token);

		} catch (IOException e) {
			// Shouldn't happen with an in-memory stream?
			throw new RuntimeException("Unexpected error", e);
		}
	}

	/**
	 * @param registry
	 *            The extension registry from which to uninstall
	 * @param token
	 *            The registry token ({@code null} if Eclipse is launched with
	 *            {@code -Declipse.registry.nulltoken=true}).
	 */
	public void uninstall(IExtensionRegistry registry, Object token) {
		for (ContributionFragment fragment : fragments) {
			fragment.uninstall(registry, token);
		}
		DelegatingExtensionFactory.removeFactoryMappings(mappedFactoryIds);
	}

	public void setFactoryObject(String id, IExecutableExtensionFactory factory)
			throws ContributionProcessingException {
		FactoryInjector injector = new FactoryInjector();
		injector.modify(document);

		try {
			DelegatingExtensionFactory.addFactoryMapping(id, factory);
			mappedFactoryIds.add(id);
		} catch (IllegalArgumentException e) {
			throw new ContributionProcessingException("Unable to map factory ID to supplied extension factory.", e);
		}
	}

	private static String getBundleNamespace(Bundle bundle) {
		String name = bundle.getSymbolicName();

		int semiColonIndex = name.indexOf(';');
		if (semiColonIndex > -1) {
			name = name.substring(0, semiColonIndex);
		}

		return name;
	}

}
