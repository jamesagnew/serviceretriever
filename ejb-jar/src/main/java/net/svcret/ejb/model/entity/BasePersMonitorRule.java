package net.svcret.ejb.model.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import net.svcret.admin.shared.enm.MonitorRuleTypeEnum;
import net.svcret.admin.shared.model.BaseGMonitorRule;
import net.svcret.admin.shared.model.DtoMonitorRuleActive;
import net.svcret.admin.shared.model.GMonitorRuleAppliesTo;
import net.svcret.admin.shared.model.GMonitorRulePassive;

@Entity
@Table(name = "PX_MONITOR_RULE")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "RULE_TYPE", length = 100, discriminatorType = DiscriminatorType.STRING)
public abstract class BasePersMonitorRule extends BasePersObject {

	private static final long serialVersionUID = 1L;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "myRule")
	private Collection<PersMonitorRuleNotifyContact> myNotifyContact;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PID")
	private Long myPid;

	@Column(name = "RULE_ACTIVE", nullable = false)
	private boolean myRuleActive;

	@Column(name = "RULE_NAME", length = 200, nullable = true)
	private String myRuleName;



	public Collection<PersMonitorRuleNotifyContact> getNotifyContact() {
		if (myNotifyContact == null) {
			myNotifyContact = new ArrayList<PersMonitorRuleNotifyContact>();
		}
		return myNotifyContact;
	}

	@Override
	public Long getPid() {
		return myPid;
	}

	public String getRuleName() {
		return myRuleName;
	}


	public boolean isRuleActive() {
		return myRuleActive;
	}

	protected void merge(BasePersObject theObject) {
		
		BasePersMonitorRule rule = (BasePersMonitorRule) theObject;
		setRuleName(rule.getRuleName());
		setRuleActive(rule.isRuleActive());

		for (PersMonitorRuleNotifyContact nextContact : new ArrayList<PersMonitorRuleNotifyContact>(rule.getNotifyContact())) {
			PersMonitorRuleNotifyContact existing = getContact(nextContact.getEmail());
			if (existing == null) {
				getNotifyContact().add(new PersMonitorRuleNotifyContact(nextContact.getEmail()));
			}
		}
		for (Iterator<PersMonitorRuleNotifyContact> iter = getNotifyContact().iterator(); iter.hasNext();) {
			PersMonitorRuleNotifyContact nextApplies = iter.next();
			PersMonitorRuleNotifyContact wanted = rule.getContact(nextApplies.getEmail());
			if (wanted == null) {
				iter.remove();
			}
		}

	}



	public void setPid(Long thePid) {
		myPid = thePid;
	}

	public void setRuleActive(boolean theRuleActive) {
		myRuleActive = theRuleActive;
	}

	public void setRuleName(String theRuleName) {
		myRuleName = theRuleName;
	}


	private PersMonitorRuleNotifyContact getContact(String theEmail) {
		for (PersMonitorRuleNotifyContact next : getNotifyContact()) {
			if (next.getEmail().equals(theEmail)) {
				return next;
			}
		}
		return null;
	}

	public abstract MonitorRuleTypeEnum getRuleType();

	public Collection<String> getNotifyEmails() {
		TreeSet<String> retVal=new TreeSet<String>();
		for (PersMonitorRuleNotifyContact next : getNotifyContact()) {
			retVal.add(next.getEmail());
		}
		return retVal;
	}

	public BaseGMonitorRule toDao(boolean theLoadDetailedStatistics) {
		BaseGMonitorRule retVal = null;
		switch (this.getRuleType()) {
		case PASSIVE: {
			GMonitorRulePassive ruleDto = new GMonitorRulePassive();
			PersMonitorRulePassive rule = (PersMonitorRulePassive) this;
			ruleDto.setPassiveFireForBackingServiceLatencyIsAboveMillis(rule.getPassiveFireForBackingServiceLatencyIsAboveMillis());
			ruleDto.setPassiveFireForBackingServiceLatencySustainTimeMins(rule.getPassiveFireForBackingServiceLatencySustainTimeMins());
			ruleDto.setPassiveFireIfAllBackingUrlsAreUnavailable(rule.isPassiveFireIfAllBackingUrlsAreUnavailable());
			ruleDto.setPassiveFireIfSingleBackingUrlIsUnavailable(rule.isPassiveFireIfSingleBackingUrlIsUnavailable());
			for (PersMonitorAppliesTo next : rule.getAppliesTo()) {
				if (next.getItem() instanceof PersDomain) {
					PersDomain domain = (PersDomain) next.getItem();
					ruleDto.getAppliesTo().add(new GMonitorRuleAppliesTo(domain.getPid(), domain.getDomainId(),null,null,null,null));
				} else if (next.getItem() instanceof PersService) {
					PersService service = (PersService) next.getItem();
					PersDomain domain = service.getDomain();
					ruleDto.getAppliesTo().add(new GMonitorRuleAppliesTo(domain.getPid(), domain.getDomainId(),service.getPid(),service.getServiceId(),null,null));
				} else if (next.getItem() instanceof BasePersServiceVersion) {
					BasePersServiceVersion svcVer = (BasePersServiceVersion) next.getItem();
					PersService service = svcVer.getService();
					PersDomain domain = service.getDomain();
					ruleDto.getAppliesTo().add(new GMonitorRuleAppliesTo(domain.getPid(), domain.getDomainId(),service.getPid(),service.getServiceId(),svcVer.getPid(),svcVer.getVersionId()));
				}
			}

			retVal = ruleDto;
			break;
		}
		case ACTIVE: {
			DtoMonitorRuleActive ruleDto = new DtoMonitorRuleActive();
			PersMonitorRuleActive rule = (PersMonitorRuleActive) this;
			for (PersMonitorRuleActiveCheck next : rule.getActiveChecks()) {
				ruleDto.getCheckList().add(next.toDto(theLoadDetailedStatistics));
			}
			retVal = ruleDto;
			break;
		}
		}

		if (retVal == null) {
			throw new IllegalStateException("Unknown type: " + this.getRuleType());
		}
		retVal.setPid(this.getPid());
		retVal.setActive(this.isRuleActive());
		retVal.setName(this.getRuleName());

		for (PersMonitorRuleNotifyContact next : this.getNotifyContact()) {
			retVal.getNotifyEmailContacts().add(next.getEmail());
		}

		return retVal;
	}

}
