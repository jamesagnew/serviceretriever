package net.svcret.ejb.model.entity.http;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import net.svcret.admin.shared.model.ClientSecurityEnum;
import net.svcret.ejb.model.entity.PersBaseClientAuth;

import org.apache.commons.lang3.ObjectUtils;


@Entity
@DiscriminatorValue("HTTP_BASIC")
public class PersHttpBasicClientAuth extends PersBaseClientAuth<PersHttpBasicClientAuth> {

	private static final long serialVersionUID = 1L;

	public PersHttpBasicClientAuth(String theUsername, String thePassword) {
		super();
		setUsername(theUsername);
		setPassword(thePassword);
	}

	public PersHttpBasicClientAuth() {
		super();
	}

	@Override
	protected boolean relevantPropertiesEqual(PersHttpBasicClientAuth theT) {
		return ObjectUtils.equals(getUsername(), theT.getUsername()) && ObjectUtils.equals(getPassword(), theT.getPassword());
	}

	@Override
	public ClientSecurityEnum getAuthType() {
		return ClientSecurityEnum.HTTP_BASICAUTH;
	}

	@Override
	public void merge(PersBaseClientAuth<?> theObj) {
		PersHttpBasicClientAuth obj = (PersHttpBasicClientAuth) theObj;
		
		obj.setUsername(obj.getUsername());
		obj.setPassword(obj.getPassword());
		obj.setServiceVersion(obj.getServiceVersion());
		
	}

}
