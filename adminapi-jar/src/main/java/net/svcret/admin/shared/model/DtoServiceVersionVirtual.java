package net.svcret.admin.shared.model;


public class DtoServiceVersionVirtual extends BaseGServiceVersion {

	private static final long serialVersionUID = 1L;

	private long myTargetServiceVersionPid;
	
	@Override
	public ServiceProtocolEnum getProtocol() {
		return ServiceProtocolEnum.VIRTUAL;
	}

	public long getTargetServiceVersionPid() {
		return myTargetServiceVersionPid;
	}

	public void setTargetServiceVersionPid(long theTargetServiceVersionPid) {
		myTargetServiceVersionPid = theTargetServiceVersionPid;
	}


}
