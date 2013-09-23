package net.svcret.admin.shared.model;

import java.io.Serializable;

import net.svcret.admin.shared.enm.NodeStatusEnum;

public class DtoNodeStatus implements Serializable {

	private static final long serialVersionUID = 1L;

	private String myNodeId;
	private NodeStatusEnum myStatus;
	private Long myTimeElapsedSinceDown;
	private Long myTimeElapsedSinceLastTx;
	private double myTransactionsFailPerMinute;
	private double myTransactionsFaultPerMinute;
	private double myTransactionsSecurityFailPerMinute;

	private double myTransactionsSuccessfulPerMinute;

	public String getNodeId() {
		return myNodeId;
	}

	public NodeStatusEnum getStatus() {
		return myStatus;
	}

	public Long getTimeElapsedSinceDown() {
		return myTimeElapsedSinceDown;
	}

	public Long getTimeElapsedSinceLastTx() {
		return myTimeElapsedSinceLastTx;
	}

	public double getTransactionsFailPerMinute() {
		return myTransactionsFailPerMinute;
	}

	public double getTransactionsFaultPerMinute() {
		return myTransactionsFaultPerMinute;
	}

	public double getTransactionsSecurityFailPerMinute() {
		return myTransactionsSecurityFailPerMinute;
	}

	public double getTransactionsSuccessfulPerMinute() {
		return myTransactionsSuccessfulPerMinute;
	}

	public void setNodeId(String theNodeId) {
		myNodeId = theNodeId;
	}

	public void setStatus(NodeStatusEnum theStatus) {
		myStatus = theStatus;
	}

	public void setTimeElapsedSinceDown(Long theTimeElapsedSinceDown) {
		myTimeElapsedSinceDown = theTimeElapsedSinceDown;
	}

	public void setTimeElapsedSinceLastTx(Long theTimeElapsedSinceLastTx) {
		myTimeElapsedSinceLastTx = theTimeElapsedSinceLastTx;
	}

	public void setTransactionsFailPerMinute(double theTransactionsFailPerMinute) {
		myTransactionsFailPerMinute = theTransactionsFailPerMinute;
	}

	public void setTransactionsFaultPerMinute(double theTransactionsFaultPerMinute) {
		myTransactionsFaultPerMinute = theTransactionsFaultPerMinute;
	}

	public void setTransactionsSecurityFailPerMinute(double theCurrentTransactionsPerMinuteSecurityFail) {
		myTransactionsSecurityFailPerMinute = theCurrentTransactionsPerMinuteSecurityFail;

	}

	public void setTransactionsSuccessfulPerMinute(double theTransactionsSuccessfulPerMinute) {
		myTransactionsSuccessfulPerMinute = theTransactionsSuccessfulPerMinute;
	}

}
