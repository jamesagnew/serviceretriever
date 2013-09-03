package net.svcret.admin.shared.model;

import java.util.Date;
import java.util.Set;

import net.svcret.admin.shared.enm.ServerSecurityModeEnum;

public abstract class BaseGServiceVersion extends BaseDtoServiceCatalogItem implements IProvidesUrlCount {

	private static final long serialVersionUID = 7886801527330335503L;

	private boolean myActive;
	private BaseGClientSecurityList myClientSecurityList;
	private String myDefaultProxyPath;
	private String myDescription;
	private GServiceVersionDetailedStats myDetailedStats;
	private String myExplicitProxyPath;
	private long myHttpClientConfigPid;
	private Date myLastAccess;
	private Set<String> myObscureRequestElementsInLog;
	private Set<String> myObscureResponseElementsInLog;
	private String myParentServiceName;
	private long myParentServicePid;
	private GServiceVersionResourcePointerList myResourcePointerList;
	private BaseGServerSecurityList myServerSecurityList;
	private ServerSecurityModeEnum myServerSecurityMode;
	private GServiceMethodList myServiceMethodList;
	private GServiceVersionUrlList myServiceUrlList;
	private boolean myUseDefaultProxyPath = true;

	public BaseGServiceVersion() {
		myServiceMethodList = new GServiceMethodList();
		myServiceUrlList = new GServiceVersionUrlList();
		myServerSecurityList = new BaseGServerSecurityList();
		myClientSecurityList = new BaseGClientSecurityList();
		myResourcePointerList = new GServiceVersionResourcePointerList();
	}

	/**
	 * @return the clientSecurityList
	 */
	public BaseGClientSecurityList getClientSecurityList() {
		return myClientSecurityList;
	}

	/**
	 * @return the proxyPath
	 */
	public String getDefaultProxyPath() {
		return myDefaultProxyPath;
	}

	public String getDescription() {
		return myDescription;
	}

	/**
	 * @return the detailedStats
	 */
	public GServiceVersionDetailedStats getDetailedStats() {
		return myDetailedStats;
	}

	public String getExplicitProxyPath() {
		return myExplicitProxyPath;
	}

	/**
	 * @return the httpClientConfigPid
	 */
	public long getHttpClientConfigPid() {
		return myHttpClientConfigPid;
	}

	/**
	 * @return the lastAccess
	 */
	public Date getLastAccess() {
		return myLastAccess;
	}

	/**
	 * @return the serviceUrlList
	 */
	public GServiceMethodList getMethodList() {
		return myServiceMethodList;
	}

	public Set<String> getObscureRequestElementsInLog() {
		return myObscureRequestElementsInLog;
	}

	public Set<String> getObscureResponseElementsInLog() {
		return myObscureResponseElementsInLog;
	}

	public String getParentServiceName() {
		return myParentServiceName;
	}

	public long getParentServicePid() {
		return myParentServicePid;
	}

	public abstract ServiceProtocolEnum getProtocol();

	/**
	 * @return the resourcePointerList
	 */
	public GServiceVersionResourcePointerList getResourcePointerList() {
		return myResourcePointerList;
	}

	/**
	 * @return the serverSecurityList
	 */
	public BaseGServerSecurityList getServerSecurityList() {
		return myServerSecurityList;
	}

	public ServerSecurityModeEnum getServerSecurityMode() {
		return myServerSecurityMode;
	}

	public GServiceVersionUrlList getUrlList() {
		return myServiceUrlList;
	}

	public boolean hasMethodWithName(String theName) {
		for (GServiceMethod next : myServiceMethodList) {
			if (next.getName().equals(theName)) {
				return true;
			}
		}
		return false;
	}

	public boolean hasUrlWithName(String theUrl) {
		for (GServiceVersionUrl next : myServiceUrlList) {
			if (next.getUrl().equals(theUrl)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean hideDashboardRowWhenExpanded() {
		return false;
	}

	/**
	 * @return the active
	 */
	public boolean isActive() {
		return myActive;
	}

	public boolean isSecure() {
		boolean secure = myServerSecurityMode.isSecure();
		boolean hasModules = myServerSecurityList.size() > 0;
		return secure && hasModules;
	}

	public boolean isUseDefaultProxyPath() {
		return myUseDefaultProxyPath;
	}

	@Override
	public void merge(BaseGObject theObject) {
		super.merge(theObject);

		BaseGServiceVersion obj=(BaseGServiceVersion) theObject;
		myActive = obj.myActive;
		myLastAccess = obj.myLastAccess;
		myServerSecurityMode = obj.myServerSecurityMode;

		if (obj.getMethodList() != null) {
			getMethodList().mergeResults(obj.getMethodList());
		}

		if (obj.getUrlList() != null) {
			getUrlList().mergeResults(obj.getUrlList());
		}

		if (obj.getServerSecurityList() != null) {
			getServerSecurityList().mergeResults(obj.getServerSecurityList());
		}

		if (obj.getClientSecurityList() != null) {
			getClientSecurityList().mergeResults(obj.getClientSecurityList());
		}

		if (obj.getResourcePointerList() != null) {
			getResourcePointerList().mergeResults(obj.getResourcePointerList());
		}

	}

	/**
	 * @param theActive
	 *            the active to set
	 */
	public void setActive(boolean theActive) {
		myActive = theActive;
	}

	public void setDefaultProxyPath(String theProxyPath) {
		myDefaultProxyPath = theProxyPath;
	}

	public void setDescription(String theDescription) {
		myDescription = theDescription;
	}

	public void setDetailedStats(GServiceVersionDetailedStats theResult) {
		myDetailedStats = theResult;
	}

	public void setExplicitProxyPath(String theExplicitProxyPath) {
		myExplicitProxyPath = theExplicitProxyPath;
	}

	/**
	 * @param theHttpClientConfigPid
	 *            the httpClientConfigPid to set
	 */
	public void setHttpClientConfigPid(long theHttpClientConfigPid) {
		myHttpClientConfigPid = theHttpClientConfigPid;
	}

	/**
	 * @param theLastAccess
	 *            the lastAccess to set
	 */
	public void setLastAccess(Date theLastAccess) {
		myLastAccess = theLastAccess;
	}

	public void setObscureRequestElementsInLog(Set<String> theObscureRequestElementsInLog) {
		myObscureRequestElementsInLog = theObscureRequestElementsInLog;
	}

	public void setObscureResponseElementsInLog(Set<String> theObscureResponseElementsInLog) {
		myObscureResponseElementsInLog = theObscureResponseElementsInLog;
	}

	public void setParentServiceName(String theParentServiceName) {
		myParentServiceName = theParentServiceName;
	}

	public void setParentServicePid(long theParentServicePid) {
		myParentServicePid = theParentServicePid;
	}

	/**
	 * @param theResourcePointerList
	 *            the resourcePointerList to set
	 */
	public void setResourcePointerList(GServiceVersionResourcePointerList theResourcePointerList) {
		myResourcePointerList = theResourcePointerList;
	}

	public void setServerSecurityMode(ServerSecurityModeEnum theServerSecurityMode) {
		myServerSecurityMode = theServerSecurityMode;
	}

	public void setUseDefaultProxyPath(boolean theDefaultProxyPath) {
		myUseDefaultProxyPath = theDefaultProxyPath;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("ServiceVersion[type=").append(getProtocol().name()).append(", ");
		b.append("pid=").append(getPid()).append(", ");
		b.append("methodCound=").append(getMethodList().size());
		b.append("]");
		return b.toString();
	}

}
