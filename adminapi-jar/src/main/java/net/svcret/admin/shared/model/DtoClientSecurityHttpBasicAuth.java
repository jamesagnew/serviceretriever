package net.svcret.admin.shared.model;

public class DtoClientSecurityHttpBasicAuth extends BaseGClientSecurity {

	private static final long serialVersionUID = 1L;

	@Override
	public ClientSecurityEnum getType() {
		return ClientSecurityEnum.HTTP_BASICAUTH;
	}

}
