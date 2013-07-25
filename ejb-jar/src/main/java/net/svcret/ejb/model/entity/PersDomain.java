package net.svcret.ejb.model.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import net.svcret.admin.shared.model.ServerSecuredEnum;
import net.svcret.admin.shared.model.StatusEnum;

import org.apache.commons.lang3.StringUtils;

@Table(name = "PX_DOMAIN")
@Entity()
public class PersDomain extends BasePersServiceCatalogItem {

	private static final long serialVersionUID = 1L;

	@Transient
	private transient boolean myAllAssociationsLoaded;

	@Column(name = "DOMAIN_ID", length = 100, nullable = false)
	private String myDomainId;

	@Column(name = "DOMAIN_NAME", length = 200, nullable = true)
	private String myDomainName;

	@Transient
	private volatile transient HashMap<String, PersService> myIdToServices;

	@Version()
	@Column(name = "OPTLOCK")
	protected int myOptLock;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PID")
	private Long myPid;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy="myDomain")
	private Collection<PersService> myServices;

	@OneToMany(fetch=FetchType.LAZY, cascade= {}, orphanRemoval=true, mappedBy="myDomain")
	private Collection<PersUserDomainPermission> myUserPermissions;
	
	@OneToMany(fetch=FetchType.LAZY, cascade= {}, orphanRemoval=true, mappedBy="myDomain")
	private Collection<PersMonitorAppliesTo> myMonitorRules;

	@Transient
	private transient StatusEnum myStatus;

	@Transient
	private transient ServerSecuredEnum myServerSecured;

	public PersDomain() {
		super();
	}

	public PersDomain(long thePid, String theDomainId) {
		myPid = thePid;
		myDomainId = theDomainId;
	}

	/**
	 * @return the domainId
	 */
	public String getDomainId() {
		return myDomainId;
	}

	/**
	 * @return the domainName
	 */
	public String getDomainName() {
		return myDomainName;
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
	 * @return the services
	 */
	public Collection<PersService> getServices() {
		if (myServices == null) {
			myServices = new ArrayList<PersService>();
		}
		return myServices;
	}

	public Collection<PersMonitorAppliesTo> getMonitorRules() {
		if (myMonitorRules==null) {
			myMonitorRules=new ArrayList<PersMonitorAppliesTo>();
		}
		return myMonitorRules;
	}

	public StatusEnum getStatus() {
		return myStatus;
	}


	public void loadAllAssociations() {
		if (myAllAssociationsLoaded) {
			return;
		}

		initIdToServices();
		
		for (PersMonitorAppliesTo next : getMonitorRules()) {
			next.loadAllAssociations();
		}

		myAllAssociationsLoaded = true;
	}

	private HashMap<String, PersService> initIdToServices() {
		HashMap<String, PersService> idToServices = new HashMap<String, PersService>();
		for (PersService next : getServices()) {
			idToServices.put(next.getServiceId(), next);
			next.loadAllAssociations();
		}
		myIdToServices = idToServices;
		return idToServices;
	}

	/**
	 * @param theDomainId
	 *            the domainId to set
	 */
	public void setDomainId(String theDomainId) {
		// TODO: validate characters
		myDomainId = theDomainId;
	}

	/**
	 * @param theDomainName
	 *            the domainName to set
	 */
	public void setDomainName(String theDomainName) {
		myDomainName = theDomainName;
	}


	/**
	 * @param theOptLock
	 *            the versionNum to set
	 */
	public void setOptLock(int theOptLock) {
		myOptLock = theOptLock;
	}

	/**
	 * @param theId
	 *            the id to set
	 */
	public void setPid(Long thePid) {
		myPid = thePid;
	}

	public Collection<PersServiceVersionMethod> getAllServiceVersionMethods() {
		List<PersServiceVersionMethod> retVal = new ArrayList<PersServiceVersionMethod>();
		for (PersService nextService : getServices()) {
			retVal.addAll(nextService.getAllServiceVersionMethods());
		}
		return retVal;
	}

	public PersService getServiceWithId(String theId) {
		if (myIdToServices == null) {
			return initIdToServices().get(theId);
		}
		return myIdToServices.get(theId);
	}

	public void addService(PersService thePersService) {
		getServices();
		myServices.add(thePersService);
		myIdToServices = null;
	}

	public void merge(PersDomain theDomain) {
		super.merge(theDomain);
		setDomainId(theDomain.getDomainId());
		setDomainName(theDomain.getDomainName());
	}

	public ServerSecuredEnum getServerSecured() {
		if (myServerSecured == null) {
			for (PersService next : getServices()) {
				myServerSecured = ServerSecuredEnum.merge(myServerSecured, next.getServerSecured());
			}
		}
		return myServerSecured;
	}

	@Override
	public Collection<? extends BasePersServiceVersion> getAllServiceVersions() {
		ArrayList<BasePersServiceVersion> retVal = new ArrayList<BasePersServiceVersion>();
		for (PersService next : getServices()) {
			retVal.addAll(next.getAllServiceVersions());
		}
		return retVal;
	}

	public String getDomainNameOrId() {
		if (StringUtils.isNotBlank(getDomainName())) {
			return getDomainName();
		}
		return getDomainId();
	}

}
