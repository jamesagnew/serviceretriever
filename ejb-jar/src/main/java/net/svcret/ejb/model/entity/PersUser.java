package net.svcret.ejb.model.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;

import net.svcret.admin.shared.model.UserGlobalPermissionEnum;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.util.Password;
import net.svcret.ejb.util.Validate;

import com.google.common.base.Objects;

@Table(name = "PX_USER")
@Entity
@NamedQueries(value = { @NamedQuery(name = Queries.PERSUSER_FIND, query = Queries.PERSUSER_FIND_Q) })
public class PersUser extends BasePersObject implements IThrottleable {

	public static final String DEFAULT_ADMIN_PASSWORD = "admin";
	public static final String DEFAULT_ADMIN_USERNAME = "admin";
	private static final long serialVersionUID = 1L;

	@Transient
	private transient List<PersUserAllowableSourceIps> myAllowableSourceIpsToDelete;

	@Column(name = "ALLOW_ALL_DOMAINS")
	private boolean myAllowAllDomains;

	@Transient
	private transient Set<PersServiceVersionMethod> myAllowedMethods;
	
	@OneToMany(cascade = {}, fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "myUser")
	@OrderBy("IP_ORDER")
	private List<PersUserAllowableSourceIps> myAllowSourceIps;
	
	@ManyToOne(cascade = {})
	@JoinColumn(name = "AUTH_HOST_PID", referencedColumnName = "PID", nullable = false)
	private BasePersAuthenticationHost myAuthenticationHost;

	@ManyToOne(cascade = {})
	@JoinColumn(name = "CONTACT_PID", referencedColumnName = "PID", nullable = true)
	private PersUserContact myContact;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "myUser")
	private Collection<PersUserDomainPermission> myDomainPermissions;

	@Column(name = "PASSWORD_HASH", nullable = true, length = 512)
	// NB: Nullable because user can be backed by external authorization
	private String myPasswordHash;

	@ElementCollection(targetClass = UserGlobalPermissionEnum.class)
	@CollectionTable(name = "PX_USER_GLOBALPERMS")
	private Set<UserGlobalPermissionEnum> myPermissions;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PID")
	private Long myPid;

	@OneToMany(fetch=FetchType.LAZY, cascade = {}, orphanRemoval = true, mappedBy = "myUser")
	private List<PersServiceVersionRecentMessage> myRecentMessages;
	
	@OneToOne(cascade = { CascadeType.PERSIST }, orphanRemoval = true, mappedBy = "myUser")
	private PersUserStatus myStatus;

	@Column(name="THROTTLE_MAX_QUEUE", nullable=true)
	private Integer myThrottleMaxQueueDepth;

	@Column(name="THROTTLE_MAX_REQS", nullable=true)
	private Integer myThrottleMaxRequests;

	@Column(name="THROTTLE_PERIOD", nullable=true)
	@Enumerated(EnumType.STRING)
	private ThrottlePeriodEnum myThrottlePeriod;


	@Column(unique = true, name = "USERNAME", nullable = false, length = 200)
	private String myUsername;

	@OneToMany(fetch=FetchType.LAZY, cascade = {}, orphanRemoval = true, mappedBy = "myUser")
	private List<PersUserRecentMessage> myUserRecentMessages;

	public PersUser() {
	}

	public PersUser(long thePid) {
		myPid = thePid;
	}

	public PersUserDomainPermission addPermission(PersDomain theServiceDomain) {
		Validate.notNull(theServiceDomain, "PersDomain");

		PersUserDomainPermission perm = new PersUserDomainPermission();
		perm.setServiceUser(this);
		perm.setServiceDomain(theServiceDomain);

		getDomainPermissions();
		myDomainPermissions.add(perm);

		return perm;
	}

	public boolean checkPassword(String thePassword) throws ProcessingException {
		if (myPasswordHash == null) {
			throw new IllegalStateException("No password stored in this user - PID " + getPid());
		}
		try {
			return Password.checkStrongHash(thePassword, myPasswordHash);
		} catch (Exception e) {
			throw new ProcessingException(e);
		}
	}

	public boolean determineIfIpIsAllowed(String theRequestHostIp) {
		List<String> sourceIps = getAllowSourceIpsAsStrings();
		if (sourceIps.isEmpty()) {
			return true;
		}
		return sourceIps.contains(theRequestHostIp);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object theObj) {
		return theObj instanceof PersUser && Objects.equal(myPid, ((PersUser) theObj).myPid);
	}

	/**
	 * @return the allowableSourceIpsToDelete
	 */
	public List<PersUserAllowableSourceIps> getAllowableSourceIpsToDelete() {
		if (myAllowableSourceIpsToDelete == null) {
			return Collections.emptyList();
		} else {
			return myAllowableSourceIpsToDelete;
		}
	}

	public List<PersUserAllowableSourceIps> getAllowSourceIps() {
		if (myAllowSourceIps == null) {
			myAllowSourceIps = new ArrayList<PersUserAllowableSourceIps>();
		}
		return myAllowSourceIps;
	}

	public List<String> getAllowSourceIpsAsStrings() {
		ArrayList<String> retVal = new ArrayList<String>();
		for (PersUserAllowableSourceIps next : getAllowSourceIps()) {
			retVal.add(next.getIp());
		}
		return retVal;
	}

	/**
	 * @return the authenticationHost
	 */
	public BasePersAuthenticationHost getAuthenticationHost() {
		return myAuthenticationHost;
	}

	/**
	 * @return the contact
	 */
	public PersUserContact getContact() {
		return myContact;
	}

	/**
	 * @return the versions
	 */
	public Collection<PersUserDomainPermission> getDomainPermissions() {
		if (myDomainPermissions == null) {
			myDomainPermissions = new ArrayList<PersUserDomainPermission>();
		}
		return (myDomainPermissions);
		// return Collections.unmodifiableCollection(myDomainPermissions);
	}

	public String getPasswordHash() {
		return myPasswordHash;
	}

	/**
	 * @return the permissions
	 */
	public Set<UserGlobalPermissionEnum> getPermissions() {
		if (myPermissions == null) {
			myPermissions = new HashSet<UserGlobalPermissionEnum>();
		}
		return myPermissions;
	}

	/**
	 * @return the pid
	 */
	public Long getPid() {
		return myPid;
	}

	/**
	 * @return the status
	 */
	public PersUserStatus getStatus() {
		return myStatus;
	}

	/* (non-Javadoc)
	 * @see net.svcret.ejb.model.entity.IThrottleable#getThrottleMaxQueueDepth()
	 */
	@Override
	public Integer getThrottleMaxQueueDepth() {
		return myThrottleMaxQueueDepth;
	}

	/* (non-Javadoc)
	 * @see net.svcret.ejb.model.entity.IThrottleable#getThrottleMaxRequests()
	 */
	@Override
	public Integer getThrottleMaxRequests() {
		return myThrottleMaxRequests;
	}

	/* (non-Javadoc)
	 * @see net.svcret.ejb.model.entity.IThrottleable#getThrottlePeriod()
	 */
	@Override
	public ThrottlePeriodEnum getThrottlePeriod() {
		return myThrottlePeriod;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return myUsername;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(myPid);
	}

	/**
	 * Does this user have access to the specified method
	 */
	public boolean hasPermission(PersServiceVersionMethod theMethod) {
		if (myAllowedMethods == null) {
			throw new IllegalStateException("Associations have not been loaded");
		}

		if (myAllowAllDomains) {
			return true;
		}

		return myAllowedMethods.contains(theMethod);
	}

	/**
	 * @return the allowAllDomains
	 */
	public boolean isAllowAllDomains() {
		return myAllowAllDomains;
	}

	public void loadAllAssociations() {
		Set<PersServiceVersionMethod> allowedMethods = new HashSet<PersServiceVersionMethod>();

		for (PersUserDomainPermission nextDomain : getDomainPermissions()) {
			allowedMethods.addAll(nextDomain.getAllAllowedMethods());
		}
		
		getStatus().loadAllAssociations();

		getAllowSourceIps().size();

		myAllowedMethods = allowedMethods;
	}

	/**
	 * @param theAllowAllDomains
	 *            the allowAllDomains to set
	 */
	public void setAllowAllDomains(boolean theAllowAllDomains) {
		myAllowAllDomains = theAllowAllDomains;
	}

	public void setAllowSourceIpsAsStrings(Collection<String> theStrings) {
		ArrayList<String> toAdd = new ArrayList<String>();
		if (theStrings != null) {
			toAdd.addAll(theStrings);
		}
		for (PersUserAllowableSourceIps next : getAllowSourceIps()) {
			toAdd.remove(next.getIp());
		}

		ArrayList<PersUserAllowableSourceIps> toDelete = new ArrayList<PersUserAllowableSourceIps>(getAllowSourceIps());
		for (Iterator<PersUserAllowableSourceIps> iterator = toDelete.iterator(); iterator.hasNext();) {
			PersUserAllowableSourceIps next = iterator.next();
			if (theStrings != null && theStrings.contains(next.getIp())) {
				iterator.remove();
			}
		}

		for (String next : toAdd) {
			getAllowSourceIps().add(new PersUserAllowableSourceIps(this, next));
		}

		if (myAllowableSourceIpsToDelete == null) {
			myAllowableSourceIpsToDelete = new ArrayList<PersUserAllowableSourceIps>();
		}
		for (PersUserAllowableSourceIps next : toDelete) {
			myAllowableSourceIpsToDelete.add(next);
			getAllowSourceIps().remove(next);
		}

	}

	/**
	 * @param theAuthenticationHost
	 *            the authenticationHost to set
	 */
	public void setAuthenticationHost(BasePersAuthenticationHost theAuthenticationHost) {
		myAuthenticationHost = theAuthenticationHost;
	}

	/**
	 * @param theContact
	 *            the contact to set
	 */
	public void setContact(PersUserContact theContact) {
		myContact = theContact;
	}

	/**
	 * @param theDomainPermissions
	 *            the domainPermissions to set
	 */
	public void setDomainPermissions(Collection<PersUserDomainPermission> theDomainPermissions) {
		myDomainPermissions = theDomainPermissions;
		for (PersUserDomainPermission next : theDomainPermissions) {
			next.setServiceUser(this);
		}
	}

	public void setPassword(String thePassword) throws ProcessingException {
		Validate.notBlank(thePassword, "Password");

		try {
			myPasswordHash = Password.getStrongHash(thePassword);
		} catch (Exception e) {
			throw new ProcessingException(e);
		}
	}

	public void setPasswordHash(String thePasswordHash) {
		if (myAuthenticationHost instanceof PersAuthenticationHostLocalDatabase) {
			Validate.notBlank(thePasswordHash);
		}
		myPasswordHash = thePasswordHash;
	}

	/**
	 * @param thePermissions
	 *            the permissions to set
	 */
	public void setPermissions(Set<UserGlobalPermissionEnum> thePermissions) {
		myPermissions = thePermissions;
	}

	/**
	 * @param thePid
	 *            the pid to set
	 */
	public void setPid(Long thePid) {
		myPid = thePid;
	}

	public void setStatus(PersUserStatus theStatus) {
		myStatus = theStatus;
	}

	public void setThrottleMaxQueueDepth(Integer theThrottleMaxQueueDepth) {
		myThrottleMaxQueueDepth = theThrottleMaxQueueDepth;
	}

	public void setThrottleMaxRequests(Integer theThrottleMaxRequests) {
		myThrottleMaxRequests = theThrottleMaxRequests;
	}

	public void setThrottlePeriod(ThrottlePeriodEnum theThrottlePeriod) {
		myThrottlePeriod = theThrottlePeriod;
	}

	/**
	 * @param theUsername
	 *            the username to set
	 */
	public void setUsername(String theUsername) {
		myUsername = theUsername;
	}

}
