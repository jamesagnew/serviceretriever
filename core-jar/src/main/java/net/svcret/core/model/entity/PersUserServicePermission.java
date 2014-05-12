package net.svcret.core.model.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

@Table(name = "PX_USER_PERM_SERVICE", uniqueConstraints = { // -
@UniqueConstraint(columnNames = { "SERVICE_PID", "USER_PERM_DOMAIN_PID" }) } // -
)
@Entity
public class PersUserServicePermission extends BasePersObject {

	private static final long serialVersionUID = 1L;

	@Column(name = "ALLOW_ALL_SVCVERSIONS")
	private boolean myAllowAllServiceVersions;

	@ManyToOne(cascade = {}, fetch = FetchType.LAZY)
	@JoinColumn(name = "USER_PERM_DOMAIN_PID", referencedColumnName = "PID", nullable = false)
	private PersUserDomainPermission myDomainPermission;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PID")
	private Long myPid;

	@ManyToOne(cascade = {}, fetch = FetchType.LAZY)
	@JoinColumn(name = "SERVICE_PID", referencedColumnName = "PID", nullable = false)
	private PersService myService;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "myServicePermission")
	private Collection<PersUserServiceVersionPermission> myServiceVersionPermissions;

	@Transient
	private transient Map<Long, PersUserServiceVersionPermission> myServiceVersionPidToServiceVersionPermission;

	/**
	 * @param theServicePermissions
	 *            the servicePermissions to set
	 * @return
	 */
	public PersUserServiceVersionPermission addPermission(BasePersServiceVersion theVersion) {
		PersUserServiceVersionPermission permission = new PersUserServiceVersionPermission();
		permission.setServicePermission(this);
		permission.setNewlyCreated(true);
		permission.setServiceVersion(theVersion);

		getServiceVersionPermissions();
		myServiceVersionPermissions.add(permission);

		return permission;
	}

	public void addServiceVersionPermission(PersUserServiceVersionPermission thePerm) {
		getServiceVersionPermissions();
		myServiceVersionPermissions.add(thePerm);
		thePerm.setServicePermission(this);
	}

	public Collection<PersMethod> getAllAllowedMethods() {
		ArrayList<PersMethod> retVal = new ArrayList<PersMethod>();

		if (myAllowAllServiceVersions) {
			retVal.addAll(getService().getAllServiceVersionMethods());
		}

		for (PersUserServiceVersionPermission nextService : getServiceVersionPermissions()) {
			retVal.addAll(nextService.getAllAllowedMethods());
		}
		return retVal;
	}

	/**
	 * @return the serviceUser
	 */
	public PersUserDomainPermission getDomainPermission() {
		return myDomainPermission;
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
	public PersService getService() {
		return myService;
	}

	/**
	 * @return the servicePermissions
	 */
	public Collection<PersUserServiceVersionPermission> getServiceVersionPermissions() {
		if (myServiceVersionPermissions == null) {
			myServiceVersionPermissions = new ArrayList<PersUserServiceVersionPermission>();
		}
		return Collections.unmodifiableCollection(myServiceVersionPermissions);
	}

	/**
	 * @return the allowAllServices
	 */
	public boolean isAllowAllServiceVersions() {
		return myAllowAllServiceVersions;
	}

	public void loadAllAssociations() {
		myServiceVersionPidToServiceVersionPermission = new HashMap<Long, PersUserServiceVersionPermission>();
		for (PersUserServiceVersionPermission nextPerm : getServiceVersionPermissions()) {
			myServiceVersionPidToServiceVersionPermission.put(nextPerm.getServiceVersion().getPid(), nextPerm);
			nextPerm.loadAllAssociations();
		}

	}

	public void removePermission(BasePersServiceVersion permission) {
		getServiceVersionPermissions();
		myServiceVersionPermissions.remove(permission);

	}

	/**
	 * @param theAllowAllServiceVersions
	 *            the allowAllServices to set
	 */
	public void setAllowAllServiceVersions(boolean theAllowAllServiceVersions) {
		myAllowAllServiceVersions = theAllowAllServiceVersions;
	}

	/**
	 * @param theDomainPermission
	 *            the domainPermission to set
	 */
	public void setDomainPermission(PersUserDomainPermission theDomainPermission) {
		myDomainPermission = theDomainPermission;
	}

	/**
	 * @param thePid
	 *            the pid to set
	 */
	public void setPid(Long thePid) {
		myPid = thePid;
	}

	/**
	 * @param theService
	 *            the serviceDomain to set
	 */
	public void setService(PersService theService) {
		myService = theService;
	}

	/**
	 * @param theServiceVersionPermissions
	 *            the serviceVersionPermissions to set
	 */
	public void setServiceVersionPermissions(Collection<PersUserServiceVersionPermission> theServiceVersionPermissions) {
		myServiceVersionPermissions = theServiceVersionPermissions;
		for (PersUserServiceVersionPermission next : theServiceVersionPermissions) {
			next.setServicePermission(this);
		}
	}

	public Map<Long, PersUserServiceVersionPermission> getServiceVersionPidToServiceVersionPermission() {
		return myServiceVersionPidToServiceVersionPermission;
	}

}
