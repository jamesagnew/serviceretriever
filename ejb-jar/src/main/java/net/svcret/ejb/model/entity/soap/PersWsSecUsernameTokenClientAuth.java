package net.svcret.ejb.model.entity.soap;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import net.svcret.ejb.api.ClientAuthTypeEnum;
import net.svcret.ejb.model.entity.PersBaseClientAuth;

import org.apache.commons.lang3.ObjectUtils;


@Entity
@DiscriminatorValue("WSSEC_UT")
public class PersWsSecUsernameTokenClientAuth extends PersBaseClientAuth<PersWsSecUsernameTokenClientAuth> {

	private static final long serialVersionUID = 1L;

	public PersWsSecUsernameTokenClientAuth(String theUsername, String thePassword) {
		super();
		setUsername(theUsername);
		setPassword(thePassword);
	}

	public PersWsSecUsernameTokenClientAuth() {
		super();
	}

	@Override
	protected boolean relevantPropertiesEqual(PersWsSecUsernameTokenClientAuth theT) {
		return ObjectUtils.equals(getUsername(), theT.getUsername()) && ObjectUtils.equals(getPassword(), theT.getPassword());
	}

	@Override
	public ClientAuthTypeEnum getAuthType() {
		return ClientAuthTypeEnum.WS_SECURITY_USERNAME_TOKEN;
	}

	@Override
	public void merge(PersBaseClientAuth<?> theObj) {
		PersWsSecUsernameTokenClientAuth obj = (PersWsSecUsernameTokenClientAuth) theObj;
		
		obj.setUsername(obj.getUsername());
		obj.setPassword(obj.getPassword());
		obj.setServiceVersion(obj.getServiceVersion());
		
	}

}
