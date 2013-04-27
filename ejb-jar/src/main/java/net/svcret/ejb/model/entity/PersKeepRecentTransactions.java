package net.svcret.ejb.model.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import net.svcret.ejb.api.ResponseTypeEnum;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Table(name = "PX_SVC_VER_KEEP_RECENT", uniqueConstraints = { @UniqueConstraint(columnNames = { "SVC_VERSION_PID", "INVOC_OUTCOME" }) })
@Entity
public class PersKeepRecentTransactions implements Serializable {

	private static final long serialVersionUID = 1L;

	@Transient
	private transient Integer myHashCode;

	@EmbeddedId
	private PersKeepRecentTransactionsPk myPk;

	@Column(name = "KEEP_NUM")
	private int myKeepNum;

	@Column(name = "KEEP_ORDER", nullable = false)
	private int myOrder;

	public PersKeepRecentTransactions() {
		super();
	}

	public PersKeepRecentTransactions(ResponseTypeEnum theInvocationOutcome, int theKeepNum) {
		Validate.notNull(theInvocationOutcome);

		myPk = new PersKeepRecentTransactionsPk();
		myPk.myInvocationOutcome = theInvocationOutcome;

		myKeepNum = theKeepNum;
	}

	@Override
	public boolean equals(Object theObj) {
		if (!(theObj instanceof PersKeepRecentTransactions)) {
			return false;
		}

		PersKeepRecentTransactions obj = (PersKeepRecentTransactions) theObj;
		return obj.getInvocationOutcome().equals(myPk.myInvocationOutcome) && obj.myPk.myServiceVersion.getPid().equals(myPk.myServiceVersion.getPid());
	}

	public ResponseTypeEnum getInvocationOutcome() {
		return myPk.myInvocationOutcome;
	}

	public int getKeepNum() {
		return myKeepNum;
	}

	public int getOrder() {
		return myOrder;
	}

	@Override
	public int hashCode() {
		if (myHashCode == null) {
			myHashCode = new HashCodeBuilder().append(myPk.myInvocationOutcome).append(myPk.myServiceVersion).toHashCode();
		}
		return myHashCode;
	}

	public void setInvocationOutcome(ResponseTypeEnum theInvocationOutcome) {
		myPk.myInvocationOutcome = theInvocationOutcome;
	}

	public void setKeepNum(int theKeepNum) {
		myKeepNum = theKeepNum;
	}

	public void setOrder(int theOrder) {
		myOrder = theOrder;
	}

	public void setServiceVersion(BasePersServiceVersion theVersion) {
		myPk.myServiceVersion = theVersion;
	}

	@Embeddable
	public static class PersKeepRecentTransactionsPk implements Serializable {
		private static final long serialVersionUID = 1L;

		@Transient
		private transient Integer myHashCode;

		@Column(name = "INVOC_OUTCOME")
		private ResponseTypeEnum myInvocationOutcome;

		@ManyToOne(cascade = {})
		@JoinColumn(name = "SVC_VERSION_PID", referencedColumnName = "PID", insertable = false, updatable = false)
		private BasePersServiceVersion myServiceVersion;

		@Override
		public boolean equals(Object theObj) {
			if (!(theObj instanceof PersKeepRecentTransactionsPk)) {
				return false;
			}

			PersKeepRecentTransactionsPk obj = (PersKeepRecentTransactionsPk) theObj;
			return obj.myInvocationOutcome.equals(myInvocationOutcome) && obj.myServiceVersion.getPid().equals(myServiceVersion.getPid());
		}

		@Override
		public int hashCode() {
			if (myHashCode == null) {
				myHashCode = new HashCodeBuilder().append(myInvocationOutcome).append(myServiceVersion).toHashCode();
			}
			return myHashCode;
		}

	}

}
