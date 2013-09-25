package net.svcret.admin.shared.model;

import net.svcret.admin.shared.enm.ClientSecurityEnum;

public class GWsSecUsernameTokenClientSecurity extends BaseDtoClientSecurity {

	private static final long serialVersionUID = 1L;

	@Override
	public ClientSecurityEnum getType() {
		return ClientSecurityEnum.WSSEC_UT;
	}

}
