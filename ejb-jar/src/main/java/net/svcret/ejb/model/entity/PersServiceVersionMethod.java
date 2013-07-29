package net.svcret.ejb.model.entity;

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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
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

	@Column(name="METHOD_ORDER", nullable=false)
	private int myOrder;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PID")
	private Long myPid;

	@Column(name = "ROOT_ELEMENTS", length = 1000, nullable=true)
	private String myRootElements;

	@ManyToOne()
	@JoinColumn(name = "SVC_VERSION_PID", referencedColumnName = "PID", nullable = false)
	private BasePersServiceVersion myServiceVersion;
	
	@OneToMany(fetch=FetchType.LAZY, cascade= {CascadeType.REMOVE}, orphanRemoval=true, mappedBy="myServiceVersionMethod")
	private Collection<PersUserServiceVersionMethodPermission> myUserPermissions;

	@OneToMany(fetch=FetchType.LAZY, cascade= {CascadeType.REMOVE}, orphanRemoval=true, mappedBy="myPk.myMethod")
	private Collection<PersInvocationStats> myInvocationStats;

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
	 * @return the order
	 */
	public int getOrder() {
		return myOrder;
	}
	
	/**
	 * @return the id
	 */
	public Long getPid() {
		return myPid;
	}

	public String getRootElements() {
		return myRootElements;
	}

	/**
	 * @return the serviceVersion
	 */
	public BasePersServiceVersion getServiceVersion() {
		return myServiceVersion;
	}

	
	
	public Collection<PersUserServiceVersionMethodPermission> getUserPermissions() {
		if (myUserPermissions==null) {
			myUserPermissions=new ArrayList<PersUserServiceVersionMethodPermission>();
		}
		return myUserPermissions;
	}

	public void loadAllAssociations() {
		for (PersUserServiceVersionMethodPermission next : getUserPermissions()) {
			next.loadAllAssociations();
		}
	}

	public void merge(PersServiceVersionMethod theObj) {
		setName(theObj.getName());
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
	 * @param theOrder the order to set
	 */
	public void setOrder(int theOrder) {
		myOrder = theOrder;
	}

	/**
	 * @param theId
	 *            the id to set
	 */
	public void setPid(Long theId) {
		myPid = theId;
	}

	public void setRootElements(String theRootElements) {
		myRootElements = theRootElements;
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
//		theServiceVersion.addMethod(this);
	}

}
