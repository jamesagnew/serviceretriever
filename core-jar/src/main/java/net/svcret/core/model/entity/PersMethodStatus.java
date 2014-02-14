package net.svcret.core.model.entity;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

//@formatter:off
@Entity()
@Table(name = "PX_METHOD_STATUS")
@NamedQueries({ 
	@NamedQuery(name = Queries.METHODSTATUS_FINDALL, query = Queries.METHODSTATUS_FINDALL_Q), 
})
//@formatter:on
public class PersMethodStatus extends BasePersMethodStatus {

	private static final long serialVersionUID = 1L;

	@EmbeddedId
	private PersMethodStatusPk myPk;

	@Column(name = "METHOD_PID", updatable = false, insertable = false)
	private long myMethodPid;

	public PersMethodStatus() {
		// nothing
	}

	public PersMethodStatus(PersMethod theMethod) {
		myPk = new PersMethodStatusPk(theMethod);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof PersMethodStatus)) {
			return false;
		}
		PersMethodStatus other = (PersMethodStatus) obj;
		if (myPk == null) {
			if (other.myPk != null) {
				return false;
			}
		} else if (!myPk.equals(other.myPk)) {
			return false;
		}
		return true;
	}

	public PersMethod getMethod() {
		return myPk.getMethod();
	}

	public long getMethodPid() {
		return myMethodPid;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((myPk == null) ? 0 : myPk.hashCode());
		return result;
	}

}
