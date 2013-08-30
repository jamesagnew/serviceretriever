package net.svcret.ejb.model.entity.http;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import net.svcret.admin.shared.model.ServerSecurityEnum;
import net.svcret.ejb.model.entity.BasePersObject;
import net.svcret.ejb.model.entity.PersBaseServerAuth;

@Entity
@DiscriminatorValue("HTTP_BASIC")
public class PersHttpBasicServerAuth  extends PersBaseServerAuth<PersHttpBasicServerAuth, PersHttpBasicCredentialGrabber> {

	private static final long serialVersionUID = 1L;

	@Override
	public ServerSecurityEnum getAuthType() {
		return ServerSecurityEnum.HTTP_BASIC_AUTH;
	}

	@Override
	public Class<? extends PersHttpBasicCredentialGrabber> getGrabberClass() {
		return PersHttpBasicCredentialGrabber.class;
	}

	@Override
	public void merge(BasePersObject theObj) {
		PersHttpBasicServerAuth obj = (PersHttpBasicServerAuth)theObj;
		setAuthenticationHost(obj.getAuthenticationHost());
		setServiceVersion(obj.getServiceVersion());
	}

}
