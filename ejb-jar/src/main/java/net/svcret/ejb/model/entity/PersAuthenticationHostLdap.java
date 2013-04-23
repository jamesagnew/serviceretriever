package net.svcret.ejb.model.entity;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import net.svcret.admin.shared.model.AuthorizationHostTypeEnum;

import static net.svcret.admin.shared.model.GLdapAuthHost.*;

/**
 * LDAP authentication host
 */
@Entity
@DiscriminatorValue("LDAP")
public class PersAuthenticationHostLdap extends BasePersAuthenticationHost {

	private static final long serialVersionUID = 1L;

	@Column(name = "LDAP_AUTHENTICATE_BASE_DN", length=200, nullable = true)
	private String myAuthenticateBaseDn;

	@Column(name = "LDAP_AUTHENTICATE_FILTER", length=200, nullable = true)
	private String myAuthenticateFilter;

	@Column(name = "LDAP_BIND_PASSWORD", length=200, nullable = true)
	private String myBindPassword;
	
	@Column(name = "LDAP_BIND_USER_DN", length=200, nullable = true)
	private String myBindUserDn;
	
	@Column(name = "LDAP_URL", length = 200, nullable = true)
	private String myUrl;

	public PersAuthenticationHostLdap() {
		super();
	}

	public PersAuthenticationHostLdap(String theModuleId) {
		super(theModuleId);
	}


	public String getAuthenticateBaseDn() {
		return myAuthenticateBaseDn;
	}

	public String getAuthenticateFilter() {
		return myAuthenticateFilter;
	}

	public String getBindPassword() {
		return myBindPassword;
	}

	public String getBindUserDn() {
		return myBindUserDn;
	}

	@Override
	public AuthorizationHostTypeEnum getType() {
		return AuthorizationHostTypeEnum.LDAP;
	}

	public String getUrl() {
		return myUrl;
	}

	public void setAuthenticateBaseDn(String theAuthenticateBaseDn) {
		myAuthenticateBaseDn = theAuthenticateBaseDn;
	}

	public void setAuthenticateFilter(String theAuthenticateFilter) {
		myAuthenticateFilter = theAuthenticateFilter;
	}

	public void setBindPassword(String theBindPassword) {
		myBindPassword = theBindPassword;
	}

	public void setBindUserDn(String theBindUserDn) {
		myBindUserDn = theBindUserDn;
	}

	public void setDefaults() {
		myUrl = DEFAULT_HOST;
		myBindUserDn=DEFAULT_BIND_USER_DN;
		myBindPassword=DEFAULT_BIND_USER_PASS;
		myAuthenticateBaseDn=DEFAULT_AUTH_BASE_DN;
		myAuthenticateFilter=DEFAULT_AUTH_FILTER;
	}

	/**
	 * @param theLdapConnTimeout
	 *            the ldapConnTimeout to set
	 */
	public void setLdapConnTimeout(int theLdapConnTimeout) {
		Validate.greaterThanZero(theLdapConnTimeout, "ConnectionTimeout");
		myLdapConnTimeout = theLdapConnTimeout;
	}

	public void setUrl(String theUrl) {
		myUrl = theUrl;
	}

	/**
	 * @param theLdapHost
	 *            the ldapHost to set
	 */
	public void setLdapHost(String theLdapHost) {
		Validate.notBlank(theLdapHost, "Host");
		myLdapHost = theLdapHost;
	}

	/**
	 * @param theLdapPort
	 *            the ldapPort to set
	 */
	public void setLdapPort(int theLdapPort) {
		Validate.greaterThanZero(theLdapPort, "Port");
		myLdapPort = theLdapPort;
	}

}
