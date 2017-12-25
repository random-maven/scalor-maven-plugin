package com.carrotgarden.maven.scalor.selfregs;

import java.util.Iterator;
import java.util.List;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.filter.ElementFilter;

public class FactoryInjector implements IDocumentModifier {

	public void modify(Document document) throws ContributionProcessingException {
		@SuppressWarnings("unchecked")
		Iterator<Element> elements = document.getRootElement().getDescendants(new ElementFilter());
		
		while (elements.hasNext()) {
			Element element = elements.next();
			
			@SuppressWarnings("unchecked")
			List<Attribute> attribs = element.getAttributes();
			for (Attribute attrib : attribs) {
				if (attrib.getAttributeType() != Attribute.CDATA_TYPE)
					continue;
				
				String value = attrib.getValue();
				if (value != null && value.startsWith(Constants.MARKER_FACTORY_ID)) {
					// Replace marker with delegator class name
					String factoryId = value.substring(Constants.MARKER_FACTORY_ID.length());
					String newValue = DelegatingExtensionFactory.class.getName() + ":" + factoryId;
					attrib.setValue(newValue);
				}
			}
		}
	}

}