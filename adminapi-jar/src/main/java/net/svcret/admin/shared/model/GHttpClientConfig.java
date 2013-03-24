package net.svcret.admin.shared.model;

public class GHttpClientConfig extends BaseGObject<GHttpClientConfig> {

	private static final long serialVersionUID = 1L;

	private int myCircuitBreakerTimeBetweenResetAttempts;
	private int myConnectTimeoutMillis;
	private int myFailureRetriesBeforeAborting;
	private String myId;
	private int myReadTimeoutMillis;
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

}
