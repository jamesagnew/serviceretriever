package net.svcret.admin.shared.model;

public class DtoNodeStatistics {

	private int[] myHistoryCpuTime;
	private double[] myHistoryFailTransactions;
	private double[] myHistoryFaultTransactions;
	private double[] myHistoryMemoryMax;
	private double[] myHistoryMemoryUsed;
	private double[] myHistorySecFailTransactions;
	private double[] myHistorySuccessTransactions;

	public int[] getHistoryCpuTime() {
		return myHistoryCpuTime;
	}

	public double[] getHistoryFailTransactions() {
		return myHistoryFailTransactions;
	}

	public double[] getHistoryFaultTransactions() {
		return myHistoryFaultTransactions;
	}

	public double[] getHistoryMemoryMax() {
		return myHistoryMemoryMax;
	}

	public double[] getHistoryMemoryUsed() {
		return myHistoryMemoryUsed;
	}

	public double[] getHistorySecFailTransactions() {
		return myHistorySecFailTransactions;
	}

	public double[] getHistorySuccessTransactions() {
		return myHistorySuccessTransactions;
	}

	public void setHistoryCpuTime(int[] theHistoryCpuTime) {
		myHistoryCpuTime = theHistoryCpuTime;
	}

	public void setHistoryFailTransactions(double[] theHistoryFailTransactions) {
		myHistoryFailTransactions = theHistoryFailTransactions;
	}

	public void setHistoryFaultTransactions(double[] theHistoryFaultTransactions) {
		myHistoryFaultTransactions = theHistoryFaultTransactions;
	}

	public void setHistoryMemoryMax(double[] theHistoryMemoryMax) {
		myHistoryMemoryMax = theHistoryMemoryMax;
	}

	public void setHistoryMemoryUsed(double[] theHistoryMemoryUsed) {
		myHistoryMemoryUsed = theHistoryMemoryUsed;
	}

	public void setHistorySecFailTransactions(double[] theHistorySecFailTransactions) {
		myHistorySecFailTransactions = theHistorySecFailTransactions;
	}

	public void setHistorySuccessTransactions(double[] theHistorySuccessTransactions) {
		myHistorySuccessTransactions = theHistorySuccessTransactions;
	}

}
