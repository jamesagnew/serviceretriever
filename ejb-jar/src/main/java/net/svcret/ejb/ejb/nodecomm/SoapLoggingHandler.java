package net.svcret.ejb.ejb.nodecomm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

/**
 * Provides logging of all SOAP based communication
 */
public class SoapLoggingHandler implements SOAPHandler<SOAPMessageContext> {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(SoapLoggingHandler.class);

	public void close(MessageContext c) {
	}

	public Set<QName> getHeaders() {
		// Not required for logging
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean handleFault(SOAPMessageContext c) {
		SOAPMessage msg = c.getMessage();
		ByteArrayOutputStream oos = new ByteArrayOutputStream();
		try {
			msg.writeTo(oos);
		} catch (SOAPException e1) {
			ourLog.error("Failed to stream SOAP payload", e1);
			return true;
		} catch (IOException e1) {
			ourLog.error("Failed to stream SOAP payload", e1);
			return true;
		}
		String message = oos.toString();
		ourLog.debug("Fault message: {}", message);

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean handleMessage(SOAPMessageContext c) {
		SOAPMessage msg = c.getMessage();
		ByteArrayOutputStream oos = new ByteArrayOutputStream();
		try {
			msg.writeTo(oos);
		} catch (SOAPException e1) {
			ourLog.error("Failed to stream SOAP payload", e1);
			return true;
		} catch (IOException e1) {
			ourLog.error("Failed to stream SOAP payload", e1);
			return true;
		}
		String message = oos.toString();

		boolean request = ((Boolean) c.get(SOAPMessageContext.MESSAGE_OUTBOUND_PROPERTY)).booleanValue();

		if (request) {
			// This is a request message.
			ourLog.debug("Request SOAP payload: {}", message);
		} else {
			// This is the response message
			ourLog.info("Response SOAP payload: {}", message);
		}

		return true;
	}

}