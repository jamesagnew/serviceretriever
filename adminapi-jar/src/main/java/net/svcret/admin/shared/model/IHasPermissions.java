package net.svcret.admin.shared.model;

import java.util.List;
import java.util.Set;

public interface IHasPermissions {

	/**
	 * @return the domainPermissions
	 */
	 List<GUserDomainPermission> getDomainPermissions();

	/**
	 * @param theDomainPermissions the domainPermissions to set
	 */
	 void setDomainPermissions(List<GUserDomainPermission> theDomainPermissions);

	/**
	 * @return the globalPermissions
	 */
	 Set<UserGlobalPermissionEnum> getGlobalPermissions();

	/**
	 * @return the allowAllDomains
	 */
	 boolean isAllowAllDomains();

	/**
	 * @param theAllowAllDomains the allowAllDomains to set
	 */
	 void setAllowAllDomains(boolean theAllowAllDomains);

	/**
	 * @param theGlobalPermissions
	 *            the globalPermissions to set
	 */
	 void setGlobalPermissions(Set<UserGlobalPermissionEnum> theGlobalPermissions);

	 /**
	  * Returns null if none
	  */
	 GUserDomainPermission getOrCreateDomainPermission(long theDomainPid);

}