package net.svcret.ejb.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import net.svcret.ejb.api.ResponseTypeEnum;


@Table(name = "PX_SVC_VER_KEEP_RECENT", uniqueConstraints= {@UniqueConstraint(columnNames= {"SVC_VERSION_PID", "INVOC_OUTCOME"})})
@Entity
public class PersKeepRecentTransactions extends BasePersObject {

	private static final long serialVersionUID = 1L;
	
	@Column(name="INVOC_OUTCOME", nullable=false)
	private ResponseTypeEnum myInvocationOutcome;
	
	@Column(name="KEEP_NUM")
	private int myKeepNum;
	
	@Column(name="KEEP_ORDER", nullable=false)
	private int myOrder;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PID")
	private Long myPid;
	
	@ManyToOne()
	@JoinColumn(name = "SVC_VERSION_PID", referencedColumnName = "PID", nullable = false)
	private BasePersServiceVersion myServiceVersion;

	public PersKeepRecentTransactions() {
		super();
	}

	public PersKeepRecentTransactions(ResponseTypeEnum theInvocationOutcome, int theKeepNum) {
		myInvocationOutcome=theInvocationOutcome;
		myKeepNum=theKeepNum;
	}

	public ResponseTypeEnum getInvocationOutcome() {
		return myInvocationOutcome;
	}

	public int getKeepNum() {
		return myKeepNum;
	}

	public int getOrder() {
		return myOrder;
	}

	@Override
	public Long getPid() {
		return myPid;
	}
	
	public void setInvocationOutcome(ResponseTypeEnum theInvocationOutcome) {
		myInvocationOutcome = theInvocationOutcome;
	}

	public void setKeepNum(int theKeepNum) {
		myKeepNum = theKeepNum;
	}

	public void setOrder(int theOrder) {
		myOrder = theOrder;
	}

	public void setServiceVersion(BasePersServiceVersion theVersion) {
		myServiceVersion=theVersion;
	}

}
