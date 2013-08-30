package net.svcret.admin.shared.model;

public class GWsSecUsernameTokenClientSecurity extends BaseGClientSecurity {

	private static final long serialVersionUID = 1L;

	@Override
	public ClientSecurityEnum getType() {
		return ClientSecurityEnum.WSSEC_UT;
	}

}
