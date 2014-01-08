package net.svcret.ejb.model.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import net.svcret.admin.shared.enm.MethodSecurityPolicyEnum;
import net.svcret.admin.shared.model.GServiceMethod;
import net.svcret.admin.shared.model.StatusEnum;
import net.svcret.ejb.api.IRuntimeStatusQueryLocal;
import net.svcret.ejb.ejb.RuntimeStatusQueryBean.StatsAccumulator;
import net.svcret.ejb.ex.UnexpectedFailureException;

import org.apache.commons.lang3.Validate;

@Table(name = "PX_SVC_VER_METHOD")
@Entity
public class PersMethod extends BasePersObject {

	private static final long serialVersionUID = 1L;

	@OneToMany(fetch=FetchType.LAZY, cascade= {CascadeType.REMOVE}, orphanRemoval=true, mappedBy="myPk.myMethod")
	private Collection<PersInvocationMethodSvcverStats> myInvocationStats;

	@Column(name = "NAME", length = 200, nullable=false)
	private String myName;

	@Column(name="METHOD_ORDER", nullable=false)
	private int myOrder;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PID")
	private Long myPid;

	@Column(name = "ROOT_ELEMENTS", length = 1000, nullable=true)
	private String myRootElements;

	@Column(name="SEC_POLICY", length=50, nullable=false)
	@Enumerated(EnumType.STRING)
	private MethodSecurityPolicyEnum mySecurityPolicy=MethodSecurityPolicyEnum.getDefault();

	@OneToOne(cascade={CascadeType.REMOVE}, fetch=FetchType.LAZY, orphanRemoval=true, mappedBy="myPk.myMethod")
	private PersMethodStatus myStatus;
	
	@ManyToOne()
	@JoinColumn(name = "SVC_VERSION_PID", referencedColumnName = "PID", nullable = false)
	private BasePersServiceVersion myServiceVersion;

	@OneToMany(fetch = FetchType.LAZY, cascade = {}, orphanRemoval = true, mappedBy = "myMethod")
	private List<PersServiceVersionRecentMessage> mySvcVerRecentMessages;

	@OneToMany(fetch=FetchType.LAZY, cascade= {CascadeType.REMOVE}, orphanRemoval=true, mappedBy="myServiceVersionMethod")
	private Collection<PersUserServiceVersionMethodPermission> myUserPermissions;

	@OneToMany(fetch = FetchType.LAZY, cascade = {}, orphanRemoval = true, mappedBy = "myMethod")
	private List<PersUserRecentMessage> myUserRecentMessages;

	@Version()
	@Column(name = "OPTLOCK")
	protected int myOptLock;
	
	/**
	 * Constructor
	 */
	public PersMethod() {
		super();
	}

	/**
	 * Constructor
	 */
	public PersMethod(long thePid, BasePersServiceVersion theServiceVersion, String theMethodName) {
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
	
	public MethodSecurityPolicyEnum getSecurityPolicy() {
		return mySecurityPolicy;
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

	public void merge(PersMethod theObj) {
		setName(theObj.getName());
		setRootElements(theObj.getRootElements());
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

	public void setSecurityPolicy(MethodSecurityPolicyEnum theSecurityPolicy) {
		Validate.notNull(theSecurityPolicy);
		mySecurityPolicy = theSecurityPolicy;
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

	public GServiceMethod toDto(boolean theLoadStats, IRuntimeStatusQueryLocal theRuntimeStatusQuerySvc) throws UnexpectedFailureException {
		GServiceMethod retVal = new GServiceMethod();
		if (this.getPid() != null) {
			retVal.setPid(this.getPid());
		}
		retVal.setId(this.getName());
		retVal.setName(this.getName());
		retVal.setRootElements(this.getRootElements());
		retVal.setSecurityPolicy(this.getSecurityPolicy());

		if (theLoadStats) {
			retVal.setStatsInitialized(new Date());
			StatusEnum status = StatusEnum.UNKNOWN;

			StatsAccumulator accumulator = new StatsAccumulator();
			theRuntimeStatusQuerySvc.extract60MinuteMethodStats(this, accumulator);
			accumulator.populateDto(retVal);

			retVal.setStatus(net.svcret.admin.shared.model.StatusEnum.valueOf(status.name()));
		}

		return retVal;
	}

}
