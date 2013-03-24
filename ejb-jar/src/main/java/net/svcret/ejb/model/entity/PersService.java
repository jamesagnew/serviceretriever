package net.svcret.ejb.model.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

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

import com.google.common.base.Objects;

@Table(name = "PX_SVC", uniqueConstraints = { @UniqueConstraint(columnNames = { "DOMAIN_PID", "SERVICE_ID" }) })
@Entity
public class PersService extends BasePersObject {

	@Column(name = "SVC_ACTIVE")
	private boolean myActive;

	private HashMap<String, BasePersServiceVersion> myIdToVersion;

	@Version()
	@Column(name = "OPTLOCK")
	private int myOptLock;

	@ManyToOne(cascade = {}, fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "DOMAIN_PID", referencedColumnName = "PID")
	private PersDomain myPersDomain;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PID")
	private Long myPid;

	@Column(name = "SERVICE_ID", nullable = false, length = 100)
	private String myServiceId;

	@Column(name = "SERVICE_NAME", nullable = false, length = 200)
	private String myServiceName = "";

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "SERVICE_PID", referencedColumnName = "PID")
	private Collection<BasePersServiceVersion> myVersions;

	public PersService() {
		super();
	}

	public PersService(long thePid, PersDomain theDomain, String theServiceId, String theServiceName) {
		myPid = thePid;
		myPersDomain = theDomain;
		myServiceId = theServiceId;
		myServiceName = theServiceName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object theObj) {
		return theObj instanceof PersService && Objects.equal(myPid, ((PersService) theObj).myPid);
	}

	/**
	 * @return the persDomain
	 */
	public PersDomain getDomain() {
		return myPersDomain;
	}

	/**
	 * @return the versionNum
	 */
	public int getOptLock() {
		return myOptLock;
	}

	/**
	 * @return the id
	 */
	public Long getPid() {
		return myPid;
	}

	/**
	 * @return the serviceId
	 */
	public String getServiceId() {
		return myServiceId;
	}

	/**
	 * @return the serviceName
	 */
	public String getServiceName() {
		return myServiceName;
	}

	/**
	 * @return the versions
	 */
	public Collection<BasePersServiceVersion> getVersions() {
		if (myVersions == null) {
			myVersions = new ArrayList<BasePersServiceVersion>();
		}
		return myVersions;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(myPid);
	}

	/**
	 * @return the active
	 */
	public boolean isActive() {
		return myActive;
	}

	public void loadAllAssociations() {
		myIdToVersion = new HashMap<String, BasePersServiceVersion>();
		for (BasePersServiceVersion next : myVersions) {
			myIdToVersion.put(next.getVersionId(), next);
			next.loadAllAssociations();
		}
	}

	/**
	 * @param theActive
	 *            the active to set
	 */
	public void setActive(boolean theActive) {
		myActive = theActive;
	}

	/**
	 * @param theOptLock
	 *            the versionNum to set
	 */
	public void setOptLock(int theOptLock) {
		myOptLock = theOptLock;
	}

	/**
	 * @param thePersDomain
	 *            the persDomain to set
	 */
	public void setPersDomain(PersDomain thePersDomain) {
		myPersDomain = thePersDomain;
	}

	/**
	 * @param thePid
	 *            the id to set
	 */
	public void setPid(Long thePid) {
		myPid = thePid;
	}

	/**
	 * @param theServiceId
	 *            the serviceId to set
	 */
	public void setServiceId(String theServiceId) {
		myServiceId = theServiceId;
	}

	/**
	 * @param theServiceName
	 *            the serviceName to set
	 */
	public void setServiceName(String theServiceName) {
		myServiceName = theServiceName;
	}

	/**
	 * @param theVersions
	 *            the versions to set
	 */
	public void setVersions(Collection<BasePersServiceVersion> theVersions) {
		myVersions = theVersions;
	}

}
