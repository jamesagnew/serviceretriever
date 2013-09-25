package net.svcret.admin.shared.enm;

import net.svcret.admin.shared.model.BaseDtoClientSecurity;
import net.svcret.admin.shared.model.DtoClientSecurityHttpBasicAuth;
import net.svcret.admin.shared.model.DtoClientSecurityJsonRpcNamedParameter;
import net.svcret.admin.shared.model.GWsSecUsernameTokenClientSecurity;


public enum ClientSecurityEnum {

	WSSEC_UT("WS-Security"){
		@Override
		public BaseDtoClientSecurity newInstance() {
			return new GWsSecUsernameTokenClientSecurity();
		}}, 
		
	HTTP_BASICAUTH("HTTP Basic Auth"){
		@Override
		public BaseDtoClientSecurity newInstance() {
			return new DtoClientSecurityHttpBasicAuth();
		}},
		
	JSONRPC_NAMPARM("JSON-RPC Named Parameter") {
			@Override
			public BaseDtoClientSecurity newInstance() {
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
	
	public abstract BaseDtoClientSecurity newInstance();
	
}
