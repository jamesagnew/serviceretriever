package net.svcret.ejb.invoker.soap;


public class Soap11ResponseValidator extends BaseXmlResponseValidator {

	public Soap11ResponseValidator() {
		setAcceptableContentTypes("text/xml", "application/soap+xml"); //$NON-NLS-1$ //$NON-NLS-2$
		setRequiredXmlElements(Constants.SOAPENV11_ENVELOPE_QNAME);
		setAcceptableHttpStatusCodes(200, 500);
	}
}
