package net.svcret.ejb.ejb;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.Singleton;

import com.google.common.annotations.VisibleForTesting;

import net.svcret.ejb.api.IConfigService;
import net.svcret.ejb.api.IRuntimeStatus;
import net.svcret.ejb.api.IRuntimeStatusQueryLocal;
import net.svcret.ejb.ejb.AdminServiceBean.IWithStats;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.model.entity.BasePersStats;
import net.svcret.ejb.model.entity.BasePersStatsPk;
import net.svcret.ejb.model.entity.InvocationStatsIntervalEnum;
import net.svcret.ejb.model.entity.PersConfig;
import net.svcret.ejb.model.entity.PersInvocationMethodSvcverStats;
import net.svcret.ejb.model.entity.PersInvocationMethodSvcverStatsPk;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.PersServiceVersionUrl;

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
	
	/**
	 */
	private void extractSuccessfulInvocationInvocationTimes(int theNumMinsBack, final List<Integer> the60MinInvCount, final List<Long> the60minTime,
			PersServiceVersionMethod nextMethod) throws ProcessingException {
		AdminServiceBean.doWithStatsByMinute(myConfigSvc.getConfig(), theNumMinsBack, myStatusSvc, nextMethod, new IWithStats<PersInvocationMethodSvcverStatsPk, PersInvocationMethodSvcverStats>() {
			@Override
			public void withStats(int theIndex, PersInvocationMethodSvcverStats theStats) {
				AdminServiceBean.growToSizeInt(the60MinInvCount, theIndex);
				AdminServiceBean.growToSizeLong(the60minTime, theIndex);
				the60MinInvCount.set(theIndex, AdminServiceBean.addToInt(the60MinInvCount.get(theIndex), theStats.getSuccessInvocationCount()));
				the60minTime.set(theIndex, the60minTime.get(theIndex) + theStats.getSuccessInvocationTotalTime());
			}
		});
	}

	@Override
	public void extract60MinuteMethodStats(PersServiceVersionMethod theMethod, List<Integer> theInvCount, List<Long> theInvTime) throws ProcessingException {
		extractSuccessfulInvocationInvocationTimes(60, theInvCount, theInvTime, theMethod);
	}

	@Override
	public void extract60MinuteServiceVersionUrlStatistics(PersServiceVersionUrl theUrl, ArrayList<Integer> theSuccessCount, ArrayList<Integer> theFaultCount, ArrayList<Integer> theFailCount,
			ArrayList<Long> theInvTime) {
		
		
	}

	
	public static void doWithStatsByMinute(PersConfig theConfig, int theNumberOfMinutes, IRuntimeStatus statusSvc, final PersServiceVersionMethod theMethod, IWithStats<PersInvocationMethodSvcverStatsPk,PersInvocationMethodSvcverStats> theOperator) {
		Date start = AdminServiceBean.getDateXMinsAgo(theNumberOfMinutes);
		Date end = new Date();

		IStatsPkBuilder<PersInvocationMethodSvcverStatsPk, PersInvocationMethodSvcverStats> builder=new IStatsPkBuilder<PersInvocationMethodSvcverStatsPk, PersInvocationMethodSvcverStats>() {
			@Override
			public PersInvocationMethodSvcverStatsPk createPk(InvocationStatsIntervalEnum theInterval, Date theDate) {
				return new PersInvocationMethodSvcverStatsPk(theInterval, theDate, theMethod.getPid());
			}
		};
		doWithStatsByMinute(theConfig, statusSvc, theOperator, builder, start, end);
	}

	
	public static <P extends BasePersStatsPk<P,O>, O extends BasePersStats<P,O>> void doWithStatsByMinute(PersConfig theConfig, IRuntimeStatus statusSvc, IWithStats<P,O> theOperator, IStatsPkBuilder<P,O> thePkBuilder, Date start, Date end) {
		Date date = start;
		for (int min = 0; date.before(end); min++) {

			InvocationStatsIntervalEnum interval = AdminServiceBean.doWithStatsSupportFindInterval(theConfig, date);
			date = interval.truncate(date);

			P pk = thePkBuilder.createPk(interval, date);
			O stats = statusSvc.getInvocationStatsSynchronously(pk);
			theOperator.withStats(min, stats);

			date = AdminServiceBean.doWithStatsSupportIncrement(date, interval);

		}
	}

	private interface IStatsPkBuilder<P extends BasePersStatsPk<P,O>, O extends BasePersStats<P,O>>
	{
		P createPk(InvocationStatsIntervalEnum theInterval, Date theDate);
	}

	
}
