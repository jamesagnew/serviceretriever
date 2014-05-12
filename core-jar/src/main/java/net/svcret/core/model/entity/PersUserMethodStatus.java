package net.svcret.core.model.entity;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity()
@Table(name = "PX_USER_METHOD_STATUS")
public class PersUserMethodStatus extends BasePersMethodStatus {

	private static final long serialVersionUID = 1L;


	@EmbeddedId
	private PersUserMethodStatusPk myPk;

	public PersUserMethodStatus() {
	}

	public PersUserMethodStatus(PersUserStatus thePersUserStatus, PersMethod theMethod) {
		myPk = new PersUserMethodStatusPk(thePersUserStatus, theMethod);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof PersUserMethodStatus)) {
			return false;
		}
		PersUserMethodStatus other = (PersUserMethodStatus) obj;
		if (myPk == null) {
			if (other.myPk != null) {
				return false;
			}
		} else if (!myPk.equals(other.myPk)) {
			return false;
		}
		return true;
	}


	/**
	 * @return the pk
	 */
	public PersUserMethodStatusPk getPk() {
		return myPk;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((myPk == null) ? 0 : myPk.hashCode());
		return result;
	}


}
