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
		@Index(columnNames = { "START_TIME" }, name = "IDX_PIS_START_TIME"),
		@Index(columnNames = { "START_TIME", "INTRVL" }, name = "IDX_PIS_TIME_AND_IVL")
	}, appliesTo = "PX_INVOC_STATS")
@Table(name = "PX_INVOC_STATS")
@NamedQueries(value= {
	@NamedQuery(name=Queries.PERSINVOC_STATS, query=Queries.PERSINVOC_STATS_Q)
})
@Entity()
//@formatter:on
public class PersInvocationStats extends BasePersMethodInvocationStats {

	private static final long serialVersionUID = 1L;

	@EmbeddedId
	private PersInvocationStatsPk myPk;

	public PersInvocationStats() {
		// nothing
	}

	public PersInvocationStats(PersInvocationStatsPk thePk) {
		myPk = thePk;
	}

	public PersInvocationStats(InvocationStatsIntervalEnum theInterval, Date theStartTime, PersServiceVersionMethod theMethod) {
		myPk = new PersInvocationStatsPk(theInterval, theStartTime, theMethod);
	}

	public PersInvocationStatsPk getPk() {
		return myPk;
	}

	@Override
	public StatsTypeEnum getStatsType() {
		return StatsTypeEnum.INVOCATION;
	}


}
