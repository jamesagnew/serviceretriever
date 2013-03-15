package ca.uhn.sail.proxy.model.entity;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import com.google.common.base.Objects;

import ca.uhn.sail.proxy.util.Validate;

@Table(name = "PX_SVC_USER")
@Entity
public class PersServiceUser extends BasePersObject {

	@Version()
	@Column(name = "OPTLOCK")
	private int myOptLock;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "SVC_USER_PID", referencedColumnName = "PID")
	private Collection<PersServiceUserPermission> myPermissions;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PID")
	private Long myPid;

	@Column(unique=true, name="USERNAME", nullable=false)
	private String myUsername;

	/**
	 * @return the optLock
	 */
	public int getOptLock() {
		return myOptLock;
	}

	/**
	 * @return the versions
	 */
	public Collection<PersServiceUserPermission> getPermissions() {
		if (myPermissions == null) {
			myPermissions = new ArrayList<PersServiceUserPermission>();
		}
		return myPermissions;
	}

	/**
	 * @return the pid
	 */
	public Long getPid() {
		return myPid;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return myUsername;
	}

	/**
	 * @param theOptLock the optLock to set
	 */
	public void setOptLock(int theOptLock) {
		myOptLock = theOptLock;
	}

	/**
	 * @param thePid the pid to set
	 */
	public void setPid(Long thePid) {
		myPid = thePid;
	}
	
	/**
	 * @param theUsername the username to set
	 */
	public void setUsername(String theUsername) {
		myUsername = theUsername;
	}

	public PersServiceUserPermission addPermission(PersDomain theServiceDomain) {
		Validate.throwIllegalArgumentExceptionIfNull("PersDomain", theServiceDomain);
		
		PersServiceUserPermission perm = new PersServiceUserPermission();
		perm.setServiceUser(this);
		perm.setServiceDomain(theServiceDomain);
		
		getPermissions().add(perm);
		return perm;
	}

	public void loadAllAssociations() {
		for (PersServiceUserPermission next : myPermissions) {
			next.loadAllAssociations();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object theObj) {
		return theObj instanceof PersServiceUser && Objects.equal(myPid, ((PersServiceUser) theObj).myPid);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(myPid);
	}

}
