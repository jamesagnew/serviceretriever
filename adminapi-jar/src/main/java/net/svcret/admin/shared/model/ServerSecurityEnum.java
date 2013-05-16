package net.svcret.admin.shared.model;

import java.util.HashSet;
import java.util.Set;

public enum ServerSecurityEnum {

	JSONRPC_NAMED_PARAMETER("JSON-RPC Named Parameter", GServiceVersionJsonRpc20.class) {
		@Override
		public BaseGServerSecurity newInstance() {
			return new GNamedParameterJsonRpcServerAuth();
		}
	},

	WSSEC_UT("WS-Security UsernameToken", GSoap11ServiceVersion.class) {
		@Override
		public BaseGServerSecurity newInstance() {
			return new GWsSecServerSecurity();
		}
	};

	private String myName;
	private Set<Class<? extends BaseGServiceVersion>> myAppliesTo;

	@SuppressWarnings("unchecked")
	ServerSecurityEnum(String theName, Class<?>... theAppliesTo) {
		myName = theName;
		myAppliesTo = new HashSet<Class<? extends BaseGServiceVersion>>();
		for (Class<?> next : theAppliesTo) {
			myAppliesTo.add((Class<? extends BaseGServiceVersion>) next);
		}
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return myName;
	}

	public abstract BaseGServerSecurity newInstance();

	public boolean appliesTo(Class<? extends BaseGServiceVersion> theClass) {
		return myAppliesTo.contains(theClass);
	}

}
