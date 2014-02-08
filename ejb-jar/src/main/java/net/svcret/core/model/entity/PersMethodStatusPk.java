package net.svcret.core.model.entity;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import org.hibernate.annotations.ForeignKey;

@Embeddable
public class PersMethodStatusPk implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@OneToOne(cascade = {})
	@JoinColumn(name = "METHOD_PID")
	@ForeignKey(name = "PX_METSTAT_METHOD")
	private PersMethod myMethod;

	public PersMethodStatusPk() {
	}
	
	public PersMethodStatusPk(PersMethod theMethod) {
		myMethod=theMethod;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((myMethod == null) ? 0 : myMethod.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PersMethodStatusPk other = (PersMethodStatusPk) obj;
		if (myMethod == null) {
			if (other.myMethod != null)
				return false;
		} else if (!myMethod.equals(other.myMethod))
			return false;
		return true;
	}

	public PersMethod getMethod() {
		return myMethod;
	}

}
