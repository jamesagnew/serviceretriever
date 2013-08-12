package net.svcret.ejb.model.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

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

	protected void merge(BasePersMonitorRule theRule) {
		setRuleName(theRule.getRuleName());
		setRuleActive(theRule.isRuleActive());

		for (PersMonitorRuleNotifyContact nextContact : new ArrayList<PersMonitorRuleNotifyContact>(theRule.getNotifyContact())) {
			PersMonitorRuleNotifyContact existing = getContact(nextContact.getEmail());
			if (existing == null) {
				getNotifyContact().add(new PersMonitorRuleNotifyContact(nextContact.getEmail()));
			}
		}
		for (Iterator<PersMonitorRuleNotifyContact> iter = getNotifyContact().iterator(); iter.hasNext();) {
			PersMonitorRuleNotifyContact nextApplies = iter.next();
			PersMonitorRuleNotifyContact wanted = theRule.getContact(nextApplies.getEmail());
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

}
