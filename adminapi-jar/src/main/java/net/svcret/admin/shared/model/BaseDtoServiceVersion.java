package net.svcret.admin.shared.model;

import java.util.Date;

import javax.xml.bind.annotation.XmlElement;

import net.svcret.admin.shared.enm.ServerSecurityModeEnum;

public abstract class BaseDtoServiceVersion extends BaseDtoServiceCatalogItem implements IProvidesUrlCount {

	private static final long serialVersionUID = 7886801527330335503L;

	@XmlElement(name="config_Active")
	private boolean myActive;
	
	@XmlElement(name="config_ClientSecurity")
	private DtoClientSecurityList myClientSecurityList;

	@XmlElement(name="runtime_DefaultProxyPath")
	private String myDefaultProxyPath;

	@XmlElement(name="config_Description")
	private String myDescription;

	@XmlElement(name="runtime_DetailedStats")
	private GServiceVersionDetailedStats myDetailedStats;
	
	@XmlElement(name="config_ExplicitProxyPath")
	private String myExplicitProxyPath;
	
	@XmlElement(name="config_HttpClientConfigId")
	private String myHttpClientConfigId;
	
	@XmlElement(name="config_HttpClientConfigPid")
	private long myHttpClientConfigPid;
	
	@XmlElement(name="runtime_LastAccess")	
	private Date myLastAccess;

	@XmlElement(name="runtime_ParentServiceName")
	private String myParentServiceName;
	
	@XmlElement(name="runtime_ParentServicePid")
	private long myParentServicePid;
	
	@XmlElement(name="config_ServiceVersionResourcePointers")
	private GServiceVersionResourcePointerList myResourcePointerList;
	
	@XmlElement(name="config_ServerSecurity")
	private DtoServerSecurityList myServerSecurityList;
	
	@XmlElement(name="config_ServerSecurityMode")
	private ServerSecurityModeEnum myServerSecurityMode;
	
	@XmlElement(name="config_Methods")
	private GServiceMethodList myServiceMethodList;

	@XmlElement(name="config_Urls")
	private GServiceVersionUrlList myServiceUrlList;

	@XmlElement(name="config_UseDefaultProxyPath")
	private boolean myUseDefaultProxyPath = true;

	public BaseDtoServiceVersion() {
		myServiceMethodList = new GServiceMethodList();
		myServiceUrlList = new GServiceVersionUrlList();
		myServerSecurityList = new DtoServerSecurityList();
		myClientSecurityList = new DtoClientSecurityList();
		myResourcePointerList = new GServiceVersionResourcePointerList();
	}

	/**
	 * @return the clientSecurityList
	 */
	public DtoClientSecurityList getClientSecurityList() {
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

	public String getHttpClientConfigId() {
		return myHttpClientConfigId;
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

//	public Set<String> getObscureRequestElementsInLog() {
//		return myObscureRequestElementsInLog;
//	}
//
//	public Set<String> getObscureResponseElementsInLog() {
//		return myObscureResponseElementsInLog;
//	}

	/**
	 * @return the serviceUrlList
	 */
	public GServiceMethodList getMethodList() {
		return myServiceMethodList;
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
	public DtoServerSecurityList getServerSecurityList() {
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
	public void merge(BaseDtoObject theObject) {
		super.merge(theObject);

		BaseDtoServiceVersion obj=(BaseDtoServiceVersion) theObject;
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

	public void setHttpClientConfigId(String theId) {
		myHttpClientConfigId=theId;
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
