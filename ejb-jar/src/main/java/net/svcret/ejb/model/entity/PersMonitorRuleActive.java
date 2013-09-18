package net.svcret.ejb.model.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

import net.svcret.admin.shared.enm.MonitorRuleTypeEnum;

@Entity
@DiscriminatorValue("ACTIVE")
public class PersMonitorRuleActive extends BasePersMonitorRule {

	private static final long serialVersionUID = 1L;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "myRule")
	private Collection<PersMonitorRuleActiveCheck> myActiveChecks;

	public Collection<PersMonitorRuleActiveCheck> getActiveChecks() {
		if (myActiveChecks == null) {
			myActiveChecks = new ArrayList<PersMonitorRuleActiveCheck>();
		}
		return myActiveChecks;
	}

	public void merge(BasePersObject theObject) {
		super.merge(theObject);

		PersMonitorRuleActive	 rule = (PersMonitorRuleActive)theObject;
		for (PersMonitorRuleActiveCheck nextApplies : new ArrayList<PersMonitorRuleActiveCheck>(rule.getActiveChecks())) {
			PersMonitorRuleActiveCheck existing = getActiveCheckOrNull(nextApplies.getPid());
			if (existing == null) {
				nextApplies.setRule(this);
				getActiveChecks().add(nextApplies);
			}else {
				existing.merge(nextApplies);
			}
		}
		
		for (Iterator<PersMonitorRuleActiveCheck> iter = getActiveChecks().iterator(); iter.hasNext();) {
			PersMonitorRuleActiveCheck nextCheck = iter.next();
			PersMonitorRuleActiveCheck wanted = rule.getActiveCheckOrNull(nextCheck.getPid());
			if (wanted == null && nextCheck.getPid() != null) {
				iter.remove();
			}
		}

		for (PersMonitorRuleActiveCheck nextCheck : getActiveChecks()) {
			if (nextCheck==null) {
				continue;
			}
			nextCheck.setRule(this);
		}

	}

	private PersMonitorRuleActiveCheck getActiveCheckOrNull(Long thePid) {
		if (thePid==null) {
			return null;
		}
		for (PersMonitorRuleActiveCheck next : getActiveChecks()) {
			if (next.getPid() != null && next.getPid().equals(thePid)) {
				return next;
			}
		}
		return null;
	}

	@Override
	public MonitorRuleTypeEnum getRuleType() {
		return MonitorRuleTypeEnum.ACTIVE;
	}



}
