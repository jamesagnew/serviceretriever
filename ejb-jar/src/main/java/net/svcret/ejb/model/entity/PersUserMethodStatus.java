package net.svcret.ejb.model.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity()
@Table(name = "PX_USER_METHOD_STATUS")
public class PersUserMethodStatus implements Serializable {

	private static final long serialVersionUID = 1L;

	@EmbeddedId
	private PersUserMethodStatusPk myPk;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="LAST_SUC_INVOC")
	private Date myLastSuccessfulInvocation;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((myPk == null) ? 0 : myPk.hashCode());
		return result;
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
	
	
	
}
