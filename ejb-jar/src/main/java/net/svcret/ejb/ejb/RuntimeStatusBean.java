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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import net.svcret.admin.shared.model.StatusEnum;
import net.svcret.ejb.Messages;
import net.svcret.ejb.api.HttpResponseBean;
import net.svcret.ejb.api.HttpResponseBean.Failure;
import net.svcret.ejb.api.IConfigService;
import net.svcret.ejb.api.IDao;
import net.svcret.ejb.api.IRuntimeStatus;
import net.svcret.ejb.api.InvocationResponseResultsBean;
import net.svcret.ejb.api.ResponseTypeEnum;
import net.svcret.ejb.api.UrlPoolBean;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.model.entity.BasePersInvocationMethodStatsPk;
import net.svcret.ejb.model.entity.BasePersInvocationStats;
import net.svcret.ejb.model.entity.BasePersInvocationStatsPk;
import net.svcret.ejb.model.entity.BasePersMethodStats;
import net.svcret.ejb.model.entity.BasePersServiceVersion;
import net.svcret.ejb.model.entity.InvocationStatsIntervalEnum;
import net.svcret.ejb.model.entity.PersConfig;
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
import net.svcret.ejb.model.entity.PersUserMethodStatus;
import net.svcret.ejb.model.entity.PersUserStatus;
import net.svcret.ejb.util.Validate;

import org.apache.commons.lang3.time.DateUtils;

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
	private Date myNowForUnitTests;
	private DateFormat myTimeFormat = new SimpleDateFormat("HH:mm:ss.SSS");
	private ConcurrentHashMap<Long, PersServiceVersionUrlStatus> myUrlStatus = new ConcurrentHashMap<Long, PersServiceVersionUrlStatus>();
	private ConcurrentHashMap<BasePersInvocationStatsPk, BasePersMethodStats> myUnflushedInvocationStats = new ConcurrentHashMap<BasePersInvocationStatsPk, BasePersMethodStats>();
	private ConcurrentHashMap<Long, PersServiceVersionStatus> myUnflushedServiceVersionStatus = new ConcurrentHashMap<Long, PersServiceVersionStatus>();

	@TransactionAttribute(TransactionAttributeType.NEVER)
	@Override
	public UrlPoolBean buildUrlPool(BasePersServiceVersion theServiceVersion) {
		UrlPoolBean retVal = new UrlPoolBean();

		switch (theServiceVersion.getHttpClientConfig().getUrlSelectionPolicy()) {
		case PREFER_LOCAL:
			choseUrlPreferLocal(theServiceVersion, retVal);
			break;
		case ROUND_ROBIN:
			chooseUrlRoundRobin(theServiceVersion, retVal);
		}

		return retVal;
	}

	private void chooseUrlRoundRobin(BasePersServiceVersion theServiceVersion, UrlPoolBean theRetVal) {
		AtomicInteger counter = theServiceVersion.getUrlCounter();

		int startIndex = counter.getAndIncrement();
		if (startIndex > 10000) {
			counter.set(0);
		}

		startIndex = startIndex % theServiceVersion.getUrls().size();

		List<PersServiceVersionUrl> urls = new LinkedList<PersServiceVersionUrl>();
		urls.add(theServiceVersion.getUrls().get(startIndex));
		for (int count = startIndex + 1; count < theServiceVersion.getUrls().size(); count++) {
			urls.add(theServiceVersion.getUrls().get(count));
		}
		for (int count = 0; count < startIndex; count++) {
			urls.add(theServiceVersion.getUrls().get(count));
		}

		for (Iterator<PersServiceVersionUrl> iter = urls.iterator(); iter.hasNext();) {
			PersServiceVersionUrl next = iter.next();
			PersServiceVersionUrlStatus status = getUrlStatus(next);
			if (status.getStatus() == StatusEnum.DOWN) {
				if (theRetVal.getPreferredUrl() == null) {
					if (status.attemptToResetCircuitBreaker()) {
						theRetVal.setPreferredUrl(next);
						iter.remove();
					} else {
						iter.remove();
						continue;
					}
				} else {
					iter.remove();
					continue;
				}
			}

		}
		
		if (theRetVal.getPreferredUrl() == null && urls.size() > 0) {
			theRetVal.setPreferredUrl(urls.remove(0));
		}
		
		theRetVal.setAlternateUrls(urls);

	}

	private void choseUrlPreferLocal(BasePersServiceVersion theServiceVersion, UrlPoolBean retVal) {
		List<PersServiceVersionUrl> urls = new ArrayList<PersServiceVersionUrl>(theServiceVersion.getUrls().size());
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

			if (next.isLocal() && retVal.getPreferredUrl() == null) {
				switch (status.getStatus()) {
				case ACTIVE:
				case UNKNOWN:
					if (retVal.getPreferredUrl() != null) {
						urls.add(next);
					} else {
						retVal.setPreferredUrl(next);
					}
					break;
				case DOWN:
					if (status.attemptToResetCircuitBreaker()) {
						retVal.setPreferredUrl(next);
					}
				}
			} else {
				switch (status.getStatus()) {
				case ACTIVE:
				case UNKNOWN:
					retVal.getAlternateUrls().add(next);
					break;
				case DOWN:
					if (retVal.getPreferredUrl() != null) {
						/*
						 * We don't try to reset the circuit breaker on more than one URL at a time
						 */
					} else if (status.attemptToResetCircuitBreaker()) {
						if (retVal.getPreferredUrl() != null) {
							urls.add(retVal.getPreferredUrl());
						}
						retVal.setPreferredUrl(next);
					} else {
						/*
						 * we just won't try this one of it's down and it's not time to try resetting the CB
						 */
					}
				}
			}
		}

		if (retVal.getPreferredUrl() == null && urls.size() > 0) {
			retVal.setPreferredUrl(urls.remove(0));
		}
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
		 * Make sure the flush only happens once at a time
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
		synchronized (myUnflushedInvocationStats) {
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
	public void recordInvocationMethod(Date theInvocationTime, int theRequestLengthChars, PersServiceVersionMethod theMethod, PersUser theUser, HttpResponseBean theHttpResponse,
			InvocationResponseResultsBean theInvocationResponseResultsBean) {
		Validate.notNull(theInvocationTime, "InvocationTime");
		Validate.notNull(theMethod, "Method");
		Validate.notNull(theInvocationResponseResultsBean, "InvocationResponseResults");

		ourLog.trace("Going to record method invocation");

		/*
		 * Record method statictics
		 */
		InvocationStatsIntervalEnum interval = MINUTE;
		BasePersInvocationMethodStatsPk statsPk = new PersInvocationStatsPk(interval, theInvocationTime, theMethod);
		doRecordInvocationMethod(theRequestLengthChars, theHttpResponse, theInvocationResponseResultsBean, statsPk);

		/*
		 * Record user/anon method statistics
		 */
		if (theUser != null) {
			PersInvocationUserStatsPk uStatsPk = new PersInvocationUserStatsPk(interval, theInvocationTime, theUser);
			doRecordInvocationMethod(theRequestLengthChars, theHttpResponse, theInvocationResponseResultsBean, uStatsPk);

			doUpdateUserStatus(theMethod, theInvocationResponseResultsBean, theUser, theInvocationTime);
		}

		if (theHttpResponse != null) {
			/*
			 * Record URL status for successful URLs
			 */
			PersServiceVersionUrl successfulUrl = theHttpResponse.getSuccessfulUrl();
			if (successfulUrl != null) {
				PersServiceVersionUrlStatus status = getUrlStatus(successfulUrl);
				boolean wasFault = theInvocationResponseResultsBean.getResponseType() == ResponseTypeEnum.FAULT;
				ourLog.debug("Recording successful invocation (fault={}) for URL {}/{}", new Object[] { wasFault, successfulUrl.getPid(), successfulUrl.getUrlId() });

				String message;
				if (wasFault) {
					message = Messages.getString("RuntimeStatusBean.faultUrl", theHttpResponse.getResponseTime(), theInvocationResponseResultsBean.getResponseFaultCode(),
							theInvocationResponseResultsBean.getResponseFaultDescription());
				} else {
					message = Messages.getString("RuntimeStatusBean.successfulUrl", theHttpResponse.getResponseTime());
				}
				doRecordUrlStatus(true, wasFault, status, message);

			}

			/*
			 * Recurd URL status for any failed URLs
			 */
			Map<PersServiceVersionUrl, Failure> failedUrlsMap = theHttpResponse.getFailedUrls();
			for (Entry<PersServiceVersionUrl, Failure> nextFailedUrlEntry : failedUrlsMap.entrySet()) {
				PersServiceVersionUrl nextFailedUrl = nextFailedUrlEntry.getKey();
				Failure failure = nextFailedUrlEntry.getValue();
				PersServiceVersionUrlStatus failedStatus = getUrlStatus(nextFailedUrl);
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

	private ConcurrentHashMap<PersUser, PersUserStatus> myUnflushedUserStatus = new ConcurrentHashMap<PersUser, PersUserStatus>();

	private void doUpdateUserStatus(PersServiceVersionMethod theMethod, InvocationResponseResultsBean theInvocationResponseResultsBean, PersUser theUser, Date theTransactionTime) {
		PersUserStatus status = getUserStatusForUser(theUser);

		PersUserMethodStatus methodStatus = status.getOrCreateMethodStatus(theMethod);
		
		
		switch (theInvocationResponseResultsBean.getResponseType()) {
		case SUCCESS:
			status.setLastAccessIfNewer(theTransactionTime);
			methodStatus.setLastSuccessfulInvocationIfNewer(theTransactionTime);
			break;
		case FAULT:
			status.setLastAccessIfNewer(theTransactionTime);
			methodStatus.setLastFaultInvocationIfNewer(theTransactionTime);
			break;
		case SECURITY_FAIL:
			status.setLastSecurityFailIfNewer(theTransactionTime);
			methodStatus.setLastSecurityFailInvocationIfNewer(theTransactionTime);
			break;
		case FAIL:
			status.setLastAccessIfNewer(theTransactionTime);
			methodStatus.setLastFailInvocationIfNewer(theTransactionTime);
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

			ourLog.debug("Going to truncate any hourly stats before {}", daysCutoff);

			doCollapseStats(myDao.getInvocationStatsBefore(HOUR, daysCutoff), DAY, PersInvocationStats.class);
			doCollapseStats(myDao.getInvocationUserStatsBefore(HOUR, daysCutoff), DAY, PersInvocationUserStats.class);
		}

		// 10 Minutes -> Hours
		{
			Date now = HOUR.truncate(getNow());
			Date hoursCutoff = DateUtils.addHours(now, -config.getCollapseStatsToHoursAfterNumHours());

			ourLog.debug("Going to truncate any 10 minute stats before {}", hoursCutoff);

			doCollapseStats(myDao.getInvocationStatsBefore(TEN_MINUTE, hoursCutoff), HOUR, PersInvocationStats.class);
			doCollapseStats(myDao.getInvocationUserStatsBefore(TEN_MINUTE, hoursCutoff), HOUR, PersInvocationUserStats.class);
		}

		// Minutes -> 10 Minutes
		{
			Date now = TEN_MINUTE.truncate(getNow());
			Date hoursCutoff = DateUtils.addHours(now, -config.getCollapseStatsToTenMinutesAfterNumHours());

			ourLog.debug("Going to truncate any 1 minute stats before {}", hoursCutoff);

			doCollapseStats(myDao.getInvocationStatsBefore(MINUTE, hoursCutoff), TEN_MINUTE, PersInvocationStats.class);
			doCollapseStats(myDao.getInvocationUserStatsBefore(MINUTE, hoursCutoff), TEN_MINUTE, PersInvocationUserStats.class);
		}
	}

	private void doCollapseStats(List<? extends BasePersMethodStats> theList, InvocationStatsIntervalEnum toIntervalTyoe, Class<?> invocClass) {
		Map<BasePersInvocationStatsPk, BasePersMethodStats> statsToFlush = new HashMap<BasePersInvocationStatsPk, BasePersMethodStats>();
		List<BasePersMethodStats> statsToDelete = new ArrayList<BasePersMethodStats>();
		for (ListIterator<? extends BasePersMethodStats> iter = theList.listIterator(); iter.hasNext();) {
			BasePersMethodStats next = iter.next();

			BasePersInvocationStatsPk dayPk;
			if (invocClass == PersInvocationStats.class) {
				dayPk = new PersInvocationStatsPk(toIntervalTyoe, next.getPk().getStartTime(), ((PersInvocationStatsPk) next.getPk()).getMethodPid());
				if (!statsToFlush.containsKey(dayPk)) {
					statsToFlush.put(dayPk, new PersInvocationStats((PersInvocationStatsPk) dayPk));
					// statsToFlush.put(dayPk, myDao.getOrCreateInvocationStats((PersInvocationStatsPk) dayPk));
				}
			} else if (invocClass == PersInvocationUserStats.class) {
				dayPk = new PersInvocationUserStatsPk(toIntervalTyoe, next.getPk().getStartTime(), ((PersInvocationUserStatsPk) next.getPk()).getUser());
				if (!statsToFlush.containsKey(dayPk)) {
					statsToFlush.put(dayPk, new PersInvocationUserStats((PersInvocationUserStatsPk) dayPk));
					// statsToFlush.put(dayPk, myDao.getOrCreateInvocationUserStats((PersInvocationUserStatsPk) dayPk));
				}
			} else {
				throw new IllegalStateException("Unknown type: " + invocClass);
			}

			BasePersMethodStats target = statsToFlush.get(dayPk);
			target.mergeUnsynchronizedEvents(next);
			statsToDelete.add(next);

			if (ourLog.isDebugEnabled()) {
				ourLog.debug("Merging stats for {} into {}", next, target);
			}

			if (statsToFlush.size() > MAX_STATS_TO_FLUSH_AT_ONCE || statsToDelete.size() > MAX_STATS_TO_FLUSH_AT_ONCE) {
				myDao.saveInvocationStats(statsToFlush.values(), statsToDelete);
				statsToDelete.clear();
				statsToFlush.clear();
			}
		}

		ourLog.trace("Deleting stats {}", statsToDelete);

		if (statsToFlush.size() > 0 || statsToDelete.size() > 0) {
			myDao.saveInvocationStats(statsToFlush.values(), statsToDelete);
		}
	}

	private void doFlushStatus() {

		ourLog.debug("Going to flush status entries");

		/*
		 * Flush method stats
		 */

		List<BasePersMethodStats> stats = new ArrayList<BasePersMethodStats>();
		HashSet<BasePersInvocationStatsPk> keys = new HashSet<BasePersInvocationStatsPk>(myUnflushedInvocationStats.keySet());

		if (keys.isEmpty()) {

			ourLog.debug("No status entries to flush");

		} else {

			Date earliest = null;
			Date latest = null;
			for (BasePersInvocationStatsPk nextKey : keys) {
				BasePersMethodStats nextStats = myUnflushedInvocationStats.remove(nextKey);
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

			// try {

			ourLog.trace("Flushing stats: {}", stats);
			for (int index = 0; index < stats.size(); index += MAX_STATS_TO_FLUSH_AT_ONCE) {
				int toIndex = Math.min(index + MAX_STATS_TO_FLUSH_AT_ONCE, stats.size());
				myDao.saveInvocationStats(stats.subList(index, toIndex));
			}
			ourLog.info("Done flushing stats");

			// } catch (PersistenceException e) {
			// ourLog.error("Failed to flush stats to disk, going to re-queue them",
			// e);
			// for (BasePersMethodStats next : stats) {
			//
			// BasePersMethodStats savedStats =
			// myUnflushedInvocationStats.putIfAbsent(next.getPk(), next);
			// if (savedStats != next) {
			// savedStats.mergeUnsynchronizedEvents(next);
			// }
			//
			// }
			// }

		}

		/*
		 * Flush URL statuses
		 */

		ArrayList<PersServiceVersionUrlStatus> urlStatuses = new ArrayList<PersServiceVersionUrlStatus>(myUrlStatus.values());
		ourLog.debug("Going to flush {} URL statuses", urlStatuses.size());
		
		for (Iterator<PersServiceVersionUrlStatus> iter = urlStatuses.iterator(); iter.hasNext();) {
			PersServiceVersionUrlStatus next = iter.next();
			if (!next.isDirty()) {
				ourLog.debug("Not removing URL status {} because it isn't dirty", next.getPid());
				iter.remove();
			} else {
				next.setLastStatusSave(new Date());
				/*
				 * TODO: Maybe use a "last saved" timestamp here instead of a flag to prevent race conditions
				 */
				next.setDirty(false);
			}
		}

		if (!urlStatuses.isEmpty()) {
			ourLog.info("Going to persist {} URL statuses", urlStatuses.size());
			myDao.saveServiceVersionUrlStatus(urlStatuses);
		}

		/*
		 * Flush Service Version Status
		 */

		ArrayList<PersServiceVersionStatus> serviceVersionStatuses = new ArrayList<PersServiceVersionStatus>(myUnflushedServiceVersionStatus.values());
		for (Iterator<PersServiceVersionStatus> iter = serviceVersionStatuses.iterator(); iter.hasNext();) {
			PersServiceVersionStatus next = iter.next();
			if (!next.isDirty()) {
				iter.remove();
			} else {
				next.setLastSave(new Date());
				/*
				 * TODO: Maybe use a "last saved" timestamp here instead of a flag to prevent race conditions
				 */
				next.setDirty(false);
			}
		}

		if (!serviceVersionStatuses.isEmpty()) {
			ourLog.info("Going to persist {} URL statuses", serviceVersionStatuses.size());
			myDao.saveServiceVersionStatuses(serviceVersionStatuses);
		}

		/*
		 * Flush user status
		 */
		
		List<PersUserStatus> userStatuses = new ArrayList<PersUserStatus>();
		for (PersUser next : new HashSet<PersUser>(myUnflushedUserStatus.keySet())) {
			PersUserStatus nextStatus = myUnflushedUserStatus.remove(next);
			userStatuses.add(nextStatus);
		}
		if (userStatuses.size() > 0) {
			myDao.saveUserStatus(userStatuses);
		}
	}

	private void doRecordInvocationMethod(int theRequestLengthChars, HttpResponseBean theHttpResponse, InvocationResponseResultsBean theInvocationResponseResultsBean,
			BasePersInvocationStatsPk theStatsPk) {
		Validate.notNull(theInvocationResponseResultsBean.getResponseType(), "responseType");
		
		BasePersInvocationStats stats = (BasePersInvocationStats) getStatsForPk(theStatsPk);

		long responseTime;
		long responseBytes;
		if (theHttpResponse != null) {
			if (theHttpResponse.getBody()==null) {
				throw new NullPointerException("HTTP Response is null");
			}
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
					ourLog.info("URL[{}] is DOWN, Next circuit breaker reset attempt is {} - {}", new Object[] { theUrlStatusBean.getUrl().getPid(), myTimeFormat.format(nextReset),
							theUrlStatusBean.getUrl().getUrl() });
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

	private BasePersMethodStats getStatsForPk(BasePersInvocationStatsPk statsPk) {
		BasePersMethodStats tryNew = statsPk.newObjectInstance();
		BasePersMethodStats stats = myUnflushedInvocationStats.putIfAbsent(statsPk, tryNew);
		if (stats == null) {
			stats = tryNew;
		}

		if (ourLog.isTraceEnabled()) {
			ourLog.trace("Now have the following {} stats: {}", myUnflushedInvocationStats.size(), new ArrayList<BasePersInvocationStatsPk>(myUnflushedInvocationStats.keySet()));
		}

		return stats;
	}

	private PersUserStatus getUserStatusForUser(PersUser theUser) {
		PersUserStatus tryNew = theUser.getStatus();
		PersUserStatus status = myUnflushedUserStatus.putIfAbsent(theUser, tryNew);
		if (status == null) {
			status = tryNew;
		}

		return status;
	}

	private PersServiceVersionStatus getStatusForPk(PersServiceVersionStatus theServiceVersionStatus, Long thePid) {
		Validate.notNull(theServiceVersionStatus, "Status");
		Validate.notNull(thePid, "PID");

		PersServiceVersionStatus status = myUnflushedServiceVersionStatus.putIfAbsent(thePid, theServiceVersionStatus);
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

	@Override
	public BasePersInvocationStats getOrCreateUserInvocationStatsSynchronously(PersInvocationUserStatsPk thePk) {
		synchronized (myUnflushedInvocationStats) {
			BasePersInvocationStats retVal = myDao.getInvocationUserStats(thePk);
			if (retVal != null) {
				return retVal;
			} else {
				return (BasePersInvocationStats) getStatsForPk(thePk);
			}
		}
	}

}
