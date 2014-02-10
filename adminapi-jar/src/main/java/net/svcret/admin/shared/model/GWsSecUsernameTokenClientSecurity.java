package net.svcret.admin.shared.model;

import net.svcret.admin.shared.enm.ClientSecurityEnum;

public class GWsSecUsernameTokenClientSecurity extends BaseDtoClientSecurity {

	private static final long serialVersionUID = 1L;

	public GWsSecUsernameTokenClientSecurity() {
	}
	
	public GWsSecUsernameTokenClientSecurity(String theUsername, String thePassword) {
		setUsername(theUsername);
		setPassword(thePassword);
	}

	@Override
	public ClientSecurityEnum getType() {
		return ClientSecurityEnum.WSSEC_UT;
	}

}
