package net.svcret.ejb.model.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
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

	@Column(name="PID")
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

	/**
	 * @param theLastAccess
	 *            the lastAccess to set
	 */
	public void setLastAccess(Date theLastAccess) {
		myLastAccess = theLastAccess;
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

	public void setLastAccessIfNewer(Date theTransactionTime) {
		Validate.notNull(theTransactionTime);

		if (myLastAccess == null || myLastAccess.before(theTransactionTime)) {
			myLastAccess = theTransactionTime;
		}
	}

	public void merge(PersUserStatus thePersUserStatus) {
		setLastAccessIfNewer(thePersUserStatus.getLastAccess());
	}

}
