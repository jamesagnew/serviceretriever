package net.svcret.ejb.ejb;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.PersistenceException;

import net.svcret.admin.shared.model.StatusEnum;
import net.svcret.ejb.api.HttpResponseBean;
import net.svcret.ejb.api.HttpResponseBean.Failure;
import net.svcret.ejb.api.IRuntimeStatus;
import net.svcret.ejb.api.IServicePersistence;
import net.svcret.ejb.api.InvocationResponseResultsBean;
import net.svcret.ejb.api.UrlPoolBean;
import net.svcret.ejb.model.entity.BasePersInvocationStats;
import net.svcret.ejb.model.entity.BasePersInvocationStatsPk;
import net.svcret.ejb.model.entity.BasePersMethodStats;
import net.svcret.ejb.model.entity.BasePersMethodStatsPk;
import net.svcret.ejb.model.entity.BasePersServiceVersion;
import net.svcret.ejb.model.entity.InvocationStatsIntervalEnum;
import net.svcret.ejb.model.entity.PersInvocationAnonStatsPk;
import net.svcret.ejb.model.entity.PersInvocationStatsPk;
import net.svcret.ejb.model.entity.PersInvocationUserStatsPk;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.PersServiceVersionResource;
import net.svcret.ejb.model.entity.PersServiceVersionUrl;
import net.svcret.ejb.model.entity.PersServiceVersionUrlStatus;
import net.svcret.ejb.model.entity.PersStaticResourceStats;
import net.svcret.ejb.model.entity.PersStaticResourceStatsPk;
import net.svcret.ejb.model.entity.PersUser;
import net.svcret.ejb.util.Validate;

@Stateless
public class RuntimeStatusBean implements IRuntimeStatus {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(RuntimeStatusBean.class);
	private ReentrantLock myFlushLock = new ReentrantLock();
	private ConcurrentHashMap<BasePersMethodStatsPk, BasePersMethodStats> myInvocationStats = new ConcurrentHashMap<BasePersMethodStatsPk, BasePersMethodStats>();

	@EJB
	private IServicePersistence myPersistence;

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

	private void doFlushStatus() {

		List<BasePersMethodStats> stats = new ArrayList<BasePersMethodStats>();
		HashSet<BasePersMethodStatsPk> keys = new HashSet<BasePersMethodStatsPk>(myInvocationStats.keySet());

		if (keys.isEmpty()) {
			return;
		}

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
			myPersistence.saveInvocationStats(stats);
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
			myPersistence.saveServiceVersionUrlStatus(urlStatuses);
		}

		for (PersServiceVersionUrlStatus next : urlStatuses) {
			next.setDirty(false);
		}

	}

	private void doRecordInvocationMethod(int theRequestLengthChars, HttpResponseBean theHttpResponse, InvocationResponseResultsBean theInvocationResponseResultsBean, BasePersInvocationStatsPk theStatsPk) {
		BasePersInvocationStats stats = (BasePersInvocationStats) getStatsForPk(theStatsPk);

		long responseTime = theHttpResponse.getResponseTime();
		long responseBytes = theHttpResponse.getBody().length();

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
		default:
			break;
		}
	}

	private void doRecordUrlStatus(boolean theWasSuccess, PersServiceVersionUrlStatus theUrlStatusBean) {
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
				theUrlStatusBean.setLastSuccess(now);
			} else {
				theUrlStatusBean.setStatus(StatusEnum.DOWN);
				theUrlStatusBean.setLastFail(now);

				Date nextReset = theUrlStatusBean.getNextCircuitBreakerReset();
				if (nextReset != null) {
					ourLog.info("URL[{}] is DOWN, Next circuit breaker reset attempt is {} - {}", new Object[] { theUrlStatusBean.getUrl().getPid(), myTimeFormat.format(nextReset), theUrlStatusBean.getUrl().getUrl() });
				} else {
					ourLog.info("URL[{}] is DOWN - {}", new Object[] { theUrlStatusBean.getUrl().getPid(), theUrlStatusBean.getUrl().getUrl() });
				}

			}

		}
	}

	@TransactionAttribute(TransactionAttributeType.NEVER)
	@Schedule(second = "0", minute = "*", hour = "*", persistent = true)
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

	@TransactionAttribute(TransactionAttributeType.NEVER)
	@Override
	public void recordInvocationMethod(Date theInvocationTime, int theRequestLengthChars, PersServiceVersionMethod theMethod, PersUser theUser, HttpResponseBean theHttpResponse, InvocationResponseResultsBean theInvocationResponseResultsBean) {
		Validate.throwIllegalArgumentExceptionIfNull("InvocationTime", theInvocationTime);
		Validate.throwIllegalArgumentExceptionIfNull("Method", theMethod);
		Validate.throwIllegalArgumentExceptionIfNull("HttpResponse", theHttpResponse);
		Validate.throwIllegalArgumentExceptionIfNull("InvocationResponseResults", theInvocationResponseResultsBean);

		/*
		 * Record method statictics
		 */
		InvocationStatsIntervalEnum interval = InvocationStatsIntervalEnum.MINUTE;
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

		/*
		 * Record URL status for successful URLs
		 */
		String successfulUrl = theHttpResponse.getSuccessfulUrl();
		if (successfulUrl != null) {
			PersServiceVersionUrl successfulUrlBean = theMethod.getServiceVersion().getUrlWithUrl(successfulUrl);
			PersServiceVersionUrlStatus status = getUrlStatus(successfulUrlBean);
			doRecordUrlStatus(true, status);
		}

		/*
		 * Recurd URL status for any failed URLs
		 */
		Map<String, Failure> failedUrlsMap = theHttpResponse.getFailedUrls();
		for (Entry<String, Failure> nextFailedUrlEntry : failedUrlsMap.entrySet()) {
			String nextFailedUrl = nextFailedUrlEntry.getKey();
			PersServiceVersionUrl failedUrl = theMethod.getServiceVersion().getUrlWithUrl(nextFailedUrl);
			PersServiceVersionUrlStatus failedStatus = getUrlStatus(failedUrl);
			doRecordUrlStatus(false, failedStatus);
		}

	}

	@TransactionAttribute(TransactionAttributeType.NEVER)
	@Override
	public void recordInvocationStaticResource(Date theInvocationTime, PersServiceVersionResource theResource) {
		Validate.throwIllegalArgumentExceptionIfNull("InvocationTime", theInvocationTime);
		Validate.throwIllegalArgumentExceptionIfNull("ServiceVersionResource", theResource);

		InvocationStatsIntervalEnum interval = InvocationStatsIntervalEnum.MINUTE;

		PersStaticResourceStatsPk statsPk = new PersStaticResourceStatsPk(interval, theInvocationTime, theResource);
		PersStaticResourceStats stats = (PersStaticResourceStats) getStatsForPk(statsPk);

		stats.addAccess();
	}

	/**
	 * FOR UNIT TESTS ONLY
	 */
	void setPersistence(IServicePersistence thePersistence) {
		myPersistence = thePersistence;
	}

	@Override
	public void recordUrlFailure(PersServiceVersionUrl theUrl, Failure theFailure) {
		Validate.throwIllegalArgumentExceptionIfNull("Url", theUrl);
		Validate.throwIllegalArgumentExceptionIfNull("Failure", theFailure);

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

}
