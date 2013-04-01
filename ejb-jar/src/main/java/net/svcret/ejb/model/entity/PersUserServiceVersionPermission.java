package net.svcret.ejb.model.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

@Table(name = "PX_USER_PERM_SVCVER", uniqueConstraints = { // -
@UniqueConstraint(columnNames = { "SVCVER_PID", "USER_PERM_SERVICE_PID" }) } // -
)
@Entity
public class PersUserServiceVersionPermission extends BasePersObject {

	@Column(name = "ALLOW_ALL_SVCVERSION_METHODS")
	private boolean myAllowAllServiceVersionMethods;

	@Version()
	@Column(name = "OPTLOCK")
	private int myOptLock;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PID")
	private Long myPid;

	@ManyToOne(cascade = {}, fetch = FetchType.LAZY)
	@JoinColumn(name = "SVCVER_PID", referencedColumnName = "PID", nullable = false)
	private BasePersServiceVersion myServiceVersion;

	/**
	 * Children
	 */
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	private Collection<PersUserServiceVersionMethodPermission> myServiceVersionMethodPermissions;

	/**
	 * Parent
	 */
	@ManyToOne(cascade = {}, fetch = FetchType.LAZY)
	@JoinColumn(name = "USER_PERM_SERVICE_PID", referencedColumnName = "PID", nullable = false)
	private PersUserServicePermission myServicePermission;

	/**
	 * @param theServicePermissions
	 *            the servicePermissions to set
	 * @return 
	 */
	public PersUserServiceVersionMethodPermission addPermission(PersServiceVersionMethod theMethod) {
		PersUserServiceVersionMethodPermission permission = new PersUserServiceVersionMethodPermission();
		permission.setServiceVersionPermission(this);
		permission.setNewlyCreated(true);
		permission.setServiceVersionMethod(theMethod);

		getServiceVersionMethodPermissions();
		myServiceVersionMethodPermissions.add(permission);
		
		return permission;
	}

	/**
	 * @return the optLock
	 */
	public int getOptLock() {
		return myOptLock;
	}

	/**
	 * @return the pid
	 */
	public Long getPid() {
		return myPid;
	}

	/**
	 * @return the serviceDomain
	 */
	public BasePersServiceVersion getServiceVersion() {
		return myServiceVersion;
	}

	/**
	 * @return the servicePermissions
	 */
	public Collection<PersUserServiceVersionMethodPermission> getServiceVersionMethodPermissions() {
		if (myServiceVersionMethodPermissions == null) {
			myServiceVersionMethodPermissions = new ArrayList<PersUserServiceVersionMethodPermission>();
		}
		return Collections.unmodifiableCollection(myServiceVersionMethodPermissions);
	}

	/**
	 * @return the serviceUser
	 */
	public PersUserServicePermission getServicePermission() {
		return myServicePermission;
	}

	/**
	 * @return the allowAllServices
	 */
	public boolean isAllowAllServiceVersionMethods() {
		return myAllowAllServiceVersionMethods;
	}

	/**
	 * @param theAllowAllServiceVersionMethods
	 *            the allowAllServices to set
	 */
	public void setAllowAllServiceVersionMethods(boolean theAllowAllServiceVersionMethods) {
		myAllowAllServiceVersionMethods = theAllowAllServiceVersionMethods;
	}

	/**
	 * @param theServiceVersion
	 *            the serviceDomain to set
	 */
	public void setServiceVersion(BasePersServiceVersion theServiceVersion) {
		myServiceVersion = theServiceVersion;
	}

	/**
	 * @param theServicePermission
	 *            the domainPermission to set
	 */
	public void setServicePermission(PersUserServicePermission theServicePermission) {
		myServicePermission = theServicePermission;
	}

	Collection<PersServiceVersionMethod> getAllAllowedMethods() {
		ArrayList<PersServiceVersionMethod> retVal = new ArrayList<PersServiceVersionMethod>();

		if (myAllowAllServiceVersionMethods) {
			retVal.addAll(getServiceVersion().getMethods());
		}

		for (PersUserServiceVersionMethodPermission nextMethod : getServiceVersionMethodPermissions()) {
			retVal.add(nextMethod.getServiceVersionMethod());
		}
		return retVal;
	}

}
