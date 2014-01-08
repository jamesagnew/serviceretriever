package net.svcret.ejb.ejb.monitor;

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

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import net.svcret.admin.shared.enm.ResponseTypeEnum;
import net.svcret.admin.shared.model.StatusEnum;
import net.svcret.ejb.Messages;
import net.svcret.ejb.api.SrBeanIncomingResponse.Failure;
import net.svcret.ejb.api.IConfigService;
import net.svcret.ejb.api.IDao;
import net.svcret.ejb.api.IRuntimeStatus;
import net.svcret.ejb.api.IRuntimeStatusQueryLocal;
import net.svcret.ejb.api.IServiceOrchestrator;
import net.svcret.ejb.api.IServiceOrchestrator.SidechannelOrchestratorResponseBean;
import net.svcret.ejb.ejb.RuntimeStatusBean;
import net.svcret.ejb.ejb.nodecomm.IBroadcastSender;
import net.svcret.ejb.ex.UnexpectedFailureException;
import net.svcret.ejb.model.entity.BasePersMonitorRule;
import net.svcret.ejb.model.entity.BasePersServiceVersion;
import net.svcret.ejb.model.entity.InvocationStatsIntervalEnum;
import net.svcret.ejb.model.entity.PersInvocationMethodSvcverStats;
import net.svcret.ejb.model.entity.PersInvocationMethodSvcverStatsPk;
import net.svcret.ejb.model.entity.PersMonitorRuleActive;
import net.svcret.ejb.model.entity.PersMonitorRuleActiveCheck;
import net.svcret.ejb.model.entity.PersMonitorRuleActiveCheckOutcome;
import net.svcret.ejb.model.entity.PersMonitorRuleFiring;
import net.svcret.ejb.model.entity.PersMonitorRuleFiringProblem;
import net.svcret.ejb.model.entity.PersMonitorRulePassive;
import net.svcret.ejb.model.entity.PersMethod;
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
	private IConfigService myConfigSvc;

	@EJB
	private IRuntimeStatusQueryLocal myRuntimeStatusQuery;

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
		ourLog.trace("About to execute {} monitor active checks");

		/*
		 * How this works: First we execute all active checks, which are
		 * essentially firing sample messages at services and seeing what
		 * happens..
		 * 
		 * Then, once they are all done, we loop through the rules and see which
		 * ones generated alerts
		 */

		List<PersMonitorRuleFiring> firings = new ArrayList<PersMonitorRuleFiring>();
		for (PersMonitorRuleActiveCheck nextCheck : activeChecks) {
			if (nextCheck.getRule().isRuleActive() == false) {
				continue;
			}

			RateLimiter rateLimiter;
			synchronized (myCheckToRateLimiter) {
				rateLimiter = myCheckToRateLimiter.get(nextCheck);
				double reqsPerSecond = nextCheck.getCheckFrequencyUnit().intervalToRequestsPerSecond(nextCheck.getCheckFrequencyNum());
				
				if (rateLimiter == null) {
					ourLog.debug("Creating a RateLimiter for rule active check {}", nextCheck.getPid());
					ourLog.trace("Active check should fire {} times / minute", (reqsPerSecond * 60.0));
					rateLimiter = RateLimiter.create(reqsPerSecond);
					myCheckToRateLimiter.put(nextCheck, rateLimiter);
				}
				if (rateLimiter.getRate() != reqsPerSecond) {
					ourLog.debug("Updating RateLimiter frequency for rule active check {}", nextCheck.getPid());
					rateLimiter.setRate(reqsPerSecond);
				}
			}

			if (rateLimiter.tryAcquire()) {
				nextCheck.loadMessageAndRule();
				ourLog.debug("Executing active rule {} check {}", nextCheck.getRule().getPid(), nextCheck.getPid());
				PersMonitorRuleFiring firing = runActiveCheck(nextCheck, true);
				firings.add(firing);
			} else {
				ourLog.trace("Rule active check {} is not scheduled to fire right now", nextCheck.getPid());
			}

		}

		ourLog.debug("Checking active rules for any state changes");
		Collection<BasePersMonitorRule> rules = myDao.getMonitorRules();
		for (BasePersMonitorRule nextRule : rules) {
			if (!(nextRule instanceof PersMonitorRuleActive)) {
				continue;
			}
			PersMonitorRuleActive rule = (PersMonitorRuleActive) nextRule;

			ourLog.debug("Checking monitor rule: {}", rule);

			if (rule.isRuleActive() == false) {
				continue;
			}

			List<PersMonitorRuleFiring> activeFirings = myDao.getAllMonitorRuleFiringsWhichAreActive();
			PersMonitorRuleFiring ruleCurrentFiring = null;
			for (PersMonitorRuleFiring next : activeFirings) {
				if (next.getRule().equals(nextRule)) {
					ruleCurrentFiring = next;
					break;
				}
			}

			/*
			 * If the most recent failure of any given check is a failure, then
			 * this rule is failing. If each of the checks passed the most recent time
			 * they fired, then we are fine.
			 */
			
			boolean failed = false;
			for (PersMonitorRuleActiveCheck nextActiveCheck : rule.getActiveChecks()) {
				List<PersMonitorRuleActiveCheckOutcome> recentOutcomes = nextActiveCheck.getRecentOutcomes();
				if (!recentOutcomes.isEmpty() && recentOutcomes.get(recentOutcomes.size() - 1).getFailed() == Boolean.TRUE) {
					failed = true;
				}
			}

			if (ruleCurrentFiring != null && !failed) {
				ourLog.info("Rule {} firing {} appears to now be over, closing it", rule.getPid(), ruleCurrentFiring.getPid());
				ruleCurrentFiring.setEndDate(new Date());
				myDao.saveMonitorRuleFiring(ruleCurrentFiring);
			} else if (ruleCurrentFiring == null && failed) {
				PersMonitorRuleFiring firing = null;
				for (PersMonitorRuleFiring next : firings) {
					if (next.getRule().equals(rule)) {
						if (firing == null) {
							firing = next;
						} else {
							firing.getProblems().addAll(next.getProblems());
						}
					}
				}

				if (firing != null && firing.getProblems().size() > 0) {
					firing = myDao.saveMonitorRuleFiring(firing);
				}
			}

			//
			// PersMonitorRuleFiring firing =
			// evaluateRuleForPassiveIssues(rule);
			// ourLog.debug("Checking firing produced result: {}", firing);
			//
			// for (PersMonitorAppliesTo nextAppliesTo : rule.getAppliesTo()) {
			// BasePersServiceCatalogItem item = nextAppliesTo.getItem();
			// PersMonitorRuleFiring mostRecentFiring =
			// item.getMostRecentMonitorRuleFiring();
			//
			// if (mostRecentFiring == null || mostRecentFiring.getEndDate() !=
			// null) {
			// if (firing != null) {
			// firing = myDao.saveMonitorRuleFiring(firing);
			// item.setMostRecentMonitorRuleFiring(firing);
			// item = myDao.saveServiceCatalogItem(item);
			// }
			// } else if (mostRecentFiring.getEndDate() == null) {
			// if (mostRecentFiring.getRule().equals(rule)) {
			// if (firing == null) {
			// mostRecentFiring.setEndDate(new Date());
			// myDao.saveMonitorRuleFiring(mostRecentFiring);
			// }
			// }
			// }
			//
			// }

		} // for active checks

	}

	@Override
	public PersMonitorRuleFiring runActiveCheck(PersMonitorRuleActiveCheck theCheck, boolean thePersistResults) {
		ourLog.debug("Beginning active check pass for check {}", theCheck.getPid());

		long svcVerPid = theCheck.getServiceVersion().getPid();
		String requestBody = theCheck.getMessage().getMessageBody();
		String contentType = theCheck.getMessage().getContentType();
		String requestedByString = "ActiveCheck";
		PersMonitorRuleFiring outcome;
		try {

			ourLog.debug("Active check going to invoke each URL for {}", svcVerPid);
			Collection<SidechannelOrchestratorResponseBean> urlResponses = myServiceOrchestrator.handleSidechannelRequestForEachUrl(svcVerPid, requestBody, contentType, requestedByString);

			outcome = evaluateRuleForActiveIssues(theCheck, urlResponses);
			ourLog.debug("Active check got URL reponses: {} -- Outcome: {}", urlResponses, outcome);

			/*
			 * Save the outcome with the check
			 */
			for (SidechannelOrchestratorResponseBean nextOutcome : urlResponses) {
				PersMonitorRuleActiveCheckOutcome recentOutcome = new PersMonitorRuleActiveCheckOutcome();
				recentOutcome.setCheck(theCheck);
				recentOutcome.setImplementationUrl(nextOutcome.getApplicableUrl());
				recentOutcome.setRequestBody(theCheck.getMessage().getMessageBody(), myConfigSvc.getConfig());
				recentOutcome.setResponseBody(nextOutcome.getResponseBody(), myConfigSvc.getConfig());
				recentOutcome.setResponseType(nextOutcome.getResponseType());
				recentOutcome.setFailDescription(nextOutcome.getFailureDescription());
				if (nextOutcome.getHttpResponse() != null) {
					recentOutcome.setTransactionMillis(nextOutcome.getHttpResponse().getResponseTime());
				}
				recentOutcome.setTransactionTime(nextOutcome.getRequestStartedTime());

				for (PersMonitorRuleFiringProblem next : outcome.getProblems()) {
					if (next.getUrl() == null || next.getUrl().equals(recentOutcome.getImplementationUrl())) {
						recentOutcome.setFailed(true);
					}
				}

				if (nextOutcome.getResponseType() == ResponseTypeEnum.FAIL) {
					int code = nextOutcome.getHttpResponse() != null ? nextOutcome.getHttpResponse().getCode() : 0;
					long latency = nextOutcome.getHttpResponse() != null ? nextOutcome.getHttpResponse().getResponseTime() : 0;
					Failure failure = new Failure(nextOutcome.getResponseBody(), nextOutcome.getResponseContentType(), nextOutcome.getFailureDescription(), code, latency, null);
					myRuntimeStatus.recordUrlFailure(recentOutcome.getImplementationUrl(), failure);
				} else if (recentOutcome.getFailed() != Boolean.TRUE) {
					boolean wasFault = nextOutcome.getResponseType() == ResponseTypeEnum.FAULT;
					
					String message = Messages.getString("MonitorServiceBean.successfulUrl", nextOutcome.getHttpResponse().getResponseTime());
					int responseCode = nextOutcome.getHttpResponse().getCode();
					myRuntimeStatus.recordUrlSuccess(recentOutcome.getImplementationUrl(), wasFault, message, contentType, responseCode);
				}

				if (thePersistResults) {
					myDao.saveMonitorRuleActiveCheckOutcome(recentOutcome);
				}
			}

			if (thePersistResults) {
				Date cutoff = new Date(System.currentTimeMillis() - (10 * DateUtils.MILLIS_PER_MINUTE));
				myDao.deleteMonitorRuleActiveCheckOutcomesBeforeCutoff(theCheck, cutoff);
			}

		} catch (Exception e) {
			ourLog.error("Failed to invoke service", e);
			PersMonitorRuleFiringProblem problem = new PersMonitorRuleFiringProblem();
			problem = PersMonitorRuleFiringProblem.getInstanceForCheckFailure(theCheck.getServiceVersion(), e.toString());
			problem.setActiveCheck(theCheck);
			Collection<PersMonitorRuleFiringProblem> problems = Collections.singletonList(problem);
			outcome = toFiring(theCheck.getRule(), problems);
		}

		return outcome;

		// /*
		// * Check if this outcome either causes a new firing for its target, or
		// cancels out a currently active one
		// */
		// BasePersServiceVersion svcVer =
		// myDao.getServiceVersionByPid(theCheck.getServiceVersion().getPid());
		// PersMonitorRuleFiring currentFiring =
		// svcVer.getMostRecentMonitorRuleFiring();
		// if (outcome != null) {
		// if (currentFiring == null || currentFiring.getEndDate() != null) {
		// /*
		// * We have a new failure
		// */
		// outcome = myDao.saveMonitorRuleFiring(outcome);
		// svcVer.setMostRecentMonitorRuleFiring(outcome);
		// myDao.saveServiceCatalogItem(svcVer);
		//
		// if (ourLog.isInfoEnabled()) {
		// StringBuilder b = new StringBuilder();
		// b.append("Saving active check failure ");
		// b.append(outcome.getPid());
		// b.append(" for rule ");
		// b.append(theCheck.getRule().getPid());
		// b.append(" which applies to service version ");
		// b.append(svcVer.getPid());
		// b.append(" which currently has {}");
		// if (currentFiring == null) {
		// b.append("no firing");
		// } else {
		// b.append("finished firing ");
		// b.append(currentFiring.getPid());
		// }
		// ourLog.info(b.toString());
		// }
		//
		// // notify listeners
		// try {
		// myMonitorNotifier.notifyFailingRule(outcome);
		// } catch (ProcessingException e) {
		// ourLog.error("Failed to notify listeners", e);
		// }
		//
		// } else {
		// ourLog.info("Active monitor check {} is failing, but not going to save it because service version {} already has a faling rule",
		// theCheck.getPid(), svcVer.getPid());
		// }
		// } else {
		// if (currentFiring != null && currentFiring.getEndDate() == null) {
		// /*
		// * Check didn't fail, but the service version it applies to has a
		// current rule failure, so let's see if the current pass will cancel
		// out that failure
		// */
		// if (currentFiring.getRule().equals(theCheck.getRule())) {
		// boolean applies = false;
		// for (PersMonitorRuleFiringProblem nextProblem :
		// currentFiring.getProblems()) {
		// if (theCheck.equals(nextProblem.getActiveCheck())) {
		// applies = true;
		// }
		// }
		//
		// if (applies) {
		// ourLog.info("Ending monitor failure {} because active check passed",
		// currentFiring.getPid());
		// currentFiring.setEndDate(new Date());
		//
		// currentFiring = myDao.saveMonitorRuleFiring(currentFiring);
		// svcVer.setMostRecentMonitorRuleFiring(currentFiring);
		// myDao.saveServiceCatalogItem(svcVer);
		// }
		// }
		// }
		// }

	}

	private PersMonitorRuleFiring evaluateRuleForActiveIssues(PersMonitorRuleActiveCheck theCheck, Collection<SidechannelOrchestratorResponseBean> theOutcomes) {
		ArrayList<PersMonitorRuleFiringProblem> problems = new ArrayList<PersMonitorRuleFiringProblem>();

		for (SidechannelOrchestratorResponseBean nextOutcome : theOutcomes) {
			if (nextOutcome.getResponseType() != theCheck.getExpectResponseType()) {
				PersMonitorRuleFiringProblem prob = PersMonitorRuleFiringProblem.getInstanceForCheckFailure(theCheck.getServiceVersion(), nextOutcome.getFailureDescription());
				prob.setActiveCheck(theCheck);
				problems.add(prob);
			}

			if (nextOutcome.getHttpResponse() != null) {
				if (theCheck.getExpectLatencyUnderMillis() != null) {
					long latency = nextOutcome.getHttpResponse().getResponseTime();
					ourLog.debug("Active check testing if latency of {} exceeds target of {}", latency, theCheck.getExpectLatencyUnderMillis());
					if (latency > theCheck.getExpectLatencyUnderMillis()) {
						PersServiceVersionUrl url = nextOutcome.getHttpResponse().getSingleUrlOrThrow();
						PersMonitorRuleFiringProblem prob = PersMonitorRuleFiringProblem.getInstanceForServiceLatency(theCheck.getServiceVersion(), latency, theCheck.getExpectLatencyUnderMillis(), null, url);
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

		}

		return toFiring(theCheck.getRule(), problems);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void runPassiveChecks() {
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

			List<PersMonitorRuleFiring> activeFirings = myDao.getAllMonitorRuleFiringsWhichAreActive();
			boolean haveActiveFiringForRule = false;
			for (PersMonitorRuleFiring nextActiveFiring : activeFirings) {
				if (nextActiveFiring.getRule().equals(rule)) {
					haveActiveFiringForRule = true;
					if (firing.getProblems().isEmpty()) {
						nextActiveFiring.setEndDate(new Date());
						myDao.saveMonitorRuleFiring(nextActiveFiring);
					}
				}
			}

			if (firing.getProblems().size() > 0 && !haveActiveFiringForRule) {
				myDao.saveMonitorRuleFiring(firing);
			}

		} // for passive checks

	}

	@VisibleForTesting
	public void setDao(IDao theDao) {
		myDao = theDao;
	}

	@VisibleForTesting
	void setRuntimeStatus(IRuntimeStatusQueryLocal theRuntimeStatus) {
		myRuntimeStatusQuery = theRuntimeStatus;
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
					} else {
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
				for (PersMethod nextMethod : nextSvcVer.getMethods()) {
					for (long nextTime : times) {
						PersInvocationMethodSvcverStatsPk statsPk = new PersInvocationMethodSvcverStatsPk(InvocationStatsIntervalEnum.MINUTE, nextTime, nextMethod);
						PersInvocationMethodSvcverStats stats = myRuntimeStatusQuery.getInvocationStatsSynchronously(statsPk);
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
		firing = new PersMonitorRuleFiring();
		firing.setRule(theRule);
		firing.getProblems().addAll(problems);
		firing.setStartDate(new Date());
		return firing;
	}

	@Override
	public BasePersMonitorRule saveRule(BasePersMonitorRule theRule) throws UnexpectedFailureException {

		BasePersMonitorRule rule;
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
			rule = existing;
		} else {
			rule = theRule;
		}

		BasePersMonitorRule retVal = myDao.saveMonitorRuleInNewTransaction(rule);

		myBroadcastSender.monitorRulesChanged();

		// In case rules have been added or removed, so that the dashboard shows
		// correctly
		myBroadcastSender.notifyServiceCatalogChanged();

		return retVal;
	}

	@VisibleForTesting
	public void setBroadcastSender(IBroadcastSender theBroadcastSender) {
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

	@VisibleForTesting
	void setRuntimeStatus(RuntimeStatusBean theStatsSvc) {
		myRuntimeStatus = theStatsSvc;
	}

	@VisibleForTesting
	public void setConfigServiceForUnitTests(IConfigService theConfigSvc) {
		myConfigSvc = theConfigSvc;
	}

	/*
	 * Force all active checks to fire again on the next invocation even
	 * if they wouldn't otherwise be scheduled to 
	 */
	@VisibleForTesting
	public void clearRateLimitersForUnitTests() {
		myCheckToRateLimiter.clear();
	}

}
