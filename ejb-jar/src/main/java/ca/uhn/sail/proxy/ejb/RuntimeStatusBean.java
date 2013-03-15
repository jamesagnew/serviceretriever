package ca.uhn.sail.proxy.ejb;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.PersistenceException;

import ca.uhn.sail.proxy.api.HttpResponseBean;
import ca.uhn.sail.proxy.api.HttpResponseBean.Failure;
import ca.uhn.sail.proxy.api.IRuntimeStatus;
import ca.uhn.sail.proxy.api.IServicePersistence;
import ca.uhn.sail.proxy.api.InvocationResponseResultsBean;
import ca.uhn.sail.proxy.api.UrlPoolBean;
import ca.uhn.sail.proxy.model.entity.BasePersInvocationStats;
import ca.uhn.sail.proxy.model.entity.BasePersInvocationStatsPk;
import ca.uhn.sail.proxy.model.entity.BasePersMethodStats;
import ca.uhn.sail.proxy.model.entity.BasePersMethodStatsPk;
import ca.uhn.sail.proxy.model.entity.BasePersServiceVersion;
import ca.uhn.sail.proxy.model.entity.InvocationStatsIntervalEnum;
import ca.uhn.sail.proxy.model.entity.PersInvocationAnonStatsPk;
import ca.uhn.sail.proxy.model.entity.PersInvocationStatsPk;
import ca.uhn.sail.proxy.model.entity.PersInvocationUserStatsPk;
import ca.uhn.sail.proxy.model.entity.PersServiceVersionMethod;
import ca.uhn.sail.proxy.model.entity.PersServiceUser;
import ca.uhn.sail.proxy.model.entity.PersServiceVersionResource;
import ca.uhn.sail.proxy.model.entity.PersServiceVersionUrl;
import ca.uhn.sail.proxy.model.entity.PersServiceVersionUrlStatus;
import ca.uhn.sail.proxy.model.entity.PersServiceVersionUrlStatus.StatusEnum;
import ca.uhn.sail.proxy.model.entity.PersStaticResourceStats;
import ca.uhn.sail.proxy.model.entity.PersStaticResourceStatsPk;
import ca.uhn.sail.proxy.util.Validate;

@Singleton
@Stateless
public class RuntimeStatusBean implements IRuntimeStatus {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(RuntimeStatusBean.class);
	private ReentrantLock myFlushLock = new ReentrantLock();
	private ConcurrentHashMap<BasePersMethodStatsPk, BasePersMethodStats> myInvocationStats = new ConcurrentHashMap<BasePersMethodStatsPk, BasePersMethodStats>();

	@EJB
	private IServicePersistence myPersistence;

	private DateFormat myTimeFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM);

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

			for (PersServiceVersionUrl next : theServiceVersion.getUrls()) {
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
						if (status.attemptToResetCircuitBreaker()) {
							if (retVal.getPreferredUrl() != null) {
								urls.add(retVal.getPreferredUrl());
							}
							retVal.setPreferredUrl(nextUrl);
						} else {
							// we just won't try this one of it's down and it's not time to
							// try resetting the CB
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

	private void doRecordInvocationMethod(int theRequestLength, HttpResponseBean theHttpResponse, InvocationResponseResultsBean theInvocationResponseResultsBean, BasePersInvocationStatsPk theStatsPk) {
		BasePersInvocationStats stats = (BasePersInvocationStats) getStatsForPk(theStatsPk);

		long responseTime = theHttpResponse.getResponseTime();
		long responseBytes = theHttpResponse.getBody().length();

		switch (theInvocationResponseResultsBean.getResponseType()) {
		case FAIL:
			stats.addFailInvocation(responseTime, theRequestLength, responseBytes);
			break;
		case FAULT:
			stats.addFaultInvocation(responseTime, theRequestLength, responseBytes);
			break;
		case SUCCESS:
			stats.addSuccessInvocation(responseTime, theRequestLength, responseBytes);
			break;
		default:
			break;
		}
	}

	private void doRecordUrlStatus(PersServiceVersionUrlStatus theStatus, HttpResponseBean theHttpResponse, InvocationResponseResultsBean theInvocationResponseResultsBean) {

		synchronized (theStatus) {
			Date now = new Date();

			switch (theInvocationResponseResultsBean.getResponseType()) {
			case FAIL:
				theStatus.setStatus(StatusEnum.DOWN);
				theStatus.setLastFail(now);

				Date nextReset = theStatus.getNextCircuitBreakerReset();
				if (nextReset != null) {
					ourLog.info("URL[{}] is DOWN, Next circuit breaker reset attempt is {} - {}", new Object[] { theStatus.getUrl().getPid(), myTimeFormat.format(nextReset), theStatus.getUrl().getUrl() });
				} else {
					ourLog.info("URL[{}] is DOWN - {}", new Object[] { theStatus.getUrl().getUrl(), theStatus.getUrl().getUrl() });
				}

				break;
			case FAULT:
				// Faults are ok, the service is allowed to throw them
				theStatus.setStatus(StatusEnum.ACTIVE);
				theStatus.setLastFault(now);
				break;
			case SUCCESS:
				theStatus.setStatus(StatusEnum.ACTIVE);
				theStatus.setLastSuccess(now);
				break;
			default:
				break;
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
	public void recordInvocationMethod(Date theInvocationTime, int theRequestLength, PersServiceVersionMethod theMethod, PersServiceUser theUser, HttpResponseBean theHttpResponse, InvocationResponseResultsBean theInvocationResponseResultsBean) {
		Validate.throwIllegalArgumentExceptionIfNull("InvocationTime", theInvocationTime);
		Validate.throwIllegalArgumentExceptionIfNull("Method", theMethod);
		Validate.throwIllegalArgumentExceptionIfNull("HttpResponse", theHttpResponse);
		Validate.throwIllegalArgumentExceptionIfNull("InvocationResponseResults", theInvocationResponseResultsBean);

		/*
		 * Record method statictics
		 */
		InvocationStatsIntervalEnum interval = InvocationStatsIntervalEnum.MINUTE;
		BasePersInvocationStatsPk statsPk = new PersInvocationStatsPk(interval, theInvocationTime, theMethod);
		doRecordInvocationMethod(theRequestLength, theHttpResponse, theInvocationResponseResultsBean, statsPk);

		/*
		 * Record user/anon method statistics
		 */
		if (theUser == null) {
			statsPk = new PersInvocationAnonStatsPk(interval, theInvocationTime, theMethod);
			doRecordInvocationMethod(theRequestLength, theHttpResponse, theInvocationResponseResultsBean, statsPk);
		} else {
			statsPk = new PersInvocationUserStatsPk(interval, theInvocationTime, theMethod, theUser);
			doRecordInvocationMethod(theRequestLength, theHttpResponse, theInvocationResponseResultsBean, statsPk);
		}

		/*
		 * Record URL status
		 */
		PersServiceVersionUrl successfulUrl = theMethod.getServiceVersion().getUrlWithUrl(theHttpResponse.getSuccessfulUrl());
		PersServiceVersionUrlStatus status = getUrlStatus(successfulUrl);

		doRecordUrlStatus(status, theHttpResponse, theInvocationResponseResultsBean);

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
