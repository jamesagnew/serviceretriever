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
public class PersInvocationMethodSvcverStats extends BasePersInvocationMethodStats<PersInvocationMethodSvcverStatsPk, PersInvocationMethodSvcverStats> {

	private static final long serialVersionUID = 1L;

	@EmbeddedId
	private PersInvocationMethodSvcverStatsPk myPk;

	public PersInvocationMethodSvcverStats() {
		// nothing
	}

	public PersInvocationMethodSvcverStats(PersInvocationMethodSvcverStatsPk thePk) {
		myPk = thePk;
	}

	public PersInvocationMethodSvcverStats(InvocationStatsIntervalEnum theInterval, Date theStartTime, PersMethod theMethod) {
		myPk = new PersInvocationMethodSvcverStatsPk(theInterval, theStartTime, theMethod);
	}

	public PersInvocationMethodSvcverStatsPk getPk() {
		return myPk;
	}

	@Override
	public StatsTypeEnum getStatsType() {
		return StatsTypeEnum.INVOCATION;
	}

	@Override
	public <T> T accept(net.svcret.ejb.model.entity.BasePersStats.IStatsVisitor<T> theVisitor) {
		return theVisitor.visit(this, getPk());
	}


}
