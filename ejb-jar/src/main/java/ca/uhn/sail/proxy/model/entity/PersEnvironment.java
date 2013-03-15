package ca.uhn.sail.proxy.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import com.google.common.base.Objects;

@Table(name = "PX_ENV", uniqueConstraints = { @UniqueConstraint(columnNames = { "ENV" }) })
@Entity
public class PersEnvironment {

	@Column(name = "ENV")
	private String myEnv;

	@Version()
	@Column(name = "OPTLOCK")
	private int myOptLock;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PID")
	private Long myPid;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object theObj) {
		return theObj instanceof PersEnvironment && Objects.equal(myPid, ((PersEnvironment) theObj).myPid);
	}

	/**
	 * @return the env
	 */
	public String getEnv() {
		return myEnv;
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
	public Long getPid() {
		return myPid;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(myEnv);
	}

	/**
	 * @param theEnv
	 *            the env to set
	 */
	public void setEnv(String theEnv) {
		myEnv = theEnv;
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

	public void loadAllAssociations() {
		// nothing
	}

}
