package net.svcret.ejb.ejb;

import static net.svcret.ejb.model.entity.InvocationStatsIntervalEnum.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.PersistenceException;

import org.apache.commons.lang3.time.DateUtils;

import net.svcret.admin.shared.model.StatusEnum;
import net.svcret.ejb.Messages;
import net.svcret.ejb.api.HttpResponseBean;
import net.svcret.ejb.api.HttpResponseBean.Failure;
import net.svcret.ejb.api.IConfigService;
import net.svcret.ejb.api.IRuntimeStatus;
import net.svcret.ejb.api.IDao;
import net.svcret.ejb.api.InvocationResponseResultsBean;
import net.svcret.ejb.api.ResponseTypeEnum;
import net.svcret.ejb.api.UrlPoolBean;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.model.entity.BasePersInvocationStats;
import net.svcret.ejb.model.entity.BasePersInvocationStatsPk;
import net.svcret.ejb.model.entity.BasePersMethodStats;
import net.svcret.ejb.model.entity.BasePersMethodStatsPk;
import net.svcret.ejb.model.entity.BasePersServiceVersion;
import net.svcret.ejb.model.entity.InvocationStatsIntervalEnum;
import net.svcret.ejb.model.entity.PersConfig;
import net.svcret.ejb.model.entity.PersInvocationAnonStats;
import net.svcret.ejb.model.entity.PersInvocationAnonStatsPk;
import net.svcret.ejb.model.entity.PersInvocationStats;
import net.svcret.ejb.model.entity.PersInvocationStatsPk;
import net.svcret.ejb.model.entity.PersInvocationUserStats;
import net.svcret.ejb.model.entity.PersInvocationUserStatsPk;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.PersServiceVersionResource;
import net.svcret.ejb.model.entity.PersServiceVersionStatus;
import net.svcret.ejb.model.entity.PersServiceVersionUrl;
import net.svcret.ejb.model.entity.PersServiceVersionUrlStatus;
import net.svcret.ejb.model.entity.PersStaticResourceStats;
import net.svcret.ejb.model.entity.PersStaticResourceStatsPk;
import net.svcret.ejb.model.entity.PersUser;
import net.svcret.ejb.util.Validate;

@Stateless
public class RuntimeStatusBean implements IRuntimeStatus {

	private static final int MAX_STATS_TO_FLUSH_AT_ONCE = 100;
	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(RuntimeStatusBean.class);
	private ReentrantLock myCollapseLock = new ReentrantLock();
	@EJB
	private IConfigService myConfigSvc;
	@EJB
	private IDao myDao;
	private ReentrantLock myFlushLock = new ReentrantLock();

	private ConcurrentHashMap<BasePersMethodStatsPk, BasePersMethodStats> myInvocationStats = new ConcurrentHashMap<BasePersMethodStatsPk, BasePersMethodStats>();

	private Date myNowForUnitTests;

	private ConcurrentHashMap<Long, PersServiceVersionStatus> myServiceVersionStatus = new ConcurrentHashMap<Long, PersServiceVersionStatus>();

	private DateFormat myTimeFormat = new SimpleDateFormat("HH:mm:ss.SSS");
	private ConcurrentHashMap<Long, PersServiceVersionUrlStatus> myUrlStatus = new ConcurrentHashMap<Long, PersServiceVersionUrlStatus>();

	@TransactionAttribute(TransactionAttributeType.NEVER)
	@Override
	public UrlPoolBean buildUrlPool(BasePersServiceVersion theServiceVersion) {
		UrlPoolBean retVal = new UrlPoolBean();

		switch (theServiceVersion.getHttpClientConfig().getUrlSelectionPolicy()) {
		case PREFER_LOCAL:
		default:
			List<String> urls = new ArrayList<String>(theServiceVersion.getUrls().size());
			retVal.setAlternateUrls(urls);

			List<PersServiceVersionUrl> urlsWithDownFirstThenLocal = new ArrayList<PersServiceVersionUrl>();
			LinkedList<PersServiceVersionUrl> allUrlsCopy = new LinkedList<PersServiceVersionUrl>(theServiceVersion.getUrls());

			for (Iterator<PersServiceVersionUrl> iter = allUrlsCopy.iterator(); iter.hasNext();) {
				PersServiceVersionUrl next = iter.next();
				if (getUrlStatus(next).getStatus() == StatusEnum.DOWN) {
					urlsWithDownFirstThenLocal.add(next);
					iter.remove();
				}
			}
			for (Iterator<PersServiceVersionUrl> iter = allUrlsCopy.iterator(); iter.hasNext();) {
				PersServiceVersionUrl next = iter.next();
				if (next.isLocal()) {
					urlsWithDownFirstThenLocal.add(next);
					iter.remove();
				}
			}
			for (Iterator<PersServiceVersionUrl> iter = allUrlsCopy.iterator(); iter.hasNext();) {
				PersServiceVersionUrl next = iter.next();
				urlsWithDownFirstThenLocal.add(next);
			}

			for (PersServiceVersionUrl next : urlsWithDownFirstThenLocal) {
				PersServiceVersionUrlStatus status = getUrlStatus(next);
				String nextUrl = next.getUrl();

				if (next.isLocal() && retVal.getPreferredUrl() == null) {
					switch (status.getStatus()) {
					case ACTIVE:
					case UNKNOWN:
						if (retVal.getPreferredUrl() != null) {
							urls.add(nextUrl);
						} else {
							retVal.setPreferredUrl(nextUrl);
						}
						break;
					case DOWN:
						if (status.attemptToResetCircuitBreaker()) {
							retVal.setPreferredUrl(nextUrl);
						}
					}
				} else {
					switch (status.getStatus()) {
					case ACTIVE:
					case UNKNOWN:
						retVal.getAlternateUrls().add(nextUrl);
						break;
					case DOWN:
						if (retVal.getPreferredUrl() != null) {
							/*
							 * We don't try to reset the circuit breaker on more
							 * than one URL at a time
							 */
						} else if (status.attemptToResetCircuitBreaker()) {
							if (retVal.getPreferredUrl() != null) {
								urls.add(retVal.getPreferredUrl());
							}
							retVal.setPreferredUrl(nextUrl);
						} else {
							/*
							 * we just won't try this one of it's down and it's
							 * not time to try resetting the CB
							 */
						}
					}
				}
			}

			if (retVal.getPreferredUrl() == null && urls.size() > 0) {
				retVal.setPreferredUrl(urls.remove(0));
			}
			break;

		}

		return retVal;
	}

	@Override
	public void collapseStats() throws ProcessingException {
		/*
		 * Make sure the flush only happens once per minute
		 */
		if (!myCollapseLock.tryLock()) {
			return;
		}
		try {
			doCollapseStats();
		} finally {
			myCollapseLock.unlock();
		}

	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void flushStatus() {

		/*
		 * Make sure the flush only happens once per minute
		 */
		if (!myFlushLock.tryLock()) {
			return;
		}
		try {
			doFlushStatus();
		} finally {
			myFlushLock.unlock();
		}

	}

	@Override
	public BasePersInvocationStats getOrCreateInvocationStatsSynchronously(PersInvocationStatsPk thePk) {
		synchronized (myInvocationStats) {
			BasePersInvocationStats retVal = myDao.getInvocationStats(thePk);
			if (retVal != null) {
				return retVal;
			} else {
				return (BasePersInvocationStats) getStatsForPk(thePk);
			}
		}
	}

	@TransactionAttribute(TransactionAttributeType.NEVER)
	@Override
	public void recordInvocationMethod(Date theInvocationTime, int theRequestLengthChars, PersServiceVersionMethod theMethod, PersUser theUser, HttpResponseBean theHttpResponse, InvocationResponseResultsBean theInvocationResponseResultsBean) {
		Validate.notNull(theInvocationTime, "InvocationTime");
		Validate.notNull(theMethod, "Method");
		Validate.notNull(theInvocationResponseResultsBean, "InvocationResponseResults");

		/*
		 * Record method statictics
		 */
		InvocationStatsIntervalEnum interval = MINUTE;
		BasePersInvocationStatsPk statsPk = new PersInvocationStatsPk(interval, theInvocationTime, theMethod);
		doRecordInvocationMethod(theRequestLengthChars, theHttpResponse, theInvocationResponseResultsBean, statsPk);

		/*
		 * Record user/anon method statistics
		 */
		if (theUser == null) {
			statsPk = new PersInvocationAnonStatsPk(interval, theInvocationTime, theMethod);
			doRecordInvocationMethod(theRequestLengthChars, theHttpResponse, theInvocationResponseResultsBean, statsPk);
		} else {
			statsPk = new PersInvocationUserStatsPk(interval, theInvocationTime, theMethod, theUser);
			doRecordInvocationMethod(theRequestLengthChars, theHttpResponse, theInvocationResponseResultsBean, statsPk);
		}

		if (theHttpResponse != null) {
			/*
			 * Record URL status for successful URLs
			 */
			String successfulUrl = theHttpResponse.getSuccessfulUrl();
			if (successfulUrl != null) {
				PersServiceVersionUrl successfulUrlBean = theMethod.getServiceVersion().getUrlWithUrl(successfulUrl);
				PersServiceVersionUrlStatus status = getUrlStatus(successfulUrlBean);
				boolean wasFault = theInvocationResponseResultsBean.getResponseType() == ResponseTypeEnum.FAULT;
				ourLog.debug("Recording successful invocation (fault {}) for URL {}", wasFault, successfulUrl);

				String message;
				if (wasFault) {
					message = Messages.getString("RuntimeStatusBean.faultUrl", theHttpResponse.getResponseTime(), theInvocationResponseResultsBean.getResponseFaultCode(), theInvocationResponseResultsBean.getResponseFaultDescription());
				} else {
					message = Messages.getString("RuntimeStatusBean.successfulUrl", theHttpResponse.getResponseTime());
				}
				doRecordUrlStatus(true, wasFault, status, message);

			}

			/*
			 * Recurd URL status for any failed URLs
			 */
			Map<String, Failure> failedUrlsMap = theHttpResponse.getFailedUrls();
			for (Entry<String, Failure> nextFailedUrlEntry : failedUrlsMap.entrySet()) {
				String nextFailedUrl = nextFailedUrlEntry.getKey();
				Failure failure = nextFailedUrlEntry.getValue();
				PersServiceVersionUrl failedUrl = theMethod.getServiceVersion().getUrlWithUrl(nextFailedUrl);
				PersServiceVersionUrlStatus failedStatus = getUrlStatus(failedUrl);
				doRecordUrlStatus(false, false, failedStatus, failure.getExplanation());
			}
		}

		/*
		 * Record Service Version status
		 */
		PersServiceVersionStatus serviceVersionStatus = theMethod.getServiceVersion().getStatus();
		serviceVersionStatus = getStatusForPk(serviceVersionStatus, serviceVersionStatus.getPid());

		switch (theInvocationResponseResultsBean.getResponseType()) {
		case SUCCESS:
			serviceVersionStatus.setLastSuccessfulInvocation(theInvocationTime);
			break;
		case SECURITY_FAIL:
			serviceVersionStatus.setLastServerSecurityFailure(theInvocationTime);
			break;
		case FAIL:
			serviceVersionStatus.setLastFailInvocation(theInvocationTime);
			break;
		case FAULT:
			serviceVersionStatus.setLastFaultInvocation(theInvocationTime);
			break;
		}

	}

	@TransactionAttribute(TransactionAttributeType.NEVER)
	@Override
	public void recordInvocationStaticResource(Date theInvocationTime, PersServiceVersionResource theResource) {
		Validate.notNull(theInvocationTime, "InvocationTime");
		Validate.notNull(theResource, "ServiceVersionResource");

		InvocationStatsIntervalEnum interval = MINUTE;

		PersStaticResourceStatsPk statsPk = new PersStaticResourceStatsPk(interval, theInvocationTime, theResource);
		PersStaticResourceStats stats = (PersStaticResourceStats) getStatsForPk(statsPk);

		stats.addAccess();
	}

	@Override
	public void recordUrlFailure(PersServiceVersionUrl theUrl, Failure theFailure) {
		Validate.notNull(theUrl, "Url");
		Validate.notNull(theFailure, "Failure");

		PersServiceVersionUrlStatus status = getUrlStatus(theUrl);
		status.setLastFail(new Date());
		status.setLastFailBody(theFailure.getBody());
		status.setLastFailContentType(theFailure.getContentType());
		status.setLastFailMessage(theFailure.getExplanation());
		status.setLastFailStatusCode(theFailure.getStatusCode());

		// Do this last since it triggers a state change
		status.setStatus(StatusEnum.DOWN);
		status.setDirty(true);
	}

	private void doCollapseStats() throws ProcessingException {
		ourLog.debug("Doing a stats collapse pass");

		PersConfig config = myConfigSvc.getConfig();

		// TODO: collapse static resource stats
		
		// Days
		{
			Date now = DAY.truncate(getNow());
			Date daysCutoff = DateUtils.addDays(now, -config.getCollapseStatsToDaysAfterNumDays());

			ourLog.info("Going to truncate any hourly stats before {}", daysCutoff);

			doCollapseStats(myDao.getInvocationStatsBefore(HOUR, daysCutoff), DAY, PersInvocationStats.class);
			doCollapseStats(myDao.getInvocationUserStatsBefore(HOUR, daysCutoff), DAY, PersInvocationUserStats.class);
			doCollapseStats(myDao.getInvocationAnonStatsBefore(HOUR, daysCutoff), DAY, PersInvocationAnonStats.class);
		}

		// 10 Minutes -> Hours
		{
			Date now = HOUR.truncate(getNow());
			Date hoursCutoff = DateUtils.addHours(now, -config.getCollapseStatsToHoursAfterNumHours());

			ourLog.info("Going to truncate any 10 minute stats before {}", hoursCutoff);

			doCollapseStats(myDao.getInvocationStatsBefore(TEN_MINUTE, hoursCutoff), HOUR, PersInvocationStats.class);
			doCollapseStats(myDao.getInvocationUserStatsBefore(TEN_MINUTE, hoursCutoff), HOUR, PersInvocationUserStats.class);
			doCollapseStats(myDao.getInvocationAnonStatsBefore(TEN_MINUTE, hoursCutoff), HOUR, PersInvocationAnonStats.class);
		}

		// Minutes -> 10 Minutes
		{
			Date now = TEN_MINUTE.truncate(getNow());
			Date hoursCutoff = DateUtils.addHours(now, -config.getCollapseStatsToTenMinutesAfterNumHours());

			ourLog.info("Going to truncate any 1 minute stats before {}", hoursCutoff);

			doCollapseStats(myDao.getInvocationStatsBefore(MINUTE, hoursCutoff), TEN_MINUTE, PersInvocationStats.class);
			doCollapseStats(myDao.getInvocationUserStatsBefore(MINUTE, hoursCutoff), TEN_MINUTE, PersInvocationUserStats.class);
			doCollapseStats(myDao.getInvocationAnonStatsBefore(MINUTE, hoursCutoff), TEN_MINUTE, PersInvocationAnonStats.class);
		}
	}

	private void doCollapseStats(List<? extends BasePersMethodStats> theList, InvocationStatsIntervalEnum toIntervalTyoe, Class<?> invocClass) {
		Map<BasePersMethodStatsPk, BasePersMethodStats> statsToFlush = new HashMap<BasePersMethodStatsPk, BasePersMethodStats>();
		List<BasePersMethodStats> statsToDelete = new ArrayList<BasePersMethodStats>();
		for (ListIterator<? extends BasePersMethodStats> iter = theList.listIterator(); iter.hasNext();) {
			BasePersMethodStats next = iter.next();

			BasePersMethodStatsPk dayPk;
			if (invocClass == PersInvocationStats.class) {
				dayPk = new PersInvocationStatsPk(toIntervalTyoe, next.getPk().getStartTime(), ((PersInvocationStatsPk)next.getPk()).getMethod());
				if (!statsToFlush.containsKey(dayPk)) {
					statsToFlush.put(dayPk, myDao.getOrCreateInvocationStats((PersInvocationStatsPk) dayPk));
				}
			} else if (invocClass == PersInvocationAnonStats.class) {
				dayPk = new PersInvocationAnonStatsPk(toIntervalTyoe, next.getPk().getStartTime(), (((PersInvocationAnonStatsPk)next.getPk()).getMethod()));
				if (!statsToFlush.containsKey(dayPk)) {
					statsToFlush.put(dayPk, myDao.getOrCreateInvocationAnonStats((PersInvocationAnonStatsPk) dayPk));
				}
			} else {
				dayPk = new PersInvocationAnonStatsPk(toIntervalTyoe, next.getPk().getStartTime(), (((PersInvocationUserStatsPk)next.getPk()).getMethod()));
				if (!statsToFlush.containsKey(dayPk)) {
					statsToFlush.put(dayPk, myDao.getOrCreateInvocationAnonStats((PersInvocationAnonStatsPk) dayPk));
				}
			}

			BasePersMethodStats target = statsToFlush.get(dayPk);
			target.mergeUnsynchronizedEvents(next);
			statsToDelete.add(next);

			ourLog.debug("Merging stats for {}/{} into {}/{}", new Object[] { next.getPk().getInterval(), next.getPk().getStartTime(), target.getPk().getInterval(), target.getPk().getStartTime() });

			if (statsToDelete.size() > MAX_STATS_TO_FLUSH_AT_ONCE) {
				myDao.saveInvocationStats(statsToFlush.values(), statsToDelete);
				statsToDelete.clear();
				statsToFlush.clear();
			}
		}
		
		if(statsToFlush.size() > 0 || statsToDelete.size() > 0) {
			myDao.saveInvocationStats(statsToFlush.values(), statsToDelete);
		}
	}

	private void doFlushStatus() {

		ourLog.debug("Going to flush status entries");

		/*
		 * Flush method stats
		 */

		List<BasePersMethodStats> stats = new ArrayList<BasePersMethodStats>();
		HashSet<BasePersMethodStatsPk> keys = new HashSet<BasePersMethodStatsPk>(myInvocationStats.keySet());

		if (keys.isEmpty()) {

			ourLog.debug("No status entries to flush");

		} else {

			Date earliest = null;
			Date latest = null;
			for (BasePersMethodStatsPk nextKey : keys) {
				BasePersMethodStats nextStats = myInvocationStats.remove(nextKey);
				if (nextStats == null) {
					continue;
				}
				stats.add(nextStats);

				if (earliest == null || earliest.after(nextStats.getPk().getStartTime())) {
					earliest = nextStats.getPk().getStartTime();
				}
				if (latest == null || latest.before(nextStats.getPk().getStartTime())) {
					latest = nextStats.getPk().getStartTime();
				}

			}

			ourLog.info("Going to flush {} stats entries with time range {} - {}", new Object[] { stats.size(), myTimeFormat.format(earliest), myTimeFormat.format(latest) });

			try {
				myDao.saveInvocationStats(stats);
				ourLog.info("Done flushing stats");
			} catch (PersistenceException e) {
				ourLog.error("Failed to flush stats to disk, going to re-queue them", e);
				for (BasePersMethodStats next : stats) {

					BasePersMethodStats savedStats = myInvocationStats.putIfAbsent(next.getPk(), next);
					if (savedStats != next) {
						savedStats.mergeUnsynchronizedEvents(next);
					}

				}
			}

		}

		/*
		 * Flush URL statuses
		 */

		ArrayList<PersServiceVersionUrlStatus> urlStatuses = new ArrayList<PersServiceVersionUrlStatus>(myUrlStatus.values());
		for (Iterator<PersServiceVersionUrlStatus> iter = urlStatuses.iterator(); iter.hasNext();) {
			PersServiceVersionUrlStatus next = iter.next();
			if (!next.isDirty()) {
				iter.remove();
			} else {
				next.setLastStatusSave(new Date());
			}
		}

		if (!urlStatuses.isEmpty()) {
			ourLog.info("Going to persist {} URL statuses", urlStatuses.size());
			myDao.saveServiceVersionUrlStatus(urlStatuses);
		}

		/*
		 * TODO: Maybe use a "last saved" timestamp here instead of a flag to
		 * prevent race conditions
		 */
		for (PersServiceVersionUrlStatus next : urlStatuses) {
			next.setDirty(false);
		}

		/*
		 * Flush Service Version Status
		 */

		ArrayList<PersServiceVersionStatus> serviceVersionStatuses = new ArrayList<PersServiceVersionStatus>(myServiceVersionStatus.values());
		for (Iterator<PersServiceVersionStatus> iter = serviceVersionStatuses.iterator(); iter.hasNext();) {
			PersServiceVersionStatus next = iter.next();
			if (!next.isDirty()) {
				iter.remove();
			} else {
				next.setLastSave(new Date());
			}
		}

		if (!serviceVersionStatuses.isEmpty()) {
			ourLog.info("Going to persist {} URL statuses", serviceVersionStatuses.size());
			myDao.saveServiceVersionStatuses(serviceVersionStatuses);
		}

		/*
		 * TODO: Maybe use a "last saved" timestamp here instead of a flag to
		 * prevent race conditions
		 */
		for (PersServiceVersionStatus next : serviceVersionStatuses) {
			next.setDirty(false);
		}

	}

	private void doRecordInvocationMethod(int theRequestLengthChars, HttpResponseBean theHttpResponse, InvocationResponseResultsBean theInvocationResponseResultsBean, BasePersInvocationStatsPk theStatsPk) {
		BasePersInvocationStats stats = (BasePersInvocationStats) getStatsForPk(theStatsPk);

		long responseTime;
		long responseBytes;
		if (theHttpResponse != null) {
			responseTime = theHttpResponse.getResponseTime();
			responseBytes = theHttpResponse.getBody().length();
		} else {
			responseTime = 0;
			responseBytes = 0;
		}
		switch (theInvocationResponseResultsBean.getResponseType()) {
		case FAIL:
			stats.addFailInvocation(responseTime, theRequestLengthChars, responseBytes);
			break;
		case FAULT:
			stats.addFaultInvocation(responseTime, theRequestLengthChars, responseBytes);
			break;
		case SUCCESS:
			stats.addSuccessInvocation(responseTime, theRequestLengthChars, responseBytes);
			break;
		case SECURITY_FAIL:
			stats.addServerSecurityFailInvocation();
			break;
		default:
			break;
		}
	}

	private void doRecordUrlStatus(boolean theWasSuccess, boolean theWasFault, PersServiceVersionUrlStatus theUrlStatusBean, String theMessage) {
		if (theUrlStatusBean.getUrl() == null) {
			throw new IllegalArgumentException("Status has no URL associated with it");
		}

		synchronized (theUrlStatusBean) {
			Date now = new Date();

			if (theWasSuccess) {

				if (theUrlStatusBean.getStatus() != StatusEnum.ACTIVE) {
					Long urlPid = theUrlStatusBean.getUrl().getPid();
					StatusEnum urlStatus = theUrlStatusBean.getStatus();
					String urlUrl = theUrlStatusBean.getUrl().getUrl();
					ourLog.info("URL[{}] is now ACTIVE, was {} - {}", new Object[] { urlPid, urlStatus, urlUrl });
				}
				theUrlStatusBean.setStatus(StatusEnum.ACTIVE);

				if (theWasFault) {
					theUrlStatusBean.setLastFault(now);
					theUrlStatusBean.setLastFaultMessage(theMessage);
				} else {
					theUrlStatusBean.setLastSuccess(now);
					theUrlStatusBean.setLastSuccessMessage(theMessage);
				}

			} else {

				theUrlStatusBean.setStatus(StatusEnum.DOWN);
				theUrlStatusBean.setLastFail(now);
				theUrlStatusBean.setLastFailMessage(theMessage);

				Date nextReset = theUrlStatusBean.getNextCircuitBreakerReset();
				if (nextReset != null) {
					ourLog.info("URL[{}] is DOWN, Next circuit breaker reset attempt is {} - {}", new Object[] { theUrlStatusBean.getUrl().getPid(), myTimeFormat.format(nextReset), theUrlStatusBean.getUrl().getUrl() });
				} else {
					ourLog.info("URL[{}] is DOWN - {}", new Object[] { theUrlStatusBean.getUrl().getPid(), theUrlStatusBean.getUrl().getUrl() });
				}

			}

		}
	}

	private Date getNow() {
		if (myNowForUnitTests != null) {
			return myNowForUnitTests;
		}
		return new Date();
	}

	private BasePersMethodStats getStatsForPk(BasePersMethodStatsPk statsPk) {
		BasePersMethodStats tryNew = statsPk.newObjectInstance();
		BasePersMethodStats stats = myInvocationStats.putIfAbsent(statsPk, tryNew);
		if (stats == null) {
			stats = tryNew;
		}

		if (ourLog.isTraceEnabled()) {
			ourLog.trace("Now have the following {} stats: {}", myInvocationStats.size(), new ArrayList<BasePersMethodStatsPk>(myInvocationStats.keySet()));
		}

		return stats;
	}

	private PersServiceVersionStatus getStatusForPk(PersServiceVersionStatus theServiceVersionStatus, Long thePid) {
		Validate.notNull(theServiceVersionStatus, "Status");
		Validate.notNull(thePid, "PID");

		PersServiceVersionStatus status = myServiceVersionStatus.putIfAbsent(thePid, theServiceVersionStatus);
		if (status == null) {
			status = theServiceVersionStatus;
		}

		return status;
	}

	private PersServiceVersionUrlStatus getUrlStatus(PersServiceVersionUrl theSuccessfulUrl) {
		PersServiceVersionUrlStatus savedStatus = theSuccessfulUrl.getStatus();
		assert savedStatus != null;

		PersServiceVersionUrlStatus existing = myUrlStatus.putIfAbsent(savedStatus.getPid(), savedStatus);
		if (existing == null) {
			return savedStatus;
		} else {
			return existing;
		}
	}

	void setConfigSvc(IConfigService theConfigSvc) {
		myConfigSvc = theConfigSvc;
	}

	/**
	 * FOR UNIT TESTS ONLY
	 */
	void setDao(IDao thePersistence) {
		myDao = thePersistence;
	}

	void setNowForUnitTests(Date theNow) {
		myNowForUnitTests = theNow;
	}

}
