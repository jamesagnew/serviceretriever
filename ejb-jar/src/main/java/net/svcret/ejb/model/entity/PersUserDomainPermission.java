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

@Table(name = "PX_USER_PERM_DOMAIN", uniqueConstraints = { // -
@UniqueConstraint(columnNames = { "SVC_DOMAIN_PID", "USER_PID" }) } // -
)
@Entity
public class PersUserDomainPermission extends BasePersObject {

	@Column(name = "ALLOW_ALL_SVCS")
	private boolean myAllowAllServices;

	@Version()
	@Column(name = "OPTLOCK")
	private int myOptLock;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PID")
	private Long myPid;

	@ManyToOne(cascade = {}, fetch = FetchType.LAZY)
	@JoinColumn(name = "SVC_DOMAIN_PID", referencedColumnName = "PID", nullable = false)
	private PersDomain myServiceDomain;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	private Collection<PersUserServicePermission> myServicePermissions;

	@ManyToOne(cascade = {}, fetch = FetchType.LAZY)
	@JoinColumn(name = "USER_PID", referencedColumnName = "PID", nullable = false)
	private PersUser myServiceUser;

	/**
	 * @param theServicePermissions
	 *            the servicePermissions to set
	 * @return 
	 */
	public PersUserServicePermission addPermission(PersService theService) {
		PersUserServicePermission permission = new PersUserServicePermission();
		permission.setService(theService);
		permission.setNewlyCreated(true);
		permission.setDomainPermission(this);

		getServicePermissions();
		myServicePermissions.add(permission);
		
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
	public PersDomain getServiceDomain() {
		return myServiceDomain;
	}

	/**
	 * @return the servicePermissions
	 */
	public Collection<PersUserServicePermission> getServicePermissions() {
		if (myServicePermissions == null) {
			myServicePermissions = new ArrayList<PersUserServicePermission>();
		}
		return Collections.unmodifiableCollection(myServicePermissions);
	}

	/**
	 * @return the serviceUser
	 */
	public PersUser getServiceUser() {
		return myServiceUser;
	}

	/**
	 * @return the allowAllServices
	 */
	public boolean isAllowAllServices() {
		return myAllowAllServices;
	}

	/**
	 * @param theAllowAllServices
	 *            the allowAllServices to set
	 */
	public void setAllowAllServices(boolean theAllowAllServices) {
		myAllowAllServices = theAllowAllServices;
	}

	/**
	 * @param theServiceDomain
	 *            the serviceDomain to set
	 */
	public void setServiceDomain(PersDomain theServiceDomain) {
		myServiceDomain = theServiceDomain;
	}

	/**
	 * @param theServiceUser
	 *            the serviceUser to set
	 */
	public void setServiceUser(PersUser theServiceUser) {
		myServiceUser = theServiceUser;
	}

	public Collection<PersServiceVersionMethod> getAllAllowedMethods() {
		ArrayList<PersServiceVersionMethod> retVal = new ArrayList<PersServiceVersionMethod>();

		if (myAllowAllServices) {
			retVal.addAll(getServiceDomain().getAllServiceVersionMethods());
		}
		
		for (PersUserServicePermission nextService : getServicePermissions()) {
			retVal.addAll(nextService.getAllAllowedMethods());
		}
		
		return retVal;
	}

}
