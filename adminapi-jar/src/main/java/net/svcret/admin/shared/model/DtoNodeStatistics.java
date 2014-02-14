package net.svcret.admin.shared.model;

public class DtoNodeStatistics {

	private double[] myCpuTime;
	private double[] myFailTransactions;
	private double[] myFaultTransactions;
	private double[] myMemoryMax;
	private double[] myMemoryUsed;
	private double[] mySecFailTransactions;
	private double[] mySuccessTransactions;

	public double[] getCpuTime() {
		return myCpuTime;
	}

	public double[] getFailTransactions() {
		return myFailTransactions;
	}

	public double[] getFaultTransactions() {
		return myFaultTransactions;
	}

	public double[] getMemoryMax() {
		return myMemoryMax;
	}

	public double[] getMemoryUsed() {
		return myMemoryUsed;
	}

	public double[] getSecFailTransactions() {
		return mySecFailTransactions;
	}

	public double[] getSuccessTransactions() {
		return mySuccessTransactions;
	}

	public void setCpuTime(double[] theCpuTime) {
		myCpuTime = theCpuTime;
	}

	public void setFailTransactions(double[] theFailTransactions) {
		myFailTransactions = theFailTransactions;
	}

	public void setFaultTransactions(double[] theFaultTransactions) {
		myFaultTransactions = theFaultTransactions;
	}

	public void setMemoryMax(double[] theMemoryMax) {
		myMemoryMax = theMemoryMax;
	}

	public void setMemoryUsed(double[] theMemoryUsed) {
		myMemoryUsed = theMemoryUsed;
	}

	public void setSecFailTransactions(double[] theSecFailTransactions) {
		mySecFailTransactions = theSecFailTransactions;
	}

	public void setSuccessTransactions(double[] theSuccessTransactions) {
		mySuccessTransactions = theSuccessTransactions;
	}

}
