package net.svcret.ejb.ejb;

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
import net.svcret.ejb.model.entity.BasePersMethodInvocationStats;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;

@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class RuntimeStatusQueryBean implements IRuntimeStatusQueryLocal {

	@EJB
	private IRuntimeStatus myStatusSvc;
	
	@EJB
	private IConfigService myConfigSvc;
	
	/**
	 */
	private void extractSuccessfulInvocationInvocationTimes(int theNumMinsBack, final List<Integer> the60MinInvCount, final List<Long> the60minTime,
			PersServiceVersionMethod nextMethod) throws ProcessingException {
		AdminServiceBean.doWithStatsByMinute(myConfigSvc.getConfig(), theNumMinsBack, myStatusSvc, nextMethod, new IWithStats() {
			@Override
			public void withStats(int theIndex, BasePersMethodInvocationStats theStats) {
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

	
}
