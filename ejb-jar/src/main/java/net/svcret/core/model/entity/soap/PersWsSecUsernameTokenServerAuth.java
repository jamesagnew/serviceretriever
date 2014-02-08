package net.svcret.core.model.entity.soap;

import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.xml.stream.events.XMLEvent;

import net.svcret.admin.shared.model.BaseDtoServerSecurity;
import net.svcret.admin.shared.model.GWsSecServerSecurity;
import net.svcret.admin.shared.model.ServerSecurityEnum;
import net.svcret.core.api.ICredentialGrabber;
import net.svcret.core.model.entity.BasePersObject;
import net.svcret.core.model.entity.PersBaseServerAuth;


@Entity
@DiscriminatorValue("SOAP_WSSEC_UT")
public class PersWsSecUsernameTokenServerAuth extends PersBaseServerAuth<PersWsSecUsernameTokenServerAuth, WsSecUsernameTokenCredentialGrabber> {

	private static final long serialVersionUID = 1L;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object theObj) {
		return super.equals(theObj);
	}

	public ICredentialGrabber newCredentialGrabber(List<XMLEvent> theHeaderEvents) {
		return new WsSecUsernameTokenCredentialGrabber(theHeaderEvents);
	}

	@Override
	public Class<WsSecUsernameTokenCredentialGrabber> getGrabberClass() {
		return WsSecUsernameTokenCredentialGrabber.class;
	}

	@Override
	public ServerSecurityEnum getAuthType() {
		return ServerSecurityEnum.WSSEC_UT;
	}

	@Override
	public void merge(BasePersObject theObj) {
		PersWsSecUsernameTokenServerAuth obj = (PersWsSecUsernameTokenServerAuth)theObj;
		setAuthenticationHost(obj.getAuthenticationHost());
		setServiceVersion(obj.getServiceVersion());
	}

	@Override
	protected BaseDtoServerSecurity createDtoAndPopulateWithTypeSpecificEntries() {
		return new GWsSecServerSecurity();
	}

}
