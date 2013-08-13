package net.svcret.ejb.api;

import java.util.HashMap;
import java.util.Map;

import net.svcret.ejb.model.entity.PersServiceVersionStatus;
import net.svcret.ejb.model.entity.PersServiceVersionUrlStatus;

public class StatusesBean {

	private Map<Long, PersServiceVersionStatus> myServiceVersionPidToStatus = new HashMap<Long, PersServiceVersionStatus>();
	private Map<Long, PersServiceVersionUrlStatus> myUrlPidToStatus = new HashMap<Long, PersServiceVersionUrlStatus>();

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

}
