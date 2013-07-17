package net.svcret.ejb.model.entity;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang3.Validate;

@Entity()
@Table(name = "PX_USER_STATUS")
public class PersUserStatus extends BasePersObject {

	private static final long serialVersionUID = 1L;

	@Column(name = "LAST_ACCESS")
	@Temporal(TemporalType.TIMESTAMP)
	private Date myLastAccess;

	@Column(name = "LAST_SECURITY_FAIL")
	@Temporal(TemporalType.TIMESTAMP)
	private Date myLastSecurityFail;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = PersUserMethodStatus.class, mappedBy = "myPk.myUserStatus")
	@MapKey(name = "myPk.myMethod")
	private Map<PersServiceVersionMethod, PersUserMethodStatus> myMethodStatuses;

	@Column(name = "PID")
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long myPid;

	@OneToOne(cascade = {})
	@JoinColumn(name = "USER_PID", referencedColumnName = "PID", nullable = false)
	private PersUser myUser;

	public PersUserStatus() {
	}

	public PersUserStatus(long thePid) {
		myPid = thePid;
	}

	/**
	 * @return the lastAccess
	 */
	public Date getLastAccess() {
		return myLastAccess;
	}

	/**
	 * @return the lastSecurityFail
	 */
	public Date getLastSecurityFail() {
		return myLastSecurityFail;
	}

	/**
	 * @return the methodStatuses
	 */
	public Map<PersServiceVersionMethod, PersUserMethodStatus> getMethodStatuses() {
		if (myMethodStatuses == null) {
			myMethodStatuses = new HashMap<PersServiceVersionMethod, PersUserMethodStatus>();
		}
		return myMethodStatuses;
	}

	public PersUserMethodStatus getOrCreateMethodStatus(PersServiceVersionMethod theMethod) {
		Validate.notNull(theMethod);

		PersUserMethodStatus retVal;
		Map<PersServiceVersionMethod, PersUserMethodStatus> statuses = getMethodStatuses();
		synchronized (statuses) {
			retVal = statuses.get(theMethod);
			if (retVal == null) {
				retVal = new PersUserMethodStatus(this, theMethod);
				statuses.put(theMethod, retVal);
			}
		}

		return retVal;
	}

	/**
	 * @return the id
	 */
	public Long getPid() {
		return myPid;
	}

	/**
	 * @return the user
	 */
	public PersUser getUser() {
		return myUser;
	}

	public void merge(PersUserStatus thePersUserStatus) {
		if (thePersUserStatus.getLastAccess() != null) {
			setLastAccessIfNewer(thePersUserStatus.getLastAccess());
		}

		for (PersUserMethodStatus nextNew : thePersUserStatus.getMethodStatuses().values()) {

			PersUserMethodStatus nextExisting = getOrCreateMethodStatus(nextNew.getPk().getMethod());
			nextExisting.merge(nextNew);

		}

	}

	/**
	 * @param theLastAccess
	 *            the lastAccess to set
	 */
	public void setLastAccess(Date theLastAccess) {
		myLastAccess = theLastAccess;
	}

	public void setLastAccessIfNewer(Date theTransactionTime) {
		Validate.notNull(theTransactionTime);

		if (myLastAccess == null || myLastAccess.before(theTransactionTime)) {
			myLastAccess = theTransactionTime;
		}
	}

	/**
	 * @param theLastSecurityFail
	 *            the lastSecurityFail to set
	 */
	public void setLastSecurityFail(Date theLastSecurityFail) {
		myLastSecurityFail = theLastSecurityFail;
	}

	public void setLastSecurityFailIfNewer(Date theTransactionTime) {
		Validate.notNull(theTransactionTime);

		if (myLastSecurityFail == null || myLastSecurityFail.before(theTransactionTime)) {
			myLastSecurityFail = theTransactionTime;
		}
	}

	/**
	 * @param theId
	 *            the id to set
	 */
	public void setPid(Long theId) {
		myPid = theId;
	}

	/**
	 * @param theUser
	 *            the user to set
	 */
	public void setUser(PersUser theUser) {
		myUser = theUser;
	}

	public void loadAllAssociations() {
		myMethodStatuses.size();
	}

}
