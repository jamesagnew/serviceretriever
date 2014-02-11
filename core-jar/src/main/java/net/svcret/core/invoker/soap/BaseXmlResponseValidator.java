package net.svcret.core.invoker.soap;

import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import net.svcret.core.Messages;

public abstract class BaseXmlResponseValidator extends BaseResponseValidator {

	private Set<QName> myRequiredXmlElements;
	private XMLInputFactory myXmlInputFactory;

	public BaseXmlResponseValidator() {
		myXmlInputFactory = XMLInputFactory.newInstance();
	}

	public void setRequiredXmlElements(QName... theQNames) {
		myRequiredXmlElements = new HashSet<QName>();
		for (QName qName : theQNames) {
			myRequiredXmlElements.add(qName);
		}
	}
	
	@Override
	public ValidationResponse validate(String theBody, int theStatusCode, String theContentType) {
		if (myRequiredXmlElements != null) {
			XMLEventReader evtReader;
			try {
				evtReader = myXmlInputFactory.createXMLEventReader(new StringReader(theBody));
	
				HashSet<QName> rNames = new HashSet<QName>(myRequiredXmlElements);
	
				while (evtReader.hasNext()) {
					XMLEvent next = evtReader.nextEvent();
					if (next.isStartElement()) {
						QName name = next.asStartElement().getName();
						rNames.remove(name);
					}
					if (rNames.isEmpty()) {
						return super.validate(theBody, theStatusCode, theContentType);
					}
				}
	
				ValidationResponse retVal = requiredElementsNotFound(theBody);
				if (retVal != null) {
					return retVal;
				}
				
				return new ValidationResponse(false, Messages.getString("Soap11ResponseValidator.requiredXmlElementFail", rNames));
	
			} catch (XMLStreamException e) {
				return new ValidationResponse(false, Messages.getString("Soap11ResponseValidator.nonXmlFail", e.getMessage()));
			}
		}

		return super.validate(theBody, theStatusCode, theContentType);
	}

	protected XMLInputFactory getXmlInputFactory() {
		return myXmlInputFactory;
	}

	/**
	 * Subclasses may override
	 * @return 
	 */
	protected ValidationResponse requiredElementsNotFound(@SuppressWarnings("unused") String theBody) {
		return null;
	}

}
