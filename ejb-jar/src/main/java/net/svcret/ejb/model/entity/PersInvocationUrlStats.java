package net.svcret.ejb.model.entity;

import java.util.Date;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.hibernate.annotations.Index;

//@formatter:off
@org.hibernate.annotations.Table( 
	indexes = {
		@Index(columnNames = { "START_TIME", "INTRVL" }, name = "IDX_PURLS_TIME_AND_IVL")
	}, appliesTo = "PX_URL_STATS")
@Table(name = "PX_URL_STATS")
@NamedQueries(value= {
	@NamedQuery(name=Queries.PERSINVOC_URLSTATS_FINDINTERVAL, query=Queries.PERSINVOC_URLSTATS_FINDINTERVAL_Q)
})
@Entity()
//@formatter:on
public class PersInvocationUrlStats extends BasePersMethodInvocationStats {

	private static final long serialVersionUID = 1L;

	@EmbeddedId
	private PersInvocationUrlStatsPk myPk;

	public PersInvocationUrlStats() {
		// nothing
	}

	public PersInvocationUrlStats(PersInvocationUrlStatsPk thePk) {
		myPk = thePk;
	}

	public PersInvocationUrlStats(InvocationStatsIntervalEnum theInterval, Date theStartTime, PersServiceVersionUrl theUrl) {
		myPk = new PersInvocationUrlStatsPk(theInterval, theStartTime, theUrl);
	}

	public PersInvocationUrlStatsPk getPk() {
		return myPk;
	}

	@Override
	public StatsTypeEnum getStatsType() {
		return StatsTypeEnum.INVOCATION;
	}


}
