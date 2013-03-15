package ca.uhn.sail.proxy.model.entity;

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
import javax.persistence.Version;

import com.google.common.base.Objects;

import ca.uhn.sail.proxy.model.entity.soap.PersServiceVersionSoap11;

@Table(name = "PX_SVC")
@Entity
public class PersService extends BasePersObject {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PID")
	private Long myPid;

	@ManyToOne(cascade = {}, fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "DOMAIN_PID", referencedColumnName = "PID")
	private PersDomain myPersDomain;

	@Column(name = "SERVICE_ID", nullable = false, length = 100)
	private String myServiceId;

	@Column(name = "SERVICE_NAME", nullable = false, length = 200)
	private String myServiceName;

	@Version()
	@Column(name = "OPTLOCK")
	private int myOptLock;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "SERVICE_PID", referencedColumnName = "PID")
	private Collection<PersServiceVersionSoap11> myVersions;

	private HashMap<String, PersServiceVersionSoap11> myIdToVersion;

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
	 * @return the persDomain
	 */
	public PersDomain getDomain() {
		return myPersDomain;
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
	 * @return the versionNum
	 */
	public int getOptLock() {
		return myOptLock;
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
	 * @param thePid
	 *            the id to set
	 */
	public void setPid(Long thePid) {
		myPid = thePid;
	}

	/**
	 * @param thePersDomain
	 *            the persDomain to set
	 */
	public void setPersDomain(PersDomain thePersDomain) {
		myPersDomain = thePersDomain;
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
	 * @param theOptLock
	 *            the versionNum to set
	 */
	public void setOptLock(int theOptLock) {
		myOptLock = theOptLock;
	}

	/**
	 * @param theVersions
	 *            the versions to set
	 */
	public void setVersions(Collection<PersServiceVersionSoap11> theVersions) {
		myVersions = theVersions;
	}

	public void loadAllAssociations() {
		myIdToVersion = new HashMap<String, PersServiceVersionSoap11>();
		for (PersServiceVersionSoap11 next : myVersions) {
			myIdToVersion.put(next.getVersionId(), next);
			next.loadAllAssociations();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object theObj) {
		return theObj instanceof PersService && Objects.equal(myPid, ((PersService) theObj).myPid);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(myPid);
	}

}
