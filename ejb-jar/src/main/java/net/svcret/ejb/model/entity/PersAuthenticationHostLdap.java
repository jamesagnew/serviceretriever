package net.svcret.ejb.model.entity;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import net.svcret.admin.shared.model.AuthorizationHostTypeEnum;
import net.svcret.ejb.util.Validate;

/**
 * LDAP authentication host
 */
@Entity
@DiscriminatorValue("LDAP")
public class PersAuthenticationHostLdap extends BasePersAuthenticationHost {

	private static final long serialVersionUID = 1L;
	
	private static final int DEFAULT_CONN_TIMEOUT = 30 * 1000;
	private static final String DEFAULT_HOST = "localhost";
	private static final int DEFAULT_PORT = 389;

	@Column(name = "LDAP_CONNECTIONTIMEOUT", nullable = true)
	private Integer myLdapConnTimeout;

	@Column(name = "LDAP_HOST", length = 200, nullable = true)
	private String myLdapHost;

	@Column(name = "LDAP_PORT", nullable = true)
	private Integer myLdapPort;

	public PersAuthenticationHostLdap() {
		super();
	}

	public PersAuthenticationHostLdap(String theModuleId) {
		super(theModuleId);
	}

	/**
	 * @return the ldapConnTimeout
	 */
	public Integer getLdapConnTimeout() {
		if (myLdapConnTimeout == null) {
			return DEFAULT_CONN_TIMEOUT;
		}
		return myLdapConnTimeout;
	}

	/**
	 * @return the ldapHost
	 */
	public String getLdapHost() {
		if (myLdapHost == null) {
			return DEFAULT_HOST;
		}
		return myLdapHost;
	}

	/**
	 * @return the ldapPort
	 */
	public Integer getLdapPort() {
		if (myLdapPort == null) {
			return DEFAULT_PORT;
		}
		return myLdapPort;
	}

	@Override
	public AuthorizationHostTypeEnum getType() {
		return AuthorizationHostTypeEnum.LDAP;
	}

	public void setDefaults() {
		myLdapHost = DEFAULT_HOST;
		myLdapPort = DEFAULT_PORT;
		myLdapConnTimeout = DEFAULT_CONN_TIMEOUT;
	}

	/**
	 * @param theLdapConnTimeout
	 *            the ldapConnTimeout to set
	 */
	public void setLdapConnTimeout(int theLdapConnTimeout) {
		Validate.throwIllegalArgumentExceptionIfNotPositive("ConnectionTimeout", theLdapConnTimeout);
		myLdapConnTimeout = theLdapConnTimeout;
	}

	/**
	 * @param theLdapHost
	 *            the ldapHost to set
	 */
	public void setLdapHost(String theLdapHost) {
		Validate.throwIllegalArgumentExceptionIfBlank("Host", theLdapHost);
		myLdapHost = theLdapHost;
	}

	/**
	 * @param theLdapPort
	 *            the ldapPort to set
	 */
	public void setLdapPort(int theLdapPort) {
		Validate.throwIllegalArgumentExceptionIfNotPositive("Port", theLdapPort);
		myLdapPort = theLdapPort;
	}

}
