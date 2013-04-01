package net.svcret.ejb.model.entity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

@Table(name = "PX_SVC_VER_METHOD")
@Entity
public class PersServiceVersionMethod extends BasePersObject {

	private static final long serialVersionUID = 1L;

	@Column(name = "NAME", length = 200, nullable=false)
	private String myName;

	@Version()
	@Column(name = "OPTLOCK")
	protected int myOptLock;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PID")
	private Long myPid;

	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "SVC_VERSION_PID", referencedColumnName = "PID", nullable = false)
	private BasePersServiceVersion myServiceVersion;

	/**
	 * Constructor
	 */
	public PersServiceVersionMethod() {
		super();
	}
	
	/**
	 * Constructor
	 */
	public PersServiceVersionMethod(long thePid, BasePersServiceVersion theServiceVersion, String theMethodName) {
		setPid(thePid);
		setServiceVersion(theServiceVersion);
		setName(theMethodName);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return myName;
	}

	/**
	 * @return the optLock
	 */
	public int getOptLock() {
		return myOptLock;
	}

	/**
	 * @return the id
	 */
	public Long getPid() {
		return myPid;
	}

	/**
	 * @return the serviceVersion
	 */
	public BasePersServiceVersion getServiceVersion() {
		return myServiceVersion;
	}

	public void loadAllAssociations() {
		// nothing
	}

	/**
	 * @param theName
	 *            the name to set
	 */
	public void setName(String theName) {
		myName = theName;
	}

	/**
	 * @param theOptLock
	 *            the optLock to set
	 */
	public void setOptLock(int theOptLock) {
		myOptLock = theOptLock;
	}

	/**
	 * @param theId
	 *            the id to set
	 */
	public void setPid(Long theId) {
		myPid = theId;
	}

	/**
	 * @param theServiceVersion
	 *            the serviceVersion to set
	 */
	public void setServiceVersion(BasePersServiceVersion theServiceVersion) {
		if (theServiceVersion != null) {
			if (theServiceVersion.equals(myServiceVersion)) {
				return;
			} else if (myServiceVersion != null) {
				throw new IllegalStateException("Can't move methods to a new version");
			}
		} else {
			throw new NullPointerException("ServiceVersion can not be null");
		}
		
		myServiceVersion = theServiceVersion;
		theServiceVersion.addMethod(this);
	}

}
