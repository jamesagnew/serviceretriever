package net.svcret.core.ejb;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.svcret.core.model.entity.BasePersAuthenticationHost;
import net.svcret.core.model.entity.PersUser;

public class InMemoryUserCatalog {
	private final Map<Long, Map<String, PersUser>> myAuthHostToUsernameToUser;
	private final Map<Long, Map<Long, PersUser>> myAuthHostToUserPidToUser;
	private final Map<Long, BasePersAuthenticationHost> myPidToAuthHost;

//	public InMemoryUserCatalog() {
//		this(new HashMap<Long, Map<String, PersUser>>(), new HashMap<Long, BasePersAuthenticationHost>());
//	}

	public InMemoryUserCatalog(Map<Long, Map<String, PersUser>> theAuthHostToUsernameToUser, Map<Long, BasePersAuthenticationHost> thePidToAuthHost) {
		super();
		myAuthHostToUsernameToUser = theAuthHostToUsernameToUser;
		myPidToAuthHost = thePidToAuthHost;

		myAuthHostToUserPidToUser = new HashMap<>();
		for (Entry<Long, Map<String, PersUser>> nextHostEntry : theAuthHostToUsernameToUser.entrySet()) {
			HashMap<Long, PersUser> nextHostMap = new HashMap<>();
			myAuthHostToUserPidToUser.put(nextHostEntry.getKey(), nextHostMap);
			for (PersUser nextUser : nextHostEntry.getValue().values()) {
				nextHostMap.put(nextUser.getPid(), nextUser);
			}
		}
	}

	public PersUser findUser(Long theAuthHostPid, String theUsername) {
		Map<String, PersUser> authHostUsers = myAuthHostToUsernameToUser.get(theAuthHostPid);
		if (authHostUsers == null) {
			return null;
		} else {
			return authHostUsers.get(theUsername);
		}
	}

	public BasePersAuthenticationHost getAuthHostByPid(Long thePid) {
		return myPidToAuthHost.get(thePid);
	}

	public PersUser getUser(Long theAuthHostPid, Long theUserPid) {
		Map<Long, PersUser> authHostUsers = myAuthHostToUserPidToUser.get(theAuthHostPid);
		if (authHostUsers == null) {
			return null;
		} else {
			return authHostUsers.get(theUserPid);
		}
	}


}
