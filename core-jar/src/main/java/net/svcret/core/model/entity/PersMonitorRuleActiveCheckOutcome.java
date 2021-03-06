package net.svcret.core.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import net.svcret.admin.shared.model.DtoMonitorRuleActiveCheckOutcome;

import org.hibernate.annotations.Index;

//@formatter:off
@Entity
@Table(name = "PX_MONITOR_RULE_AC_OUTCM")
@NamedQueries(value= {
		@NamedQuery(name=Queries.PMRACO_DELETEBEFORE, query=Queries.PMRACO_DELETEBEFORE_Q)
	})
@org.hibernate.annotations.Table(appliesTo= "PX_MONITOR_RULE_AC_OUTCM",indexes={
	@Index(name="IDX_MRAO_CHECK_AND_TS", columnNames= {"ACTIVE_CHECK_PID","XACT_TIME"})
})
//@formatter:on
public class PersMonitorRuleActiveCheckOutcome extends BasePersSavedTransaction {

	private static final long serialVersionUID = 1L;

	@ManyToOne(cascade = {}, optional = false)
	@JoinColumn(name = "ACTIVE_CHECK_PID", nullable = false)
	private PersMonitorRuleActiveCheck myCheck;

	// TODO: make this not nullable and update DB
	@Column(name = "CHECK_FAILED", nullable = true)
	private Boolean myFailed;

	public PersMonitorRuleActiveCheck getCheck() {
		return myCheck;
	}

	public Boolean getFailed() {
		return myFailed;
	}

	public void setCheck(PersMonitorRuleActiveCheck theCheck) {
		myCheck = theCheck;
	}

	public void setFailed(boolean theFailed) {
		myFailed = theFailed;
	}

	public DtoMonitorRuleActiveCheckOutcome toDto(boolean theLoadMessage) {
		DtoMonitorRuleActiveCheckOutcome retVal = new DtoMonitorRuleActiveCheckOutcome();
		if (getPid() != null) {
			retVal.setPid(getPid());
		}
		retVal.setFailed(Boolean.TRUE.equals(myFailed));

		super.populateDto(retVal, theLoadMessage);
		
		// TODO: should we be storing the reason the check failed so that we have
		// something here even if the call itself doesn't fail (i.e. latency issue).
		// Otherwise it's just an actual cause for a FAIL outcome here (i.e. can't connect)

		return retVal;
	}

	@Override
	public BasePersServiceVersion getServiceVersion() {
		return getCheck().getServiceVersion();
	}

}
