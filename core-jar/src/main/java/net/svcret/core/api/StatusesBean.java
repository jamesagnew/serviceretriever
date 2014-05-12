package net.svcret.core.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.svcret.admin.shared.model.StatusEnum;
import net.svcret.core.model.entity.PersConfig;
import net.svcret.core.model.entity.PersMethodStatus;
import net.svcret.core.model.entity.PersMonitorRuleFiring;
import net.svcret.core.model.entity.PersServiceVersionStatus;
import net.svcret.core.model.entity.PersServiceVersionUrlStatus;

public class StatusesBean {

	private PersConfig myConfig;
	private Map<Long, PersMethodStatus> myMethodPidToStatus = new HashMap<>();
	private Map<Long, PersServiceVersionStatus> myServiceVersionPidToStatus = new HashMap<>();
	private Map<Long, List<PersMonitorRuleFiring>> mySvcVerPidToActiveRuleFirings = new HashMap<>();
	private Map<Long, PersServiceVersionUrlStatus> myUrlPidToStatus = new HashMap<>();
	
	public StatusesBean(PersConfig theConfig) {
		myConfig = theConfig;
	}

	public void addActiveRuleFiring(PersMonitorRuleFiring theFiring) {
		for (Long next : theFiring.getAppliesToServiceVersionPids()) {
			if (!mySvcVerPidToActiveRuleFirings.containsKey(next)) {
				mySvcVerPidToActiveRuleFirings.put(next, new ArrayList<PersMonitorRuleFiring>());
			}
			mySvcVerPidToActiveRuleFirings.get(next).add(theFiring);
		}
	}

	public List<PersMonitorRuleFiring> getFirings(long theSvcVerPid) {
		List<PersMonitorRuleFiring> retVal = mySvcVerPidToActiveRuleFirings.get(theSvcVerPid);
		if (retVal == null) {
			retVal = Collections.emptyList();
		}
		return retVal;
	}

	public Map<Long, PersMethodStatus> getMethodPidToStatus() {
		return myMethodPidToStatus;
	}

	public Map<Long, PersServiceVersionStatus> getServiceVersionPidToStatus() {
		return myServiceVersionPidToStatus;
	}

	public PersServiceVersionStatus getServiceVersionStatus(Long thePid) {
		return myServiceVersionPidToStatus.get(thePid);
	}

	public Map<Long, PersServiceVersionUrlStatus> getUrlPidToStatus() {
		return myUrlPidToStatus;
	}

	public PersServiceVersionUrlStatus getUrlStatusBean(Long thePid) {
		return myUrlPidToStatus.get(thePid);
	}

	public StatusEnum getUrlStatusEnum(Long thePid) {
		PersServiceVersionUrlStatus status = myUrlPidToStatus.get(thePid);
		if (status == null || status.getStatus() == null) {
			return null;
		}

		if (status.getStatus() == StatusEnum.ACTIVE && status.getTimeElapsedSinceLastSuccessOrFault() > myConfig.getDeclareBackingUrlUnknownStatusAfterMillisUnused()) {
			return StatusEnum.UNKNOWN;
		}

		return status.getStatus();
	}

}
