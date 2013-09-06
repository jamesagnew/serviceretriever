package net.svcret.ejb.ejb;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.Singleton;

import net.svcret.ejb.api.IConfigService;
import net.svcret.ejb.api.IRuntimeStatus;
import net.svcret.ejb.api.IRuntimeStatusQueryLocal;
import net.svcret.ejb.ejb.AdminServiceBean.IWithStats;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.model.entity.BasePersInvocationStats;
import net.svcret.ejb.model.entity.BasePersInvocationStatsPk;
import net.svcret.ejb.model.entity.BasePersStats;
import net.svcret.ejb.model.entity.BasePersStatsPk;
import net.svcret.ejb.model.entity.InvocationStatsIntervalEnum;
import net.svcret.ejb.model.entity.PersConfig;
import net.svcret.ejb.model.entity.PersInvocationMethodSvcverStats;
import net.svcret.ejb.model.entity.PersInvocationMethodSvcverStatsPk;
import net.svcret.ejb.model.entity.PersInvocationUrlStats;
import net.svcret.ejb.model.entity.PersInvocationUrlStatsPk;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.PersServiceVersionUrl;

import com.google.common.annotations.VisibleForTesting;

@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class RuntimeStatusQueryBean implements IRuntimeStatusQueryLocal {

	@EJB
	private IRuntimeStatus myStatusSvc;

	@VisibleForTesting
	public void setStatusSvcForUnitTest(IRuntimeStatus theStatusSvc) {
		myStatusSvc = theStatusSvc;
	}

	@VisibleForTesting
	public void setConfigSvcForUnitTest(IConfigService theConfigSvc) {
		myConfigSvc = theConfigSvc;
	}

	@EJB
	private IConfigService myConfigSvc;


	@Override
	public void extract60MinuteMethodStats(final PersServiceVersionMethod theMethod, List<Integer> theSuccessInvCount, List<Integer> theFaultInvCount, List<Integer> theFailInvCount,
			List<Long> theTotalInvTime) throws ProcessingException {
		IWithStats<PersInvocationMethodSvcverStatsPk, PersInvocationMethodSvcverStats> operator = new StatsAdderOperator<PersInvocationMethodSvcverStatsPk, PersInvocationMethodSvcverStats>(theTotalInvTime, theFailInvCount, theFaultInvCount, theSuccessInvCount);
		IStatsPkBuilder<PersInvocationMethodSvcverStatsPk, PersInvocationMethodSvcverStats> builder = new IStatsPkBuilder<PersInvocationMethodSvcverStatsPk, PersInvocationMethodSvcverStats>() {
			@Override
			public PersInvocationMethodSvcverStatsPk createPk(InvocationStatsIntervalEnum theInterval, Date theDate) {
				return new PersInvocationMethodSvcverStatsPk(theInterval, theDate, theMethod.getPid());
			}
		};

		doWithStatsByMinute(60, operator, builder);
	}

	@Override
	public void extract60MinuteServiceVersionUrlStatistics(final PersServiceVersionUrl theUrl, ArrayList<Integer> theSuccessCount, ArrayList<Integer> theFaultCount, ArrayList<Integer> theFailCount,
			ArrayList<Long> theInvTime) throws ProcessingException {
		StatsAdderOperator<PersInvocationUrlStatsPk, PersInvocationUrlStats> operator = new StatsAdderOperator<PersInvocationUrlStatsPk, PersInvocationUrlStats>(theInvTime, theFailCount, theFaultCount, theSuccessCount);
		IStatsPkBuilder<PersInvocationUrlStatsPk, PersInvocationUrlStats> builder = new IStatsPkBuilder<PersInvocationUrlStatsPk, PersInvocationUrlStats>() {
			@Override
			public PersInvocationUrlStatsPk createPk(InvocationStatsIntervalEnum theInterval, Date theDate) {
				return new PersInvocationUrlStatsPk(theInterval, theDate, theUrl.getPid());
			}
		};

		doWithStatsByMinute(60, operator, builder);

	}

	private 
	<P extends BasePersStatsPk<P,O>, O extends BasePersStats<P,O>>
	void doWithStatsByMinute(int theNumberOfMinutes, IWithStats<P, O> theOperator,
			IStatsPkBuilder<P,O> theBuilder) throws ProcessingException {
		Date start = AdminServiceBean.getDateXMinsAgo(theNumberOfMinutes);
		Date end = new Date();
		doWithStatsByMinute(theOperator, theBuilder, start, end);
	}


	private <P extends BasePersStatsPk<P, O>, O extends BasePersStats<P, O>> void doWithStatsByMinute(IWithStats<P, O> theOperator, IStatsPkBuilder<P, O> thePkBuilder, Date start, Date end)
			throws ProcessingException {
		PersConfig config = myConfigSvc.getConfig();

		Date date = start;
		for (int min = 0; date.before(end); min++) {

			InvocationStatsIntervalEnum interval = AdminServiceBean.doWithStatsSupportFindInterval(config, date);
			date = interval.truncate(date);

			P pk = thePkBuilder.createPk(interval, date);
			O stats = myStatusSvc.getInvocationStatsSynchronously(pk);
			theOperator.withStats(min, stats);

			date = AdminServiceBean.doWithStatsSupportIncrement(date, interval);

		}
	}

	private final class StatsAdderOperator<P extends BasePersInvocationStatsPk<P,O>, O extends BasePersInvocationStats<P,O>> implements IWithStats<P,O> {
		private final List<Long> myThe60minTime;
		private final List<Integer> myFailInvCount;
		private final List<Integer> myFaultInvCount;
		private final List<Integer> mySuccessInvCount;

		private StatsAdderOperator(List<Long> theThe60minTime, List<Integer> theFailInvCount, List<Integer> theFaultInvCount, List<Integer> theSuccessInvCount) {
			myThe60minTime = theThe60minTime;
			myFailInvCount = theFailInvCount;
			myFaultInvCount = theFaultInvCount;
			mySuccessInvCount = theSuccessInvCount;
		}

		@Override
		public void withStats(int theIndex, O theStats) {
			AdminServiceBean.growToSizeInt(mySuccessInvCount, theIndex);
			AdminServiceBean.growToSizeInt(myFaultInvCount, theIndex);
			AdminServiceBean.growToSizeInt(myFailInvCount, theIndex);
			AdminServiceBean.growToSizeLong(myThe60minTime, theIndex);
			mySuccessInvCount.set(theIndex, AdminServiceBean.addToInt(mySuccessInvCount.get(theIndex), theStats.getSuccessInvocationCount()));
			myFaultInvCount.set(theIndex, AdminServiceBean.addToInt(myFaultInvCount.get(theIndex), theStats.getFaultInvocationCount()));
			myFailInvCount.set(theIndex, AdminServiceBean.addToInt(myFailInvCount.get(theIndex), theStats.getFailInvocationCount()));
			myThe60minTime.set(theIndex, myThe60minTime.get(theIndex) + theStats.getSuccessInvocationTotalTime() + theStats.getFaultInvocationTime() + theStats.getFailInvocationTime());
		}
	}

	private interface IStatsPkBuilder<P extends BasePersStatsPk<P, O>, O extends BasePersStats<P, O>> {
		P createPk(InvocationStatsIntervalEnum theInterval, Date theDate);
	}

}
