package ca.uhn.sail.proxy.admin.shared.model;

public class GWsSecServerSecurity extends BaseGServerSecurity {

	private static final long serialVersionUID = 1L;

	@Override
	public ServerSecurityEnum getType() {
		return ServerSecurityEnum.WSSEC;
	}

}
