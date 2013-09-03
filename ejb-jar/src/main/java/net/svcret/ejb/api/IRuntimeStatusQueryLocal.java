package net.svcret.ejb.api;

import java.util.List;

import javax.ejb.Local;

import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;

@Local
public interface IRuntimeStatusQueryLocal {

	void extract60MinuteMethodStats(PersServiceVersionMethod thMMethod, List<Integer> theInvCount, List<Long> theInvTime) throws ProcessingException;

}
