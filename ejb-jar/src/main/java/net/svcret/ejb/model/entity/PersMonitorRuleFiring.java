package net.svcret.ejb.model.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import net.svcret.admin.shared.model.GMonitorRuleFiring;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.annotations.Index;

//@formatter:off
@org.hibernate.annotations.Table( 
	indexes = {
		@Index(columnNames = { "START_DATE" }, name = "IDX_PMRF_START_DATE"),
		}, appliesTo = "PX_MONITOR_RULE_FIRING")
@Entity()
@Table(name = "PX_MONITOR_RULE_FIRING", uniqueConstraints= {
		@UniqueConstraint(name="IDX_PMRF_RULEEND", columnNames= {"RULE_PID", "END_DATE"})
})
@NamedQueries(value= {
	@NamedQuery(name=Queries.RULEFIRING, query=Queries.RULEFIRING_Q),
	@NamedQuery(name=Queries.RULEFIRING_FINDACTIVE, query=Queries.RULEFIRING_FINDACTIVE_Q)
})
//@formatter:on
public class PersMonitorRuleFiring extends BasePersObject {

	public static Date NULL_DATE = new Date(0L);
	private static final long NULL_DATE_TIME = NULL_DATE.getTime();

	private static final long serialVersionUID = 1L;

	public PersMonitorRuleFiring() {
		
	}
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "END_DATE", nullable = false)
	private Date myEndDate = NULL_DATE;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PID")
	private Long myPid;

	@OneToMany(cascade = { CascadeType.REMOVE }, fetch = FetchType.EAGER, orphanRemoval = true, mappedBy = "myFiring")
	private Collection<PersMonitorRuleFiringProblem> myProblems;

	@ManyToOne(cascade = {}, optional = false)
	@JoinColumn(name = "RULE_PID", nullable = false)
	private BasePersMonitorRule myRule;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "START_DATE", nullable = true)
	private Date myStartDate;

	/**
	 * @return the endDate
	 */
	public Date getEndDate() {
		if (myEndDate.getTime() == NULL_DATE_TIME) {
			return null;
		}
		return myEndDate;
	}

	/**
	 * @return the pid
	 */
	public Long getPid() {
		return myPid;
	}

	public Collection<PersMonitorRuleFiringProblem> getProblems() {
		if (myProblems == null) {
			myProblems = new ArrayList<PersMonitorRuleFiringProblem>();
		}
		return myProblems;
	}

	/**
	 * @return the rule
	 */
	public BasePersMonitorRule getRule() {
		return myRule;
	}

	/**
	 * @return the startDate
	 */
	public Date getStartDate() {
		return myStartDate;
	}

	/**
	 * @param theEndDate
	 *            the endDate to set
	 */
	public void setEndDate(Date theEndDate) {
		if (theEndDate == null) {
			myEndDate = NULL_DATE;
		} else {
			myEndDate = theEndDate;
		}
	}

	/**
	 * @param theRule
	 *            the rule to set
	 */
	public void setRule(BasePersMonitorRule theRule) {
		myRule = theRule;
	}

	/**
	 * @param theStartDate
	 *            the startDate to set
	 */
	public void setStartDate(Date theStartDate) {
		myStartDate = theStartDate;
	}

	public Set<Long> getAppliesToServiceVersionPids() {
		HashSet<Long> retVal = new HashSet<Long>();
		for (PersMonitorRuleFiringProblem next : getProblems()) {
			retVal.add(next.getServiceVersion().getPid());
		}
		return retVal;
	}

	@Override
	public String toString() {
		ToStringBuilder b = new ToStringBuilder(this, ToStringStyle.DEFAULT_STYLE);
		b.append("pid", getPid());
		b.append("rule", getRule().getPid());
		b.append("start", getStartDate());
		b.append("end", getEndDate());
		int index = 1;
		for (PersMonitorRuleFiringProblem next : getProblems()) {
			b.append("problem_" + (index++), next.toStringShort());
		}
		return b.toString();
	}

	public GMonitorRuleFiring toDto() {
		GMonitorRuleFiring retVal = new GMonitorRuleFiring();
		retVal.setPid(this.getPid());
		retVal.setStartDate(this.getStartDate());

		if (myEndDate.getTime() == NULL_DATE_TIME) {
			retVal.setEndDate(null);
		} else {
			retVal.setEndDate(this.getEndDate());
		}

		retVal.setRulePid(this.getRule().getPid());

		for (PersMonitorRuleFiringProblem next : this.getProblems()) {
			retVal.getProblems().add(next.toDto());
		}

		return retVal;
	}

}
