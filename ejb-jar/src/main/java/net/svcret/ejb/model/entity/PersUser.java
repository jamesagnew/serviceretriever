package net.svcret.ejb.model.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
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
import javax.persistence.OneToOne;
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
public class PersUser extends BasePersObject {

	public static final String DEFAULT_ADMIN_PASSWORD = "admin";
	public static final String DEFAULT_ADMIN_USERNAME = "admin";
	private static final long serialVersionUID = 1L;

	@Column(name = "ALLOW_ALL_DOMAINS")
	private boolean myAllowAllDomains;

	@Transient
	private transient Set<PersServiceVersionMethod> myAllowedMethods;

	@ManyToOne(cascade = {})
	@JoinColumn(name = "AUTH_HOST_PID", referencedColumnName = "PID", nullable = false)
	private BasePersAuthenticationHost myAuthenticationHost;

	@ManyToOne(cascade = {})
	@JoinColumn(name = "CONTACT_PID", referencedColumnName = "PID", nullable = true)
	private PersContact myContact;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "myUser")
	private Collection<PersUserDomainPermission> myDomainPermissions;

	// NB: Nullable because user can be backed by external authorization
	@Column(name = "PASSWORD_HASH", nullable = true, length = 512)
	private String myPasswordHash;

	@ElementCollection(targetClass = UserGlobalPermissionEnum.class)
	@CollectionTable(name = "PX_USER_GLOBALPERMS")
	private Set<UserGlobalPermissionEnum> myPermissions;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PID")
	private Long myPid;

	@OneToOne(cascade = { CascadeType.PERSIST }, orphanRemoval = true, mappedBy = "myUser")
	private PersUserStatus myStatus;

	@Column(unique = true, name = "USERNAME", nullable = false, length = 200)
	private String myUsername;

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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object theObj) {
		return theObj instanceof PersUser && Objects.equal(myPid, ((PersUser) theObj).myPid);
	}

	/**
	 * @return the authenticationHost
	 */
	public BasePersAuthenticationHost getAuthenticationHost() {
		return myAuthenticationHost;
	}

	/**
	 * @return the versions
	 */
	public Collection<PersUserDomainPermission> getDomainPermissions() {
		if (myDomainPermissions == null) {
			myDomainPermissions = new ArrayList<PersUserDomainPermission>();
		}
		return Collections.unmodifiableCollection(myDomainPermissions);
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

		myAllowedMethods = allowedMethods;
		myStatus.toString();
	}

	/**
	 * @param theAllowAllDomains
	 *            the allowAllDomains to set
	 */
	public void setAllowAllDomains(boolean theAllowAllDomains) {
		myAllowAllDomains = theAllowAllDomains;
	}

	/**
	 * @param theAuthenticationHost
	 *            the authenticationHost to set
	 */
	public void setAuthenticationHost(BasePersAuthenticationHost theAuthenticationHost) {
		myAuthenticationHost = theAuthenticationHost;
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

	/**
	 * @param theUsername
	 *            the username to set
	 */
	public void setUsername(String theUsername) {
		myUsername = theUsername;
	}

}
