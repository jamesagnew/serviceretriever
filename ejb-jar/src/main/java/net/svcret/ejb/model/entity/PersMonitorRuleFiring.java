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

import org.hibernate.annotations.Index;

//@formatter:off
@org.hibernate.annotations.Table( 
	indexes = {
		@Index(columnNames = { "START_DATE" }, name = "IDX_PMRF_START_DATE"),
	}, appliesTo = "PX_MONITOR_RULE_FIRING")
@Entity()
@Table(name = "PX_MONITOR_RULE_FIRING")
@NamedQueries(value= {
	@NamedQuery(name=Queries.RULEFIRING, query=Queries.RULEFIRING_Q)
})
//@formatter:on

public class PersMonitorRuleFiring extends BasePersObject {

	private static final long serialVersionUID = 1L;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "END_DATE", nullable = true)
	private Date myEndDate;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PID")
	private Long myPid;

	@OneToMany(cascade= {CascadeType.REMOVE}, fetch=FetchType.EAGER, orphanRemoval = true, mappedBy="myFiring")
	private Collection<PersMonitorRuleFiringProblem> myProblems;

	@ManyToOne(cascade = {}, optional = false)
	@JoinColumn(name = "RULE_PID", nullable = false)
	private PersMonitorRule myRule;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "START_DATE", nullable = true)
	private Date myStartDate;

	/**
	 * @return the endDate
	 */
	public Date getEndDate() {
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
	public PersMonitorRule getRule() {
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
		myEndDate = theEndDate;
	}

	/**
	 * @param theRule
	 *            the rule to set
	 */
	public void setRule(PersMonitorRule theRule) {
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

}
