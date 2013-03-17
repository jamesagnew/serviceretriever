package ca.uhn.sail.proxy.admin.shared.model;

public enum ServerSecurityEnum {


	WSSEC("WS-Security"){
		@Override
		public BaseGServerSecurity newInstance() {
			return new GWsSecServerSecurity();
		}};
	
	private String myName;

	ServerSecurityEnum(String theName) {
		myName = theName;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return myName;
	}
	
	public abstract BaseGServerSecurity newInstance();

	
	
}
