package net.svcret.ejb.model.entity.soap;

import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.xml.stream.events.XMLEvent;

import net.svcret.ejb.api.ICredentialGrabber;
import net.svcret.ejb.api.ServerAuthTypeEnum;
import net.svcret.ejb.model.entity.PersBaseServerAuth;

import org.apache.commons.lang3.ObjectUtils;


@Entity
@DiscriminatorValue("WSSEC_UT")
public class PersWsSecUsernameTokenServerAuth extends PersBaseServerAuth<PersWsSecUsernameTokenServerAuth, WsSecUsernameTokenCredentialGrabber> {

	private static final long serialVersionUID = 1L;

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

	@Override
	public ServerAuthTypeEnum getAuthType() {
		return ServerAuthTypeEnum.WS_SECURITY_USERNAME_TOKEN;
	}

	@Override
	public void merge(PersBaseServerAuth<?, ?> theObj) {
		PersWsSecUsernameTokenServerAuth obj = (PersWsSecUsernameTokenServerAuth)theObj;
		setAuthenticationHost(obj.getAuthenticationHost());
		setServiceVersion(obj.getServiceVersion());
	}

}
