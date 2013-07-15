package net.svcret.ejb.model.entity;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Embeddable
public class PersUserMethodStatusPk implements Serializable {

	private static final long serialVersionUID = 1L;

	@ManyToOne(cascade = {})
	@JoinColumn(name = "METHOD_PID")
	private PersServiceVersionMethod myMethod;

	@ManyToOne(cascade = {})
	@JoinColumn(name = "USER_STATUS_PID")
	private PersUserStatus myUserStatus;

	public PersUserMethodStatusPk() {
		super();
	}

	public PersUserMethodStatusPk(PersUserStatus theUserStatus, PersServiceVersionMethod theMethod) {
		super();
		myUserStatus = theUserStatus;
		myMethod = theMethod;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof PersUserMethodStatusPk)) {
			return false;
		}
		PersUserMethodStatusPk other = (PersUserMethodStatusPk) obj;
		if (myMethod == null) {
			if (other.myMethod != null) {
				return false;
			}
		} else if (!myMethod.equals(other.myMethod)) {
			return false;
		}
		if (myUserStatus == null) {
			if (other.myUserStatus != null) {
				return false;
			}
		} else if (!myUserStatus.equals(other.myUserStatus)) {
			return false;
		}
		return true;
	}

	/**
	 * @return the method
	 */
	public PersServiceVersionMethod getMethod() {
		return myMethod;
	}

	/**
	 * @return the userStatus
	 */
	public PersUserStatus getUserStatus() {
		return myUserStatus;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((myMethod == null) ? 0 : myMethod.hashCode());
		result = prime * result + ((myUserStatus == null) ? 0 : myUserStatus.hashCode());
		return result;
	}

	/**
	 * @param theMethod
	 *            the method to set
	 */
	public void setMethod(PersServiceVersionMethod theMethod) {
		myMethod = theMethod;
	}

	/**
	 * @param theUserStatus
	 *            the userStatus to set
	 */
	public void setUserStatus(PersUserStatus theUserStatus) {
		myUserStatus = theUserStatus;
	}

}
