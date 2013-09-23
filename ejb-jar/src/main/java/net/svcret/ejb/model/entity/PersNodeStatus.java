package net.svcret.ejb.model.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang3.time.DateUtils;

import net.svcret.admin.shared.enm.NodeStatusEnum;
import net.svcret.admin.shared.model.DtoNodeStatus;

//@formatter:off
@Table(name = "PX_NODE_STATUS")
@NamedQueries(value = {
	@NamedQuery(name=Queries.NODESTATUS_FINDALL, query=Queries.NODRSTATUS_FINDALL_Q)
})
@Entity()
//@formatter:on
public class PersNodeStatus {

	public static final int NODEID_MAXLENGTH = 20;

	@Column(name = "CUR_TPM_FAIL", nullable = false)
	public double myCurrentTransactionsPerMinuteFail;

	@Column(name = "CUR_TPM_FAULT", nullable = false)
	public double myCurrentTransactionsPerMinuteFault;

	@Column(name = "CUR_TPM_SUC", nullable = false)
	public double myCurrentTransactionsPerMinuteSuccessful;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "ACTIVE_SINCE", nullable = true)
	public Date myNodeActiveSince;

	@Id
	@Column(name = "NODE_ID", length = NODEID_MAXLENGTH, nullable = false)
	public String myNodeId;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "LAST_TRANSACTION", nullable = true)
	public Date myNodeLastTransaction;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "LAST_UPDATE", nullable = false)
	public Date myStatusTimestamp;

	@Column(name = "CUR_TPM_SECFAIL", nullable = false)
	private double myCurrentTransactionsPerMinuteSecurityFail;

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof PersNodeStatus)) {
			return false;
		}
		PersNodeStatus other = (PersNodeStatus) obj;
		if (myNodeId == null) {
			if (other.myNodeId != null) {
				return false;
			}
		} else if (!myNodeId.equals(other.myNodeId)) {
			return false;
		}
		return true;
	}

	public double getCurrentTransactionsPerMinuteFail() {
		return myCurrentTransactionsPerMinuteFail;
	}

	public double getCurrentTransactionsPerMinuteFault() {
		return myCurrentTransactionsPerMinuteFault;
	}

	public double getCurrentTransactionsPerMinuteSecurityFail() {
		return myCurrentTransactionsPerMinuteSecurityFail;
	}

	public double getCurrentTransactionsPerMinuteSuccessful() {
		return myCurrentTransactionsPerMinuteSuccessful;
	}

	public Date getNodeActiveSince() {
		return myNodeActiveSince;
	}

	public String getNodeId() {
		return myNodeId;
	}

	public Date getNodeLastTransaction() {
		return myNodeLastTransaction;
	}

	public Date getStatusTimestamp() {
		return myStatusTimestamp;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((myNodeId == null) ? 0 : myNodeId.hashCode());
		return result;
	}

	public void setCurrentTransactionsPerMinuteFail(double theCurrentTransactionsPerMinuteFail) {
		myCurrentTransactionsPerMinuteFail = theCurrentTransactionsPerMinuteFail;
	}

	public void setCurrentTransactionsPerMinuteFault(double theCurrentTransactionsPerMinuteFault) {
		myCurrentTransactionsPerMinuteFault = theCurrentTransactionsPerMinuteFault;
	}

	public void setCurrentTransactionsPerMinuteSecurityFail(double theTransactionsPerMinute) {
		myCurrentTransactionsPerMinuteSecurityFail = theTransactionsPerMinute;
	}

	public void setCurrentTransactionsPerMinuteSuccessful(double theCurrentTransactionsPerMinuteSuccessful) {
		myCurrentTransactionsPerMinuteSuccessful = theCurrentTransactionsPerMinuteSuccessful;
	}

	public void setNodeActiveSince(Date theNodeActiveSince) {
		myNodeActiveSince = theNodeActiveSince;
	}

	public void setNodeId(String theNodeId) {
		myNodeId = theNodeId;
	}

	public void setNodeLastTransactionIfNewer(Date theNodeLastTransaction) {
		if (myNodeLastTransaction == null) {
			myNodeLastTransaction = theNodeLastTransaction;
		} else if (theNodeLastTransaction != null && theNodeLastTransaction.after(myNodeLastTransaction)) {
			myNodeLastTransaction = theNodeLastTransaction;
		}
	}

	public void setStatusTimestamp(Date theStatusTimestamp) {
		myStatusTimestamp = theStatusTimestamp;
	}

	public DtoNodeStatus toDao() {
		DtoNodeStatus retVal = new DtoNodeStatus();

		retVal.setNodeId(getNodeId());

		long timeElapsedSinceStartup = System.currentTimeMillis() - getNodeActiveSince().getTime();
		if (timeElapsedSinceStartup < (2 * DateUtils.MILLIS_PER_MINUTE)) {

			retVal.setStatus(NodeStatusEnum.RECENTLY_STARTED);

		} else {

			long timeElapsedSinceUpdate = System.currentTimeMillis() - getStatusTimestamp().getTime();
			if (timeElapsedSinceUpdate > (2 * DateUtils.MILLIS_PER_MINUTE)) {
				retVal.setStatus(NodeStatusEnum.DOWN);
				retVal.setTimeElapsedSinceDown(timeElapsedSinceUpdate - DateUtils.MILLIS_PER_MINUTE);
			} else {

				double currentThroughput = getCurrentTransactionsPerMinuteSuccessful() + getCurrentTransactionsPerMinuteFault() + getCurrentTransactionsPerMinuteFail();
				if (currentThroughput > 0.0) {
					retVal.setStatus(NodeStatusEnum.ACTIVE);
				} else {
					retVal.setStatus(NodeStatusEnum.NO_REQUESTS);
					if (getNodeLastTransaction() != null) {
						retVal.setTimeElapsedSinceLastTx(System.currentTimeMillis() - getNodeLastTransaction().getTime());
					}
				}

				retVal.setTransactionsSuccessfulPerMinute(getCurrentTransactionsPerMinuteSuccessful());
				retVal.setTransactionsFaultPerMinute(getCurrentTransactionsPerMinuteFault());
				retVal.setTransactionsFailPerMinute(getCurrentTransactionsPerMinuteFail());
				retVal.setTransactionsSecurityFailPerMinute(getCurrentTransactionsPerMinuteSecurityFail());

			}

		}

		return retVal;
	}

}
