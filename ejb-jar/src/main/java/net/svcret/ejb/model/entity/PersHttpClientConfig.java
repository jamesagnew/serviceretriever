package net.svcret.ejb.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

import net.svcret.admin.shared.model.GHttpClientConfig;
import net.svcret.admin.shared.model.UrlSelectionPolicy;

import com.google.common.base.Objects;

@Entity
@Table(name = "PX_HTTP_CLIENT_CONFIG")
public class PersHttpClientConfig extends BasePersObject {

	private static final long serialVersionUID = 1L;
	
	public static final int DEFAULT_CB_TIME_BETWEEN_ATTEMPTS = 60 * 1000;
	public static final int DEFAULT_CONNECT_TIMEOUT_MILLIS = 10 * 1000;
	public static final int DEFAULT_FAIL_RETRIES_BEFORE_ABORT = 1;
	/**
	 * Default ID for config. At least this config will always exist.
	 */
	public static final String DEFAULT_ID = GHttpClientConfig.DEFAULT_ID;

	private static final String DEFAULT_NAME = "Default Configuration";

	public static final int DEFAULT_READ_TIMEOUT_MILLIS = 20 * 1000;

	public static final UrlSelectionPolicy DEFAULT_URL_SELECTION_POLICY = UrlSelectionPolicy.PREFER_LOCAL;

	@Column(name = "CB_ENABLED")
	private boolean myCircuitBreakerEnabled;

	@Column(name = "CB_TIME_BET_ATT")
	private int myCircuitBreakerTimeBetweenResetAttempts;

	@Column(name = "CONN_TIMEOUT", nullable = false)
	private int myConnectTimeoutMillis;

	@Column(name = "FAIL_RET_BEF_ABORT")
	private int myFailureRetriesBeforeAborting;

	@Column(name = "CONFIG_ID", unique = true, nullable = false, length = 100)
	private String myId;

	@Column(name = "CONFIG_NAME", unique = true, nullable = true, length = 200)
	private String myName;

	@Version()
	@Column(name = "OPTLOCK")
	private int myOptLock;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PID")
	private Long myPid;

	@Column(name = "READ_TIMEOUT", nullable = false)
	private int myReadTimeoutMillis;

	@Column(name = "URL_SEL_POLICY", length = 20, nullable = false)
	@Enumerated(EnumType.STRING)
	private UrlSelectionPolicy myUrlSelectionPolicy;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object theObj) {
		return theObj instanceof PersHttpClientConfig && Objects.equal(myPid, ((PersHttpClientConfig) theObj).myPid);
	}

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
	 * @return the optLock
	 */
	public int getOptLock() {
		return myOptLock;
	}

	/**
	 * @return the pid
	 */
	public Long getPid() {
		return myPid;
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(myPid);
	}

	/**
	 * @param theCircuitBreakerEnabled the circuitBreakerEnabled to set
	 */
	public void setCircuitBreakerEnabled(boolean theCircuitBreakerEnabled) {
		myCircuitBreakerEnabled = theCircuitBreakerEnabled;
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

	public void setDefaults() {
		myId = DEFAULT_ID;
		myName = DEFAULT_NAME;
		myCircuitBreakerEnabled = true;
		myCircuitBreakerTimeBetweenResetAttempts = DEFAULT_CB_TIME_BETWEEN_ATTEMPTS;
		myUrlSelectionPolicy = DEFAULT_URL_SELECTION_POLICY;
		myConnectTimeoutMillis = DEFAULT_CONNECT_TIMEOUT_MILLIS;
		myReadTimeoutMillis = DEFAULT_READ_TIMEOUT_MILLIS;
		myFailureRetriesBeforeAborting = DEFAULT_FAIL_RETRIES_BEFORE_ABORT;
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
	 * @param theOptLock
	 *            the optLock to set
	 */
	public void setOptLock(int theOptLock) {
		myOptLock = theOptLock;
	}

	/**
	 * @param thePid
	 *            the pid to set
	 */
	public void setPid(Long thePid) {
		myPid = thePid;
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

	public void loadAllAssociations() {
		myUrlSelectionPolicy.name();
	}

}