package net.svcret.ejb.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang3.Validate;

@Entity
@Table(name = "PX_MONITOR_APPLIES_TO")
public class PersMonitorAppliesTo extends BasePersObject {

	private static final long serialVersionUID = 1L;

	@ManyToOne(cascade = {}, optional = true)
	@JoinColumn(name = "DOMAIN_PID", nullable = true)
	private PersDomain myDomain;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PID")
	private Long myPid;

	@ManyToOne(cascade = {}, optional = false)
	@JoinColumn(name = "RULE_PID", nullable = false)
	private PersMonitorRule myRule;

	@ManyToOne(cascade = {}, optional = true)
	@JoinColumn(name = "SVC_PID", nullable = true)
	private PersService myService;

	@ManyToOne(cascade = {}, optional = true)
	@JoinColumn(name = "SVCVER_PID", nullable = true)
	private BasePersServiceVersion myServiceVersion;

	public PersMonitorAppliesTo() {
	}

	public PersMonitorAppliesTo(PersMonitorRule theRule, BasePersServiceCatalogItem theItem) {
		Validate.notNull(theRule);
		Validate.notNull(theItem);
		
		myRule=theRule;
		setItem(theItem);
	}

	public BasePersServiceCatalogItem getItem() {
		if (myDomain != null) {
			return myDomain;
		}
		if (myService != null) {
			return myService;
		}
		return myServiceVersion;
	}

	@Override
	public Long getPid() {
		return myPid;
	}

	public PersMonitorRule getRule() {
		return myRule;
	}

	public void setItem(BasePersServiceCatalogItem theItem) {
		myDomain = null;
		myService = null;
		myServiceVersion = null;
		if (theItem instanceof PersDomain) {
			myDomain = (PersDomain) theItem;
		} else if (theItem instanceof PersService) {
			myService = (PersService) theItem;
		} else if (theItem instanceof BasePersServiceVersion) {
			myServiceVersion = (BasePersServiceVersion) theItem;
		}
	}

	public void setRule(PersMonitorRule theRule) {
		myRule = theRule;
	}

}
