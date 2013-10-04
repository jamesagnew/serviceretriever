package net.svcret.ejb.runtimestatus;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.ListIterator;

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

	public synchronized double getTransactionsPerMinute() {
		if (myRecentDates.size() == 0) {
			return 0.0;
		}

		Date start = new Date();
		Date cutoffOneMin = DateUtils.addMinutes(start, -1);
		Date cutoffFiveMin = DateUtils.addMinutes(start, -5);
		Date cutoffTenMin = DateUtils.addMinutes(start, -10);

		Date firstDate = myRecentDates.peek();
		boolean havePassedOneMinute = firstDate.before(cutoffOneMin);
		boolean havePassedFiveMinutes = firstDate.before(cutoffFiveMin);
		boolean havePassedTenMinutes = firstDate.before(cutoffTenMin);
		int count = 0;

		if (havePassedTenMinutes) {
			return 0.0;
		}

		Date nextCutoff;
		if (havePassedOneMinute) {
			nextCutoff = cutoffFiveMin;
		} else if (havePassedFiveMinutes) {
			nextCutoff = cutoffTenMin;
		} else {
			nextCutoff = cutoffOneMin;
		}

		for (Date next : myRecentDates) {
			if (next.before(nextCutoff)) {
				if (!havePassedOneMinute) {
					if (count > 0) {
						return count;
					}
					havePassedOneMinute = true;
					nextCutoff = cutoffFiveMin;
				} else if (!havePassedFiveMinutes) {
					if (count > 0) {
						return count / 5.0;
					}
					havePassedFiveMinutes = true;
					nextCutoff = cutoffTenMin;
				} else if (!havePassedTenMinutes) {
					if (count > 0) {
						return count / 10.0;
					} else {
						return 0.0;
					}
				}
			}

			count++;
		}

		if (myRecentDates.size() > 1) {
			long mostRecentTime = start.getTime();
			long leastRecentTime = myRecentDates.peekLast().getTime();
			double span = mostRecentTime - leastRecentTime;
			if (span < DateUtils.MILLIS_PER_SECOND) {
				if (myRecentDates.size() < MAX_SIZE) {
					span = DateUtils.MILLIS_PER_MINUTE;
				}else {
					span = DateUtils.MILLIS_PER_SECOND;
				}
			} else if (span < DateUtils.MILLIS_PER_MINUTE) {
				if (myRecentDates.size() < MAX_SIZE) {
					span = DateUtils.MILLIS_PER_MINUTE;
				}
			} else if (span > DateUtils.MILLIS_PER_MINUTE && span < (5*DateUtils.MILLIS_PER_MINUTE)){
				span = 5*DateUtils.MILLIS_PER_MINUTE;
			} else if (span > (5*DateUtils.MILLIS_PER_MINUTE) && span < (10*DateUtils.MILLIS_PER_MINUTE)){
				span = 10*DateUtils.MILLIS_PER_MINUTE;
			}
			if (myRecentDates.size() == MAX_SIZE) {
				double spanMultiplier = DateUtils.MILLIS_PER_MINUTE / span;
				spanMultiplier = Math.max(spanMultiplier, 0.0001d);
				return spanMultiplier * MAX_SIZE;
			} else {
				return myRecentDates.size() / (span / DateUtils.MILLIS_PER_MINUTE);
			}
		}

		return 0.0;
	}

	public synchronized Date getLastTransaction() {
		return myRecentDates.peekFirst();
	}

	public synchronized int getSize() {
		return myRecentDates.size();
	}

	public String describeFirstTenEntries() {
		SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss.SSS");
		StringBuilder b = new StringBuilder();

		for (ListIterator<Date> iterator = myRecentDates.listIterator(); iterator.hasNext();) {
			Date next = iterator.next();
			if (b.length() > 0) {
				b.append(", ");
			}
			b.append(fmt.format(next));

			if (iterator.nextIndex() > 10) {
				break;
			}
		}

		return b.toString();
	}

}
