package net.svcret.ejb.model.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import net.svcret.admin.shared.model.ServerSecuredEnum;
import net.svcret.ejb.api.ResponseTypeEnum;

import org.hibernate.annotations.ForeignKey;

import com.google.common.base.Objects;

@Table(name = "PX_SVC", uniqueConstraints = { @UniqueConstraint(name = "PX_SVC_CONS_DOMSVC", columnNames = { "DOMAIN_PID", "SERVICE_ID" }) })
@Entity
@NamedQueries(value = { @NamedQuery(name = Queries.SERVICE_FIND, query = Queries.SERVICE_FIND_Q) })
public class PersService extends BasePersKeepsRecentTransactions {

	private static final long serialVersionUID = 1L;

	@Column(name = "SVC_ACTIVE")
	private boolean myActive;

	@ManyToOne()
	@JoinColumn(name = "DOMAIN_PID", referencedColumnName = "PID", nullable = false)
	@ForeignKey(name = "PX_SVC_DOMAIN_PID")
	private PersDomain myDomain;

	@Transient
	private transient HashMap<String, BasePersServiceVersion> myIdToVersion;

	@Version()
	@Column(name = "OPTLOCK")
	private int myOptLock;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PID")
	private Long myPid;

	@Transient
	private transient ServerSecuredEnum myServerSecured;

	@Column(name = "SERVICE_ID", nullable = false, length = 100)
	private String myServiceId;

	@Column(name = "SERVICE_NAME", nullable = false, length = 200)
	private String myServiceName = "";

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "myService")
	private Collection<BasePersServiceVersion> myVersions;

	public PersService() {
		super();
	}

	public PersService(long thePid, PersDomain theDomain, String theServiceId, String theServiceName) {
		setPid(thePid);
		setServiceId(theServiceId);
		setServiceName(theServiceName);
		setDomain(theDomain);
	}

	public void addVersion(BasePersServiceVersion theVersion) {
		getVersions();
		if (!myVersions.contains(theVersion)) {
			myVersions.add(theVersion);
		}
	}

	@Override
	public Integer determineKeepNumRecentTransactions(ResponseTypeEnum theResultType) {
		Integer retVal = super.determineKeepNumRecentTransactions(theResultType);
		if (retVal == null) {
			retVal = myDomain.determineKeepNumRecentTransactions(theResultType);
		}
		return retVal;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object theObj) {
		if (!(theObj instanceof PersService)) {
			return false;
		}

		PersService obj = (PersService) theObj;
		if (myPid == null && obj.getPid() != null) {
			return false;
		}
		if (obj.getPid() == null && myPid != null) {
			return false;
		}
		if (myPid == null) {
			return Objects.equal(myServiceId, obj.myServiceId);
		}

		return Objects.equal(myPid, obj.myPid);
	}

	public Collection<PersServiceVersionMethod> getAllServiceVersionMethods() {
		List<PersServiceVersionMethod> retVal = new ArrayList<PersServiceVersionMethod>();
		for (BasePersServiceVersion nextServicVersion : getVersions()) {
			retVal.addAll(nextServicVersion.getMethods());
		}
		return Collections.unmodifiableList(retVal);
	}

	/**
	 * @return the persDomain
	 */
	public PersDomain getDomain() {
		return myDomain;
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

	public ServerSecuredEnum getServerSecured() {
		if (myServerSecured == null) {
			for (BasePersServiceVersion next : getVersions()) {
				myServerSecured = ServerSecuredEnum.merge(myServerSecured, next.getServerSecured());
			}
		}
		return myServerSecured;
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
		return Collections.unmodifiableCollection(myVersions);
	}

	public BasePersServiceVersion getVersionWithId(String theId) {
		for (BasePersServiceVersion next : getVersions()) {
			if (next.getVersionId().equals(theId)) {
				return next;
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		if (myPid == null) {
			return 0;
		}
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
		for (BasePersServiceVersion next : getVersions()) {
			myIdToVersion.put(next.getVersionId(), next);
			next.loadAllAssociations();
		}
	}

	public void merge(PersService theService) {
		setServiceId(theService.getServiceId());
		setServiceName(theService.getServiceName());
		setActive(theService.isActive());
	}

	public void removeVersion(BasePersServiceVersion theVersion) {
		getVersions();
		myVersions.remove(theVersion);
	}

	/**
	 * @param theActive
	 *            the active to set
	 */
	public void setActive(boolean theActive) {
		myActive = theActive;
	}

	/**
	 * @param thePersDomain
	 *            the persDomain to set
	 */
	public void setDomain(PersDomain thePersDomain) {
		if (!thePersDomain.getServices().contains(this)) {
			thePersDomain.addService(this);
		}
		myDomain = thePersDomain;
	}

	/**
	 * @param theOptLock
	 *            the versionNum to set
	 */
	public void setOptLock(int theOptLock) {
		myOptLock = theOptLock;
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
		// TODO: validate characters
		myServiceId = theServiceId;
	}

	/**
	 * @param theServiceName
	 *            the serviceName to set
	 */
	public void setServiceName(String theServiceName) {
		myServiceName = theServiceName;
	}

}
