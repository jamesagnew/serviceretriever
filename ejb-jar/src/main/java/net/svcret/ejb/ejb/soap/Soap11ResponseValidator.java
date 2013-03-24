package net.svcret.ejb.ejb.soap;

import java.io.StringReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import net.svcret.ejb.Messages;
import net.svcret.ejb.api.IResponseValidator;


public class Soap11ResponseValidator implements IResponseValidator {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(Soap11ResponseValidator.class);
	private Set<String> myAcceptableContentTypes;
	private Set<Integer> myAcceptableHttpStatusCodes;
	private Collection<String> myRequiredBodyFragments;
	private Set<QName> myRequiredXmlElements;
	private XMLInputFactory myXmlInputFactory;

	public Soap11ResponseValidator() {
		setAcceptableContentTypes("text/xml", "application/soap+xml"); //$NON-NLS-1$ //$NON-NLS-2$
		setRequiredXmlElements(Constants.SOAPENV11_ENVELOPE_QNAME);
		setAcceptableHttpStatusCodes(200, 500);

		myXmlInputFactory = XMLInputFactory.newInstance();
	}

	public void setAcceptableContentTypes(String... theStrings) {
		myAcceptableContentTypes = new HashSet<String>();
		for (String string : theStrings) {
			myAcceptableContentTypes.add(string);
		}
	}

	public void setAcceptableHttpStatusCodes(int... theCodes) {
		myAcceptableHttpStatusCodes = new HashSet<Integer>();
		for (int i : theCodes) {
			myAcceptableHttpStatusCodes.add(i);
		}
	}

	public void setRequiredXmlElements(QName... theQNames) {
		myRequiredXmlElements = new HashSet<QName>();
		for (QName qName : theQNames) {
			myRequiredXmlElements.add(qName);
		}
	}

	@Override
	public ValidationResponse validate(String theBody, int theStatusCode, String theContentType) {
		if (myAcceptableHttpStatusCodes != null) {
			if (!myAcceptableHttpStatusCodes.contains(theStatusCode)) {
				return new ValidationResponse(false, Messages.getString("Soap11ResponseValidator.statusFail", theStatusCode));
			}
		}

		if (myAcceptableContentTypes != null) {
			if (!myAcceptableContentTypes.contains(theContentType)) {
				return new ValidationResponse(false, Messages.getString("Soap11ResponseValidator.contentTypeFail", theContentType));
			}
		}

		if (myRequiredBodyFragments != null) {
			for (String next : myRequiredBodyFragments) {
				if (!theBody.contains(next)) {
					return new ValidationResponse(false, Messages.getString("Soap11ResponseValidator.bodyFragmentFail", next));
				}
			}
		}

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
						return new ValidationResponse(true);
					}
				}

				return new ValidationResponse(false, Messages.getString("Soap11ResponseValidator.requiredXmlElementFail", rNames));

			} catch (XMLStreamException e) {
				return new ValidationResponse(false, Messages.getString("Soap11ResponseValidator.nonXmlFail", e.getMessage()));
			}
		}
		
		return new ValidationResponse(true);
	}
}
