package net.svcret.ejb.runtimestatus;

import java.util.Date;
import java.util.LinkedList;

import org.apache.commons.lang3.time.DateUtils;

public class RecentTransactionQueue {
	static final int MAX_SIZE = 50000;

	private final Date myInitialized = new Date();

	private final LinkedList<Date> myRecentDates = new LinkedList<Date>();

	public synchronized void addDate(Date theDate) {
		myRecentDates.push(theDate);
		while (myRecentDates.size() > MAX_SIZE) {
			myRecentDates.pollLast();
		}
	}

	public Date getInitialized() {
		return myInitialized;
	}

	public double getTransactionsPerMinute() {
		Date start = new Date();
		Date nextCutoff = DateUtils.addMinutes(start, 1);

		boolean havePassedOneMinute = false;
		boolean havePassedFiveMinutes = false;
		boolean havePassedTenMinutes = false;
		int count = 0;
		for (Date next : myRecentDates) {
			count++;

			if (next.after(nextCutoff)) {
				if (!havePassedOneMinute) {
					if (count > 1) {
						return ((double) count - 1);
					}
					havePassedOneMinute = true;
					nextCutoff = DateUtils.addMinutes(start, 5);
				} else if (!havePassedFiveMinutes) {
					if (count > 1) {
						return ((double) count - 1) / 5.0;
					}
					havePassedFiveMinutes = true;
					nextCutoff = DateUtils.addMinutes(start, 10);
				} else if (!havePassedTenMinutes) {
					if (count > 1) {
						return ((double) count - 1) / 10.0;
					} else {
						return 0.0;
					}
				}
			}
		}

		if (myRecentDates.size() > 1) {
			long mostRecentTime = myRecentDates.peekFirst().getTime();
			long leastRecentTime = myRecentDates.peekLast().getTime();
			double span = mostRecentTime - leastRecentTime;
			if (span > DateUtils.MILLIS_PER_MINUTE) {
				return 0.0;
			}
			if (span == 0) {
				span = DateUtils.MILLIS_PER_SECOND;
			}
			if (myRecentDates.size() == MAX_SIZE) {
				double spanMultiplier = DateUtils.MILLIS_PER_MINUTE / span;
				spanMultiplier = Math.max(spanMultiplier, 0.0001d);
				return spanMultiplier * MAX_SIZE;
			}else {
				return myRecentDates.size();
			}
		}

		return 0.0;
	}

	public Date getLastTransaction() {
		return myRecentDates.peekFirst();
	}

}
