package ca.uhn.sail.proxy.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

import com.google.common.base.Objects;

@Entity
@Table(name = "PX_HTTP_CLIENT_CONFIG")
public class PersHttpClientConfig extends BasePersObject {

	private static final long DEFAULT_CB_TIME_BETWEEN_ATTEMPTS = 60 * 10000;

	/**
	 * Default ID for config. At least this config will always exist.
	 */
	public static final String DEFAULT_ID = "DEFAULT";

	private static final UrlSelectionPolicy DEFAULT_URL_SELECTION_POLICY = UrlSelectionPolicy.PREFER_LOCAL;

	@Column(name = "CB_TIME_BET_ATT")
	private long myCircuitBreakerTimeBetweenResetAttempts;

	@Column(name = "CONFIG_ID", unique = true, nullable = false)
	private String myId;

	@Version()
	@Column(name = "OPTLOCK")
	private int myOptLock;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PID")
	private Long myPid;

	@Column(name = "URL_SEL_POLICY", length = 20, nullable=false)
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
	 * @return the circuitBreakerTimeBetweenResetAttempts
	 */
	public long getCircuitBreakerTimeBetweenResetAttempts() {
		return myCircuitBreakerTimeBetweenResetAttempts;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return myId;
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
	 * @param theCircuitBreakerTimeBetweenResetAttempts
	 *            the circuitBreakerTimeBetweenResetAttempts to set
	 */
	public void setCircuitBreakerTimeBetweenResetAttempts(long theCircuitBreakerTimeBetweenResetAttempts) {
		myCircuitBreakerTimeBetweenResetAttempts = theCircuitBreakerTimeBetweenResetAttempts;
	}

	public void setDefaults() {
		myCircuitBreakerTimeBetweenResetAttempts = DEFAULT_CB_TIME_BETWEEN_ATTEMPTS;
		myUrlSelectionPolicy = DEFAULT_URL_SELECTION_POLICY;
	}

	/**
	 * @param theId
	 *            the id to set
	 */
	public void setId(String theId) {
		myId = theId;
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
	 * @param theUrlSelectionPolicy
	 *            the urlSelectionPolicy to set
	 */
	public void setUrlSelectionPolicy(UrlSelectionPolicy theUrlSelectionPolicy) {
		myUrlSelectionPolicy = theUrlSelectionPolicy;
	}

}
