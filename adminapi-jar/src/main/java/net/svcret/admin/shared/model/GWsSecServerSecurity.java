package net.svcret.admin.shared.model;

public class GWsSecServerSecurity extends BaseDtoServerSecurity {

	private static final long serialVersionUID = 1L;

	@Override
	public ServerSecurityEnum getType() {
		return ServerSecurityEnum.WSSEC_UT;
	}

}
