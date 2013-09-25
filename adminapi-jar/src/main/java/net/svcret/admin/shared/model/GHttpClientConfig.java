package net.svcret.admin.shared.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import net.svcret.admin.shared.util.XmlConstants;

@XmlType(namespace=XmlConstants.DTO_NAMESPACE, name="HttpClientConfig")
@XmlAccessorType(XmlAccessType.FIELD)
public class GHttpClientConfig extends BaseDtoObject {

	public static final String DEFAULT_ID = "DEFAULT";

	private static final long serialVersionUID = 1L;

	@XmlElement(name="config_CircuitBreakerEnabled")
	private boolean myCircuitBreakerEnabled;
	@XmlElement(name="config_CircuitBreakerTimeBetweenResetAttempts")
	private int myCircuitBreakerTimeBetweenResetAttempts;
	@XmlElement(name="config_ConnectTimeoutMillis")
	private int myConnectTimeoutMillis;
	@XmlElement(name="config_FailureRetriesBeforeAborting")
	private int myFailureRetriesBeforeAborting;
	@XmlElement(name="config_Id")
	private String myId;
	@XmlElement(name="config_Name")
	private String myName;
	@XmlElement(name="config_ReadTimeoutMillis")
	private int myReadTimeoutMillis;
	@XmlElement(name="config_StickySessionCookieForSessionId")
	private String myStickySessionCookieForSessionId;
	@XmlElement(name="config_TlsKeystore")
	private DtoKeystoreAnalysis myTlsKeystore;
	@XmlElement(name="config_TlsTruststore")
	private DtoKeystoreAnalysis myTlsTruststore;
	@XmlElement(name="config_UrlSelectionPolicy")
	private UrlSelectionPolicy myUrlSelectionPolicy;

	/**
	 * @return the circuitBreakerTimeBetweenResetAttempts
	 */
	public int getCircuitBreakerTimeBetweenResetAttempts() {
		return myCircuitBreakerTimeBetweenResetAttempts;
	}

	/**
	 * @return the connectTimeoutMillis
	 */
	public int getConnectTimeoutMillis() {
		return myConnectTimeoutMillis;
	}

	/**
	 * @return the failureRetriesBeforeAborting
	 */
	public int getFailureRetriesBeforeAborting() {
		return myFailureRetriesBeforeAborting;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return myId;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return myName;
	}

	/**
	 * @return the readTimeoutMillis
	 */
	public int getReadTimeoutMillis() {
		return myReadTimeoutMillis;
	}

	public String getStickySessionCookieForSessionId() {
		return myStickySessionCookieForSessionId;
	}

	public DtoKeystoreAnalysis getTlsKeystore() {
		return myTlsKeystore;
	}

	public DtoKeystoreAnalysis getTlsTruststore() {
		return myTlsTruststore;
	}

	/**
	 * @return the urlSelectionPolicy
	 */
	public UrlSelectionPolicy getUrlSelectionPolicy() {
		return myUrlSelectionPolicy;
	}

	/**
	 * @return the circuitBreakerEnabled
	 */
	public boolean isCircuitBreakerEnabled() {
		return myCircuitBreakerEnabled;
	}

	public boolean isDefault() {
		return DEFAULT_ID.equals(myId);
	}

	@Override
	public void merge(BaseDtoObject theObject) {
		super.merge(theObject);
		
		GHttpClientConfig obj = (GHttpClientConfig)theObject;
		myCircuitBreakerEnabled = obj.myCircuitBreakerEnabled;
		myCircuitBreakerTimeBetweenResetAttempts = obj.myCircuitBreakerTimeBetweenResetAttempts;
		myConnectTimeoutMillis = obj.myConnectTimeoutMillis;
		myFailureRetriesBeforeAborting = obj.myFailureRetriesBeforeAborting;
		myId = obj.myId;
		myName = obj.myName;
		myReadTimeoutMillis = obj.myReadTimeoutMillis;
		myUrlSelectionPolicy = obj.myUrlSelectionPolicy;
	}

	/**
	 * @param theEnabled
	 *            the circuitBreakerEnabled to set
	 */
	public void setCircuitBreakerEnabled(boolean theEnabled) {
		myCircuitBreakerEnabled = theEnabled;
	}

	/**
	 * @param theCircuitBreakerTimeBetweenResetAttempts
	 *            the circuitBreakerTimeBetweenResetAttempts to set
	 */
	public void setCircuitBreakerTimeBetweenResetAttempts(int theCircuitBreakerTimeBetweenResetAttempts) {
		myCircuitBreakerTimeBetweenResetAttempts = theCircuitBreakerTimeBetweenResetAttempts;
	}

	/**
	 * @param theConnectTimeoutMillis
	 *            the connectTimeoutMillis to set
	 */
	public void setConnectTimeoutMillis(int theConnectTimeoutMillis) {
		myConnectTimeoutMillis = theConnectTimeoutMillis;
	}

	/**
	 * @param theFailureRetriesBeforeAborting
	 *            the failureRetriesBeforeAborting to set
	 */
	public void setFailureRetriesBeforeAborting(int theFailureRetriesBeforeAborting) {
		myFailureRetriesBeforeAborting = theFailureRetriesBeforeAborting;
	}

	/**
	 * @param theId
	 *            the id to set
	 */
	public void setId(String theId) {
		myId = theId;
	}

	/**
	 * @param theName
	 *            the name to set
	 */
	public void setName(String theName) {
		myName = theName;
	}

	/**
	 * @param theReadTimeoutMillis
	 *            the readTimeoutMillis to set
	 */
	public void setReadTimeoutMillis(int theReadTimeoutMillis) {
		myReadTimeoutMillis = theReadTimeoutMillis;
	}

	public void setStickySessionCookieForSessionId(String theStickySessionCookieForSessionId) {
		myStickySessionCookieForSessionId=theStickySessionCookieForSessionId;
	}

	public void setTlsKeystore(DtoKeystoreAnalysis theKeystore) {
		myTlsKeystore = theKeystore;
	}

	public void setTlsTruststore(DtoKeystoreAnalysis theTruststore) {
		myTlsTruststore = theTruststore;
	}

	/**
	 * @param theUrlSelectionPolicy
	 *            the urlSelectionPolicy to set
	 */
	public void setUrlSelectionPolicy(UrlSelectionPolicy theUrlSelectionPolicy) {
		myUrlSelectionPolicy = theUrlSelectionPolicy;
	}

}
