package net.svcret.admin.client.ui.config.sec;

import net.svcret.admin.shared.model.BaseDtoClientSecurity;
import net.svcret.admin.shared.model.BaseDtoServerSecurity;

public class ViewAndEditFactory {

	private ViewAndEditFactory() {
		// non instantiable
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends BaseDtoClientSecurity> IProvidesViewAndEdit<T> provideClientSecurity(T theClientSecurity) {
		switch (theClientSecurity.getType()) {
		case WSSEC_UT:
			return (IProvidesViewAndEdit<T>) new WsSecUsernameTokenClientSecurityViewAndEdit();
		case HTTP_BASICAUTH:
			return (IProvidesViewAndEdit<T>) new HttpBasicAuthClientSecurityViewAndEdit();
		case JSONRPC_NAMPARM:
			return (IProvidesViewAndEdit<T>) new ClientSecurityViewAndEditJsonRpcNamedParameter();
		}
		
		throw new IllegalStateException("Type: " + theClientSecurity.getType());
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends BaseDtoServerSecurity> IProvidesViewAndEdit<T> provideServerSecurity(T theServerSecurity) {
		switch (theServerSecurity.getType()) {
		case WSSEC_UT:
			return (IProvidesViewAndEdit<T>) new SoapWsSecUsernameTokenServerSecurity();
		case JSONRPC_NAMED_PARAMETER:
			return (IProvidesViewAndEdit<T>) new JsonRpcNamedParameterServerSecurity();
		case HTTP_BASIC_AUTH:
			return (IProvidesViewAndEdit<T>) new HttpBasicServerSecurity();
		}
		
		throw new IllegalStateException("Type: " + theServerSecurity.getType());
	}

}
