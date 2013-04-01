package net.svcret.ejb.model.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
import javax.persistence.Version;

import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.util.Password;
import net.svcret.ejb.util.Validate;

import com.google.common.base.Objects;

@Table(name = "PX_USER")
@Entity
@NamedQueries(value = { @NamedQuery(name = Queries.PERSUSER_FIND, query = Queries.PERSUSER_FIND_Q) })
public class PersUser extends BasePersObject {

	public static final String DEFAULT_ADMIN_USERNAME = "admin";

	public static final String DEFAULT_ADMIN_PASSWORD = "admin";

	@Column(name = "ALLOW_ALL_DOMAINS")
	private boolean myAllowAllDomains;

	@Transient
	private transient Set<PersServiceVersionMethod> myAllowedMethods;

	@ManyToOne(cascade = {})
	@JoinColumn(name = "AUTH_HOST_PID", referencedColumnName = "PID", nullable = false)
	private BasePersAuthenticationHost myAuthenticationHost;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
	@JoinColumn(name = "SVC_USER_PID", referencedColumnName = "PID")
	private Collection<PersUserDomainPermission> myDomainPermissions;

	@Version()
	@Column(name = "OPTLOCK")
	private int myOptLock;

	// NB: Nullable because user can be backed by external authorization
	@Column(name = "PASSWORD_HASH", nullable = true, length = 512)
	private String myPasswordHash;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PID")
	private Long myPid;

	@Column(unique = true, name = "USERNAME", nullable = false, length = 200)
	private String myUsername;

	public PersUserDomainPermission addPermission(PersDomain theServiceDomain) {
		Validate.throwIllegalArgumentExceptionIfNull("PersDomain", theServiceDomain);

		PersUserDomainPermission perm = new PersUserDomainPermission();
		perm.setServiceUser(this);
		perm.setServiceDomain(theServiceDomain);

		getDomainPermissions();
		myDomainPermissions.add(perm);

		return perm;
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
	 * @param theUsername
	 *            the username to set
	 */
	public void setUsername(String theUsername) {
		myUsername = theUsername;
	}

	public boolean checkPassword(String thePassword) throws ProcessingException {
		try {
			return Password.check(thePassword, myPasswordHash);
		} catch (Exception e) {
			throw new ProcessingException(e);
		}
	}
	
	public void setPassword(String thePassword) throws ProcessingException {
		Validate.throwIllegalArgumentExceptionIfBlank("Password", thePassword);
		
		try {
			myPasswordHash = Password.getSaltedHash(thePassword);
		} catch (Exception e) {
			throw new ProcessingException(e);
		}
	}

}
