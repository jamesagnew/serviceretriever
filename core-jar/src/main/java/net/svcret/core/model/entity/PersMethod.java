package net.svcret.core.model.entity;

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
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import net.svcret.admin.api.UnexpectedFailureException;
import net.svcret.admin.shared.enm.MethodSecurityPolicyEnum;
import net.svcret.admin.shared.model.DtoMethod;
import net.svcret.admin.shared.model.StatusEnum;
import net.svcret.core.api.IRuntimeStatusQueryLocal;
import net.svcret.core.api.StatusesBean;
import net.svcret.core.status.RuntimeStatusQueryBean.StatsAccumulator;

import org.apache.commons.lang3.Validate;

@Table(name = "PX_SVC_VER_METHOD", uniqueConstraints= {@UniqueConstraint(columnNames= {"SVC_VERSION_PID", "NAME"}, name="CONS_METHOD_VERNAME")})
@Entity
public class PersMethod extends BasePersObject {

	private static final long serialVersionUID = 1L;

	/*
	 * This is disabled now because invocation stats don't actually have
	 * a FK relationship to the method
	 */
//	@OneToMany(fetch=FetchType.LAZY, cascade= {CascadeType.REMOVE}, orphanRemoval=true, mappedBy="myPk.myMethod")
//	private Collection<PersInvocationMethodSvcverStats> myInvocationStats;

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

    /*
        By default, all methods can be throttled.
        Only flagged (blacklisted) methods are
        exempted from throttling.
    */
    @Column(name = "THROTTLE_DISABLED", nullable = false)
    private boolean myThrottleDisabled = false;

	@OneToOne(cascade={}, fetch=FetchType.LAZY, orphanRemoval=true, mappedBy="myMethod")
	private PersMethodStatus myStatus;
	
	@ManyToOne()
	@JoinColumn(name = "SVC_VERSION_PID", referencedColumnName = "PID", nullable = false, foreignKey=@ForeignKey(name="FK_METHOD_SVCVER"))
	private BasePersServiceVersion myServiceVersion;

	@OneToMany(fetch = FetchType.LAZY, cascade = {}, orphanRemoval = true, mappedBy = "myMethod")
	private List<PersServiceVersionRecentMessage> mySvcVerRecentMessages;

	@OneToMany(fetch=FetchType.LAZY, cascade= {CascadeType.REMOVE}, orphanRemoval=true, mappedBy="myServiceVersionMethod")
	private Collection<PersUserServiceVersionMethodPermission> myUserPermissions;

	@OneToMany(fetch = FetchType.LAZY, cascade = {}, orphanRemoval = true, mappedBy = "myMethod")
	private List<PersUserRecentMessage> myUserRecentMessages;

	@OneToMany(fetch = FetchType.LAZY, cascade = {}, orphanRemoval = true, mappedBy = "myPk.myMethod")
	private List<PersUserMethodStatus> myUserMethodStatus;

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

    /**
     *
     * @return <code>true</code> if the throttle is disabled,
     *         <code>false</code> otherwise
     */
    public boolean isThrottleDisabled() {
        return myThrottleDisabled;
    }

    public void loadAllAssociations() {
		for (PersUserServiceVersionMethodPermission next : getUserPermissions()) {
			next.loadAllAssociations();
		}
	}

	public void merge(PersMethod theObj) {
		setName(theObj.getName());
		setRootElements(theObj.getRootElements());
        setThrottleDisabled(theObj.isThrottleDisabled());
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

    /**
     * @param theThrottleDisabled the throttle disabled flag to set
     */
    public void setThrottleDisabled(boolean theThrottleDisabled) {
        myThrottleDisabled = theThrottleDisabled;
    }

    /**
	 * No stats
	 */
	public DtoMethod toDto() throws UnexpectedFailureException {
		return toDto(false, null, null);
	}
		
	public DtoMethod toDto(boolean theLoadStats, IRuntimeStatusQueryLocal theRuntimeStatusQuerySvc, StatusesBean theStatuses) throws UnexpectedFailureException {
		DtoMethod retVal = new DtoMethod();
		if (this.getPid() != null) {
			retVal.setPid(this.getPid());
		}
		retVal.setId(this.getName());
		retVal.setName(this.getName());
		retVal.setRootElements(this.getRootElements());
		retVal.setSecurityPolicy(this.getSecurityPolicy());
		retVal.setThrottleDisabled(this.isThrottleDisabled());

		if (theLoadStats) {
			retVal.setStatsInitialized(new Date());
			StatusEnum status = StatusEnum.UNKNOWN;

			StatsAccumulator accumulator = new StatsAccumulator();
			theRuntimeStatusQuerySvc.extract60MinuteMethodStats(this, accumulator);
			accumulator.populateDto(retVal);

			retVal.setStatus(net.svcret.admin.shared.model.StatusEnum.valueOf(status.name()));
			
			PersMethodStatus methodStatus = theStatuses.getMethodPidToStatus().get(retVal.getPid());
			if (methodStatus != null) {
				retVal.setLastSuccessfulInvocation(methodStatus.getLastSuccessfulInvocation());
				retVal.setLastFaultInvocation(methodStatus.getLastFaultInvocation());
				retVal.setLastFailInvocation(methodStatus.getLastFailInvocation());
				retVal.setLastServerSecurityFailure(methodStatus.getLastSecurityFailInvocation());
			}
			
		}

		return retVal;
	}

	public PersMethodStatus getStatus() {
		return myStatus;
	}

}
