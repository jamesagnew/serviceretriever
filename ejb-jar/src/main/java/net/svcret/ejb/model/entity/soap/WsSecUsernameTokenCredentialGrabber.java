package net.svcret.ejb.model.entity.soap;

import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import net.svcret.ejb.api.ICredentialGrabber;
import net.svcret.ejb.ejb.soap.Constants;

import org.apache.commons.lang3.StringUtils;


class WsSecUsernameTokenCredentialGrabber implements ICredentialGrabber {

	private String myPassword;
	private String myUsername;

	public WsSecUsernameTokenCredentialGrabber(List<XMLEvent> theHeaderEvents) {
		boolean inSecurity = false;
		boolean inUsernameToken = false;
		boolean inUsername = false;
		boolean inPassword = false;

		for (XMLEvent nextEvent : theHeaderEvents) {

			QName schemaType = null;

			if (nextEvent.isStartElement()) {
				schemaType = ((StartElement) nextEvent).getName();
			} else if (nextEvent.isEndElement()) {
				schemaType = ((EndElement) nextEvent).getName();
			}

			if (inUsernameToken) {
				if (Constants.WSSE_USERNAME_QNAME.equals(schemaType)) {
					if (nextEvent.isStartElement()) {
						inUsername = true;
					}
					if (nextEvent.isEndElement()) {
						inUsername = false;
					}
				} else if (Constants.WSSE_PASSWORD_QNAME.equals(schemaType)) {
					if (nextEvent.isStartElement()) {
						inPassword = true;
					}
					if (nextEvent.isEndElement()) {
						inPassword = false;
					}
				} else if (nextEvent.isCharacters()) {
					if (inUsername) {
						myUsername = addText(myUsername, (Characters) nextEvent);
					} else if (inPassword) {
						myPassword = addText(myPassword, (Characters) nextEvent);
					}
				}

			} else {

				if (inSecurity) {

					if (Constants.WSSE_USERNAME_TOKEN_QNAME.equals(schemaType)) {
						if (nextEvent.isStartElement()) {
							inUsernameToken = true;
							continue;
						}
						if (nextEvent.isEndElement()) {
							inUsernameToken = false;
							continue;
						}
					}

					if (nextEvent.isEndElement() && Constants.WSSE_SECURITY_QNAME.equals(schemaType)) {
						inSecurity = false;
						continue;
					}
				} else {
					if (nextEvent.isStartElement() && Constants.WSSE_SECURITY_QNAME.equals(schemaType)) {
						inSecurity = true;
						continue;
					}
				}

			}
		}
	}

	private String addText(String theText, Characters theNextEvent) {
		if (theText == null) {
			return theNextEvent.getData();
		}
		return theText + theNextEvent.getData();
	}

	@Override
	public String getUsername() {
		return StringUtils.defaultString(myUsername);
	}

	@Override
	public String getPassword() {
		return StringUtils.defaultString(myPassword);
	}

}
