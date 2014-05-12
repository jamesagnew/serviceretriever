package net.svcret.core.model.entity;

import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import net.svcret.admin.shared.enm.AuthorizationHostTypeEnum;
import net.svcret.admin.shared.enm.ResponseTypeEnum;

/**
 * Authentication host: This is a module which is used to take credentials of
 * clients who are calling our services and validate that they are correct
 */
//@formatter:off
@Table(name = "PX_AUTH_HOST", uniqueConstraints= {
		@UniqueConstraint(name="PX_AUTHHOST_CONS_MID", columnNames= {"MODULE_ID"})
	})
@Entity()
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "AUTH_TYPE", length = 20, discriminatorType = DiscriminatorType.STRING)
@NamedQueries(value = { @NamedQuery(name = Queries.AUTHHOST_FINDALL, query = Queries.AUTHHOST_FINDALL_Q) })
//@formatter:on
public abstract class BasePersAuthenticationHost extends BasePersKeepsRecentTransactions {

	public static final String MODULE_DESC_ADMIN_AUTH = "Default authentication host";
	public static final String MODULE_ID_ADMIN_AUTH = "DEFAULT";

	private static final long serialVersionUID = 1L;

	@OneToMany(cascade=CascadeType.REMOVE, orphanRemoval=true, fetch=FetchType.LAZY, mappedBy="myAuthenticationHost")
	private Collection<PersUser> myUsers;

	@OneToMany(cascade=CascadeType.REMOVE, orphanRemoval=true, fetch=FetchType.LAZY, mappedBy="myAuthenticationHost")
	private Collection<PersBaseServerAuth<?,?>> myServerAuths;

	@Column(name = "AUTOCREATE_AUTHD_USERS", nullable = false)
	private boolean myAutoCreateAuthorizedUsers;

	@Column(name = "CACHE_SUCCESS_MILLIS", nullable = true)
	private Integer myCacheSuccessfulCredentialsForMillis;

	@Column(name = "MODULE_ID", length = 100, nullable = false)
	private String myModuleId;

	@Column(name = "MODULE_NAME", length = 200, nullable = false)
	private String myModuleName;

	@Version()
	@Column(name = "OPTLOCK")
	private int myOptLock;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PID")
	private Long myPid;

	@Column(name = "SUPTS_PW_CHG", nullable = false)
	private boolean mySupportsPasswordChange;

	public BasePersAuthenticationHost() {
		super();
	}

	public BasePersAuthenticationHost(String theModuleId) {
		myModuleId = theModuleId;
	}

	@Override
	public boolean canInheritKeepNumRecentTransactions() {
		return false;
	}

	@Override
	public Integer determineInheritedKeepNumRecentTransactions(ResponseTypeEnum theResultType) {
		return null;
	}

	/**
	 * @return the cacheSuccessfulCredentialsForMillis
	 */
	public Integer getCacheSuccessfulCredentialsForMillis() {
		return myCacheSuccessfulCredentialsForMillis;
	}

	/**
	 * @return the moduleId
	 */
	public String getModuleId() {
		return myModuleId;
	}

	/**
	 * @return the moduleName
	 */
	public String getModuleName() {
		return myModuleName;
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
	@Override
	public Long getPid() {
		return myPid;
	}

	public abstract AuthorizationHostTypeEnum getType();

	/**
	 * @return the autoCreateAuthorizedUsers
	 */
	public boolean isAutoCreateAuthorizedUsers() {
		return myAutoCreateAuthorizedUsers;
	}

	/**
	 * @return the supportsPasswordChange
	 */
	public boolean isSupportsPasswordChange() {
		return mySupportsPasswordChange;
	}

	@Override
	public void merge(BasePersObject theHost) {
		super.merge(theHost);

		BasePersAuthenticationHost obj = (BasePersAuthenticationHost) theHost;
		setAutoCreateAuthorizedUsers(obj.isAutoCreateAuthorizedUsers());
		setAutoCreateAuthorizedUsers(obj.isAutoCreateAuthorizedUsers());
		setCacheSuccessfulCredentialsForMillis(obj.getCacheSuccessfulCredentialsForMillis());
		setModuleId(obj.getModuleId());
		setModuleName(obj.getModuleName());
		setSupportsPasswordChange(obj.isSupportsPasswordChange());
	}

	/**
	 * @param theAutoCreateAuthorizedUsers
	 *            the autoCreateAuthorizedUsers to set
	 */
	public void setAutoCreateAuthorizedUsers(boolean theAutoCreateAuthorizedUsers) {
		myAutoCreateAuthorizedUsers = theAutoCreateAuthorizedUsers;
	}

	/**
	 * @param theCacheSuccessfulCredentialsForMillis
	 *            the cacheSuccessfulCredentialsForMillis to set
	 */
	public void setCacheSuccessfulCredentialsForMillis(Integer theCacheSuccessfulCredentialsForMillis) {
		myCacheSuccessfulCredentialsForMillis = theCacheSuccessfulCredentialsForMillis;
	}

	/**
	 * @param theModuleId
	 *            the moduleId to set
	 */
	public void setModuleId(String theModuleId) {
		myModuleId = theModuleId;
	}

	/**
	 * @param theModuleName
	 *            the moduleName to set
	 */
	public void setModuleName(String theModuleName) {
		myModuleName = theModuleName;
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
	 * @param theSupportsPasswordChange
	 *            the supportsPasswordChange to set
	 */
	public void setSupportsPasswordChange(boolean theSupportsPasswordChange) {
		mySupportsPasswordChange = theSupportsPasswordChange;
	}

	@Override
	public boolean determineInheritedAuditLogEnable() {
		if (getAuditLogEnable() != null) {
			return getAuditLogEnable();
		}
		return false;
	}

}
