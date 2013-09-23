package net.svcret.ejb.model.entity.http;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import net.svcret.admin.shared.model.BaseGClientSecurity;
import net.svcret.admin.shared.model.ClientSecurityEnum;
import net.svcret.admin.shared.model.DtoClientSecurityHttpBasicAuth;
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
	protected PersBaseClientAuth<?> doMerge(PersBaseClientAuth<?> theObj) {
		PersHttpBasicClientAuth obj = (PersHttpBasicClientAuth) theObj;
		PersHttpBasicClientAuth retVal = new PersHttpBasicClientAuth();
		
		retVal.setUsername(obj.getUsername());
		
		retVal.setPassword(obj.getPassword());
		
		return retVal;
	}

	@Override
	protected BaseGClientSecurity createDtoAndPopulateWithTypeSpecificFields() {
		return new DtoClientSecurityHttpBasicAuth();
	}

	
}
