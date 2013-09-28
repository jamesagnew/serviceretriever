package net.svcret.admin.shared.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import net.svcret.admin.shared.enm.AuthorizationHostTypeEnum;
import net.svcret.admin.shared.util.XmlConstants;

@XmlRootElement(namespace=XmlConstants.DTO_NAMESPACE, name="AuthenticationHostLdap")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoAuthenticationHostLdap extends BaseDtoAuthenticationHost {

	private static final long serialVersionUID = 1L;

	public static final String DEFAULT_AUTH_FILTER = "(sAMAccountName={0})";
	public static final String DEFAULT_AUTH_BASE_DN = "DC=some,DC=org";
	public static final String DEFAULT_BIND_USER_PASS = "password";
	public static final String DEFAULT_BIND_USER_DN = "CN=some.user,OU=All\\ Users,DC=some,DC=org";
	public static final String DEFAULT_HOST = "ldap://localhost:389";

	@XmlElement(name="AuthenticateBaseDn")
	private String myAuthenticateBaseDn;
	@XmlElement(name="AuthenticateFilter")
	private String myAuthenticateFilter;
	@XmlElement(name="BindUserDn")
	private String myBindUserDn;
	@XmlElement(name="BindUserPassword")
	private String myBindUserPassword;
	@XmlElement(name="Url")
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
	public void merge(BaseDtoAuthenticationHost theObject) {
		super.merge(theObject);

		DtoAuthenticationHostLdap obj = (DtoAuthenticationHostLdap) theObject;
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
