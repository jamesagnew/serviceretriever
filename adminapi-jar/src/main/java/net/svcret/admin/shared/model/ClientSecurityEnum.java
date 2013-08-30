package net.svcret.admin.shared.model;


public enum ClientSecurityEnum {

	WSSEC_UT("WS-Security"){
		@Override
		public BaseGClientSecurity newInstance() {
			return new GWsSecUsernameTokenClientSecurity();
		}}, 
		
	HTTP_BASICAUTH("HTTP Basic Auth"){
		@Override
		public BaseGClientSecurity newInstance() {
			return new DtoClientSecurityHttpBasicAuth();
		}},
		
	JSONRPC_NAMPARM("JSON-RPC Named Parameter") {
			@Override
			public BaseGClientSecurity newInstance() {
				return new DtoClientSecurityJsonRpcNamedParameter();
			}	
		},;
	
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
