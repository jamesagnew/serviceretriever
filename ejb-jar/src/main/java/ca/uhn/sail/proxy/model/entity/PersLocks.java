package ca.uhn.sail.proxy.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="PX_LOCKS")
public class PersLocks {

	@Id()
	@Column(name="LOCK_ID", length=20, nullable=false, updatable=false)
	private String myLockId;

	public PersLocks() {
	}
	
	public PersLocks(String theLockId) {
		myLockId = theLockId;
	}

	/**
	 * @return the lockId
	 */
	public String getLockId() {
		return myLockId;
	}

	/**
	 * @param theLockId the lockId to set
	 */
	public void setLockId(String theLockId) {
		myLockId = theLockId;
	}
	
}
