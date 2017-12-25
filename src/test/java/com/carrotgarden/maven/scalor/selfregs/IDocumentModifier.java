package com.carrotgarden.maven.scalor.selfregs;

import org.jdom.Document;

public interface IDocumentModifier {
	void modify(Document document) throws ContributionProcessingException;
}