package net.svcret.admin.shared.model;

import net.svcret.admin.shared.enm.AuthorizationHostTypeEnum;


public class GLdapAuthHost extends BaseDtoAuthHost {

	private static final long serialVersionUID = 1L;

	public static final String DEFAULT_AUTH_FILTER = "(sAMAccountName={0})";
	public static final String DEFAULT_AUTH_BASE_DN = "DC=some,DC=org";
	public static final String DEFAULT_BIND_USER_PASS = "password";
	public static final String DEFAULT_BIND_USER_DN = "CN=some.user,OU=All\\ Users,DC=some,DC=org";
	public static final String DEFAULT_HOST = "ldap://localhost:389";

	private String myAuthenticateBaseDn;
	private String myAuthenticateFilter;
	private String myBindUserDn;
	private String myBindUserPassword;
	private String myUrl;

	public void setDefaults() {
		myUrl = DEFAULT_HOST;
		myBindUserDn=DEFAULT_BIND_USER_DN;
		myBindUserPassword=DEFAULT_BIND_USER_PASS;
		myAuthenticateBaseDn=DEFAULT_AUTH_BASE_DN;
		myAuthenticateFilter=DEFAULT_AUTH_FILTER;
	}

	public String getAuthenticateBaseDn() {
		return myAuthenticateBaseDn;
	}

	public void setAuthenticateBaseDn(String theAuthenticateBaseDn) {
		myAuthenticateBaseDn = theAuthenticateBaseDn;
	}

	public String getAuthenticateFilter() {
		return myAuthenticateFilter;
	}

	public void setAuthenticateFilter(String theAuthenticateFilter) {
		myAuthenticateFilter = theAuthenticateFilter;
	}

	public String getBindUserDn() {
		return myBindUserDn;
	}

	public void setBindUserDn(String theBindUserDn) {
		myBindUserDn = theBindUserDn;
	}

	public String getBindUserPassword() {
		return myBindUserPassword;
	}

	public void setBindUserPassword(String theBindUserPassword) {
		myBindUserPassword = theBindUserPassword;
	}

	public String getUrl() {
		return myUrl;
	}

	public void setUrl(String theUrl) {
		myUrl = theUrl;
	}

	@Override
	public void merge(BaseDtoAuthHost theObject) {
		super.merge(theObject);

		GLdapAuthHost obj = (GLdapAuthHost) theObject;
		setAuthenticateBaseDn(obj.getAuthenticateBaseDn());
		setAuthenticateFilter(obj.getAuthenticateFilter());
		setBindUserDn(obj.getBindUserDn());
		setBindUserPassword(obj.getBindUserPassword());
		setUrl(obj.getUrl());
	}

	@Override
	public AuthorizationHostTypeEnum getType() {
		return AuthorizationHostTypeEnum.LDAP;
	}

}
