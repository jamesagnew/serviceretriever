package net.svcret.core.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.ForeignKey;

import net.svcret.admin.shared.enm.ThrottlePeriodEnum;
import net.svcret.admin.shared.model.DtoServiceVersionThrottle;

@Table(name = "PX_SVC_VER_THROTTLE")
@Entity()
public class PersServiceVersionThrottle extends BasePersObject {

	private static final long serialVersionUID = 1L;

	@Column(name = "PER_USER", nullable = false)
	private boolean myApplyPerUser;

	@Column(name = "PROP_CAP_NAME", nullable = true)
	private String myApplyPropCapName;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PID")
	private Long myPid;

	@OneToOne(optional=false)
	@JoinColumn(name = "SVC_VER_PID", nullable=false, updatable=false)
	@ForeignKey(name = "FK_PERS_SVCVERTHT_SVCVERPID")
	private BasePersServiceVersion myServiceVersion;

	@Column(name = "THROTTLE_MAX_QUEUE", nullable = true)
	private Integer myThrottleMaxQueueDepth;

	@Column(name = "THROTTLE_MAX_REQS", nullable = false)
	private Integer myThrottleMaxRequests;

	@Column(name = "THROTTLE_PERIOD", nullable = false)
	@Enumerated(EnumType.STRING)
	private ThrottlePeriodEnum myThrottlePeriod;

	public String getApplyPropCapName() {
		return myApplyPropCapName;
	}

	@Override
	public Long getPid() {
		return myPid;
	}

	public BasePersServiceVersion getServiceVersion() {
		return myServiceVersion;
	}

	public Integer getThrottleMaxQueueDepth() {
		return myThrottleMaxQueueDepth;
	}

	public Integer getThrottleMaxRequests() {
		return myThrottleMaxRequests;
	}

	public ThrottlePeriodEnum getThrottlePeriod() {
		return myThrottlePeriod;
	}

	public boolean isApplyPerUser() {
		return myApplyPerUser;
	}

	public void loadAllAssociations() {
		// nothing
	}

	public void setApplyPerUser(boolean theApplyPerUser) {
		myApplyPerUser = theApplyPerUser;
	}

	public void setApplyPropCapName(String theApplyPropCapName) {
		myApplyPropCapName = theApplyPropCapName;
	}

	public void setServiceVersion(BasePersServiceVersion theSvcVer) {
		myServiceVersion = theSvcVer;
	}

	public void setThrottleMaxQueueDepth(Integer theThrottleMaxQueueDepth) {
		myThrottleMaxQueueDepth = theThrottleMaxQueueDepth;
	}

	public void setThrottleMaxRequests(Integer theThrottleMaxRequests) {
		myThrottleMaxRequests = theThrottleMaxRequests;
	}

	public void setThrottlePeriod(ThrottlePeriodEnum theThrottlePeriod) {
		myThrottlePeriod = theThrottlePeriod;
	}

	public DtoServiceVersionThrottle toDto() {
		DtoServiceVersionThrottle retVal = new DtoServiceVersionThrottle();
		retVal.setApplyPerUser(isApplyPerUser());
		retVal.setApplyPropCapName(getApplyPropCapName());
		retVal.setThrottleMaxQueueDepth(getThrottleMaxQueueDepth());
		retVal.setThrottleMaxRequests(getThrottleMaxRequests());
		retVal.setThrottlePeriod(getThrottlePeriod());
		return retVal;
	}

	public static PersServiceVersionThrottle fromDto(DtoServiceVersionThrottle theThrottle, BasePersServiceVersion theServiceVersion) {
		PersServiceVersionThrottle retVal = new PersServiceVersionThrottle();
		retVal.setServiceVersion(theServiceVersion);
		retVal.merge(theThrottle);
		return retVal;
	}

	public void merge(DtoServiceVersionThrottle theThrottle) {
		setApplyPerUser(theThrottle.isApplyPerUser());
		setApplyPropCapName(theThrottle.getApplyPropCapName());
		setThrottleMaxQueueDepth(theThrottle.getThrottleMaxQueueDepth());
		setThrottleMaxRequests(theThrottle.getThrottleMaxRequests());
		setThrottlePeriod(theThrottle.getThrottlePeriod());
	}

}
