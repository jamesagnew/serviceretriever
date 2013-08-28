package net.svcret.admin.shared.model;


public class DtoServiceVersionJsonRpc20 extends BaseGServiceVersion {

	private static final long serialVersionUID = 1L;

	@Override
	public ServiceProtocolEnum getProtocol() {
		return ServiceProtocolEnum.JSONRPC20;
	}

}
