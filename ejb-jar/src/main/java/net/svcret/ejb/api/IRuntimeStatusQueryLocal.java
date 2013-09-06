package net.svcret.ejb.api;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Local;

import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.PersServiceVersionUrl;

@Local
public interface IRuntimeStatusQueryLocal {

	void extract60MinuteMethodStats(PersServiceVersionMethod theMethod, List<Integer> theSuccessInvCount, List<Integer> theFaultInvCount, List<Integer> theFailInvCount, List<Long> theTotalInvTime) throws ProcessingException;

	void extract60MinuteServiceVersionUrlStatistics(PersServiceVersionUrl theUrl, ArrayList<Integer> theSuccessCount, ArrayList<Integer> theFaultCount, ArrayList<Integer> theFailCount,
			ArrayList<Long> theInvTime) throws ProcessingException;

}
