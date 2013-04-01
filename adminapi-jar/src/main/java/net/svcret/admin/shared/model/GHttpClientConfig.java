package net.svcret.admin.shared.model;

public class GHttpClientConfig extends BaseGObject<GHttpClientConfig> {

	public static final String DEFAULT_ID = "DEFAULT";

	private static final long serialVersionUID = 1L;

	private boolean myCircuitBreakerEnabled;
	private int myCircuitBreakerTimeBetweenResetAttempts;
	private int myConnectTimeoutMillis;
	private int myFailureRetriesBeforeAborting;
	private String myId;
	private String myName;
	private int myReadTimeoutMillis;
	private UrlSelectionPolicy myUrlSelectionPolicy;

	/**
	 * @return the circuitBreakerEnabled
	 */
	public boolean isCircuitBreakerEnabled() {
		return myCircuitBreakerEnabled;
	}

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

	/**
	 * @return the urlSelectionPolicy
	 */
	public UrlSelectionPolicy getUrlSelectionPolicy() {
		return myUrlSelectionPolicy;
	}

	@Override
	public void merge(GHttpClientConfig theObject) {
		myCircuitBreakerEnabled = theObject.myCircuitBreakerEnabled;
		myCircuitBreakerTimeBetweenResetAttempts = theObject.myCircuitBreakerTimeBetweenResetAttempts;
		myConnectTimeoutMillis = theObject.myConnectTimeoutMillis;
		myFailureRetriesBeforeAborting = theObject.myFailureRetriesBeforeAborting;
		myId = theObject.myId;
		myName = theObject.myName;
		myReadTimeoutMillis = theObject.myReadTimeoutMillis;
		myUrlSelectionPolicy = theObject.myUrlSelectionPolicy;
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

	/**
	 * @param theUrlSelectionPolicy
	 *            the urlSelectionPolicy to set
	 */
	public void setUrlSelectionPolicy(UrlSelectionPolicy theUrlSelectionPolicy) {
		myUrlSelectionPolicy = theUrlSelectionPolicy;
	}

	public boolean isDefault() {
		return DEFAULT_ID.equals(myId);
	}

}
