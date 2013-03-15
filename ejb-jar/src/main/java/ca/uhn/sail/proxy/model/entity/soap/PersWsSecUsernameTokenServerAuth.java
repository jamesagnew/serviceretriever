package ca.uhn.sail.proxy.model.entity.soap;

import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.lang3.ObjectUtils;

import ca.uhn.sail.proxy.api.ICredentialGrabber;
import ca.uhn.sail.proxy.model.entity.PersBaseServerAuth;

@Entity
@DiscriminatorValue("WSSEC_UT")
public class PersWsSecUsernameTokenServerAuth extends PersBaseServerAuth<PersWsSecUsernameTokenServerAuth, WsSecUsernameTokenCredentialGrabber> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object theObj) {
		return super.equals(theObj);
	}

	@Override
	protected boolean relevantPropertiesEqual(PersWsSecUsernameTokenServerAuth theT) {
		return ObjectUtils.equals(getAuthenticationHost(), theT.getAuthenticationHost());
	}

	public ICredentialGrabber newCredentialGrabber(List<XMLEvent> theHeaderEvents) {
		return new WsSecUsernameTokenCredentialGrabber(theHeaderEvents);
	}

	@Override
	public Class<WsSecUsernameTokenCredentialGrabber> getGrabberClass() {
		return WsSecUsernameTokenCredentialGrabber.class;
	}

}
