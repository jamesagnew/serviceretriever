package net.svcret.ejb.ejb;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.Singleton;

import net.svcret.admin.shared.model.BaseDtoDashboardObject;
import net.svcret.ejb.api.IConfigService;
import net.svcret.ejb.api.IDao;
import net.svcret.ejb.api.IRuntimeStatus;
import net.svcret.ejb.api.IRuntimeStatusQueryLocal;
import net.svcret.ejb.ejb.AdminServiceBean.IWithStats;
import net.svcret.ejb.ex.UnexpectedFailureException;
import net.svcret.ejb.model.entity.BasePersInvocationStats;
import net.svcret.ejb.model.entity.BasePersInvocationStatsPk;
import net.svcret.ejb.model.entity.BasePersServiceCatalogItem;
import net.svcret.ejb.model.entity.BasePersServiceVersion;
import net.svcret.ejb.model.entity.BasePersStats;
import net.svcret.ejb.model.entity.BasePersStatsPk;
import net.svcret.ejb.model.entity.InvocationStatsIntervalEnum;
import net.svcret.ejb.model.entity.PersConfig;
import net.svcret.ejb.model.entity.PersDomain;
import net.svcret.ejb.model.entity.PersInvocationMethodSvcverStats;
import net.svcret.ejb.model.entity.PersInvocationMethodSvcverStatsPk;
import net.svcret.ejb.model.entity.PersInvocationMethodUserStats;
import net.svcret.ejb.model.entity.PersInvocationMethodUserStatsPk;
import net.svcret.ejb.model.entity.PersInvocationUrlStats;
import net.svcret.ejb.model.entity.PersInvocationUrlStatsPk;
import net.svcret.ejb.model.entity.PersNodeStatus;
import net.svcret.ejb.model.entity.PersService;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.PersServiceVersionUrl;
import net.svcret.ejb.model.entity.PersUser;
import net.svcret.ejb.model.entity.PersUserMethodStatus;
import net.svcret.ejb.util.Validate;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.time.DateUtils;

import com.google.common.annotations.VisibleForTesting;

@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class RuntimeStatusQueryBean implements IRuntimeStatusQueryLocal {
	private static final int INITIAL_CACHED_ENTRIES = 80000;
	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(RuntimeStatusQueryBean.class);

	private final Map<CachedStatsKey, StatsAccumulator> myCache60MinAccumulators;
	private final ArrayDeque<BasePersStatsPk<?, ?>> myCacheInvocationStatEmptyKeys;
	private final ArrayDeque<BasePersStatsPk<?, ?>> myCacheInvocationStatPopulatedKeys;

	private final ConcurrentHashMap<BasePersStatsPk<?, ?>, BasePersStats<?, ?>> myCacheInvocationStats;

	@EJB
	private IConfigService myConfigSvc;

	@EJB
	private IDao myDao;
	private int myMaxNullCachedEntries = INITIAL_CACHED_ENTRIES;

	private int myMaxPopulatedCachedEntries = INITIAL_CACHED_ENTRIES;

	@EJB
	private IRuntimeStatus myStatusSvc;

	private final BasePersStats<?, ?> PLACEHOLDER = new Placeholder();

	public RuntimeStatusQueryBean() {
		myCache60MinAccumulators = new HashMap<CachedStatsKey, RuntimeStatusQueryBean.StatsAccumulator>();
		myCacheInvocationStats = new ConcurrentHashMap<BasePersStatsPk<?, ?>, BasePersStats<?, ?>>(myMaxNullCachedEntries + myMaxPopulatedCachedEntries);
		myCacheInvocationStatEmptyKeys = new ArrayDeque<BasePersStatsPk<?, ?>>(myMaxNullCachedEntries);
		myCacheInvocationStatPopulatedKeys = new ArrayDeque<BasePersStatsPk<?, ?>>(myMaxPopulatedCachedEntries);
	}

	@Override
	public void extract60MinuteMethodStats(final PersServiceVersionMethod theMethod, StatsAccumulator theAccumulator) throws UnexpectedFailureException {
		IWithStats<PersInvocationMethodSvcverStatsPk, PersInvocationMethodSvcverStats> operator = new StatsAdderOperator<PersInvocationMethodSvcverStatsPk, PersInvocationMethodSvcverStats>(
				theAccumulator);
		IStatsPkBuilder<PersInvocationMethodSvcverStatsPk, PersInvocationMethodSvcverStats> builder = new MethodBuilder(theMethod);
		doWithStatsByMinute(0, 59, theAccumulator, operator, builder);
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

		doWithStatsByMinute(0, 59, theAccumulator, operator, builder);
	}

	@Override
	public StatsAccumulator extract60MinuteStats(BasePersServiceCatalogItem theItem) throws UnexpectedFailureException {

		StatsAccumulator accumulator = new StatsAccumulator();
		synchronized (myCache60MinAccumulators) {
			Date wantStartTime = AdminServiceBean.getDateXMinsAgoTruncatedToMinute(59);
			accumulator.setFirstDate(wantStartTime);
			Date startTime = wantStartTime;
			Date endTime = AdminServiceBean.getDateXMinsAgoTruncatedToMinute(0);
			accumulator.setFirstDate(startTime);

			extractMinuteStats(theItem, accumulator, startTime, endTime);
		}

		return accumulator;
		// if (!myCachedServiceVersionAccumulators.containsKey(theNextVersion.getPid())) {
		//
		// StatsAccumulator acc = new StatsAccumulator();
		// for (PersServiceVersionMethod nextMethod : theNextVersion.getMethods()) {
		// extract60MinuteMethodStats(nextMethod, acc);
		// }
		// if (theNextVersion.getMethods().size() == 0) {
		// return;
		// }
		// myCachedServiceVersionAccumulators.put(theNextVersion.getPid(), acc);
		// theAccumulator.populateFromLast(60, acc);
		//
		// } else {
		//
		// StatsAccumulator acc = myCachedServiceVersionAccumulators.get(theNextVersion.getPid());
		//
		// Date end = AdminServiceBean.getDateXMinsAgo(0);
		// Long lastTimestamp = acc.getTimestamps().get(acc.getTimestamps().size() - 1);
		//
		// long numNeeded = (end.getTime() - lastTimestamp) / DateUtils.MILLIS_PER_DAY;
		// if (numNeeded > 59) {
		// numNeeded = 59;
		// } else if (numNeeded < 0) {
		// numNeeded=0;
		// }
		//
		// Date start = AdminServiceBean.getDateXMinsAgo((int) numNeeded);
		//
		// StatsAdderOperator<PersInvocationMethodSvcverStatsPk, PersInvocationMethodSvcverStats> operator = new StatsAdderOperator<PersInvocationMethodSvcverStatsPk,
		// PersInvocationMethodSvcverStats>(acc);
		// for (PersServiceVersionMethod nextMethod : theNextVersion.getMethods()) {
		// doWithStatsByMinute(acc.getTimestamps().size(), operator, new MethodBuilder(nextMethod), start, end);
		// }
		//
		// theAccumulator.populateFromLast(59, acc);
		//
		// }
		//
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

		doWithStatsByMinute(0, 59, theAccumulator, operator, builder);
	}

	@Override
	public Collection<PersNodeStatus> getAllNodeStatuses() {
		return myDao.getAllNodeStatuses();
	}

	@Override
	public int getCachedEmptyKeyCount() {
		return myCacheInvocationStatEmptyKeys.size();
	}

	@Override
	public int getCachedPopulatedKeyCount() {
		return myCacheInvocationStatPopulatedKeys.size();
	}

	@Override
	public <P extends BasePersStatsPk<P, O>, O extends BasePersStats<P, O>> O getInvocationStatsSynchronously(P thePk) {
		Date noCacheCutoff = provideNoCacheCutoff();
		if (!thePk.getStartTime().before(noCacheCutoff)) {
			O retVal = myDao.getInvocationStats(thePk);
			if (retVal == null) {
				return thePk.newObjectInstance();
			}
			return retVal;
		}

		BasePersStats<?, ?> retVal = myCacheInvocationStats.get(thePk);
		if (retVal == PLACEHOLDER) {
			return thePk.newObjectInstance();
		} else if (retVal == null) {
			synchronized (myCacheInvocationStats) {
				retVal = myDao.getInvocationStats(thePk);
				if (retVal != null) {
					if (myCacheInvocationStatPopulatedKeys.size() + 1 > myMaxPopulatedCachedEntries) {
						myCacheInvocationStats.remove(myCacheInvocationStatPopulatedKeys.pollFirst());
					}
					if (myCacheInvocationStats.put(thePk, retVal) == null) {
						myCacheInvocationStatPopulatedKeys.add(thePk);
					}
				} else {
					if (myCacheInvocationStatEmptyKeys.size() + 1 > myMaxNullCachedEntries) {
						myCacheInvocationStats.remove(myCacheInvocationStatEmptyKeys.pollFirst());
					}
					if (myCacheInvocationStats.put(thePk, PLACEHOLDER) == null) {
						myCacheInvocationStatEmptyKeys.add(thePk);
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
		synchronized (myCacheInvocationStats) {
			myCacheInvocationStats.clear();
			myCacheInvocationStatEmptyKeys.clear();
			myCacheInvocationStatPopulatedKeys.clear();
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

	private <P extends BasePersStatsPk<P, O>, O extends BasePersStats<P, O>> Date doWithStatsByMinute(int theStartMinuteIndex, int theNumberOfMinutes, IWithStats<P, O> theOperator,
			IStatsPkBuilder<P, O> theBuilder) throws UnexpectedFailureException {
		Date start = AdminServiceBean.getDateXMinsAgoTruncatedToMinute(theNumberOfMinutes);
		Date end = new Date();
		doWithStatsByMinute(theStartMinuteIndex, theOperator, theBuilder, start, end);

		return start;
	}

	private <P extends BasePersStatsPk<P, O>, O extends BasePersStats<P, O>> void doWithStatsByMinute(int theStartMinuteIndex, int theMinutes, StatsAccumulator theAccumulator,
			IWithStats<P, O> operator, IStatsPkBuilder<P, O> builder) throws UnexpectedFailureException {
		Date startTime = doWithStatsByMinute(theStartMinuteIndex, theMinutes, operator, builder);
		theAccumulator.setFirstDate(startTime);
	}

	private <P extends BasePersStatsPk<P, O>, O extends BasePersStats<P, O>> void doWithStatsByMinute(int theStartMinuteIndex, IWithStats<P, O> theOperator, IStatsPkBuilder<P, O> thePkBuilder,
			Date start, Date end) throws UnexpectedFailureException {
		PersConfig config = myConfigSvc.getConfig();

		Date date = start;
		for (int min = 0; date.before(end); min++) {

			InvocationStatsIntervalEnum interval = AdminServiceBean.doWithStatsSupportFindInterval(config, date);
			date = interval.truncate(date);

			Collection<P> pk = thePkBuilder.createPk(interval, date);
			for (P nextPk : pk) {
				O stats = getInvocationStatsSynchronously(nextPk);
				theOperator.withStats(theStartMinuteIndex + min, stats);
			}

			date = AdminServiceBean.doWithStatsSupportIncrement(date, interval);

		}
	}

	private void extractMinuteStats(BasePersServiceCatalogItem theItem, StatsAccumulator theAccumulator, Date theStartTime, Date endTime) throws UnexpectedFailureException {
		CachedStatsKey key = new CachedStatsKey(theItem.getClass(), theItem.getPid());

		StatsAccumulator cachedAccum;
		if (!myCache60MinAccumulators.containsKey(key)) {
			myCache60MinAccumulators.put(key, new StatsAccumulator());
		}
		cachedAccum = myCache60MinAccumulators.get(key);

		Date cutoffFor60Mins = myConfigSvc.getConfig().getCollapseStatsToTenMinutesCutoff();
		cachedAccum.deleteBefore(cutoffFor60Mins);

		Date startTime = theStartTime;
		if (cachedAccum.getTimestamps().size() > 0) {
			Date lastTimestamp = new Date(cachedAccum.getTimestamps().get(cachedAccum.getTimestamps().size() - 1));
			if (!startTime.after(lastTimestamp)) {
				startTime = new Date(lastTimestamp.getTime() + DateUtils.MILLIS_PER_MINUTE);
			}

			if (startTime.before(cutoffFor60Mins)) {
				startTime = cutoffFor60Mins;
			}
		}

		if (!startTime.after(endTime)) {

			StatsAccumulator childAccum = null;
			if (theItem instanceof PersDomain) {
				childAccum = new StatsAccumulator(startTime, endTime, InvocationStatsIntervalEnum.MINUTE);
				for (PersService next : ((PersDomain) theItem).getServices()) {
					extractMinuteStats(next, childAccum, startTime, endTime);
				}
			} else if (theItem instanceof PersService) {
				childAccum = new StatsAccumulator(startTime, endTime, InvocationStatsIntervalEnum.MINUTE);
				for (BasePersServiceVersion next : ((PersService) theItem).getVersions()) {
					extractMinuteStats(next, childAccum, startTime, endTime);
				}
			} else if (theItem instanceof BasePersServiceVersion) {
				childAccum = new StatsAccumulator(startTime, endTime, InvocationStatsIntervalEnum.MINUTE);
				for (PersServiceVersionMethod next : ((BasePersServiceVersion) theItem).getMethods()) {
					int numMins = (int) ((endTime.getTime() - startTime.getTime()) / DateUtils.MILLIS_PER_MINUTE);
					IWithStats<PersInvocationMethodSvcverStatsPk, PersInvocationMethodSvcverStats> operator = new StatsAdderOperator<PersInvocationMethodSvcverStatsPk, PersInvocationMethodSvcverStats>(
							childAccum);
					IStatsPkBuilder<PersInvocationMethodSvcverStatsPk, PersInvocationMethodSvcverStats> builder = new MethodBuilder(next);
					doWithStatsByMinute(0, numMins, childAccum, operator, builder);
				}
			} else {
				throw new IllegalArgumentException(); // Should not happen..
			}

			cachedAccum.populateFromLast(startTime, endTime, childAccum);
		}

		Date firstDate = theAccumulator.getFirstDate();
		theAccumulator.populateFromLast(firstDate, endTime, cachedAccum);

		cachedAccum.deleteOnOrAfter(provideNoCacheCutoff());

	}


	private Date provideNoCacheCutoff() {
		return DateUtils.truncate(new Date(System.currentTimeMillis() - DateUtils.MILLIS_PER_MINUTE), Calendar.MINUTE);
	}

	@VisibleForTesting
	void setConfigSvcForUnitTests(IConfigService theSvc) {
		myConfigSvc = theSvc;
	}

	@VisibleForTesting
	void setDaoForUnitTests(IDao theDao) {
		myDao = theDao;
	}

	public static class StatsAccumulator implements Serializable {

		private static final long serialVersionUID = 1L;

		private ArrayList<Integer> myFailCounts = new ArrayList<Integer>();
		private ArrayList<Integer> myFaultCounts = new ArrayList<Integer>();
		private Date myFirstDate;
		private ArrayList<Integer> mySecFailCounts = new ArrayList<Integer>();
		private ArrayList<Integer> mySuccessCounts = new ArrayList<Integer>();
		private ArrayList<Integer> myThrottleAcceptCounts = new ArrayList<Integer>();
		private ArrayList<Integer> myThrottleRejectCounts = new ArrayList<Integer>();
		private ArrayList<Long> myTimes = new ArrayList<Long>();
		private ArrayList<Long> myTimestamps = new ArrayList<Long>();

		public StatsAccumulator() {
		}

		public StatsAccumulator(Date theStartTime, Date theEndTime, InvocationStatsIntervalEnum theInterval) {
			myFirstDate = theStartTime;
			for (Date time = theStartTime; !time.after(theEndTime); time = new Date(time.getTime() + theInterval.millis())) {
				myTimestamps.add(time.getTime());
				mySuccessCounts.add(0);
				myFaultCounts.add(0);
				myFailCounts.add(0);
				mySecFailCounts.add(0);
				myThrottleAcceptCounts.add(0);
				myThrottleRejectCounts.add(0);
				myTimes.add(0L);
			}
		}

		public void deleteBefore(Date theCutoffFor60Mins) {
			int countToDelete = 0;
			for (long next : myTimestamps) {
				if (next < theCutoffFor60Mins.getTime()) {
					countToDelete++;
				}
			}

			if (countToDelete > 0) {
				mySuccessCounts = trimFirstNEntries(countToDelete, mySuccessCounts);
				myFaultCounts = trimFirstNEntries(countToDelete, myFaultCounts);
				myFailCounts = trimFirstNEntries(countToDelete, myFailCounts);
				mySecFailCounts = trimFirstNEntries(countToDelete, mySecFailCounts);
				myThrottleAcceptCounts = trimFirstNEntries(countToDelete, myThrottleAcceptCounts);
				myThrottleRejectCounts = trimFirstNEntries(countToDelete, myThrottleRejectCounts);
				myTimes = trimFirstNEntries(countToDelete, myTimes);
				myTimestamps = trimFirstNEntries(countToDelete, myTimestamps);
			}
		}

		public void deleteOnOrAfter(Date theCutoff) {
			int countToDelete = 0;
			for (long next : myTimestamps) {
				if (next >= theCutoff.getTime()) {
					countToDelete++;
				}
			}

			if (countToDelete > 0) {
				mySuccessCounts = trimLastNEntries(countToDelete, mySuccessCounts);
				myFaultCounts = trimLastNEntries(countToDelete, myFaultCounts);
				myFailCounts = trimLastNEntries(countToDelete, myFailCounts);
				mySecFailCounts = trimLastNEntries(countToDelete, mySecFailCounts);
				myThrottleAcceptCounts = trimLastNEntries(countToDelete, myThrottleAcceptCounts);
				myThrottleRejectCounts = trimLastNEntries(countToDelete, myThrottleRejectCounts);
				myTimes = trimLastNEntries(countToDelete, myTimes);
				myTimestamps = trimLastNEntries(countToDelete, myTimestamps);
			}
		}

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

		public Date getFirstDate() {
			return myFirstDate;
		}

		public void populateDto(BaseDtoDashboardObject theObject) {
			theObject.setStatistics60MinuteFirstDate(myFirstDate);
			theObject.setTransactions60mins(AdminServiceBean.toArray(mySuccessCounts));
			theObject.setTransactionsFault60mins(AdminServiceBean.toArray(myFaultCounts));
			theObject.setTransactionsFail60mins(AdminServiceBean.toArray(myFailCounts));
			theObject.setTransactionsSecurityFail60mins(AdminServiceBean.toArray(mySecFailCounts));
			theObject.setLatency60mins(AdminServiceBean.toLatency(myTimes, mySuccessCounts, myFaultCounts, myFailCounts));
		}

		public void populateFromLast(Date theStartTime, Date theEndTime, StatsAccumulator theSource) {

			int count = 0;
			for (Date nextDate = theStartTime; !nextDate.after(theEndTime); nextDate = new Date(nextDate.getTime() + DateUtils.MILLIS_PER_MINUTE)) {
				count++;
				int theirIndex = theSource.myTimestamps.indexOf(nextDate.getTime());
				int thisIndex = myTimestamps.indexOf(nextDate.getTime());

				if (thisIndex == -1) {
					myTimestamps.add(nextDate.getTime());
				}

				addToListOfInts(thisIndex, theirIndex, mySuccessCounts, theSource.mySuccessCounts);
				addToListOfInts(thisIndex, theirIndex, myFailCounts, theSource.myFailCounts);
				addToListOfInts(thisIndex, theirIndex, myFaultCounts, theSource.myFaultCounts);
				addToListOfInts(thisIndex, theirIndex, mySecFailCounts, theSource.mySecFailCounts);
				addToListOfInts(thisIndex, theirIndex, myThrottleAcceptCounts, theSource.myThrottleAcceptCounts);
				addToListOfInts(thisIndex, theirIndex, myThrottleRejectCounts, theSource.myThrottleRejectCounts);
				addToListOfLongs(thisIndex, theirIndex, myTimes, theSource.myTimes);
			}

			ourLog.trace("Copied {} values", count);
		}

		private void addToListOfInts(int theThisIndex, int theTheirIndex, ArrayList<Integer> theMine, ArrayList<Integer> theTheirs) {
			Integer newValue = theTheirIndex == -1 ? 0 : theTheirs.get(theTheirIndex);

			if (theThisIndex == -1) {
				theMine.add(newValue);
			} else {
				theMine.set(theThisIndex, theMine.get(theThisIndex) + newValue);
			}
		}

		private void addToListOfLongs(int theThisIndex, int theTheirIndex, ArrayList<Long> theMine, ArrayList<Long> theTheirs) {
			Long newValue = theTheirIndex == -1 ? 0 : theTheirs.get(theTheirIndex);
			if (theThisIndex == -1) {
				theMine.add(newValue);
			} else {
				theMine.set(theThisIndex, theMine.get(theThisIndex) + newValue);
			}
		}

		private <T extends Number> ArrayList<T> trimFirstNEntries(int theCountToDelete, ArrayList<T> theValues) {
			return new ArrayList<T>( theValues.subList(theCountToDelete, theValues.size()));
		}

		private <T extends Number> ArrayList<T> trimLastNEntries(int theCountToDelete, ArrayList<T> theValues) {
			return new ArrayList<T>(theValues.subList(0, theValues.size() - theCountToDelete));
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

	private static class CachedStatsKey {
		private final Class<? extends BasePersServiceCatalogItem> myClazz;
		private Integer myHashCode;
		private final long myPid;

		public CachedStatsKey(Class<? extends BasePersServiceCatalogItem> theClazz, long thePid) {
			myClazz = theClazz;
			myPid = thePid;

			HashCodeBuilder b = new HashCodeBuilder();
			b.append(myClazz);
			b.append(myPid);
			myHashCode = b.toHashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CachedStatsKey other = (CachedStatsKey) obj;
			if (myClazz == null) {
				if (other.myClazz != null)
					return false;
			} else if (!myClazz.equals(other.myClazz))
				return false;
			if (myPid != other.myPid)
				return false;
			return true;
		}

		// public Class<? extends BasePersServiceCatalogItem> getClazz() {
		// return myClazz;
		// }
		//
		// public long getPid() {
		// return myPid;
		// }

		@Override
		public int hashCode() {
			return myHashCode;
		}
	}
	private interface IStatsPkBuilder<P extends BasePersStatsPk<P, O>, O extends BasePersStats<P, O>> {
		Collection<P> createPk(InvocationStatsIntervalEnum theInterval, Date theDate);
	}

	
	private final class MethodBuilder implements IStatsPkBuilder<PersInvocationMethodSvcverStatsPk, PersInvocationMethodSvcverStats> {
		private final PersServiceVersionMethod myMethod;

		private MethodBuilder(PersServiceVersionMethod theMethod) {
			myMethod = theMethod;
		}

		@Override
		public Collection<PersInvocationMethodSvcverStatsPk> createPk(InvocationStatsIntervalEnum theInterval, Date theDate) {
			return Collections.singletonList(new PersInvocationMethodSvcverStatsPk(theInterval, theDate, myMethod.getPid()));
		}
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
		private final List<Integer> myThrottleAcceptCounts;
		private final List<Integer> myThrottleRejectCounts;
		private final List<Long> myTimes;
		private final List<Long> myTimestamps;

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
