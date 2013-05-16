package net.svcret.ejb.model.entity;

import static org.apache.commons.lang3.StringUtils.*;

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
import net.svcret.ejb.util.LogUtil;
import net.svcret.ejb.util.Validate;

import org.apache.commons.lang3.ObjectUtils;

import com.google.common.base.Objects;

@Table(name = "PX_SVC_VER_URL_STATUS")
@Entity
public class PersServiceVersionUrlStatus extends BasePersObject {

	public static final int MAX_LENGTH_BODY = 2000;

	public static final int MAX_LENGTH_CONTENT_TYPE = 100;
	public static final int MAX_LENGTH_MSG = 500;
	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(PersServiceVersionUrlStatus.class);

	private static final long serialVersionUID = 1L;

	@Transient
	private transient volatile boolean myDirty = false;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "LAST_FAIL")
	private volatile Date myLastFail;

	@Column(name = "LAST_FAIL_BODY", nullable = true, length = MAX_LENGTH_BODY)
	private volatile String myLastFailBody;

	@Column(name = "LAST_FAIL_CTYPE", nullable = true, length = MAX_LENGTH_CONTENT_TYPE)
	private volatile String myLastFailContentType;

	@Column(name = "LAST_FAIL_MSG", nullable = true, length = MAX_LENGTH_MSG)
	private volatile String myLastFailMessage;

	@Column(name = "LAST_FAIL_STATUS", nullable = true)
	private volatile Integer myLastFailStatusCode;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "LAST_FAULT")
	private volatile Date myLastFault;

	@Column(name = "LAST_FAULT_BODY", nullable = true, length = MAX_LENGTH_BODY)
	private volatile String myLastFaultBody;

	@Column(name = "LAST_FAULT_CTYPE", nullable = true, length = MAX_LENGTH_CONTENT_TYPE)
	private volatile String myLastFaultContentType;

	@Column(name = "LAST_FAULT_MSG", nullable = true, length = MAX_LENGTH_MSG)
	private volatile String myLastFaultMessage;

	@Column(name = "LAST_FAULT_STATUS", nullable = true)
	private volatile Integer myLastFaultStatusCode;

	@Transient
	private transient Date myLastStatusSave;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "LAST_SUCCESS")
	private volatile Date myLastSuccess;

	@Column(name = "LAST_SUCCESS_MSG", nullable = true, length = MAX_LENGTH_MSG)
	private String myLastSuccessMessage;

	@Column(name = "NEXT_CB_RESET", nullable = true)
	@Temporal(TemporalType.TIMESTAMP)
	private volatile Date myNextCircuitBreakerReset;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PID")
	private Long myPid;

	@Column(name = "URL_STATUS", length = 10, nullable = false)
	@Enumerated(EnumType.STRING)
	private StatusEnum myStatus;

	@OneToOne(cascade = {}, fetch = FetchType.LAZY)
	@JoinColumn(name = "URL_PID", referencedColumnName = "PID", nullable = false, unique = true)
	private PersServiceVersionUrl myUrl;

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

		if (now.after(nextReset)) {
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
	 * @return the lastFailBody
	 */
	public String getLastFailBody() {
		return myLastFailBody;
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

	/**
	 * @return the lastFaultMessage
	 */
	public String getLastFaultMessage() {
		return myLastFaultMessage;
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

	/**
	 * @return the lastSuccessMessage
	 */
	public String getLastSuccessMessage() {
		return myLastSuccessMessage;
	}

	/**
	 * @return the nextCircuitBreakerReset
	 */
	public Date getNextCircuitBreakerReset() {
		return myNextCircuitBreakerReset;
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

	/**
	 * @return the url
	 */
	public PersServiceVersionUrl getUrl() {
		return myUrl;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(myPid);
	}

	/**
	 * @return the dirty
	 */
	public boolean isDirty() {
		return myDirty;
	}

	public void loadAllAssociations() {
		// nothing
	}

	public void setDirty(boolean theB) {
		myDirty = theB;
	}

	/**
	 * @param theLastFail
	 *            the lastFail to set
	 */
	public void setLastFail(Date theLastFail) {
		if (!ObjectUtils.equals(myLastFail, theLastFail)) {
			myDirty = true;
		}
		myLastFail = theLastFail;
	}

	/**
	 * @param theLastFailBody
	 *            the lastFailBody to set
	 */
	public void setLastFailBody(String theLastFailBody) {
		myLastFailBody = left(theLastFailBody, MAX_LENGTH_BODY);
	}

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
		myLastFailMessage = left(theLastFailMessage, MAX_LENGTH_MSG);
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
		if (!ObjectUtils.equals(myLastFault, theLastFault)) {
			myDirty = true;
		}
		myLastFault = theLastFault;
	}

	/**
	 * @param theLastFaultBody
	 *            the lastFaultBody to set
	 */
	public void setLastFaultBody(String theLastFaultBody) {
		myLastFaultBody = left(theLastFaultBody, MAX_LENGTH_BODY);
	}

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
		myLastFaultMessage = left(theLastFaultMessage, MAX_LENGTH_MSG);
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
		if (!ObjectUtils.equals(myLastSuccess, theLastSuccess)) {
			myDirty = true;
		}
		myLastSuccess = theLastSuccess;
	}

	public void setLastSuccessMessage(String theMessage) {
		myLastSuccessMessage = left(theMessage, MAX_LENGTH_MSG);
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
		myDirty = true;
		myStatus = theStatus;
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

	private void resetCurcuitBreaker() {
		if (myNextCircuitBreakerReset == null) {
			return;
		}

		myNextCircuitBreakerReset = null;
		myDirty = true;
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
		myNextCircuitBreakerReset = nextReset;
		myDirty = true;

		return nextReset;
	}

}