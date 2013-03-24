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
import javax.persistence.Version;

import net.svcret.ejb.util.LogUtil;
import net.svcret.ejb.util.Validate;

import org.apache.commons.lang3.ObjectUtils;


import com.google.common.base.Objects;

@Table(name = "PX_SVC_VER_URL_STATUS")
@Entity
public class PersServiceVersionUrlStatus extends BasePersObject {

	public static final int MAX_LENGTH_BODY = 5000;
	public static final int MAX_LENGTH_CONTENT_TYPE = 100;
	public static final int MAX_LENGTH_MSG = 500;

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(PersServiceVersionUrlStatus.class);

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

	@Transient
	private transient Date myLastStatusSave;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "LAST_SUCCESS")
	private volatile Date myLastSuccess;

	@Column(name = "NEXT_CB_RESET", nullable = true)
	@Temporal(TemporalType.TIMESTAMP)
	private volatile Date myNextCircuitBreakerReset;

	@Version()
	@Column(name = "OPTLOCK")
	private int myOptLock;

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
	 * 
	 * @return
	 */
	public synchronized boolean attemptToResetCircuitBreaker() {
		StatusEnum status = myStatus;
		Date nextReset = myNextCircuitBreakerReset;
		if (status != StatusEnum.DOWN) {
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
	 * @return the nextCircuitBreakerReset
	 */
	public Date getNextCircuitBreakerReset() {
		return myNextCircuitBreakerReset;
	}

	/**
	 * @return the versionNum
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

	private void resetCurcuitBreaker() {
		if (myNextCircuitBreakerReset == null) {
			return;
		}

		myNextCircuitBreakerReset = null;
		myDirty = true;
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

	/**
	 * @param theOptLock
	 *            the optLock to set
	 */
	public void setOptLock(int theOptLock) {
		myOptLock = theOptLock;
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
		Validate.throwIllegalArgumentExceptionIfNull("Status", theStatus);
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
			tripCircuitBreaker();
			break;
		}
	}

	/**
	 * @param theUrl
	 *            the url to set
	 */
	public void setUrl(PersServiceVersionUrl theUrl) {
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
		myNextCircuitBreakerReset = nextReset;
		myDirty = true;

		return nextReset;
	}

	public static enum StatusEnum {
		/*
		 * NB: Column is 10 chars, don't exceed that
		 */

		/**
		 * URL is up
		 */
		ACTIVE,

		/**
		 * URL is down
		 */
		DOWN,

		/**
		 * URL has not yet been tried
		 */
		UNKNOWN
	}

}
