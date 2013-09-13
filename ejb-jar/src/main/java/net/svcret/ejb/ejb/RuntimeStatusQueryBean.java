package net.svcret.ejb.ejb;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.Singleton;

import net.svcret.admin.shared.model.BaseGDashboardObject;
import net.svcret.ejb.api.IConfigService;
import net.svcret.ejb.api.IDao;
import net.svcret.ejb.api.IRuntimeStatus;
import net.svcret.ejb.api.IRuntimeStatusQueryLocal;
import net.svcret.ejb.ejb.AdminServiceBean.IWithStats;
import net.svcret.ejb.ex.UnexpectedFailureException;
import net.svcret.ejb.model.entity.BasePersInvocationStats;
import net.svcret.ejb.model.entity.BasePersInvocationStatsPk;
import net.svcret.ejb.model.entity.BasePersStats;
import net.svcret.ejb.model.entity.BasePersStatsPk;
import net.svcret.ejb.model.entity.InvocationStatsIntervalEnum;
import net.svcret.ejb.model.entity.PersConfig;
import net.svcret.ejb.model.entity.PersInvocationMethodSvcverStats;
import net.svcret.ejb.model.entity.PersInvocationMethodSvcverStatsPk;
import net.svcret.ejb.model.entity.PersInvocationMethodUserStats;
import net.svcret.ejb.model.entity.PersInvocationMethodUserStatsPk;
import net.svcret.ejb.model.entity.PersInvocationUrlStats;
import net.svcret.ejb.model.entity.PersInvocationUrlStatsPk;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.PersServiceVersionUrl;
import net.svcret.ejb.model.entity.PersUser;
import net.svcret.ejb.model.entity.PersUserMethodStatus;
import net.svcret.ejb.util.Validate;

import org.apache.commons.lang3.time.DateUtils;

import com.google.common.annotations.VisibleForTesting;

@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class RuntimeStatusQueryBean implements IRuntimeStatusQueryLocal {
	private static final int INITIAL_CACHED_ENTRIES = 80000;

	@EJB
	private IConfigService myConfigSvc;

	@EJB
	private IDao myDao;

	private final ConcurrentHashMap<BasePersStatsPk<?, ?>, BasePersStats<?, ?>> myInvocationStatCache;
	private final ArrayDeque<BasePersStatsPk<?, ?>> myInvocationStatEmptyKeys;
	private final ArrayDeque<BasePersStatsPk<?, ?>> myInvocationStatPopulatedKeys;
	private int myMaxNullCachedEntries = INITIAL_CACHED_ENTRIES;
	private int myMaxPopulatedCachedEntries = INITIAL_CACHED_ENTRIES;

	@EJB
	private IRuntimeStatus myStatusSvc;

	private final BasePersStats<?, ?> PLACEHOLDER = new Placeholder();

	public RuntimeStatusQueryBean() {
		myInvocationStatCache = new ConcurrentHashMap<BasePersStatsPk<?, ?>, BasePersStats<?, ?>>(myMaxNullCachedEntries + myMaxPopulatedCachedEntries);
		myInvocationStatEmptyKeys = new ArrayDeque<BasePersStatsPk<?, ?>>(myMaxNullCachedEntries);
		myInvocationStatPopulatedKeys = new ArrayDeque<BasePersStatsPk<?, ?>>(myMaxPopulatedCachedEntries);
	}

	@Override
	public void extract60MinuteMethodStats(final PersServiceVersionMethod theMethod, StatsAccumulator theAccumulator) throws UnexpectedFailureException {
		IWithStats<PersInvocationMethodSvcverStatsPk, PersInvocationMethodSvcverStats> operator = new StatsAdderOperator<PersInvocationMethodSvcverStatsPk, PersInvocationMethodSvcverStats>(
				theAccumulator);
		IStatsPkBuilder<PersInvocationMethodSvcverStatsPk, PersInvocationMethodSvcverStats> builder = new IStatsPkBuilder<PersInvocationMethodSvcverStatsPk, PersInvocationMethodSvcverStats>() {
			@Override
			public Collection<PersInvocationMethodSvcverStatsPk> createPk(InvocationStatsIntervalEnum theInterval, Date theDate) {
				return Collections.singletonList(new PersInvocationMethodSvcverStatsPk(theInterval, theDate, theMethod.getPid()));
			}
		};

		doWithStatsByMinute(59, theAccumulator, operator, builder);
	}

	@Override
	public void extract60MinuteServiceVersionUrlStatistics(final PersServiceVersionUrl theUrl, StatsAccumulator theAccumulator) throws UnexpectedFailureException {
		StatsAdderOperator<PersInvocationUrlStatsPk, PersInvocationUrlStats> operator = new StatsAdderOperator<PersInvocationUrlStatsPk, PersInvocationUrlStats>(theAccumulator);
		IStatsPkBuilder<PersInvocationUrlStatsPk, PersInvocationUrlStats> builder = new IStatsPkBuilder<PersInvocationUrlStatsPk, PersInvocationUrlStats>() {
			@Override
			public Collection<PersInvocationUrlStatsPk> createPk(InvocationStatsIntervalEnum theInterval, Date theDate) {
				return Collections.singletonList(new PersInvocationUrlStatsPk(theInterval, theDate, theUrl.getPid()));
			}
		};

		doWithStatsByMinute(59, theAccumulator, operator, builder);
	}

	@Override
	public void extract60MinuteUserStats(final PersUser theUser, StatsAccumulator theAccumulator) throws UnexpectedFailureException {
		PersUser user = myDao.getUser(theUser.getPid());
		if (user == null) {
			throw new IllegalArgumentException("Unknown user PID " + theUser.getPid());
		}

		final Collection<PersUserMethodStatus> methodStatuses = user.getStatus().getMethodStatuses().values();

		IWithStats<PersInvocationMethodUserStatsPk, PersInvocationMethodUserStats> operator = new StatsAdderOperator<PersInvocationMethodUserStatsPk, PersInvocationMethodUserStats>(theAccumulator);
		IStatsPkBuilder<PersInvocationMethodUserStatsPk, PersInvocationMethodUserStats> builder = new IStatsPkBuilder<PersInvocationMethodUserStatsPk, PersInvocationMethodUserStats>() {
			@Override
			public Collection<PersInvocationMethodUserStatsPk> createPk(InvocationStatsIntervalEnum theInterval, Date theDate) {
				ArrayList<PersInvocationMethodUserStatsPk> retVal = new ArrayList<PersInvocationMethodUserStatsPk>();
				for (PersUserMethodStatus next : methodStatuses) {
					boolean applies = next.doesDateFallWithinAtLeastOneOfMyRanges(theDate);
					if (applies) {
						PersInvocationMethodUserStatsPk pk = new PersInvocationMethodUserStatsPk(theInterval, theDate, next.getPk().getMethod(), theUser);
						retVal.add(pk);
					}
				}
				return retVal;
			}
		};

		doWithStatsByMinute(59, theAccumulator, operator, builder);
	}

	@Override
	public int getCachedEmptyKeyCount() {
		return myInvocationStatEmptyKeys.size();
	}

	@Override
	public int getCachedPopulatedKeyCount() {
		return myInvocationStatPopulatedKeys.size();
	}

	@Override
	public <P extends BasePersStatsPk<P, O>, O extends BasePersStats<P, O>> O getInvocationStatsSynchronously(P thePk) {
		Date oneMinuteAgoTruncated = DateUtils.truncate(new Date(System.currentTimeMillis() - DateUtils.MILLIS_PER_MINUTE), Calendar.MINUTE);
		if (!thePk.getStartTime().before(oneMinuteAgoTruncated)) {
			O retVal = myDao.getInvocationStats(thePk);
			if (retVal == null) {
				return thePk.newObjectInstance();
			}
			return retVal;
		}

		BasePersStats<?, ?> retVal = myInvocationStatCache.get(thePk);
		if (retVal == PLACEHOLDER) {
			return thePk.newObjectInstance();
		} else if (retVal == null) {
			synchronized (myInvocationStatCache) {
				retVal = myDao.getInvocationStats(thePk);
				if (retVal != null) {
					if (myInvocationStatPopulatedKeys.size() + 1 > myMaxPopulatedCachedEntries) {
						myInvocationStatCache.remove(myInvocationStatPopulatedKeys.pollFirst());
					}
					if (myInvocationStatCache.put(thePk, retVal) == null) {
						myInvocationStatPopulatedKeys.add(thePk);
					}
				} else {
					if (myInvocationStatEmptyKeys.size() + 1 > myMaxNullCachedEntries) {
						myInvocationStatCache.remove(myInvocationStatEmptyKeys.pollFirst());
					}
					if (myInvocationStatCache.put(thePk, PLACEHOLDER) == null) {
						myInvocationStatEmptyKeys.add(thePk);
					}
					retVal = thePk.newObjectInstance();
				}
			}
		}

		@SuppressWarnings("unchecked")
		O temp = (O) retVal;
		return temp;

	}

	@Override
	public int getMaxCachedNullStatCount() {
		return myMaxNullCachedEntries;
	}

	@Override
	public int getMaxCachedPopulatedStatCount() {
		return myMaxPopulatedCachedEntries;
	}

	@Override
	public void purgeCachedStats() {
		synchronized (myInvocationStatCache) {
			myInvocationStatCache.clear();
			myInvocationStatEmptyKeys.clear();
			myInvocationStatPopulatedKeys.clear();
		}
	}

	@VisibleForTesting
	public void setConfigSvcForUnitTest(IConfigService theConfigSvc) {
		myConfigSvc = theConfigSvc;
	}

	@Override
	public void setMaxCachedNullStatCount(int theCount) {
		Validate.greaterThanZero(theCount, "Count");
		myMaxNullCachedEntries = theCount;
	}

	@Override
	public void setMaxCachedPopulatedStatCount(int theCount) {
		Validate.greaterThanZero(theCount, "Count");
		myMaxPopulatedCachedEntries = theCount;
	}

	@VisibleForTesting
	public void setStatusSvcForUnitTest(IRuntimeStatus theStatusSvc) {
		myStatusSvc = theStatusSvc;
	}

	private <P extends BasePersStatsPk<P, O>, O extends BasePersStats<P, O>> Date doWithStatsByMinute(int theNumberOfMinutes, IWithStats<P, O> theOperator, IStatsPkBuilder<P, O> theBuilder)
			throws  UnexpectedFailureException {
		Date start = AdminServiceBean.getDateXMinsAgo(theNumberOfMinutes);
		Date end = new Date();
		doWithStatsByMinute(theOperator, theBuilder, start, end);

		return start;
	}

	private <P extends BasePersStatsPk<P, O>, O extends BasePersStats<P, O>> void doWithStatsByMinute(int theMinutes, StatsAccumulator theAccumulator, IWithStats<P, O> operator,
			IStatsPkBuilder<P, O> builder) throws UnexpectedFailureException {
		Date startTime = doWithStatsByMinute(theMinutes, operator, builder);
		theAccumulator.setFirstDate(startTime);
	}

	private <P extends BasePersStatsPk<P, O>, O extends BasePersStats<P, O>> void doWithStatsByMinute(IWithStats<P, O> theOperator, IStatsPkBuilder<P, O> thePkBuilder, Date start, Date end)
			throws UnexpectedFailureException {
		PersConfig config = myConfigSvc.getConfig();

		Date date = start;
		for (int min = 0; date.before(end); min++) {

			InvocationStatsIntervalEnum interval = AdminServiceBean.doWithStatsSupportFindInterval(config, date);
			date = interval.truncate(date);

			Collection<P> pk = thePkBuilder.createPk(interval, date);
			for (P nextPk : pk) {
				O stats = getInvocationStatsSynchronously(nextPk);
				theOperator.withStats(min, stats);
			}

			date = AdminServiceBean.doWithStatsSupportIncrement(date, interval);

		}
	}

	@VisibleForTesting
	void setDaoForUnitTests(IDao theDao) {
		myDao = theDao;
	}

	@VisibleForTesting
	void setConfigSvcForUnitTests(IConfigService theSvc) {
		myConfigSvc = theSvc;
	}

	public static class StatsAccumulator {

		private final ArrayList<Integer> myFailCounts = new ArrayList<Integer>();
		private final ArrayList<Integer> myFaultCounts = new ArrayList<Integer>();
		private Date myFirstDate;
		private final ArrayList<Integer> mySecFailCounts = new ArrayList<Integer>();
		private final ArrayList<Integer> mySuccessCounts = new ArrayList<Integer>();
		private final ArrayList<Integer> myThrottleAcceptCounts = new ArrayList<Integer>();
		private final ArrayList<Integer> myThrottleRejectCounts = new ArrayList<Integer>();
		private final ArrayList<Long> myTimes = new ArrayList<Long>();

		private final ArrayList<Long> myTimestamps = new ArrayList<Long>();

		public List<Integer> getAllTimingCounts() {
			ArrayList<Integer> retVal = new ArrayList<Integer>();
			for (Integer next : mySuccessCounts) {
				retVal.add(next);
			}
			int i = 0;
			for (Integer next : myFaultCounts) {
				retVal.set(i, retVal.get(i) + next);
				i++;
			}
			i = 0;
			for (Integer next : myFailCounts) {
				retVal.set(i, retVal.get(i) + next);
				i++;
			}
			i = 0;
			for (Integer next : mySecFailCounts) {
				retVal.set(i, retVal.get(i) + next);
				i++;
			}
			return retVal;
		}

		public void populateDto(BaseGDashboardObject theObject) {
			theObject.setStatistics60MinuteFirstDate(myFirstDate);
			theObject.setTransactions60mins(AdminServiceBean.toArray(mySuccessCounts));
			theObject.setTransactionsFault60mins(AdminServiceBean.toArray(myFaultCounts));
			theObject.setTransactionsFail60mins(AdminServiceBean.toArray(myFailCounts));
			theObject.setTransactionsSecurityFail60mins(AdminServiceBean.toArray(mySecFailCounts));
			theObject.setLatency60mins(AdminServiceBean.toLatency(myTimes, mySuccessCounts, myFaultCounts, myFailCounts));
		}

		ArrayList<Integer> getFailCounts() {
			return myFailCounts;
		}

		ArrayList<Integer> getFaultCounts() {
			return myFaultCounts;
		}

		ArrayList<Integer> getSecurityFailCounts() {
			return mySecFailCounts;
		}

		ArrayList<Integer> getSuccessCounts() {
			return mySuccessCounts;
		}

		ArrayList<Integer> getThrottleAcceptCounts() {
			return myThrottleAcceptCounts;
		}

		ArrayList<Integer> getThrottleRejectCounts() {
			return myThrottleRejectCounts;
		}

		ArrayList<Long> getTimes() {
			return myTimes;
		}

		List<Long> getTimestamps() {
			return myTimestamps;
		}

		void setFirstDate(Date theFirstTime) {
			myFirstDate = theFirstTime;
		}

	}

	private interface IStatsPkBuilder<P extends BasePersStatsPk<P, O>, O extends BasePersStats<P, O>> {
		Collection<P> createPk(InvocationStatsIntervalEnum theInterval, Date theDate);
	}

	private class Placeholder extends BasePersStats<BasePersStatsPk<?, ?>, BasePersStats<?, ?>> {

		private static final long serialVersionUID = 1L;

		@Override
		public <T> T accept(IStatsVisitor<T> theVisitor) {
			throw new UnsupportedOperationException();
		}

		@Override
		public BasePersStatsPk<?, ?> getPk() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void mergeUnsynchronizedEvents(BasePersStats<?, ?> theNext) {
			throw new UnsupportedOperationException();
		}

	}

	private final class StatsAdderOperator<P extends BasePersInvocationStatsPk<P, O>, O extends BasePersInvocationStats<P, O>> implements IWithStats<P, O> {
		private final List<Integer> myFailInvCount;
		private final List<Integer> myFaultInvCount;
		private final List<Integer> mySecurityFailCounts;
		private final List<Integer> mySuccessInvCount;
		private final List<Long> myTimes;
		private final List<Long> myTimestamps;
		private final List<Integer> myThrottleAcceptCounts;
		private final List<Integer> myThrottleRejectCounts;

		public StatsAdderOperator(StatsAccumulator theAccumulator) {
			myTimes = theAccumulator.getTimes();
			myTimestamps = theAccumulator.getTimestamps();
			mySuccessInvCount = theAccumulator.getSuccessCounts();
			myFailInvCount = theAccumulator.getFailCounts();
			myFaultInvCount = theAccumulator.getFaultCounts();
			mySecurityFailCounts = theAccumulator.getSecurityFailCounts();
			myThrottleAcceptCounts = theAccumulator.getThrottleAcceptCounts();
			myThrottleRejectCounts = theAccumulator.getThrottleRejectCounts();
		}

		@Override
		public void withStats(int theIndex, O theStats) {
			AdminServiceBean.growToSizeInt(mySuccessInvCount, theIndex);
			AdminServiceBean.growToSizeInt(myFaultInvCount, theIndex);
			AdminServiceBean.growToSizeInt(myFailInvCount, theIndex);
			AdminServiceBean.growToSizeInt(mySecurityFailCounts, theIndex);
			AdminServiceBean.growToSizeInt(myThrottleAcceptCounts, theIndex);
			AdminServiceBean.growToSizeInt(myThrottleRejectCounts, theIndex);
			AdminServiceBean.growToSizeLong(myTimes, theIndex);
			AdminServiceBean.growToSizeLong(myTimestamps, theIndex);

			myTimestamps.set(theIndex, theStats.getPk().getStartTime().getTime());

			mySuccessInvCount.set(theIndex, AdminServiceBean.addToInt(mySuccessInvCount.get(theIndex), theStats.getSuccessInvocationCount()));
			myFaultInvCount.set(theIndex, AdminServiceBean.addToInt(myFaultInvCount.get(theIndex), theStats.getFaultInvocationCount()));
			myFailInvCount.set(theIndex, AdminServiceBean.addToInt(myFailInvCount.get(theIndex), theStats.getFailInvocationCount()));
			mySecurityFailCounts.set(theIndex, AdminServiceBean.addToInt(mySecurityFailCounts.get(theIndex), theStats.getServerSecurityFailures()));
			myThrottleAcceptCounts.set(theIndex, AdminServiceBean.addToInt(myThrottleAcceptCounts.get(theIndex), theStats.getTotalThrottleAccepts()));
			myThrottleRejectCounts.set(theIndex, AdminServiceBean.addToInt(myThrottleRejectCounts.get(theIndex), theStats.getTotalThrottleRejections()));

			long totalTime = myTimes.get(theIndex) + theStats.getSuccessInvocationTotalTime() + theStats.getFaultInvocationTime() + theStats.getFailInvocationTime();
			myTimes.set(theIndex, totalTime);
		}
	}

}
