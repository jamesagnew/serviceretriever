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
@org.hibernate.annotations.Table(
	indexes = { 
		@Index(columnNames = { "START_TIME" }, name = "IDX_PISU_START_TIME"), 
		@Index(columnNames = { "START_TIME", "INTRVL" }, name = "IDX_PISU_TIME_AND_IVL")
	}, appliesTo = "PX_INVOC_STATS_USR")
@Table(name = "PX_INVOC_STATS_USR")
@NamedQueries(value= {
		@NamedQuery(name=Queries.PERSINVOC_USERSTATS, query=Queries.PERSINVOC_USERSTATS_Q)
	})
@Entity()
@Cacheable
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
//@formatter:on
public class PersInvocationUserStats extends BasePersInvocationStats {

	private static final long serialVersionUID = 1L;
	
	@EmbeddedId
	private PersInvocationUserStatsPk myPk;

	public PersInvocationUserStats() {
		// nothing
	}

	public PersInvocationUserStats(PersInvocationUserStatsPk thePk) {
		myPk = thePk;
	}

	public PersInvocationUserStats(InvocationStatsIntervalEnum theInterval, Date theStartTime, PersServiceVersionMethod theMethod, PersUser theUser) {
		myPk = new PersInvocationUserStatsPk(theInterval, theStartTime, theMethod, theUser);
	}

	public PersInvocationUserStatsPk getPk() {
		return myPk;
	}


}
