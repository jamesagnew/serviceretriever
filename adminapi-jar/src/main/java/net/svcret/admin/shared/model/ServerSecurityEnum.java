package net.svcret.admin.shared.model;

import java.util.HashSet;
import java.util.Set;

public enum ServerSecurityEnum {

	JSONRPC_NAMED_PARAMETER("JSON-RPC Named Parameter", DtoServiceVersionJsonRpc20.class) {
		@Override
		public BaseDtoServerSecurity newInstance() {
			return new GNamedParameterJsonRpcServerAuth();
		}
	},

	WSSEC_UT("WS-Security UsernameToken", DtoServiceVersionSoap11.class) {
		@Override
		public BaseDtoServerSecurity newInstance() {
			return new GWsSecServerSecurity();
		}
	},

	HTTP_BASIC_AUTH("HTTP Basic") {
		@Override
		public BaseDtoServerSecurity newInstance() {
			return new GHttpBasicAuthServerSecurity();
		}
	};

	private String myName;
	private Set<Class<? extends BaseDtoServiceVersion>> myAppliesTo;

	@SuppressWarnings("unchecked")
	ServerSecurityEnum(String theName, Class<?>... theAppliesTo) {
		myName = theName;
		myAppliesTo = new HashSet<Class<? extends BaseDtoServiceVersion>>();
		for (Class<?> next : theAppliesTo) {
			myAppliesTo.add((Class<? extends BaseDtoServiceVersion>) next);
		}
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return myName;
	}

	public abstract BaseDtoServerSecurity newInstance();

	public boolean appliesTo(Class<? extends BaseDtoServiceVersion> theClass) {
		if (myAppliesTo.isEmpty()) {
			return true;
		}
		return myAppliesTo.contains(theClass);
	}

}
