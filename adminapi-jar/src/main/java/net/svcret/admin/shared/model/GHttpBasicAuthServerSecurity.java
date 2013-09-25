package net.svcret.admin.shared.model;

public class GHttpBasicAuthServerSecurity extends BaseDtoServerSecurity {

	private static final long serialVersionUID = 1L;

	@Override
	public ServerSecurityEnum getType() {
		return ServerSecurityEnum.HTTP_BASIC_AUTH;
	}

}
