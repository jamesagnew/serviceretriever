package net.svcret.ejb.model.entity;

import java.util.ArrayList;
import java.util.Collection;

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
import javax.persistence.Table;
import javax.persistence.Version;

import net.svcret.ejb.model.entity.soap.PersServiceVersionSoap11;


@Table(name = "PX_SVC_USER_SVC_PERM")
@Entity
public class PersServiceUserServicePermission extends BasePersObject {

	@Version()
	@Column(name = "OPTLOCK")
	private int myOptLock;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PID")
	private Long myPid;

	@ManyToOne(cascade = {}, fetch = FetchType.LAZY)
	@JoinColumn(name = "SVC_PID", referencedColumnName = "PID", nullable = false)
	private PersService myService;

	@ManyToOne(cascade = {}, fetch = FetchType.LAZY)
	@JoinColumn(name = "SVC_USER_PERM_PID", referencedColumnName = "PID", nullable = false)
	private PersServiceUserPermission mySvcUserPermission;

	@ManyToMany(cascade = {}, fetch = FetchType.LAZY)
	@JoinTable(joinColumns = { @JoinColumn(name = "SVC_USER_SVC_PERM_PID") }, inverseJoinColumns = { @JoinColumn(name = "SVC_VER_PID") })
	private Collection<PersServiceVersionSoap11> myVersions;

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
	 * @return the service
	 */
	public PersService getService() {
		return myService;
	}

	/**
	 * @return the svcUserPermission
	 */
	public PersServiceUserPermission getSvcUserPermission() {
		return mySvcUserPermission;
	}

	/**
	 * @return the versions
	 */
	public Collection<PersServiceVersionSoap11> getVersions() {
		if (myVersions == null) {
			myVersions = new ArrayList<PersServiceVersionSoap11>();
		}
		return myVersions;
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
	 * @param theService
	 *            the service to set
	 */
	public void setService(PersService theService) {
		myService = theService;
	}

	/**
	 * @param theSvcUserPermission
	 *            the svcUserPermission to set
	 */
	public void setSvcUserPermission(PersServiceUserPermission theSvcUserPermission) {
		mySvcUserPermission = theSvcUserPermission;
	}

	public void loadAllAssociations() {
		for (BasePersServiceVersion next : myVersions) {
			next.loadAllAssociations();
		}
	}

}
