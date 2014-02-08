package net.svcret.core.invoker.soap;

import java.io.StringReader;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import net.svcret.core.Messages;

public class Soap11ResponseValidator extends BaseXmlResponseValidator {

	public Soap11ResponseValidator() {
		setAcceptableContentTypes("text/xml", "application/soap+xml"); //$NON-NLS-1$ //$NON-NLS-2$
		setRequiredXmlElements(Constants.SOAPENV11_ENVELOPE_QNAME);
		setAcceptableHttpStatusCodes(200, 500);
	}

	@Override
	protected ValidationResponse requiredElementsNotFound(String theBody) {
		XMLEventReader evtReader;
		try {
			evtReader = getXmlInputFactory().createXMLEventReader(new StringReader(theBody));

			while (evtReader.hasNext()) {
				XMLEvent next = evtReader.nextEvent();
				if (next.isStartElement()) {
					QName name = next.asStartElement().getName();
					if (name.equals(Constants.SOAPENV12_ENVELOPE_QNAME) || Constants.SOAPENV12_ENVELOPE_QNAME_ALTERNATES.contains(name)) {
						return new ValidationResponse(false, Messages.getString("Soap11ResponseValidator.wrongSoapVersion", name));
					}
				}
			}

		} catch (XMLStreamException e) {
			return new ValidationResponse(false, Messages.getString("Soap11ResponseValidator.nonXmlFail", e.getMessage()));
		}

		return null;
	}

}
