package net.svcret.ejb.model.entity;

import java.util.Date;

import javax.persistence.Cacheable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Index;

//@formatter:off
@org.hibernate.annotations.Table(indexes = { 
		@Index(columnNames = { "START_TIME" }, name = "IDX_PISA_START_TIME"), 
		@Index(columnNames = { "START_TIME", "INTRVL" }, name = "IDX_PISA_TIME_AND_IVL")
	}, appliesTo = "PX_INVOC_STATS_ANON")
@Table(name = "PX_INVOC_STATS_ANON")
@NamedQueries(value= {
		@NamedQuery(name=Queries.PERSINVOC_ANONSTATS, query=Queries.PERSINVOC_ANONSTATS_Q)
	})
@Entity()
@Cacheable
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
//@formatter:on
public class PersInvocationAnonStats extends BasePersInvocationStats {

	private static final long serialVersionUID = 1L;
	
	@EmbeddedId
	private PersInvocationAnonStatsPk myPk;

	public PersInvocationAnonStats() {
		// nothing
	}

	public PersInvocationAnonStats(PersInvocationAnonStatsPk thePk) {
		myPk = thePk;
	}

	public PersInvocationAnonStats(InvocationStatsIntervalEnum theInterval, Date theStartTime, PersServiceVersionMethod theMethod) {
		myPk = new PersInvocationAnonStatsPk(theInterval, theStartTime, theMethod);
	}

	public PersInvocationAnonStatsPk getPk() {
		return myPk;
	}

}
