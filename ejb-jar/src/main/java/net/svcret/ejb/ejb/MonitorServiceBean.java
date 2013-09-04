package net.svcret.ejb.ejb;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import net.svcret.admin.shared.model.StatusEnum;
import net.svcret.ejb.Messages;
import net.svcret.ejb.api.IBroadcastSender;
import net.svcret.ejb.api.IDao;
import net.svcret.ejb.api.IMonitorNotifier;
import net.svcret.ejb.api.IMonitorService;
import net.svcret.ejb.api.IRuntimeStatus;
import net.svcret.ejb.api.IServiceOrchestrator;
import net.svcret.ejb.api.IServiceOrchestrator.SidechannelOrchestratorResponseBean;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.model.entity.BasePersMonitorRule;
import net.svcret.ejb.model.entity.BasePersServiceCatalogItem;
import net.svcret.ejb.model.entity.BasePersServiceVersion;
import net.svcret.ejb.model.entity.InvocationStatsIntervalEnum;
import net.svcret.ejb.model.entity.PersInvocationMethodSvcverStats;
import net.svcret.ejb.model.entity.PersInvocationMethodSvcverStatsPk;
import net.svcret.ejb.model.entity.PersMonitorAppliesTo;
import net.svcret.ejb.model.entity.PersMonitorRuleActive;
import net.svcret.ejb.model.entity.PersMonitorRuleActiveCheck;
import net.svcret.ejb.model.entity.PersMonitorRuleFiring;
import net.svcret.ejb.model.entity.PersMonitorRuleFiringProblem;
import net.svcret.ejb.model.entity.PersMonitorRulePassive;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.PersServiceVersionUrl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.RateLimiter;

@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class MonitorServiceBean implements IMonitorService {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(MonitorServiceBean.class);

	private Map<PersMonitorRuleActiveCheck, RateLimiter> myCheckToRateLimiter = new HashMap<PersMonitorRuleActiveCheck, RateLimiter>();

	@EJB
	private IDao myDao;

	@EJB
	private IRuntimeStatus myRuntimeStatus;

	@EJB
	private IBroadcastSender myBroadcastSender;

	@EJB
	private IServiceOrchestrator myServiceOrchestrator;

	@EJB
	private IMonitorService myThis;

	@EJB
	private IMonitorNotifier myMonitorNotifier;

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void runActiveChecks() {

		Collection<PersMonitorRuleActiveCheck> activeChecks = myDao.getAllMonitorRuleActiveChecks();
		for (PersMonitorRuleActiveCheck nextCheck : activeChecks) {
			if (nextCheck.getRule().isRuleActive() == false) {
				continue;
			}

			RateLimiter rateLimiter;
			synchronized (myCheckToRateLimiter) {
				rateLimiter = myCheckToRateLimiter.get(myCheckToRateLimiter.get(nextCheck));
				double reqsPerSecond = nextCheck.getCheckFrequencyUnit().toRequestsPerSecond(nextCheck.getCheckFrequencyNum());
				if (rateLimiter == null) {
					rateLimiter = RateLimiter.create(reqsPerSecond);
					myCheckToRateLimiter.put(nextCheck, rateLimiter);
				}
				if (rateLimiter.getRate() != reqsPerSecond) {
					rateLimiter.setRate(reqsPerSecond);
				}
			}

			if (rateLimiter.tryAcquire()) {
				nextCheck.loadMessageAndRule();
				ourLog.debug("Queuing active rule {} check {} for execution", nextCheck.getRule().getPid(), nextCheck.getPid());
				myThis.runActiveCheck(nextCheck);
			} else {
				ourLog.trace("Rule active check {} is not yet scheduled to fire", nextCheck.getPid());
			}

		}

	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public Future<Void> runActiveCheckInNewTransaction(PersMonitorRuleActiveCheck theCheck) {
		ourLog.debug("Beginning active check pass for check {}", theCheck.getPid());

		long svcVerPid = theCheck.getServiceVersion().getPid();
		String requestBody = theCheck.getMessage().getMessage();
		String contentType = theCheck.getMessage().getContentType();
		String requestedByString = "ActiveCheck";
		PersMonitorRuleFiring outcome;
		try {

			ourLog.debug("Active check going to invoke each URL for {}", svcVerPid);
			Collection<SidechannelOrchestratorResponseBean> outcomes = myServiceOrchestrator.handleSidechannelRequestForEachUrl(svcVerPid, requestBody, contentType, requestedByString);

			outcome = evaluateRuleForActiveIssues(theCheck, outcomes);
			ourLog.debug("Active check got {} outcomes. Problem: {}", outcomes.size(), outcome);

		} catch (Exception e) {
			ourLog.error("Failed to invoke service", e);
			PersMonitorRuleFiringProblem problem = new PersMonitorRuleFiringProblem();
			problem = PersMonitorRuleFiringProblem.getInstanceForCheckFailure(theCheck.getServiceVersion(), e.toString());
			problem.setActiveCheck(theCheck);
			Collection<PersMonitorRuleFiringProblem> problems = Collections.singletonList(problem);
			outcome = toFiring(theCheck.getRule(), problems);
		}

		BasePersServiceVersion svcVer = myDao.getServiceVersionByPid(theCheck.getServiceVersion().getPid());
		PersMonitorRuleFiring currentFiring = svcVer.getMostRecentMonitorRuleFiring();
		if (outcome != null) {
			if (currentFiring == null || currentFiring.getEndDate() != null) {
				/*
				 * We have a new failure
				 */
				outcome = myDao.saveMonitorRuleFiring(outcome);
				svcVer.setMostRecentMonitorRuleFiring(outcome);
				myDao.saveServiceCatalogItem(svcVer);

				if (ourLog.isInfoEnabled()) {
					StringBuilder b = new StringBuilder();
					b.append("Saving active check failure ");
					b.append(outcome.getPid());
					b.append(" for rule ");
					b.append(theCheck.getRule().getPid());
					b.append(" which applies to service version ");
					b.append(svcVer.getPid());
					b.append(" which currently has {}");
					if (currentFiring == null) {
						b.append("no firing");
					} else {
						b.append("finished firing ");
						b.append(currentFiring.getPid());
					}
					ourLog.info(b.toString());
				}

				// notify listeners
				try {
					myMonitorNotifier.notifyFailingRule(outcome);
				} catch (ProcessingException e) {
					ourLog.error("Failed to notify listeners", e);
				}

			} else {
				ourLog.info("Active monitor check {} is failing, but not going to save it because service version {} already has a faling rule", theCheck.getPid(), svcVer.getPid());
			}
		} else {
			if (currentFiring != null && currentFiring.getEndDate() == null) {
				/*
				 * Check didn't fail, but the service version it applies to has a current rule failure, so let's see if the current pass will cancel out that failure
				 */
				if (currentFiring.getRule().equals(theCheck.getRule())) {
					boolean applies = false;
					for (PersMonitorRuleFiringProblem nextProblem : currentFiring.getProblems()) {
						if (theCheck.equals(nextProblem.getActiveCheck())) {
							applies = true;
						}
					}

					if (applies) {
						ourLog.info("Ending monitor failure {} because active check passed", currentFiring.getPid());
						currentFiring.setEndDate(new Date());

						currentFiring = myDao.saveMonitorRuleFiring(currentFiring);
						svcVer.setMostRecentMonitorRuleFiring(currentFiring);
						myDao.saveServiceCatalogItem(svcVer);
					}
				}
			}
		}

		return new AsyncResult<Void>(null);
	}

	private PersMonitorRuleFiring evaluateRuleForActiveIssues(PersMonitorRuleActiveCheck theCheck, Collection<SidechannelOrchestratorResponseBean> theOutcomes) {
		ArrayList<PersMonitorRuleFiringProblem> problems = new ArrayList<PersMonitorRuleFiringProblem>();

		for (SidechannelOrchestratorResponseBean nextOutcome : theOutcomes) {
			if (theCheck.getExpectLatencyUnderMillis() != null) {
				long latency = nextOutcome.getHttpResponse().getResponseTime();
				ourLog.debug("Active check testing if latency of {} exceeds target of {}", latency, theCheck.getExpectLatencyUnderMillis());
				if (latency > theCheck.getExpectLatencyUnderMillis()) {
					PersServiceVersionUrl url = nextOutcome.getHttpResponse().getSingleUrlOrThrow();
					PersMonitorRuleFiringProblem prob = PersMonitorRuleFiringProblem.getInstanceForServiceLatency(theCheck.getServiceVersion(), latency, theCheck.getExpectLatencyUnderMillis(), null,
							url);
					prob.setActiveCheck(theCheck);
					problems.add(prob);
				}
			}

			if (StringUtils.isNotEmpty(theCheck.getExpectResponseContainsText())) {
				boolean contains = nextOutcome.getResponseBody().contains(theCheck.getExpectResponseContainsText());
				ourLog.debug("Active check testing if response contains \"{}\": {}", theCheck.getExpectResponseContainsText(), contains);

				if (!contains) {
					PersServiceVersionUrl url = nextOutcome.getHttpResponse().getSingleUrlOrThrow();
					String message = Messages.getString("MonitorServiceBean.failedActiveCheckExpectText", theCheck.getExpectResponseContainsText());
					PersMonitorRuleFiringProblem prob = PersMonitorRuleFiringProblem.getInstanceForUrlDown(theCheck.getServiceVersion(), url, message);
					prob.setActiveCheck(theCheck);
				}
			}

			ourLog.debug("Active check testing if outcome of {} matches expected {}", nextOutcome.getResponseType(), theCheck.getExpectResponseType());
			if (nextOutcome.getResponseType() != theCheck.getExpectResponseType()) {
				PersServiceVersionUrl url = nextOutcome.getHttpResponse().getSingleUrlOrThrow();
				String message = Messages.getString("MonitorServiceBean.failedActiveCheckExpectResponseType", nextOutcome.getResponseType(), theCheck.getExpectResponseType());
				PersMonitorRuleFiringProblem prob = PersMonitorRuleFiringProblem.getInstanceForUrlDown(theCheck.getServiceVersion(), url, message);
				prob.setActiveCheck(theCheck);
			}

		}

		return toFiring(theCheck.getRule(), problems);
	}

	@Override
	@Asynchronous
	public Future<Void> runActiveCheck(PersMonitorRuleActiveCheck theCheck) {
		return myThis.runActiveCheckInNewTransaction(theCheck);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void check() {
		ourLog.debug("Beginning monitor checking pass");

		Collection<BasePersMonitorRule> rules = myDao.getMonitorRules();
		for (BasePersMonitorRule nextRule : rules) {
			if (!(nextRule instanceof PersMonitorRulePassive)) {
				continue;
			}
			PersMonitorRulePassive rule = (PersMonitorRulePassive) nextRule;

			ourLog.debug("Checking monitor rule: {}", rule);

			if (rule.isRuleActive() == false) {
				continue;
			}

			PersMonitorRuleFiring firing = evaluateRuleForPassiveIssues(rule);
			ourLog.debug("Checking firing produced result: {}", firing);

			for (PersMonitorAppliesTo nextAppliesTo : rule.getAppliesTo()) {
				BasePersServiceCatalogItem item = nextAppliesTo.getItem();
				PersMonitorRuleFiring mostRecentFiring = item.getMostRecentMonitorRuleFiring();

				if (mostRecentFiring == null || mostRecentFiring.getEndDate() != null) {
					if (firing != null) {
						firing = myDao.saveMonitorRuleFiring(firing);
						item.setMostRecentMonitorRuleFiring(firing);
						item = myDao.saveServiceCatalogItem(item);
					}
				} else if (mostRecentFiring.getEndDate() == null) {
					if (mostRecentFiring.getRule().equals(rule)) {
						if (firing == null) {
							mostRecentFiring.setEndDate(new Date());
							myDao.saveMonitorRuleFiring(mostRecentFiring);
						}
					}
				}

			}

		}

	}

	@VisibleForTesting
	void setDao(IDao theDao) {
		myDao = theDao;
	}

	@VisibleForTesting
	void setRuntimeStatus(IRuntimeStatus theRuntimeStatus) {
		myRuntimeStatus = theRuntimeStatus;
	}

	private PersMonitorRuleFiring evaluateRuleForPassiveIssues(PersMonitorRulePassive theRule) {
		Set<PersMonitorRuleFiringProblem> problems = new HashSet<PersMonitorRuleFiringProblem>();

		Set<BasePersServiceVersion> appliesToSvcVersions = theRule.toAppliesToServiceVersions();
		for (BasePersServiceVersion nextSvcVer : appliesToSvcVersions) {

			Set<PersMonitorRuleFiringProblem> svcVerProblems = new HashSet<PersMonitorRuleFiringProblem>();
			if (theRule.isPassiveFireIfAllBackingUrlsAreUnavailable() || theRule.isPassiveFireIfSingleBackingUrlIsUnavailable()) {
				for (PersServiceVersionUrl nextUrl : nextSvcVer.getUrls()) {
					if (nextUrl.getStatus() != null) {
						if (nextUrl.getStatus().getStatus() == StatusEnum.DOWN) {
							svcVerProblems.add(PersMonitorRuleFiringProblem.getInstanceForUrlDown(nextSvcVer, nextUrl, nextUrl.getStatus().getLastFailMessage()));
						}
					}else {
						ourLog.debug("URL {} has no status entry", nextUrl.getPid());
					}
				}
			}

			// Check backing URLs unavailable

			if (theRule.isPassiveFireIfAllBackingUrlsAreUnavailable()) {
				if (svcVerProblems.size() == nextSvcVer.getUrls().size()) {
					problems.addAll(svcVerProblems);
				}
			} else if (theRule.isPassiveFireIfSingleBackingUrlIsUnavailable()) {
				if (svcVerProblems.size() > 0) {
					problems.addAll(svcVerProblems);
				}
			}

			// Check latency
			Integer threshold = theRule.getPassiveFireForBackingServiceLatencyIsAboveMillis();
			if (threshold != null) {
				List<Long> times = new ArrayList<Long>();
				long startTime = DateUtils.truncate(new Date(), Calendar.MINUTE).getTime() - DateUtils.MILLIS_PER_MINUTE;
				times.add(startTime);

				Integer timeMins = theRule.getPassiveFireForBackingServiceLatencySustainTimeMins();
				if (timeMins == null) {
					// Careful if this gets removed- We cast to primitive below
					timeMins = 1;
				}
				for (int i = 2; i < timeMins; i++) {
					startTime -= DateUtils.MILLIS_PER_MINUTE;
					times.add(startTime);
				}

				long totalTime = 0;
				long totalInvocations = 0;
				for (PersServiceVersionMethod nextMethod : nextSvcVer.getMethods()) {
					for (long nextTime : times) {
						PersInvocationMethodSvcverStatsPk statsPk = new PersInvocationMethodSvcverStatsPk(InvocationStatsIntervalEnum.MINUTE, nextTime, nextMethod);
						PersInvocationMethodSvcverStats stats = myRuntimeStatus.getInvocationStatsSynchronously(statsPk);
						totalTime += stats.getSuccessInvocationTotalTime();
						totalTime += stats.getSuccessInvocationCount();
					}
				}
				if (totalInvocations > 0) {
					long avgLatency = totalTime / totalInvocations;
					if (avgLatency > threshold) {
						svcVerProblems.add(PersMonitorRuleFiringProblem.getInstanceForServiceLatency(nextSvcVer, avgLatency, threshold, (long) timeMins, null));
					}
				}
			}
		}

		PersMonitorRuleFiring firing = toFiring(theRule, problems);

		return firing;
	}

	private PersMonitorRuleFiring toFiring(BasePersMonitorRule theRule, Collection<PersMonitorRuleFiringProblem> problems) {
		PersMonitorRuleFiring firing;
		if (problems.isEmpty()) {
			firing = null;
		} else {
			firing = new PersMonitorRuleFiring();
			firing.setRule(theRule);
			firing.getProblems().addAll(problems);
			firing.setStartDate(new Date());
		}
		return firing;
	}

	@Override
	public void saveRule(BasePersMonitorRule theRule) throws ProcessingException {

		if (theRule.getPid() != null) {
			BasePersMonitorRule existing = myDao.getMonitorRule(theRule.getPid());
			switch (existing.getRuleType()) {
			case ACTIVE:
				((PersMonitorRuleActive) existing).merge(theRule);
				break;
			case PASSIVE:
				((PersMonitorRulePassive) existing).merge((PersMonitorRulePassive) theRule);
				break;
			}
		}

		myDao.saveMonitorRule(theRule);

		myBroadcastSender.monitorRulesChanged();
	}

	@VisibleForTesting
	void setBroadcastSender(IBroadcastSender theBroadcastSender) {
		myBroadcastSender = theBroadcastSender;

	}

	@VisibleForTesting
	void setServiceOrchestratorForUnitTests(IServiceOrchestrator theOrch) {
		myServiceOrchestrator = theOrch;
	}

	@VisibleForTesting
	void setThisForUnitTests(IMonitorService theThis) {
		myThis = theThis;
	}

	@VisibleForTesting
	void setMonitorNotifierForUnitTests(IMonitorNotifier theMock) {
		myMonitorNotifier = theMock;
	}

}
