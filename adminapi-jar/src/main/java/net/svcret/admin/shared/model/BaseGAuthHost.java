package net.svcret.admin.shared.model;

public abstract class BaseGAuthHost extends BaseGKeepsRecentMessages<BaseGAuthHost> {

	private static final long serialVersionUID = 1L;

	private boolean myAutoCreateAuthorizedUsers;
	private Integer myCacheSuccessesForMillis;
	private String myModuleId;
	private String myModuleName;

	private boolean mySupportsPasswordChange;
	
	/**
	 * @return the cacheSuccessesForMillis
	 */
	public Integer getCacheSuccessesForMillis() {
		return myCacheSuccessesForMillis;
	}

	/**
	 * @return the moduleId
	 */
	public String getModuleId() {
		return myModuleId;
	}

	/**
	 * @return the moduleName
	 */
	public String getModuleName() {
		return myModuleName;
	}

	public abstract AuthorizationHostTypeEnum getType();

	/**
	 * @return the autoCreateAuthorizedUsers
	 */
	public boolean isAutoCreateAuthorizedUsers() {
		return myAutoCreateAuthorizedUsers;
	}

	public boolean isSupportsPasswordChange() {
		return mySupportsPasswordChange;
	}

	public void merge(BaseGAuthHost theObject) {
		setPid(theObject.getPid());
		setAutoCreateAuthorizedUsers(theObject.isAutoCreateAuthorizedUsers());
		setCacheSuccessesForMillis(theObject.getCacheSuccessesForMillis());
		setModuleId(theObject.getModuleId());
		setModuleName(theObject.getModuleName());
		setSupportsPasswordChange(theObject.isSupportsPasswordChange());
	}

	/**
	 * @param theAutoCreateAuthorizedUsers the autoCreateAuthorizedUsers to set
	 */
	public void setAutoCreateAuthorizedUsers(boolean theAutoCreateAuthorizedUsers) {
		myAutoCreateAuthorizedUsers = theAutoCreateAuthorizedUsers;
	}

	/**
	 * @param theCacheSuccessesForMillis the cacheSuccessesForMillis to set
	 */
	public void setCacheSuccessesForMillis(Integer theCacheSuccessesForMillis) {
		myCacheSuccessesForMillis = theCacheSuccessesForMillis;
	}

	/**
	 * @param theModuleId the moduleId to set
	 */
	public void setModuleId(String theModuleId) {
		myModuleId = theModuleId;
	}

	/**
	 * @param theModuleName the moduleName to set
	 */
	public void setModuleName(String theModuleName) {
		myModuleName = theModuleName;
	}

	/**
	 * @param theSupportsPasswordChange the supportsPasswordChange to set
	 */
	public void setSupportsPasswordChange(boolean theSupportsPasswordChange) {
		mySupportsPasswordChange = theSupportsPasswordChange;
	}

}
