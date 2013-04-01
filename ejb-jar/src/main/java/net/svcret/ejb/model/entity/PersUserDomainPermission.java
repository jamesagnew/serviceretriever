package net.svcret.ejb.model.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Version;

@Table(name = "PX_SVC_USER_PERM")
@Entity
public class PersServiceUserPermission extends BasePersObject {

	@Column(name = "ALL_ENVS", nullable = false)
	private boolean myAllEnvironments;

	@Column(name = "ALL_SVCS", nullable = false)
	private boolean myAllServices;

	@ManyToMany(cascade = {})
	@JoinTable(name = "PX_SVC_USER_PERM_ENV", joinColumns = @JoinColumn(name = "SVC_USER_PERM2_PID", referencedColumnName = "PID"), inverseJoinColumns = @JoinColumn(name = "ENV_PID", referencedColumnName = "PID"))
	@OrderBy("ENV")
	private List<PersEnvironment> myEnvironments;

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

	@OneToMany(cascade = CascadeType.ALL)
	private Collection<PersServiceUserServicePermission> myServicePermissions;

	@ManyToOne(cascade = {}, fetch = FetchType.LAZY)
	@JoinColumn(name = "SVC_USER_PID", referencedColumnName = "PID", nullable = false)
	private PersServiceUser myServiceUser;

	/**
	 * @return the environments
	 */
	public List<PersEnvironment> getEnvironments() {
		if (myEnvironments == null) {
			myEnvironments = new ArrayList<PersEnvironment>();
		}
		return myEnvironments;
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
	public Collection<PersServiceUserServicePermission> getServicePermissions() {
		if (myServicePermissions == null) {
			myServicePermissions = new ArrayList<PersServiceUserServicePermission>();
		}
		return myServicePermissions;
	}

	/**
	 * @return the serviceUser
	 */
	public PersServiceUser getServiceUser() {
		return myServiceUser;
	}

	/**
	 * @return the allEnvironments
	 */
	public boolean isAllEnvironments() {
		return myAllEnvironments;
	}

	/**
	 * @return the allServices
	 */
	public boolean isAllServices() {
		return myAllServices;
	}

	/**
	 * @param theAllEnvironments
	 *            the allEnvironments to set
	 */
	public void setAllEnvironments(boolean theAllEnvironments) {
		myAllEnvironments = theAllEnvironments;
	}

	/**
	 * @param theAllServices
	 *            the allServices to set
	 */
	public void setAllServices(boolean theAllServices) {
		myAllServices = theAllServices;
	}

	/**
	 * @param theOptLock
	 *            the optLock to set
	 */
	public void setOptLock(int theOptLock) {
		myOptLock = theOptLock;
	}

	/**
	 * @param thePid
	 *            the pid to set
	 */
	public void setPid(Long thePid) {
		myPid = thePid;
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
	public void setServiceUser(PersServiceUser theServiceUser) {
		myServiceUser = theServiceUser;
	}

	public void addEnvironment(PersEnvironment theEnv) {
		getEnvironments().add(theEnv);
	}

	public void removeEnvironment(PersEnvironment theEnv) {
		getEnvironments().remove(theEnv);
	}

	public void loadAllAssociations() {
		for (PersEnvironment next : myEnvironments) {
			next.loadAllAssociations();
		}
		for (PersServiceUserServicePermission next : myServicePermissions) {
			next.loadAllAssociations();
		}
	}

}
