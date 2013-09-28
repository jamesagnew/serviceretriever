package net.svcret.admin.shared.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import net.svcret.admin.shared.enm.AuthorizationHostTypeEnum;

@XmlAccessorType(XmlAccessType.FIELD)
public abstract class BaseDtoAuthenticationHost extends BaseDtoKeepsRecentMessages {

	private static final long serialVersionUID = 1L;

	@XmlElement(name="AutoCreateAuthorizedUsers")
	private boolean myAutoCreateAuthorizedUsers;
	
	@XmlElement(name="CacheSuccessesForMillis")
	private Integer myCacheSuccessesForMillis;
	
	@XmlElement(name="ModuleId")
	private String myModuleId;
	
	@XmlElement(name="ModuleName")
	private String myModuleName;

	@XmlElement(name="SupportsPasswordChange")
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

	public void merge(BaseDtoAuthenticationHost theObject) {
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
