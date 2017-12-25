package com.carrotgarden.maven.scalor.selfregs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;

public class IdInjectionDocumentModifier implements IDocumentModifier {

	private final AtomicLong counter;
	private final String namespace;
	private final List<ContributionFragment> fragments = new ArrayList<ContributionFragment>();
	
	public IdInjectionDocumentModifier(String namespace) {
		this(namespace, new AtomicLong(0));
	}
	
	public IdInjectionDocumentModifier(String namespace, AtomicLong counter) {
		this.namespace = namespace;
		this.counter = counter;
	}

	public void modify(Document document) throws ContributionProcessingException {
		Element pluginElem = document.getRootElement();
		if (!Constants.ELEM_PLUGIN.equals(pluginElem.getName()))
			throw new ContributionProcessingException("Invalid root element in plugin contribution document: " + pluginElem.getName());
		
		@SuppressWarnings("unchecked")
		List<Element> children = pluginElem.getChildren();
		
		for (Element element : children) {
			String elemName = element.getName();
			if (Constants.ELEM_EXTENSION.equals(elemName) || Constants.ELEM_EXTENSION_POINT.equals(elemName)) {
				injectId(element);
			}
		}
	}

	private void injectId(Element element) {
		boolean extension = Constants.ELEM_EXTENSION.equals(element.getName());
		String generatedId = null;
		
		Attribute idAttrib = element.getAttribute(Constants.ATTR_ID);
		if (idAttrib == null) {
			generatedId = generateIdString();
			idAttrib = new Attribute(Constants.ATTR_ID, generatedId, Attribute.CDATA_TYPE);
			element.setAttribute(idAttrib);
		} else {
			String id = idAttrib.getValue();
			if (id == null || id.length() == 0) {
				generatedId = generateIdString();
				idAttrib.setValue(generatedId);
			}
		}
		
		if (generatedId != null) {
			if (extension) {
				fragments.add(new ExtensionContributionFragment(namespace, generatedId));
			} else {
				fragments.add(new ExtPointContributionFragment(namespace, generatedId));
			}
		}
	}

	private String generateIdString() {
		return Long.toString(counter.getAndIncrement());
	}

	public List<ContributionFragment> getFragments() {
		return Collections.unmodifiableList(fragments);
	}

}