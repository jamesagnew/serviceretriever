package net.svcret.admin.shared.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GConfig implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<String> myProxyUrlBases;
	private Integer myTruncateRecentDatabaseTransactionsToBytes;

	/**
	 * @return the proxyUrlBase
	 */
	public List<String> getProxyUrlBases() {
		if (myProxyUrlBases == null) {
			myProxyUrlBases = new ArrayList<String>();
		}
		return myProxyUrlBases;
	}

	public Integer getTruncateRecentDatabaseTransactionsToBytes() {
		return myTruncateRecentDatabaseTransactionsToBytes;
	}

	public void setTruncateRecentDatabaseTransactionsToBytes(Integer theTruncateRecentDatabaseTransactionsToBytes) {
		myTruncateRecentDatabaseTransactionsToBytes=theTruncateRecentDatabaseTransactionsToBytes;
	}

}
