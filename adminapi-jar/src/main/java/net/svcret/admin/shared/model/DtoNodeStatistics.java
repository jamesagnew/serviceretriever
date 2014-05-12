package net.svcret.admin.shared.model;

import java.io.Serializable;

public class DtoNodeStatistics implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private int[] myCpuTime;
	private int[] myFailTransactions;
	private int[] myFaultTransactions;
	private int[] myMemoryMax;
	private int[] myMemoryUsed;
	private int[] mySecFailTransactions;
	private int[] mySuccessTransactions;

	public int[] getCpuTime() {
		return myCpuTime;
	}

	public int[] getFailTransactions() {
		return myFailTransactions;
	}

	public int[] getFaultTransactions() {
		return myFaultTransactions;
	}

	public int[] getMemoryMax() {
		return myMemoryMax;
	}

	public int[] getMemoryUsed() {
		return myMemoryUsed;
	}

	public int[] getSecFailTransactions() {
		return mySecFailTransactions;
	}

	public int[] getSuccessTransactions() {
		return mySuccessTransactions;
	}

	public void setCpuTime(int[] theCpuTime) {
		myCpuTime = theCpuTime;
	}

	public void setFailTransactions(int[] theFailTransactions) {
		myFailTransactions = theFailTransactions;
	}

	public void setFaultTransactions(int[] theFaultTransactions) {
		myFaultTransactions = theFaultTransactions;
	}

	public void setMemoryMax(int[] theMemoryMax) {
		myMemoryMax = theMemoryMax;
	}

	public void setMemoryUsed(int[] theMemoryUsed) {
		myMemoryUsed = theMemoryUsed;
	}

	public void setSecFailTransactions(int[] theSecFailTransactions) {
		mySecFailTransactions = theSecFailTransactions;
	}

	public void setSuccessTransactions(int[] theSuccessTransactions) {
		mySuccessTransactions = theSuccessTransactions;
	}

}
