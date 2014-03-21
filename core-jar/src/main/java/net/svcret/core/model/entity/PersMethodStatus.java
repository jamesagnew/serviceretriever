package net.svcret.core.model.entity;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
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

	@Id
	@Column(name = "METHOD_PID", insertable=true, updatable=false)
	private Long myPk;

	@OneToOne(cascade = {})
	@JoinColumn(name = "METHOD_PID", foreignKey=@ForeignKey(name="FK_METSTAT_METHOD"), updatable=false, insertable=false)
	private PersMethod myMethod;

	
//	@Column(name = "METHOD_PID", updatable = false, insertable = false)
//	private long myMethodPid;

//	public PersMethodStatusPk getPk() {
//		return myPk;
//	}
//
//	public void setPk(PersMethodStatusPk thePk) {
//		myPk = thePk;
//	}

	public PersMethodStatus() {
		// nothing
	}

	public PersMethodStatus(PersMethod theMethod) {
		myMethod = (theMethod);
		myPk = theMethod.getPid();
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
		return myMethod;
	}

//	public long getMethodPid() {
//		return myMethodPid;
//	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((myPk == null) ? 0 : myPk.hashCode());
		return result;
	}

}
