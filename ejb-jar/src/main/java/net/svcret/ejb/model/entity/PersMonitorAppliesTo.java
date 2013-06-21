package net.svcret.ejb.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="PX_MONITOR_APPLIES_TO")
public class PersMonitorAppliesTo extends BasePersObject {

	private static final long serialVersionUID = 1L;
	
	@ManyToOne(cascade= {}, optional=true)
	@JoinColumn(name="DOMAIN_PID", nullable=true)
	private PersDomain myDomain;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PID")
	private Long myPid;

	@ManyToOne(cascade= {}, optional=false)
	@JoinColumn(name="RULE_PID", nullable=false)
	private PersMonitorRule myRule;
	
	@ManyToOne(cascade= {}, optional=true)
	@JoinColumn(name="SVC_PID", nullable=true)
	private PersService myService;

	@ManyToOne(cascade= {}, optional=true)
	@JoinColumn(name="SVCVER_PID", nullable=true)
	private BasePersServiceVersion myServiceVersion;

	public PersDomain getDomain() {
		return myDomain;
	}

	@Override
	public Long getPid() {
		return myPid;
	}

	public PersMonitorRule getRule() {
		return myRule;
	}

	public PersService getService() {
		return myService;
	}

	public BasePersServiceVersion getServiceVersion() {
		return myServiceVersion;
	}

	public void setDomain(PersDomain theDomain) {
		myDomain = theDomain;
	}

	public void setRule(PersMonitorRule theRule) {
		myRule = theRule;
	}

	public void setService(PersService theService) {
		myService = theService;
	}

	public void setServiceVersion(BasePersServiceVersion theServiceVersion) {
		myServiceVersion = theServiceVersion;
	}

	
}
