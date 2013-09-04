package net.svcret.ejb.api;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Local;

import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.PersServiceVersionUrl;

@Local
public interface IRuntimeStatusQueryLocal {

	void extract60MinuteMethodStats(PersServiceVersionMethod thMMethod, List<Integer> theInvCount, List<Long> theInvTime) throws ProcessingException;

	void extract60MinuteServiceVersionUrlStatistics(PersServiceVersionUrl theUrl, ArrayList<Integer> theSuccessCount, ArrayList<Integer> theFaultCount, ArrayList<Integer> theFailCount,
			ArrayList<Long> theInvTime);

}
