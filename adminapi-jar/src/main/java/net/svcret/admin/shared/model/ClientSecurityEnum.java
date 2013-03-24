package net.svcret.admin.shared.model;


public enum ClientSecurityEnum {

	WSSEC("WS-Security"){
		@Override
		public BaseGClientSecurity newInstance() {
			return new GWsSecClientSecurity();
		}};
	
	private String myName;

	ClientSecurityEnum(String theName) {
		myName = theName;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return myName;
	}
	
	public abstract BaseGClientSecurity newInstance();
	
}
