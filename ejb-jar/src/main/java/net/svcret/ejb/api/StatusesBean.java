package net.svcret.ejb.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.svcret.ejb.model.entity.PersMonitorRuleFiring;
import net.svcret.ejb.model.entity.PersServiceVersionStatus;
import net.svcret.ejb.model.entity.PersServiceVersionUrlStatus;

public class StatusesBean {

	private Map<Long, PersServiceVersionStatus> myServiceVersionPidToStatus = new HashMap<Long, PersServiceVersionStatus>();
	private Map<Long, PersServiceVersionUrlStatus> myUrlPidToStatus = new HashMap<Long, PersServiceVersionUrlStatus>();
	private Map<Long, List<PersMonitorRuleFiring>> mySvcVerPidToActiveRuleFirings = new HashMap<Long, List<PersMonitorRuleFiring>>();

	public Map<Long, PersServiceVersionStatus> getServiceVersionPidToStatus() {
		return myServiceVersionPidToStatus;
	}

	public Map<Long, PersServiceVersionUrlStatus> getUrlPidToStatus() {
		return myUrlPidToStatus;
	}

	public PersServiceVersionStatus getServiceVersionStatus(Long thePid) {
		return myServiceVersionPidToStatus.get(thePid);
	}

	public PersServiceVersionUrlStatus getUrlStatus(Long thePid) {
		return myUrlPidToStatus.get(thePid);
	}

	public List<PersMonitorRuleFiring> getFirings(long theSvcVerPid) {
		List<PersMonitorRuleFiring> retVal = mySvcVerPidToActiveRuleFirings.get(theSvcVerPid);
		if (retVal==null) {
			retVal=Collections.emptyList();
		}
		return retVal;
	}
	
	public void addActiveRuleFiring(PersMonitorRuleFiring theFiring) {
		for (Long next : theFiring.getAppliesToServiceVersionPids()) {
			if (!mySvcVerPidToActiveRuleFirings.containsKey(next)) {
				mySvcVerPidToActiveRuleFirings.put(next, new ArrayList<PersMonitorRuleFiring>());
			}
			mySvcVerPidToActiveRuleFirings.get(next).add(theFiring);
		}
	}

}