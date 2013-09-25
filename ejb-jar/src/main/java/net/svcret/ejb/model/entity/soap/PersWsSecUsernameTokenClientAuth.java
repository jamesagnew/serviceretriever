package net.svcret.ejb.model.entity.soap;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import net.svcret.admin.shared.enm.ClientSecurityEnum;
import net.svcret.admin.shared.model.BaseDtoClientSecurity;
import net.svcret.admin.shared.model.GWsSecUsernameTokenClientSecurity;
import net.svcret.ejb.model.entity.PersBaseClientAuth;

import org.apache.commons.lang3.ObjectUtils;


@Entity
@DiscriminatorValue("SOAP_WSSEC_UT")
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
	public ClientSecurityEnum getAuthType() {
		return ClientSecurityEnum.WSSEC_UT;
	}
	
	@Override
	protected PersBaseClientAuth<?> doMerge(PersBaseClientAuth<?> theObj) {
		PersWsSecUsernameTokenClientAuth obj = (PersWsSecUsernameTokenClientAuth) theObj;
		PersWsSecUsernameTokenClientAuth retVal = new PersWsSecUsernameTokenClientAuth();
		
		retVal.setUsername(obj.getUsername());
		
		retVal.setPassword(obj.getPassword());
		
		return retVal;
	}

	@Override
	protected BaseDtoClientSecurity createDtoAndPopulateWithTypeSpecificFields() {
		GWsSecUsernameTokenClientSecurity retVal = new GWsSecUsernameTokenClientSecurity();
		retVal.setUsername(getUsername());
		retVal.setPassword(getPassword());
		return retVal;
	}


}
