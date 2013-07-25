package net.svcret.ejb.ejb;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.apache.commons.lang3.time.DateUtils;

import com.google.common.annotations.VisibleForTesting;

import net.svcret.admin.shared.model.StatusEnum;
import net.svcret.ejb.api.IDao;
import net.svcret.ejb.api.IMonitorService;
import net.svcret.ejb.api.IRuntimeStatus;
import net.svcret.ejb.model.entity.BasePersInvocationStats;
import net.svcret.ejb.model.entity.BasePersServiceCatalogItem;
import net.svcret.ejb.model.entity.BasePersServiceVersion;
import net.svcret.ejb.model.entity.InvocationStatsIntervalEnum;
import net.svcret.ejb.model.entity.PersInvocationStatsPk;
import net.svcret.ejb.model.entity.PersMonitorAppliesTo;
import net.svcret.ejb.model.entity.PersMonitorRule;
import net.svcret.ejb.model.entity.PersMonitorRuleFiring;
import net.svcret.ejb.model.entity.PersMonitorRuleFiringProblem;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.PersServiceVersionUrl;

@Singleton
public class MonitorServiceBean implements IMonitorService {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(MonitorServiceBean.class);

	@EJB
	private IDao myDao;

	@EJB
	private IRuntimeStatus myRuntimeStatus;

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void check() {
		ourLog.debug("Beginning monitor checking pass");

		Collection<PersMonitorRule> rules = myDao.getMonitorRules();
		for (PersMonitorRule rule : rules) {
			ourLog.debug("Checking monitor rule: {}", rule);

			PersMonitorRuleFiring firing = evaluateRule(rule);
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
	public void setDao(IDao theDao) {
		myDao = theDao;
	}

	@VisibleForTesting
	public void setRuntimeStatus(IRuntimeStatus theRuntimeStatus) {
		myRuntimeStatus = theRuntimeStatus;
	}

	private PersMonitorRuleFiring evaluateRule(PersMonitorRule theRule) {
		Set<PersMonitorRuleFiringProblem> problems = new HashSet<PersMonitorRuleFiringProblem>();

		Set<BasePersServiceVersion> appliesToSvcVersions = theRule.toAppliesToServiceVersions();
		for (BasePersServiceVersion nextSvcVer : appliesToSvcVersions) {

			Set<PersMonitorRuleFiringProblem> svcVerProblems = new HashSet<PersMonitorRuleFiringProblem>();
			if (theRule.isFireIfAllBackingUrlsAreUnavailable() || theRule.isFireIfSingleBackingUrlIsUnavailable()) {
				for (PersServiceVersionUrl nextUrl : nextSvcVer.getUrls()) {
					if (nextUrl.getStatus().getStatus() == StatusEnum.DOWN) {
						svcVerProblems.add(PersMonitorRuleFiringProblem.getInstanceForUrlDown(nextSvcVer, nextUrl, nextUrl.getStatus().getLastFailMessage()));
					}
				}
			}

			// Check backing URLs unavailable

			if (theRule.isFireIfAllBackingUrlsAreUnavailable()) {
				if (svcVerProblems.size() == nextSvcVer.getUrls().size()) {
					problems.addAll(svcVerProblems);
				}
			} else if (theRule.isFireIfSingleBackingUrlIsUnavailable()) {
				if (svcVerProblems.size() > 0) {
					problems.addAll(svcVerProblems);
				}
			}

			// Check latency
			Integer threshold = theRule.getFireForBackingServiceLatencyIsAboveMillis();
			if (threshold != null) {
				List<Long> times = new ArrayList<Long>();
				long startTime = DateUtils.truncate(new Date(), Calendar.MINUTE).getTime() - DateUtils.MILLIS_PER_MINUTE;
				times.add(startTime);

				Integer timeMins = theRule.getFireForBackingServiceLatencySustainTimeMins();
				if (timeMins == null) {
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
						PersInvocationStatsPk statsPk = new PersInvocationStatsPk(InvocationStatsIntervalEnum.MINUTE, nextTime, nextMethod);
						BasePersInvocationStats stats = myRuntimeStatus.getInvocationStatsSynchronously(statsPk);
						totalTime += stats.getSuccessInvocationTotalTime();
						totalTime += stats.getSuccessInvocationCount();
					}
				}
				if (totalInvocations > 0) {
					long avgLatency = totalTime / totalInvocations;
					if (avgLatency > threshold) {
						svcVerProblems.add(PersMonitorRuleFiringProblem.getInstanceForServiceLatency(nextSvcVer, avgLatency, timeMins));
					}
				}
			}
		}

		if (problems.isEmpty()) {
			return null;
		}

		PersMonitorRuleFiring firing = new PersMonitorRuleFiring();
		firing.setRule(theRule);
		firing.getProblems().addAll(problems);
		firing.setStartDate(new Date());
		return firing;
	}

}
