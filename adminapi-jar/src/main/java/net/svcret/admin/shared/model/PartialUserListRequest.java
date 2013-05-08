package net.svcret.admin.shared.model;

import java.io.Serializable;

public class PartialUserListRequest implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private boolean myLoadStats;

	/**
	 * @return the loadStats
	 */
	public boolean isLoadStats() {
		return myLoadStats;
	}

	/**
	 * @param theLoadStats the loadStats to set
	 */
	public void setLoadStats(boolean theLoadStats) {
		myLoadStats = theLoadStats;
	}

	@Override
	public String toString() {
		// We can add a better description when there are params here
		return "Complete";
	}

}
