package net.svcret.ejb.model.entity;

import static org.apache.commons.lang3.StringUtils.*;
import static net.svcret.ejb.util.DateUtil.*;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import net.svcret.admin.shared.model.StatusEnum;
import net.svcret.admin.shared.util.Validate;
import net.svcret.ejb.util.LogUtil;

import com.google.common.base.Objects;

@Table(name = "PX_SVC_VER_URL_STATUS")
@Entity
public class PersServiceVersionUrlStatus extends BasePersObject {

	public static final int MAX_LENGTH_BODY = 2000;

	public static final int MAX_LENGTH_CONTENT_TYPE = 100;
	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(PersServiceVersionUrlStatus.class);

	private static final long serialVersionUID = 1L;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "LAST_FAIL")
	private volatile Date myLastFail;

	// @Column(name = "LAST_FAIL_BODY", nullable = true, length = MAX_LENGTH_BODY)
	// private volatile String myLastFailBody;

	@Column(name = "LAST_FAIL_CTYPE", nullable = true, length = MAX_LENGTH_CONTENT_TYPE)
	private volatile String myLastFailContentType;

	@Column(name = "LAST_FAIL_MSG", nullable = true, length = EntityConstants.MAXLEN_INVOC_OUTCOME_MSG)
	private volatile String myLastFailMessage;

	@Column(name = "LAST_FAIL_STATUS", nullable = true)
	private volatile Integer myLastFailStatusCode;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "LAST_FAULT")
	private volatile Date myLastFault;

	// @Column(name = "LAST_FAULT_BODY", nullable = true, length = MAX_LENGTH_BODY)
	// private volatile String myLastFaultBody;

	@Column(name = "LAST_FAULT_CTYPE", nullable = true, length = MAX_LENGTH_CONTENT_TYPE)
	private volatile String myLastFaultContentType;

	@Column(name = "LAST_FAULT_MSG", nullable = true, length = EntityConstants.MAXLEN_INVOC_OUTCOME_MSG)
	private volatile String myLastFaultMessage;

	@Column(name = "LAST_FAULT_STATUS", nullable = true)
	private volatile Integer myLastFaultStatusCode;

	@Transient
	private transient Date myLastStatusSave;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "LAST_SUCCESS")
	private volatile Date myLastSuccess;

	@Column(name = "LAST_SUCCESS_CTYPE", nullable = true, length = MAX_LENGTH_CONTENT_TYPE)
	private volatile String myLastSuccessContentType;

	@Column(name = "LAST_SUCCESS_MSG", nullable = true, length = EntityConstants.MAXLEN_INVOC_OUTCOME_MSG)
	private String myLastSuccessMessage;

	@Column(name = "LAST_SUCCESS_STATUS", nullable = true)
	private volatile Integer myLastSuccessStatusCode;

	@Column(name = "NEXT_CB_RESET", nullable = true)
	@Temporal(TemporalType.TIMESTAMP)
	private volatile Date myNextCircuitBreakerReset;

	@Column(name = "NEXT_CB_RESET_TIMESTAMP", nullable = true)
	@Temporal(TemporalType.TIMESTAMP)
	private volatile Date myNextCircuitBreakerResetTimestamp;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PID")
	private Long myPid;

	@Column(name = "URL_STATUS", length = 10, nullable = false)
	@Enumerated(EnumType.STRING)
	private StatusEnum myStatus;

	@Column(name = "URL_STATUS_TIMESTAMP", nullable = true)
	@Temporal(TemporalType.TIMESTAMP)
	private Date myStatusTimestamp;

	@OneToOne(cascade = {}, fetch = FetchType.LAZY)
	@JoinColumn(name = "URL_PID", referencedColumnName = "PID", nullable = false, unique = true)
	private PersServiceVersionUrl myUrl;

	@Column(name = "URL_PID", updatable = false, insertable = false)
	private long myUrlPid;

	/**
	 * Constructor
	 */
	public PersServiceVersionUrlStatus() {
		super();
	}

	/**
	 * Unit test Constructor (sets sensible defaults)
	 */
	public PersServiceVersionUrlStatus(long thePid) {
		myPid = thePid;
		myStatus = StatusEnum.UNKNOWN;
	}

	/**
	 * 
	 * @return
	 */
	public synchronized boolean attemptToResetCircuitBreaker() {
		StatusEnum status = myStatus;
		Date nextReset = myNextCircuitBreakerReset;
		if (status != StatusEnum.DOWN) {
			return true;
		}

		PersHttpClientConfig httpClientConfig = getUrl().getServiceVersion().getHttpClientConfig();
		if (!httpClientConfig.isCircuitBreakerEnabled()) {
			return true;
		}

		Date now = new Date();

		if (ourLog.isTraceEnabled()) {
			ourLog.trace("Checking if {} is after {}", LogUtil.formatTime(now), LogUtil.formatTime(nextReset));
		}

		if (nextReset == null || after(now,nextReset)) {
			myNextCircuitBreakerReset = null;
			tripCircuitBreaker();
			return true;
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object theObj) {
		return theObj instanceof PersServiceVersionUrlStatus && Objects.equal(myPid, ((PersServiceVersionUrlStatus) theObj).myPid);
	}

	/**
	 * @return the lastFail
	 */
	public Date getLastFail() {
		return myLastFail;
	}

	/**
	 * @return the lastFailContentType
	 */
	public String getLastFailContentType() {
		return myLastFailContentType;
	}

	/**
	 * @return the lastFailMessage
	 */
	public String getLastFailMessage() {
		return myLastFailMessage;
	}

	/**
	 * @return the lastFailStatusCode
	 */
	public Integer getLastFailStatusCode() {
		return myLastFailStatusCode;
	}

	/**
	 * @return the lastFault
	 */
	public Date getLastFault() {
		return myLastFault;
	}

	// /**
	// * @return the lastFailBody
	// */
	// public String getLastFailBody() {
	// return myLastFailBody;
	// }

	public String getLastFaultContentType() {
		return myLastFaultContentType;
	}

	/**
	 * @return the lastFaultMessage
	 */
	public String getLastFaultMessage() {
		return myLastFaultMessage;
	}

	public Integer getLastFaultStatusCode() {
		return myLastFaultStatusCode;
	}

	/**
	 * @return the lastStatusSave
	 */
	public Date getLastStatusSave() {
		return myLastStatusSave;
	}

	/**
	 * @return the lastSuccess
	 */
	public Date getLastSuccess() {
		return myLastSuccess;
	}

	public String getLastSuccessContentType() {
		return myLastSuccessContentType;
	}

	/**
	 * @return the lastSuccessMessage
	 */
	public String getLastSuccessMessage() {
		return myLastSuccessMessage;
	}

	public Integer getLastSuccessStatusCode() {
		return myLastSuccessStatusCode;
	}

	/**
	 * @return the nextCircuitBreakerReset
	 */
	public Date getNextCircuitBreakerReset() {
		return myNextCircuitBreakerReset;
	}

	public Date getNextCircuitBreakerResetTimestamp() {
		return myNextCircuitBreakerResetTimestamp;
	}

	/**
	 * @return the pid
	 */
	public Long getPid() {
		return myPid;
	}

	/**
	 * @return the status
	 */
	public StatusEnum getStatus() {
		return myStatus;
	}

	public Date getStatusTimestamp() {
		return myStatusTimestamp;
	}

	/**
	 * @return Returns the number of millis since the last successful or fault invocation for this URL. If none, just returns a very large value (System.currentTimeMillis())
	 */
	public long getTimeElapsedSinceLastSuccessOrFault() {
		long now = System.currentTimeMillis();
		long elapsed = -1;

		if (myLastFault != null) {
			elapsed = now - myLastFault.getTime();
		}
		if (myLastSuccess != null) {
			elapsed = Math.min(elapsed, now - myLastSuccess.getTime());
		}

		if (elapsed > 0) {
			return elapsed;
		} else {
			return 0;
		}
	}

	/**
	 * @return the url
	 */
	public PersServiceVersionUrl getUrl() {
		return myUrl;
	}

	public long getUrlPid() {
		return myUrlPid;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(myPid);
	}

	public void loadAllAssociations() {
		// nothing
	}

	/**
	 * @return Returns true if this bean has any values that are more recent then the given bean, and therefore has values that should presumably be saved back
	 */
	public boolean mergeNewer(PersServiceVersionUrlStatus theExisting) {

		boolean retVal = false;

		if (getLastSuccess() == null || (theExisting.getLastSuccess() != null && after(theExisting.getLastSuccess(),getLastSuccess()))) {
			setLastSuccess(theExisting.getLastSuccess());
			setLastSuccessContentType(theExisting.getLastSuccessContentType());
			setLastSuccessMessage(theExisting.getLastSuccessMessage());
			setLastSuccessStatusCode(theExisting.getLastSuccessStatusCode());
		} else if (getLastSuccess() != null && (theExisting.getLastSuccess() == null || before(theExisting.getLastSuccess(),getLastSuccess()))) {
			retVal = true;
		}

		if (getLastFault() == null || (theExisting.getLastFault() != null && after(theExisting.getLastFault(),getLastFault()))) {
			setLastFault(theExisting.getLastFault());
			setLastFaultContentType(theExisting.getLastFaultContentType());
			setLastFaultMessage(theExisting.getLastFaultMessage());
			setLastFaultStatusCode(theExisting.getLastFaultStatusCode());
		} else if (getLastFault() != null && (theExisting.getLastFault() == null || before(theExisting.getLastFault(),getLastFault()))) {
			retVal = true;
		}

		if (getLastFail() == null || (theExisting.getLastFail() != null && after(theExisting.getLastFail(),getLastFail()))) {
			setLastFail(theExisting.getLastFail());
			setLastFailContentType(theExisting.getLastFailContentType());
			setLastFailMessage(theExisting.getLastFailMessage());
			setLastFailStatusCode(theExisting.getLastFailStatusCode());
		} else if (getLastFail() != null && (theExisting.getLastFail() == null || before(theExisting.getLastFail(),getLastFail()))) {
			retVal = true;
		}

		if (getStatusTimestamp() == null || (theExisting.getStatusTimestamp() != null && after(theExisting.getStatusTimestamp(),getStatusTimestamp()))) {
			myStatus = theExisting.getStatus(); // Setter will trip the CB here!
			setStatusTimestamp(theExisting.getStatusTimestamp());
		} else if (getStatusTimestamp() != null && (theExisting.getStatusTimestamp() == null || before(theExisting.getStatusTimestamp(),getStatusTimestamp()))) {
			retVal = true;
		}

		if (getNextCircuitBreakerResetTimestamp() == null
				|| (theExisting.getNextCircuitBreakerResetTimestamp() != null && after(theExisting.getNextCircuitBreakerResetTimestamp(),getNextCircuitBreakerResetTimestamp()))) {
			myNextCircuitBreakerReset = theExisting.getNextCircuitBreakerReset(); // Setter will trip the CB here!
			setNextCircuitBreakerResetTimestamp(theExisting.getNextCircuitBreakerResetTimestamp());
		} else if (getNextCircuitBreakerResetTimestamp() != null
				&& (theExisting.getNextCircuitBreakerResetTimestamp() == null || before(theExisting.getNextCircuitBreakerResetTimestamp(),getNextCircuitBreakerResetTimestamp()))) {
			retVal = true;
		}

		return retVal;
	}

	private void resetCurcuitBreaker() {
		if (myNextCircuitBreakerReset == null) {
			return;
		}

		setNextCircuitBreakerReset(null);
	}

	/**
	 * @param theLastFail
	 *            the lastFail to set
	 */
	public void setLastFail(Date theLastFail) {
		myLastFail = theLastFail;
	}

	// /**
	// * @param theLastFailBody
	// * the lastFailBody to set
	// */
	// public void setLastFailBody(String theLastFailBody) {
	// myLastFailBody = left(theLastFailBody, MAX_LENGTH_BODY);
	// }

	/**
	 * @param theLastFailContentType
	 *            the lastFailContentType to set
	 */
	public void setLastFailContentType(String theLastFailContentType) {
		myLastFailContentType = left(theLastFailContentType, MAX_LENGTH_CONTENT_TYPE);
	}

	/**
	 * @param theLastFailMessage
	 *            the lastFailMessage to set
	 */
	public void setLastFailMessage(String theLastFailMessage) {
		myLastFailMessage = left(theLastFailMessage, EntityConstants.MAXLEN_INVOC_OUTCOME_MSG);
	}

	/**
	 * @param theLastFailStatusCode
	 *            the lastFailStatusCode to set
	 */
	public void setLastFailStatusCode(Integer theLastFailStatusCode) {
		myLastFailStatusCode = theLastFailStatusCode;
	}

	/**
	 * @param theLastFault
	 *            the lastFail to set
	 */
	public void setLastFault(Date theLastFault) {
		myLastFault = theLastFault;
	}

	// /**
	// * @param theLastFaultBody
	// * the lastFaultBody to set
	// */
	// public void setLastFaultBody(String theLastFaultBody) {
	// myLastFaultBody = left(theLastFaultBody, MAX_LENGTH_BODY);
	// }

	/**
	 * @param theLastFaultContentType
	 *            the lastFaultContentType to set
	 */
	public void setLastFaultContentType(String theLastFaultContentType) {
		myLastFaultContentType = left(theLastFaultContentType, MAX_LENGTH_CONTENT_TYPE);
	}

	/**
	 * @param theLastFaultMessage
	 *            the lastFaultMessage to set
	 */
	public void setLastFaultMessage(String theLastFaultMessage) {
		myLastFaultMessage = left(theLastFaultMessage, EntityConstants.MAXLEN_INVOC_OUTCOME_MSG);
	}

	/**
	 * @param theLastFaultStatusCode
	 *            the lastFaultStatusCode to set
	 */
	public void setLastFaultStatusCode(Integer theLastFaultStatusCode) {
		myLastFaultStatusCode = theLastFaultStatusCode;
	}

	/**
	 * @param theLastStatusSave
	 *            the lastStatusSave to set
	 */
	public void setLastStatusSave(Date theLastStatusSave) {
		myLastStatusSave = theLastStatusSave;
	}

	/**
	 * @param theLastSuccess
	 *            the lastSuccess to set
	 */
	public void setLastSuccess(Date theLastSuccess) {
		myLastSuccess = theLastSuccess;
	}

	public void setLastSuccessContentType(String theLastSuccessContentType) {
		myLastSuccessContentType = theLastSuccessContentType;
	}

	public void setLastSuccessMessage(String theMessage) {
		myLastSuccessMessage = left(theMessage, EntityConstants.MAXLEN_INVOC_OUTCOME_MSG);
	}

	public void setLastSuccessStatusCode(Integer theLastSuccessStatusCode) {
		myLastSuccessStatusCode = theLastSuccessStatusCode;
	}

	public void setNextCircuitBreakerReset(Date theNextCircuitBreakerReset) {
		myNextCircuitBreakerReset = theNextCircuitBreakerReset;
		myNextCircuitBreakerResetTimestamp = new Date();
	}

	public void setNextCircuitBreakerResetTimestamp(Date theNextCircuitBreakerResetTimestamp) {
		myNextCircuitBreakerResetTimestamp = theNextCircuitBreakerResetTimestamp;
	}

	/**
	 * @param thePid
	 *            the id to set
	 */
	public void setPid(Long thePid) {
		myPid = thePid;
	}

	/**
	 * @param theStatus
	 *            the status to set
	 */
	public synchronized void setStatus(StatusEnum theStatus) {
		Validate.notNull(theStatus, "Status");
		if (theStatus.equals(myStatus)) {
			return;
		}

		myStatus = theStatus;
		myStatusTimestamp = new Date();

		switch (theStatus) {
		case ACTIVE:
		case UNKNOWN:
			resetCurcuitBreaker();
			break;
		case DOWN:
			PersHttpClientConfig httpClientConfig = getUrl().getServiceVersion().getHttpClientConfig();
			if (httpClientConfig.isCircuitBreakerEnabled()) {
				tripCircuitBreaker();
			}
			break;
		}
	}

	public void setStatusTimestamp(Date theStatusTimestamp) {
		myStatusTimestamp = theStatusTimestamp;
	}

	/**
	 * @param theUrl
	 *            the url to set
	 */
	public void setUrl(PersServiceVersionUrl theUrl) {
		if (myUrl == theUrl) {
			return;
		}
		theUrl.setStatus(this);
		myUrl = theUrl;
	}

	/**
	 * @param theNextCircuitBreakerReset
	 *            the nextCircuitBreakerReset to set
	 * @return
	 */
	private Date tripCircuitBreaker() {
		Date nextReset = myNextCircuitBreakerReset;
		if (nextReset != null) {
			return nextReset;
		}

		PersHttpClientConfig httpClientConfig = getUrl().getServiceVersion().getHttpClientConfig();
		long timeBetweenResetAttempts = httpClientConfig.getCircuitBreakerTimeBetweenResetAttempts();
		long now = new Date().getTime();

		nextReset = new Date(now + timeBetweenResetAttempts);
		setNextCircuitBreakerReset(nextReset);

		return nextReset;
	}

}
