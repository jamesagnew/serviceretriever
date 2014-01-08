package net.svcret.ejb.model.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
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

import org.apache.commons.lang3.Validate;

@Table(name = "PX_USER_PERM_SVCVER", uniqueConstraints = { // -
@UniqueConstraint(columnNames = { "SVCVER_PID", "USER_PERM_SERVICE_PID" }) } // -
)
@Entity
public class PersUserServiceVersionPermission extends BasePersObject {

	private static final long serialVersionUID = 1L;

	@Column(name = "ALLOW_ALL_SVCVERSION_METHODS")
	private boolean myAllowAllServiceVersionMethods;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PID")
	private Long myPid;

	/**
	 * Parent
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "USER_PERM_SERVICE_PID", referencedColumnName = "PID", nullable = false)
	private PersUserServicePermission myServicePermission;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "SVCVER_PID", referencedColumnName = "PID", nullable = false)
	private BasePersServiceVersion myServiceVersion;

	/**
	 * Children
	 */
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "myServiceVersionPermission")
	private Collection<PersUserServiceVersionMethodPermission> myServiceVersionMethodPermissions;

	@Transient
	private transient Map<Long, PersUserServiceVersionMethodPermission> myServiceVersionMethodPidToServiceVersionMethodPermission;

	/**
	 * @param theServicePermissions
	 *            the servicePermissions to set
	 * @return
	 */
	public PersUserServiceVersionMethodPermission addPermission(PersMethod theMethod) {
		PersUserServiceVersionMethodPermission permission = new PersUserServiceVersionMethodPermission();
		permission.setServiceVersionPermission(this);
		permission.setNewlyCreated(true);
		permission.setServiceVersionMethod(theMethod);

		getServiceVersionMethodPermissions();
		myServiceVersionMethodPermissions.add(permission);

		return permission;
	}

	public void addServiceVersionMethodPermissions(PersUserServiceVersionMethodPermission thePerm) {
		getServiceVersionMethodPermissions();
		myServiceVersionMethodPermissions.add(thePerm);
		thePerm.setServiceVersionPermission(this);
	}

	/**
	 * @return the pid
	 */
	public Long getPid() {
		return myPid;
	}

	/**
	 * @return the serviceUser
	 */
	public PersUserServicePermission getServicePermission() {
		return myServicePermission;
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
	 * @return the allowAllServices
	 */
	public boolean isAllowAllServiceVersionMethods() {
		return myAllowAllServiceVersionMethods;
	}

	public void loadAllAssociations() {
		myServiceVersionMethodPidToServiceVersionMethodPermission = new HashMap<Long, PersUserServiceVersionMethodPermission>();
		for (PersUserServiceVersionMethodPermission nextPerm : getServiceVersionMethodPermissions()) {
			myServiceVersionMethodPidToServiceVersionMethodPermission.put(nextPerm.getServiceVersionMethod().getPid(), nextPerm);
			nextPerm.loadAllAssociations();
		}

	}

	public void removePermission(PersMethod theMethod) {
		Validate.notNull(theMethod);

		getServiceVersionMethodPermissions();
		for (Iterator<PersUserServiceVersionMethodPermission> iter = myServiceVersionMethodPermissions.iterator(); iter.hasNext();) {
			if (iter.next().getServiceVersionMethod().equals(theMethod)) {
				iter.remove();
			}
		}

	}

	/**
	 * @param theAllowAllServiceVersionMethods
	 *            the allowAllServices to set
	 */
	public void setAllowAllServiceVersionMethods(boolean theAllowAllServiceVersionMethods) {
		myAllowAllServiceVersionMethods = theAllowAllServiceVersionMethods;
	}

	/**
	 * @param thePid
	 *            the pid to set
	 */
	public void setPid(Long thePid) {
		myPid = thePid;
	}

	/**
	 * @param theServicePermission
	 *            the domainPermission to set
	 */
	public void setServicePermission(PersUserServicePermission theServicePermission) {
		myServicePermission = theServicePermission;
	}

	/**
	 * @param theServiceVersion
	 *            the serviceDomain to set
	 */
	public void setServiceVersion(BasePersServiceVersion theServiceVersion) {
		myServiceVersion = theServiceVersion;
	}

	/**
	 * @param theServiceVersionMethodPermissions
	 *            the serviceVersionMethodPermissions to set
	 */
	public void setServiceVersionMethodPermissions(Collection<PersUserServiceVersionMethodPermission> theServiceVersionMethodPermissions) {
		myServiceVersionMethodPermissions = theServiceVersionMethodPermissions;
		for (PersUserServiceVersionMethodPermission next : theServiceVersionMethodPermissions) {
			next.setServiceVersionPermission(this);
		}
	}

	Collection<PersMethod> getAllAllowedMethods() {
		ArrayList<PersMethod> retVal = new ArrayList<PersMethod>();

		if (myAllowAllServiceVersionMethods) {
			retVal.addAll(getServiceVersion().getMethods());
		}

		for (PersUserServiceVersionMethodPermission nextMethod : getServiceVersionMethodPermissions()) {
			retVal.add(nextMethod.getServiceVersionMethod());
		}
		return retVal;
	}

	public Map<Long, PersUserServiceVersionMethodPermission> getMethodPidToMethodPermission() {
		return myServiceVersionMethodPidToServiceVersionMethodPermission;
	}

}
