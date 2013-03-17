package ca.uhn.sail.proxy.admin.shared.model;

public enum ProtocolEnum {

	SOAP11("SOAP 1.1");

	private String myNiceName;

	private ProtocolEnum(String theName) {
		myNiceName = theName;
	}

	/**
	 * @return the niceName
	 */
	public String getNiceName() {
		return myNiceName;
	}
	
}
